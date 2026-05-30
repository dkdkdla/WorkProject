package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.*;
import java.time.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;
import work.util.PayCalcUtil;

@WebServlet("/ExcelDownload")
public class ExcelDownload extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String role = session != null ? (String)session.getAttribute("userRole") : null;
        if (role == null || !"A".equals(role.trim())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다.");
            return;
        }

        String storeId = request.getParameter("storeId");
        String year    = request.getParameter("year");
        String month   = request.getParameter("month");

        String formattedMonth = (month.length() == 1) ? "0" + month : month;
        String startDate = year + "-" + formattedMonth + "-01";
        // 해당 월 마지막 날 계산
        LocalDate lastDay = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1)
                                     .withDayOfMonth(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1).lengthOfMonth());
        String endDate = lastDay.toString();

        String fileName = "salary_" + year + "_" + month + ".xls";
        response.setContentType("application/vnd.ms-excel; charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20") + "\"");

        PrintWriter out = response.getWriter();
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<style>");
        out.println("table{border-collapse:collapse;width:100%}");
        out.println("th{background:#4e73df;color:#fff;border:1px solid #000;padding:8px 12px;font-weight:bold}");
        out.println("td{border:1px solid #ccc;padding:8px 12px}");
        out.println(".money{text-align:right} .center{text-align:center}");
        out.println("h2{color:#333;margin-bottom:4px} .sub{color:#666;font-size:13px}");
        out.println("</style></head><body>");
        out.println("<h2>" + year + "년 " + month + "월 급여 정산 내역</h2>");
        out.println("<p class='sub'>매장: " + storeId + " &nbsp;|&nbsp; 기간: " + startDate + " ~ " + endDate + "</p>");
        out.println("<table><thead><tr>");
        out.println("<th>이름</th><th>아이디</th><th>총 근무시간</th><th>기본 시급</th><th>기본급</th><th>주휴수당</th><th>예상 총 급여</th>");
        out.println("</tr></thead><tbody>");

        int grandTotal = 0;

        try (Connection conn = DBConn.getConnection()) {
            // 직원 목록 (tb_my_stores 기반, NULL join_status도 포함 - 기존 데이터 호환)
            String memSql = "SELECT m.mem_id, m.mem_name, m.hourly_wage, " +
                            "ISNULL(ms.work_type,'') as work_type " +
                            "FROM tb_my_stores ms JOIN tb_member m ON ms.mem_id = m.mem_id " +
                            "WHERE ms.store_id = ? " +
                            "AND (ms.join_status = 'ACTIVE' OR ms.join_status IS NULL) " +
                            "AND m.mem_role = 'S' ORDER BY m.mem_name";

            String attSql = "SELECT att_type, att_time, ISNULL(role_id,0) as role_id " +
                            "FROM tb_attendance WHERE mem_id = ? AND store_id = ? " +
                            "AND (att_type IN ('IN','OUT','출근','퇴근')) " +
                            "AND CAST(att_time AS DATE) BETWEEN ? AND ? ORDER BY att_time ASC";

            try (PreparedStatement ps = conn.prepareStatement(memSql)) {
                ps.setString(1, storeId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String memId = rs.getString("mem_id");
                        String name  = rs.getString("mem_name");
                        int wage     = rs.getInt("hourly_wage");
                        String wt    = rs.getString("work_type");

                        long totalMin = 0; int totalPay = 0; int holidayPay = 0;
                        Map<String, Long> weeklyMap = new LinkedHashMap<>();
                        Map<String, Object[]> lastInMap = new HashMap<>();

                        try (PreparedStatement psAtt = conn.prepareStatement(attSql)) {
                            psAtt.setString(1, memId); psAtt.setString(2, storeId);
                            psAtt.setString(3, startDate); psAtt.setString(4, endDate);
                            try (ResultSet rsAtt = psAtt.executeQuery()) {
                                while (rsAtt.next()) {
                                    String type    = rsAtt.getString("att_type");
                                    String timeStr = rsAtt.getString("att_time").substring(0, 16);
                                    int rid        = rsAtt.getInt("role_id");

                                    if ("IN".equals(type) || "출근".equals(type)) {
                                        lastInMap.put("k", new Object[]{timeStr, rid});
                                    } else if (("OUT".equals(type) || "퇴근".equals(type)) && lastInMap.containsKey("k")) {
                                        Object[] in = lastInMap.get("k");
                                        String inStr = (String)in[0]; int rid2 = (int)in[1];
                                        try {
                                            LocalDate wd = LocalDate.parse(inStr.substring(0, 10));
                                            LocalTime st = LocalTime.parse(inStr.substring(11, 16));
                                            LocalTime et = LocalTime.parse(timeStr.substring(11, 16));
                                            int pay = PayCalcUtil.calcPay(rid2, wd, st, et, wt);
                                            // role_id 없으면 기본 시급으로 계산
                                            if (pay == 0 && wage > 0) {
                                                long diff = Duration.between(st, et).toMinutes();
                                                if (diff < 0) diff += 1440;
                                                pay = (int)(diff / 60.0 * wage);
                                            }
                                            totalPay += pay;
                                            long diff = Duration.between(st, et).toMinutes();
                                            if (diff < 0) diff += 1440;
                                            totalMin += diff;
                                            // 주휴수당용 주간 누적
                                            String wk = wd.getYear() + "-W" +
                                                wd.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
                                            weeklyMap.put(wk, weeklyMap.getOrDefault(wk, 0L) + diff);
                                        } catch (Exception e) { e.printStackTrace(); }
                                        lastInMap.remove("k");
                                    }
                                }
                            }
                        }

                        // 주휴수당 계산 (주 15시간 이상 시 적용)
                        for (long wm : weeklyMap.values()) {
                            if (wm >= 900) {
                                holidayPay += (int)((Math.min(wm / 60.0, 40) / 40.0) * 8 * wage);
                            }
                        }
                        int totalWithHoliday = totalPay + holidayPay;

                        if (totalMin > 0) {
                            grandTotal += totalWithHoliday;
                            out.println("<tr>");
                            out.println("<td>" + name + "</td>");
                            out.println("<td class='center'>" + memId + "</td>");
                            out.println("<td class='center'>" + (totalMin/60) + "h " + (totalMin%60) + "m</td>");
                            out.println("<td class='money'>" + String.format("%,d", wage) + "원</td>");
                            out.println("<td class='money'>" + String.format("%,d", totalPay) + "원</td>");
                            out.println("<td class='money' style='color:#1cc88a;'>" + (holidayPay > 0 ? "+" + String.format("%,d", holidayPay) + "원" : "-") + "</td>");
                            out.println("<td class='money'><b>" + String.format("%,d", totalWithHoliday) + "원</b></td>");
                            out.println("</tr>");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<tr><td colspan='5' style='color:red;'>오류: " + e.getMessage() + "</td></tr>");
        }

        // 합계 행
        out.println("<tr style='background:#f8f9fc;'>");
        out.println("<td colspan='6' style='text-align:right;font-weight:bold;'>합계</td>");
        out.println("<td class='money' style='font-weight:bold;color:#4e73df;'>" + String.format("%,d", grandTotal) + "원</td>");
        out.println("</tr>");

        out.println("</tbody></table></body></html>");
    }
}
package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import work.util.DBConn;

@WebServlet("/ExcelDownload")
public class ExcelDownload extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 세션 체크
        javax.servlet.http.HttpSession session = request.getSession(false);
        String role = session != null ? (String)session.getAttribute("userRole") : null;
        if (role == null || !"A".equals(role.trim())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다.");
            return;
        }

        // 1. 파라미터 수신
        String storeId = request.getParameter("storeId");
        String year = request.getParameter("year");
        String month = request.getParameter("month");

        // 2. 엑셀 다운로드 헤더 설정
        String fileName = "salary_" + year + "_" + month + ".xls";
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        
        response.setContentType("application/vnd.ms-excel; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

        PrintWriter out = response.getWriter();

        // 3. 날짜 범위 계산
        String formattedMonth = (month.length() == 1) ? "0" + month : month;
        String startDate = year + "-" + formattedMonth + "-01 00:00:00";
        
        int nextMonth = Integer.parseInt(month) + 1;
        String nextYear = year;
        if (nextMonth > 12) {
            nextMonth = 1;
            nextYear = String.valueOf(Integer.parseInt(year) + 1);
        }
        String formattedNextMonth = (nextMonth < 10) ? "0" + nextMonth : String.valueOf(nextMonth);
        String endDate = nextYear + "-" + formattedNextMonth + "-01 00:00:00";

        // 4. HTML(Excel) 출력 시작
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<style>table { border-collapse: collapse; } th { background-color: #f0f0f0; border: 1px solid #000; } td { border: 1px solid #000; text-align: center; }</style>");
        out.println("</head><body>");
        out.println("<h2>" + year + "년 " + month + "월 급여 정산 내역 (" + storeId + ")</h2>");
        out.println("<table><thead><tr><th>이름 (아이디)</th><th>총 근무시간</th><th>시급</th><th>총 급여</th></tr></thead><tbody>");

        // 5. DB 데이터 조회 및 계산
        try (Connection conn = DBConn.getConnection()) {
            // 멤버 조회
            String memSql = "SELECT mem_id, mem_name, hourly_wage FROM tb_member WHERE store_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(memSql)) {
                pstmt.setString(1, storeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    
                    while (rs.next()) {
                        String memId = rs.getString("mem_id");
                        String name = rs.getString("mem_name");
                        int wage = rs.getInt("hourly_wage");

                        // 해당 멤버의 출퇴근 기록 조회
                        String attSql = "SELECT att_type, att_time FROM tb_attendance " +
                                       "WHERE mem_id = ? AND store_id = ? " +
                                       "AND att_time >= ? AND att_time < ? ORDER BY att_time ASC";
                        
                        try (PreparedStatement pstmtAtt = conn.prepareStatement(attSql)) {
                            pstmtAtt.setString(1, memId);
                            pstmtAtt.setString(2, storeId);
                            pstmtAtt.setString(3, startDate);
                            pstmtAtt.setString(4, endDate);
                            
                            try (ResultSet rsAtt = pstmtAtt.executeQuery()) {
                                long totalMin = 0;
                                Timestamp lastIn = null;

                                while (rsAtt.next()) {
                                    String type = rsAtt.getString("att_type");
                                    Timestamp curTime = rsAtt.getTimestamp("att_time");

                                    if ("출근".equals(type)) {
                                        lastIn = curTime;
                                    } else if ("퇴근".equals(type) && lastIn != null) {
                                        long diff = curTime.getTime() - lastIn.getTime();
                                        totalMin += (diff / (1000 * 60));
                                        lastIn = null;
                                    }
                                }

                                if (totalMin > 0) {
                                    long totalPay = (long)((totalMin / 60.0) * wage);
                                    out.println("<tr>");
                                    out.println("<td>" + name + " (" + memId + ")</td>");
                                    out.println("<td>" + (totalMin / 60) + "시간 " + (totalMin % 60) + "분</td>");
                                    out.println("<td>" + String.format("%,d", wage) + "</td>");
                                    out.println("<td style='text-align:right;'>" + String.format("%,d", totalPay) + "원</td>");
                                    out.println("</tr>");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<tr><td colspan='4 text-danger'>데이터 처리 오류 발생</td></tr>");
        }

        out.println("</tbody></table></body></html>");
    }
}
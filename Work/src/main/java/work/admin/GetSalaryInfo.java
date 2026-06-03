package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;

@WebServlet("/GetSalaryInfo")
public class GetSalaryInfo extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        String role = session != null ? (String)session.getAttribute("userRole") : null;
        String storeId = request.getParameter("storeId");
        boolean isApp = (storeId != null && !storeId.isEmpty());

        if (!isApp && (role == null || !"A".equals(role.trim()))) {
            out.print("{\"status\":\"fail\",\"message\":\"권한이 없습니다.\"}");
            return;
        }

        String year  = request.getParameter("year");
        String month = request.getParameter("month");

        if (storeId == null || year == null || month == null) {
            out.print("{\"status\":\"fail\",\"message\":\"필수 파라미터 누락\"}");
            return;
        }

        String fm = month.length() == 1 ? "0" + month : month;
        String startDate = year + "-" + fm + "-01 00:00:00";
        int nm = Integer.parseInt(month) + 1;
        String ny = year;
        if (nm > 12) { nm = 1; ny = String.valueOf(Integer.parseInt(year) + 1); }
        String endDate = ny + "-" + (nm < 10 ? "0"+nm : nm) + "-01 00:00:00";

        try (Connection conn = DBConn.getConnection()) {
            // tb_my_stores 기준으로 직원 조회, 역할 시급 우선 사용
            String memSql = "SELECT m.mem_id, m.mem_name, " +
                            "ISNULL(r.hourly_wage, m.hourly_wage) as hourly_wage " +
                            "FROM tb_member m " +
                            "JOIN tb_my_stores ms ON m.mem_id = ms.mem_id " +
                            "LEFT JOIN tb_role r ON ms.role_id = r.role_id " +
                            "WHERE ms.store_id = ? AND ms.join_status = 'ACTIVE' AND m.mem_role = 'S'";

            List<Map<String,String>> members = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(memSql)) {
                ps.setString(1, storeId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String,String> m = new HashMap<>();
                    m.put("id",   rs.getString("mem_id"));
                    m.put("name", rs.getString("mem_name"));
                    m.put("wage", rs.getString("hourly_wage"));
                    members.add(m);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"success\",\"data\":[");
            boolean first = true;

            for (Map<String,String> member : members) {
                String memId = member.get("id");
                String memName = member.get("name");
                int wage = 9860;
                try { wage = Integer.parseInt(member.get("wage")); } catch (Exception e) {}

                String attSql = "SELECT att_type, att_time FROM tb_attendance " +
                                "WHERE mem_id = ? AND store_id = ? " +
                                "AND att_time >= ? AND att_time < ? ORDER BY att_time ASC";
                long totalMin = 0;
                try (PreparedStatement ps = conn.prepareStatement(attSql)) {
                    ps.setString(1, memId); ps.setString(2, storeId);
                    ps.setString(3, startDate); ps.setString(4, endDate);
                    ResultSet rs = ps.executeQuery();
                    java.util.Date lastIn = null;
                    while (rs.next()) {
                        String type = rs.getString("att_type");
                        Timestamp ts = rs.getTimestamp("att_time");
                        if ("출근".equals(type) || "IN".equals(type)) {
                            lastIn = new java.util.Date(ts.getTime());
                        } else if (("퇴근".equals(type) || "OUT".equals(type)) && lastIn != null) {
                            totalMin += (ts.getTime() - lastIn.getTime()) / 60000;
                            lastIn = null;
                        }
                    }
                }

                long totalPay = (long)((totalMin / 60.0) * wage);
                if (!first) sb.append(",");
                sb.append("{\"name\":\"").append(memName).append("\",")
                  .append("\"time\":\"").append(totalMin/60).append("시간 ").append(totalMin%60).append("분\",")
                  .append("\"pay\":\"").append(String.format("%,d원", totalPay)).append("\"}");
                first = false;
            }
            sb.append("]}");
            out.print(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"서버 오류\"}");
        }
    }
}
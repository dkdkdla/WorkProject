package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/GetSalaryInfo")
public class GetSalaryInfo extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 설정 및 파라미터 수신
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String storeId = request.getParameter("storeId");
        String year = request.getParameter("year");
        String month = request.getParameter("month");

        if (storeId == null || year == null || month == null) {
            out.print("{\"status\":\"fail\", \"message\":\"필수 파라미터가 누락되었습니다.\"}");
            return;
        }

        // 2. 검색 기간(한 달) 계산 로직
        String startDate = year + "-" + (month.length() == 1 ? "0" + month : month) + "-01 00:00:00";
        int nextMonth = Integer.parseInt(month) + 1;
        String nextYear = year;
        if (nextMonth > 12) {
            nextMonth = 1;
            nextYear = String.valueOf(Integer.parseInt(year) + 1);
        }
        String endDate = nextYear + "-" + (nextMonth < 10 ? "0" + nextMonth : nextMonth) + "-01 00:00:00";

        // 3. DB 처리
        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        
        try (Connection conn = DriverManager.getConnection(dbUrl, "WorkUser", "pass")) {
            // [Step 1] 매장 소속 직원 명단 확보
            String memSql = "SELECT mem_id, mem_name, hourly_wage FROM tb_member WHERE store_id = ?";
            List<Map<String, String>> members = new ArrayList<>();
            
            try (PreparedStatement pstmt = conn.prepareStatement(memSql)) {
                pstmt.setString(1, storeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> m = new HashMap<>();
                        m.put("id", rs.getString("mem_id"));
                        m.put("name", rs.getString("mem_name"));
                        m.put("wage", rs.getString("hourly_wage"));
                        members.add(m);
                    }
                }
            }

            // [Step 2] 각 직원별 급여 계산 및 JSON 조립
            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"success\", \"data\":[");
            boolean isFirstMember = true;

            for (Map<String, String> member : members) {
                String memId = member.get("id");
                String memName = member.get("name");
                int wage = 9860; // 기본 최저시급 설정
                try { wage = Integer.parseInt(member.get("wage")); } catch (Exception e) {}

                String attSql = "SELECT att_type, att_time FROM tb_attendance " +
                                "WHERE mem_id = ? AND store_id = ? AND att_time >= ? AND att_time < ? " +
                                "ORDER BY att_time ASC";
                
                long totalMin = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(attSql)) {
                    pstmt.setString(1, memId);
                    pstmt.setString(2, storeId);
                    pstmt.setString(3, startDate);
                    pstmt.setString(4, endDate);
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        java.util.Date lastIn = null;
                        while (rs.next()) {
                            String type = rs.getString("att_type");
                            Timestamp ts = rs.getTimestamp("att_time");
                            if ("출근".equals(type)) {
                                lastIn = new java.util.Date(ts.getTime());
                            } else if ("퇴근".equals(type) && lastIn != null) {
                                long diff = ts.getTime() - lastIn.getTime();
                                totalMin += (diff / (1000 * 60));
                                lastIn = null;
                            }
                        }
                    }
                }

                // 결과 계산
                long totalPay = (long) ((totalMin / 60.0) * wage);
                String timeStr = (totalMin / 60) + "시간 " + (totalMin % 60) + "분";
                String payStr = String.format("%,d원", totalPay);

                if (!isFirstMember) sb.append(",");
                sb.append("{")
                  .append("\"name\":\"").append(memName).append("\",")
                  .append("\"time\":\"").append(timeStr).append("\",")
                  .append("\"pay\":\"").append(payStr).append("\"")
                  .append("}");
                isFirstMember = false;
            }

            sb.append("]}");
            out.print(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
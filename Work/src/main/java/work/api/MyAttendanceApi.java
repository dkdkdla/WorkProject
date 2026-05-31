package work.api;

import java.io.*;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;

@WebServlet("/MyAttendanceApi")
public class MyAttendanceApi extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String userId    = request.getParameter("id");
        String storeId   = request.getParameter("storeId");
        String startDate = request.getParameter("startDate");
        String endDate   = request.getParameter("endDate");

        if (userId == null || userId.isEmpty()) {
            out.print("{\"status\":\"fail\",\"message\":\"userId 없음\"}");
            return;
        }

        // 시급 조회
        int hourlyWage = 0;
        String wageSql = "SELECT hourly_wage FROM tb_member WHERE mem_id = ?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(wageSql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) hourlyWage = rs.getInt("hourly_wage");
        } catch (Exception e) { e.printStackTrace(); }

        // 출퇴근 기록 조회
        StringBuilder listSb = new StringBuilder();
        long totalMinutes = 0;
        String lastInTime = null;

        String sql = "SELECT att_type, " +
                     "FORMAT(att_time,'yyyy-MM-dd') as att_date, " +
                     "FORMAT(att_time,'HH:mm') as att_hm " +
                     "FROM tb_attendance " +
                     "WHERE mem_id = ? AND store_id = ? " +
                     "AND (att_type IN ('IN','OUT','출근','퇴근')) ";

        if (startDate != null && !startDate.isEmpty()) sql += "AND CAST(att_time AS DATE) >= ? ";
        if (endDate   != null && !endDate.isEmpty())   sql += "AND CAST(att_time AS DATE) <= ? ";
        sql += "ORDER BY att_time DESC";

        try (Connection conn = DBConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, userId);
            ps.setString(idx++, storeId != null ? storeId : "");
            if (startDate != null && !startDate.isEmpty()) ps.setString(idx++, startDate);
            if (endDate   != null && !endDate.isEmpty())   ps.setString(idx++, endDate);

            ResultSet rs = ps.executeQuery();
            boolean first = true;
            while (rs.next()) {
                String type = rs.getString("att_type");
                String date = rs.getString("att_date");
                String time = rs.getString("att_hm");

                // 간단 급여 계산 (IN/OUT 쌍 매칭)
                if ("OUT".equals(type) || "퇴근".equals(type)) lastInTime = time;
                else if (("IN".equals(type) || "출근".equals(type)) && lastInTime != null) {
                    try {
                        String[] inParts  = time.split(":");
                        String[] outParts = lastInTime.split(":");
                        int inMin  = Integer.parseInt(inParts[0])  * 60 + Integer.parseInt(inParts[1]);
                        int outMin = Integer.parseInt(outParts[0]) * 60 + Integer.parseInt(outParts[1]);
                        if (outMin > inMin) totalMinutes += (outMin - inMin);
                    } catch (Exception e) {}
                    lastInTime = null;
                }

                if (!first) listSb.append(",");
                listSb.append("{");
                listSb.append("\"date\":\"").append(date).append("\",");
                listSb.append("\"time\":\"").append(time).append("\",");
                listSb.append("\"type\":\"").append(type).append("\"");
                listSb.append("}");
                first = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            return;
        }

        long totalPay = (totalMinutes / 60) * hourlyWage;

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":\"success\"");
        sb.append(",\"summary\":{");
        sb.append("\"totalPay\":").append(totalPay);
        sb.append(",\"hourlyWage\":").append(hourlyWage);
        sb.append("}");
        sb.append(",\"list\":[").append(listSb).append("]");
        sb.append("}");

        out.print(sb.toString());
    }
}
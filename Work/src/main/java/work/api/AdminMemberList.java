package work.api;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;
import java.sql.*;

@WebServlet("/AdminMemberList")
public class AdminMemberList extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        String role = session != null ? (String)session.getAttribute("userRole") : null;
        PrintWriter out = response.getWriter();

        if (!"A".equals(role)) {
            out.print("{\"status\":\"fail\",\"message\":\"권한 없음\"}");
            return;
        }

        String storeId = request.getParameter("storeId");
        if (storeId == null || storeId.isEmpty()) {
            storeId = (String)session.getAttribute("userStoreId");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":\"success\",\"members\":[");

        String sql = "SELECT m.mem_id, m.mem_name, m.mem_phone, m.hourly_wage " +
                     "FROM tb_member m " +
                     "JOIN tb_my_stores ms ON m.mem_id = ms.mem_id " +
                     "WHERE ms.store_id = ? " +
                     "AND (ms.join_status = 'ACTIVE' OR ms.join_status IS NULL) " +
                     "AND m.mem_role = 'S' " +
                     "ORDER BY m.mem_name ASC";

        try (Connection conn = DBConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, storeId);
            ResultSet rs = ps.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append("{")
                  .append("\"id\":\"").append(rs.getString("mem_id")).append("\",")
                  .append("\"name\":\"").append(rs.getString("mem_name")).append("\",")
                  .append("\"phone\":\"").append(rs.getString("mem_phone") != null ? rs.getString("mem_phone") : "").append("\",")
                  .append("\"wage\":").append(rs.getInt("hourly_wage"))
                  .append("}");
                first = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            return;
        }

        sb.append("]}");
        out.print(sb.toString());
    }
}
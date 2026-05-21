package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import work.util.DBConn;

@WebServlet("/GetRoles")
public class GetRoles extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String storeId = request.getParameter("storeId");

        if (storeId == null || storeId.trim().isEmpty()) {
            out.print("{\"status\":\"fail\", \"list\":[]}");
            return;
        }

        String sql = "SELECT role_id, role_name, hourly_wage FROM tb_role WHERE store_id = ? ORDER BY role_name ASC";

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":\"success\", \"list\":[");

        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, storeId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append("{");
                    sb.append("\"roleId\":").append(rs.getInt("role_id")).append(",");
                    sb.append("\"roleName\":\"").append(rs.getString("role_name")).append("\",");
                    sb.append("\"wage\":").append(rs.getInt("hourly_wage"));
                    sb.append("}");
                    first = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"list\":[]}");
            return;
        }

        sb.append("]}");
        out.print(sb.toString());
    }
}
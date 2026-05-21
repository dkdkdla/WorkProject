package work.store;

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

@WebServlet("/StoreList")
public class StoreList extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String userId = request.getParameter("id");

        if (userId == null || userId.trim().isEmpty()) {
            out.print("[]");
            return;
        }

        String sql = "SELECT s.store_id, s.store_name " +
                     "FROM tb_store s " +
                     "JOIN tb_my_stores m ON s.store_id = m.store_id " +
                     "WHERE m.mem_id = ? " +
                     "AND m.join_status = 'ACTIVE' " +
                     "AND s.store_status = 'ACTIVE' " +
                     "ORDER BY s.store_name ASC";

        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                boolean first = true;

                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append("{");
                    sb.append("\"id\":\"").append(rs.getString("store_id")).append("\",");
                    sb.append("\"name\":\"").append(rs.getString("store_name")).append("\"");
                    sb.append("}");
                    first = false;
                }

                sb.append("]");
                out.print(sb.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("[]");
        }
    }
}
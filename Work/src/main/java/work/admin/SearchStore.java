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

@WebServlet("/SearchStore")
public class SearchStore extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String keyword = request.getParameter("keyword");

        if (keyword == null || keyword.trim().isEmpty()) {
            out.print("{\"status\":\"fail\", \"list\":[]}");
            return;
        }

        // ACTIVE 매장만 검색 (이름 또는 ID로)
        String sql = "SELECT store_id, store_name FROM tb_store " +
                     "WHERE store_status = 'ACTIVE' " +
                     "AND (store_name LIKE ? OR store_id LIKE ?) " +
                     "ORDER BY store_name ASC";

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":\"success\", \"list\":[");

        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchParam = "%" + keyword.trim() + "%";
            pstmt.setString(1, searchParam);
            pstmt.setString(2, searchParam);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append("{")
                      .append("\"id\":\"").append(rs.getString("store_id")).append("\",")
                      .append("\"name\":\"").append(rs.getString("store_name")).append("\"")
                      .append("}");
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
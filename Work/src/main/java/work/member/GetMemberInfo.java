package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;

@WebServlet("/GetMemberInfo")
public class GetMemberInfo extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String id = request.getParameter("id");
        if (id == null || id.isEmpty()) {
            out.print("{\"status\":\"fail\",\"message\":\"ID 누락\"}");
            return;
        }

        // store_id는 tb_my_stores에서 조회
        String sql = "SELECT m.mem_name, m.mem_phone, m.hourly_wage, " +
                     "ISNULL(ms.store_id, '') as store_id " +
                     "FROM tb_member m " +
                     "LEFT JOIN tb_my_stores ms ON m.mem_id = ms.mem_id AND ms.join_status = 'ACTIVE' " +
                     "WHERE m.mem_id = ?";

        try (Connection conn = DBConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                out.print("{");
                out.print("\"status\":\"success\",");
                out.print("\"name\":\""    + (rs.getString("mem_name")  != null ? rs.getString("mem_name")  : "") + "\",");
                out.print("\"phone\":\""   + (rs.getString("mem_phone") != null ? rs.getString("mem_phone") : "") + "\",");
                out.print("\"wage\":\""    + rs.getInt("hourly_wage") + "\",");
                out.print("\"storeId\":\"" + rs.getString("store_id") + "\"");
                out.print("}");
            } else {
                out.print("{\"status\":\"fail\",\"message\":\"정보를 찾을 수 없습니다.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"서버 오류\"}");
        }
    }
}
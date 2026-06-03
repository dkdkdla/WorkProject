package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;
import java.sql.*;

@WebServlet("/MyStoreDelete")
public class MyStoreDelete extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String userId  = request.getParameter("id");
        String storeId = request.getParameter("storeId");

        if (userId == null || storeId == null) {
            out.print("{\"status\":\"fail\",\"message\":\"필수 파라미터 누락\"}");
            return;
        }

        String sql = "DELETE FROM tb_my_stores WHERE mem_id = ? AND store_id = ?";

        try (Connection conn = DBConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, storeId);
            int result = ps.executeUpdate();
            if (result > 0) {
                out.print("{\"status\":\"success\",\"message\":\"매장 소속이 해제되었습니다.\"}");
            } else {
                out.print("{\"status\":\"fail\",\"message\":\"삭제할 대상을 찾지 못했습니다.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"서버 오류\"}");
        }
    }
}
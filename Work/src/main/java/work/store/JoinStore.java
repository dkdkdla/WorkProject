package work.store;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;

@WebServlet("/JoinStore")
public class JoinStore extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String userId  = request.getParameter("id");
        String storeId = request.getParameter("storeId");

        if (userId == null || storeId == null) {
            out.print("{\"status\":\"fail\",\"message\":\"필수 파라미터 누락\"}");
            return;
        }

        try (Connection conn = DBConn.getConnection()) {
            conn.setAutoCommit(false);

            // 1. 매장 존재 여부 체크
            String checkStoreSql = "SELECT COUNT(*) FROM tb_store WHERE store_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkStoreSql)) {
                ps.setString(1, storeId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    out.print("{\"status\":\"fail\",\"message\":\"존재하지 않는 매장 ID입니다.\"}");
                    conn.rollback();
                    return;
                }
            }

            String checkSql = "SELECT COUNT(*) FROM tb_my_stores WHERE mem_id = ? AND store_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, userId);
                ps.setString(2, storeId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    out.print("{\"status\":\"fail\",\"message\":\"이미 소속 신청한 매장입니다.\"}");
                    conn.rollback();
                    return;
                }
            }

            // 3. tb_my_stores에 PENDING으로 소속 신청
            String insertSql = "INSERT INTO tb_my_stores (mem_id, store_id, join_status) VALUES (?, ?, 'PENDING')";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, userId);
                ps.setString(2, storeId);
                ps.executeUpdate();
            }

            conn.commit();
            out.print("{\"status\":\"success\",\"message\":\"소속 신청이 완료되었습니다. 점장 승인 후 이용 가능합니다.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"서버 오류\"}");
        }
    }
}
package work.store;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/JoinStore")
public class JoinStore extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String userId = request.getParameter("id");
        String storeId = request.getParameter("storeId");

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        String dbId = "WorkUser";
        String dbPw = "pass";

        Connection conn = null;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(dbUrl, dbId, dbPw);
            
            // 🚨 팩트체크: 여러 쿼리를 하나의 작업으로 묶기 위해 자동 커밋을 끈다.
            conn.setAutoCommit(false);

            // 1. 매장 존재 여부 체크
            String checkStoreSql = "SELECT count(*) FROM tb_store WHERE store_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkStoreSql)) {
                pstmt.setString(1, storeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        out.print("{\"status\":\"fail\", \"message\":\"존재하지 않는 매장 ID입니다.\"}");
                        conn.rollback();
                        return;
                    }
                }
            }

            // 2. 이미 등록된 매장인지 체크
            String checkLinkSql = "SELECT count(*) FROM tb_link WHERE mem_id = ? AND store_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkLinkSql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, storeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        out.print("{\"status\":\"fail\", \"message\":\"이미 등록된 매장입니다.\"}");
                        conn.rollback();
                        return;
                    }
                }
            }

            // 3. tb_link에 연결 정보 삽입
            String insertSql = "INSERT INTO tb_link (mem_id, store_id, origin, reg_date) VALUES (?, ?, 'Web', GETDATE())";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, storeId);
                pstmt.executeUpdate();
            }

            // 4. tb_member 매장 목록 업데이트 (기존 목록에 추가)
            String updateMemberSql = "UPDATE tb_member SET store_id = " +
                                     "CASE WHEN store_id IS NULL OR store_id = '' THEN ? " +
                                     "ELSE store_id + ',' + ? END " +
                                     "WHERE mem_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateMemberSql)) {
                pstmt.setString(1, storeId);
                pstmt.setString(2, storeId);
                pstmt.setString(3, userId);
                pstmt.executeUpdate();
            }

            // 모든 작업이 성공하면 실제 DB에 반영
            conn.commit();
            out.print("{\"status\":\"success\", \"message\":\"새로운 매장이 추가되었습니다.\"}");

        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch(Exception ex) {}
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류: " + e.getMessage().replace("\"", "'") + "\"}");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 상태 복구
                    conn.close();
                } catch(Exception e) {}
            }
        }
    }
}
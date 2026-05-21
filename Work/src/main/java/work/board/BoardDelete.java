package work.board;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.util.DBConn;

@WebServlet("/BoardDelete")
public class BoardDelete extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        String postId = request.getParameter("id");
        String storeId = request.getParameter("storeId");

        if (userId == null || postId == null) {
            response.sendRedirect("board_list.jsp");
            return;
        }

        // 첨부파일 이름 먼저 조회
        String fileName = null;
        String selectSql = "SELECT filename, writer_id FROM tb_board WHERE post_id = ?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setInt(1, Integer.parseInt(postId));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 작성자 본인만 삭제 가능
                    if (!userId.equals(rs.getString("writer_id"))) {
                        response.sendRedirect("board_view.jsp?id=" + postId + "&storeId=" + storeId);
                        return;
                    }
                    fileName = rs.getString("filename");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("board_list.jsp?storeId=" + storeId);
            return;
        }

        // DB에서 삭제
        String deleteSql = "DELETE FROM tb_board WHERE post_id = ? AND writer_id = ?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, Integer.parseInt(postId));
            pstmt.setString(2, userId);
            pstmt.executeUpdate();

            // 첨부파일 삭제
            if (fileName != null && !fileName.isEmpty()) {
                String uploadPath = request.getServletContext().getRealPath("upload") + File.separator + fileName;
                File file = new File(uploadPath);
                if (file.exists()) file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("board_list.jsp?storeId=" + storeId);
    }
}
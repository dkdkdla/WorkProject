package work.board;

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
import javax.servlet.http.HttpSession;
import work.util.DBConn;

@WebServlet("/BoardEdit")
public class BoardEdit extends HttpServlet {

    // GET: 수정 페이지로 이동 (기존 데이터 로드)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        String postId = request.getParameter("id");

        if (userId == null || postId == null) {
            response.sendRedirect("board_list.jsp");
            return;
        }

        // 게시글 조회 및 작성자 확인
        String sql = "SELECT post_id, title, content, writer_id, store_id, filename, original_filename FROM tb_board WHERE post_id = ?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(postId));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 작성자 본인만 수정 가능
                    if (!userId.equals(rs.getString("writer_id"))) {
                        response.sendRedirect("board_view.jsp?id=" + postId);
                        return;
                    }
                    request.setAttribute("postId",   rs.getString("post_id"));
                    request.setAttribute("title",    rs.getString("title"));
                    request.setAttribute("content",  rs.getString("content"));
                    request.setAttribute("storeId",  rs.getString("store_id"));
                    request.setAttribute("fileName", rs.getString("filename") != null ? rs.getString("filename") : "");
                    request.setAttribute("orgName",  rs.getString("original_filename") != null ? rs.getString("original_filename") : "");
                    request.getRequestDispatcher("board_edit.jsp").forward(request, response);
                } else {
                    response.sendRedirect("board_list.jsp");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("board_list.jsp");
        }
    }

    // POST: 수정 처리
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        String postId  = request.getParameter("postId");
        String title   = request.getParameter("title");
        String content = request.getParameter("content");
        String storeId = request.getParameter("storeId");

        if (userId == null || postId == null) {
            out.print("{\"status\":\"fail\", \"message\":\"권한이 없습니다.\"}");
            return;
        }

        String sql = "UPDATE tb_board SET title = ?, content = ? WHERE post_id = ? AND writer_id = ?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setInt(3, Integer.parseInt(postId));
            pstmt.setString(4, userId);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                out.print("{\"status\":\"success\", \"message\":\"수정되었습니다.\"}");
            } else {
                out.print("{\"status\":\"fail\", \"message\":\"수정에 실패했습니다.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류가 발생했습니다.\"}");
        }
    }
}
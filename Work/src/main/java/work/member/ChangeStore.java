package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;

@WebServlet("/ChangeStore")
public class ChangeStore extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String userId    = (String) session.getAttribute("userId");
        String userRole  = (String) session.getAttribute("userRole");
        String newStoreId = request.getParameter("storeId");

        if (userId == null || newStoreId == null) {
            out.print("{\"status\":\"fail\",\"message\":\"세션이 만료되었습니다.\"}");
            return;
        }

        try (Connection conn = DBConn.getConnection()) {
            // 점장: tb_store에 존재하는지 확인
            // 직원: tb_my_stores에 ACTIVE로 소속되어 있는지 확인
            String sql;
            if ("A".equals(userRole)) {
                sql = "SELECT COUNT(*) FROM tb_store WHERE store_id = ?";
            } else {
                sql = "SELECT COUNT(*) FROM tb_my_stores " +
                      "WHERE mem_id = ? AND store_id = ? AND join_status = 'ACTIVE'";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "A".equals(userRole) ? newStoreId : userId);
                if (!"A".equals(userRole)) ps.setString(2, newStoreId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    out.print("{\"status\":\"fail\",\"message\":\"접근할 수 없는 매장입니다.\"}");
                    return;
                }
            }

            // 세션만 업데이트 (tb_member.store_id 제거됨)
            session.setAttribute("userStoreId", newStoreId);

            // 매장명 조회해서 세션에 저장
            String nameSql = "SELECT store_name FROM tb_store WHERE store_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(nameSql)) {
                ps.setString(1, newStoreId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) session.setAttribute("userStoreName", rs.getString("store_name"));
            }

            out.print("{\"status\":\"success\",\"message\":\"매장이 변경되었습니다.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"서버 오류\"}");
        }
    }
}
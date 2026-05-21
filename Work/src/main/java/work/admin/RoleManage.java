package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.util.DBConn;

@WebServlet("/RoleManage")
public class RoleManage extends HttpServlet {

    // GET: 역할 관리 페이지로 이동
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userRole  = (String) session.getAttribute("userRole");
        String storeId   = (String) session.getAttribute("userStoreId");

        if (!"A".equals(userRole != null ? userRole.trim() : "")) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 현재 매장의 역할 목록 조회
        ArrayList<String[]> roleList = getRoleList(storeId);
        request.setAttribute("roleList", roleList);
        request.setAttribute("storeId", storeId);
        request.getRequestDispatcher("role_manage.jsp").forward(request, response);
    }

    // POST: 역할 추가/수정/삭제
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");
        String storeId  = (String) session.getAttribute("userStoreId");

        if (!"A".equals(userRole != null ? userRole.trim() : "")) {
            out.print("{\"status\":\"fail\", \"message\":\"권한이 없습니다.\"}");
            return;
        }

        String mode     = request.getParameter("mode"); // add / edit / delete
        String roleId   = request.getParameter("roleId");
        String roleName = request.getParameter("roleName");
        String wageStr  = request.getParameter("wage");

        try {
            if ("add".equals(mode)) {
                // 역할 추가
                if (roleName == null || roleName.trim().isEmpty()) {
                    out.print("{\"status\":\"fail\", \"message\":\"역할 이름을 입력해주세요.\"}");
                    return;
                }
                int wage = 0;
                try { wage = Integer.parseInt(wageStr.trim()); } catch (Exception e) {}

                String sql = "INSERT INTO tb_role (store_id, role_name, hourly_wage) VALUES (?, ?, ?)";
                try (Connection conn = DBConn.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, storeId);
                    pstmt.setString(2, roleName.trim());
                    pstmt.setInt(3, wage);
                    pstmt.executeUpdate();
                }
                out.print("{\"status\":\"success\", \"message\":\"역할이 추가되었습니다.\"}");

            } else if ("edit".equals(mode)) {
                // 역할 수정
                int wage = 0;
                try { wage = Integer.parseInt(wageStr.trim()); } catch (Exception e) {}

                String sql = "UPDATE tb_role SET role_name = ?, hourly_wage = ? WHERE role_id = ? AND store_id = ?";
                try (Connection conn = DBConn.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, roleName.trim());
                    pstmt.setInt(2, wage);
                    pstmt.setInt(3, Integer.parseInt(roleId));
                    pstmt.setString(4, storeId);
                    pstmt.executeUpdate();
                }
                out.print("{\"status\":\"success\", \"message\":\"역할이 수정되었습니다.\"}");

            } else if ("delete".equals(mode)) {
                // 역할 삭제 (해당 역할 직원 있으면 삭제 불가)
                String checkSql = "SELECT COUNT(*) FROM tb_member WHERE role_id = ?";
                try (Connection conn = DBConn.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                    pstmt.setInt(1, Integer.parseInt(roleId));
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            out.print("{\"status\":\"fail\", \"message\":\"해당 역할에 소속된 직원이 있어 삭제할 수 없습니다.\"}");
                            return;
                        }
                    }
                }
                String sql = "DELETE FROM tb_role WHERE role_id = ? AND store_id = ?";
                try (Connection conn = DBConn.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, Integer.parseInt(roleId));
                    pstmt.setString(2, storeId);
                    pstmt.executeUpdate();
                }
                out.print("{\"status\":\"success\", \"message\":\"역할이 삭제되었습니다.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류가 발생했습니다.\"}");
        }
    }

    private ArrayList<String[]> getRoleList(String storeId) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT role_id, role_name, hourly_wage FROM tb_role WHERE store_id = ? ORDER BY role_name ASC";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, storeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        String.valueOf(rs.getInt("role_id")),
                        rs.getString("role_name"),
                        String.valueOf(rs.getInt("hourly_wage"))
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
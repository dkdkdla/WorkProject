package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;

@WebServlet("/RoleAssign")
public class RoleAssign extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");
        String storeId  = (String) session.getAttribute("userStoreId");

        if (!"A".equals(userRole != null ? userRole.trim() : "")) {
            out.print("{\"status\":\"fail\",\"message\":\"권한이 없습니다.\"}");
            return;
        }

        String memId     = request.getParameter("memId");
        String roleIdStr = request.getParameter("roleId");

        if (memId == null || roleIdStr == null || storeId == null) {
            out.print("{\"status\":\"fail\",\"message\":\"필수 정보 누락\"}");
            return;
        }

        try {
            int roleId = Integer.parseInt(roleIdStr);
            int wage = 0;

            // 역할 시급 조회
            if (roleId > 0) {
                String wageSql = "SELECT hourly_wage FROM tb_role WHERE role_id = ?";
                try (Connection conn = DBConn.getConnection();
                     PreparedStatement ps = conn.prepareStatement(wageSql)) {
                    ps.setInt(1, roleId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) wage = rs.getInt("hourly_wage");
                }
            }

            // tb_my_stores 에만 role_id 업데이트 (tb_member 중복 제거)
            String sql = roleId > 0
                ? "UPDATE tb_my_stores SET role_id = ? WHERE mem_id = ? AND store_id = ?"
                : "UPDATE tb_my_stores SET role_id = NULL WHERE mem_id = ? AND store_id = ?";

            try (Connection conn = DBConn.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                if (roleId > 0) {
                    ps.setInt(1, roleId);
                    ps.setString(2, memId);
                    ps.setString(3, storeId);
                } else {
                    ps.setString(1, memId);
                    ps.setString(2, storeId);
                }
                ps.executeUpdate();
            }

            String msg = roleId > 0 ? "역할이 지정되었습니다." : "역할이 해제되었습니다.";
            out.print("{\"status\":\"success\",\"message\":\"" + msg + "\",\"wage\":" + wage + "}");

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"서버 오류\"}");
        }
    }
}
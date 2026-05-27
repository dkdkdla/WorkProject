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
import javax.servlet.http.HttpSession;
import work.util.DBConn;

@WebServlet("/RoleAssign")
public class RoleAssign extends HttpServlet {

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

        String memId     = request.getParameter("memId");
        String roleIdStr = request.getParameter("roleId");

        if (memId == null || roleIdStr == null || storeId == null) {
            out.print("{\"status\":\"fail\", \"message\":\"필수 정보가 누락되었습니다.\"}");
            return;
        }

        try {
            int roleId = Integer.parseInt(roleIdStr);

            // 역할의 기본 시급 조회
            int wage = 0;
            if (roleId > 0) {
                String wageSql = "SELECT hourly_wage FROM tb_role WHERE role_id = ?";
                try (Connection conn = DBConn.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(wageSql)) {
                    pstmt.setInt(1, roleId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) wage = rs.getInt("hourly_wage");
                    }
                }
            }

            // tb_my_stores에 role_id 업데이트 (매장별 역할 지정)
            String sql;
            if (roleId > 0) {
                sql = "UPDATE tb_my_stores SET role_id = ? WHERE mem_id = ? AND store_id = ?";
            } else {
                sql = "UPDATE tb_my_stores SET role_id = NULL WHERE mem_id = ? AND store_id = ?";
            }

            try (Connection conn = DBConn.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (roleId > 0) {
                    pstmt.setInt(1, roleId);
                    pstmt.setString(2, memId);
                    pstmt.setString(3, storeId);
                } else {
                    pstmt.setString(1, memId);
                    pstmt.setString(2, storeId);
                }
                pstmt.executeUpdate();
            }

            // tb_member에도 동기화 (시급 표시용)
            String memberSql = roleId > 0
                ? "UPDATE tb_member SET role_id = ?, hourly_wage = ? WHERE mem_id = ?"
                : "UPDATE tb_member SET role_id = NULL, hourly_wage = 0 WHERE mem_id = ?";
            try (Connection conn = DBConn.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(memberSql)) {
                if (roleId > 0) {
                    pstmt.setInt(1, roleId);
                    pstmt.setInt(2, wage);
                    pstmt.setString(3, memId);
                } else {
                    pstmt.setString(1, memId);
                }
                pstmt.executeUpdate();
            }

            String msg = roleId > 0 ? "역할이 지정되었습니다." : "역할이 해제되었습니다.";
            out.print("{\"status\":\"success\", \"message\":\"" + msg + "\", \"wage\":" + wage + "}");

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류가 발생했습니다.\"}");
        }
    }
}
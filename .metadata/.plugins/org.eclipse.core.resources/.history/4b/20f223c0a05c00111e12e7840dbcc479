package work.api;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;
import java.sql.*;

@WebServlet("/AdminMemberUpdate")
public class AdminMemberUpdate extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        String role = session != null ? (String)session.getAttribute("userRole") : null;
        PrintWriter out = response.getWriter();

        if (!"A".equals(role)) {
            out.print("{\"status\":\"fail\",\"message\":\"권한 없음\"}");
            return;
        }

        String memId   = request.getParameter("id");
        String wageStr = request.getParameter("wage");

        if (memId == null || wageStr == null) {
            out.print("{\"status\":\"fail\",\"message\":\"파라미터 누락\"}");
            return;
        }

        int wage;
        try { wage = Integer.parseInt(wageStr); }
        catch (Exception e) {
            out.print("{\"status\":\"fail\",\"message\":\"시급 형식 오류\"}");
            return;
        }

        String sql = "UPDATE tb_member SET hourly_wage = ? WHERE mem_id = ?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wage);
            ps.setString(2, memId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                out.print("{\"status\":\"success\",\"message\":\"시급이 수정되었습니다.\"}");
            } else {
                out.print("{\"status\":\"fail\",\"message\":\"해당 직원을 찾을 수 없습니다.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
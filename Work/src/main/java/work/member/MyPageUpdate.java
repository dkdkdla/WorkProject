package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.util.DBConn;

@WebServlet("/MyPageUpdate")
public class MyPageUpdate extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String userId   = (String) session.getAttribute("userId");
        String phone    = request.getParameter("phone");
        String wage     = request.getParameter("wage");
        String newPw    = request.getParameter("new_pw");
        String workDays = request.getParameter("workDays");

        // 앱에서 호출 시 세션 없으므로 파라미터로도 받음
        if (userId == null) userId = request.getParameter("id");

        if (userId == null) {
            out.print("{\"status\":\"fail\", \"message\":\"로그인이 필요합니다.\"}");
            return;
        }

        try (Connection conn = DBConn.getConnection()) {
            boolean isPwUpdate = (newPw != null && !newPw.trim().isEmpty());

            String sql;
            if (isPwUpdate) {
                sql = "UPDATE tb_member SET mem_phone = ?, hourly_wage = ?, work_days = ?, mem_pw = ? WHERE mem_id = ?";
            } else {
                sql = "UPDATE tb_member SET mem_phone = ?, hourly_wage = ?, work_days = ? WHERE mem_id = ?";
            }

            if (isPwUpdate) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(newPw.getBytes("UTF-8"));
                byte[] byteData = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : byteData) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
                newPw = sb.toString();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, phone);
                pstmt.setInt(2, wage != null && !wage.isEmpty() ? Integer.parseInt(wage) : 0);
                pstmt.setString(3, workDays != null ? workDays : "");
                if (isPwUpdate) {
                    pstmt.setString(4, newPw);
                    pstmt.setString(5, userId);
                } else {
                    pstmt.setString(4, userId);
                }
                pstmt.executeUpdate();
                out.print("{\"status\":\"success\", \"message\":\"정보가 수정되었습니다.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"DB 오류: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}

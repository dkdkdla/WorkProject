package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/MyPageUpdate")
public class MyPageUpdate extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        
        String userId = (String)session.getAttribute("userId");
        String phone = request.getParameter("phone");
        String wage = request.getParameter("wage");
        String newPw = request.getParameter("new_pw");

        if (userId == null) {
            out.print("{\"status\":\"fail\", \"message\":\"로그인이 필요합니다.\"}");
            return;
        }

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        try (Connection conn = DriverManager.getConnection(dbUrl, "WorkUser", "pass")) {
            
            String sql;
            boolean isPwUpdate = (newPw != null && !newPw.trim().isEmpty());

            // 🚨 팩트체크: 컬럼명을 'mem_phone', 'hourly_wage', 'mem_pw'로 수정했습니다.
            if (isPwUpdate) {
                sql = "UPDATE tb_member SET mem_phone = ?, hourly_wage = ?, mem_pw = ? WHERE mem_id = ?";
                // SHA-256 암호화 로직
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(newPw.getBytes("UTF-8"));
                byte[] byteData = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : byteData) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
                newPw = sb.toString();
            } else {
                sql = "UPDATE tb_member SET mem_phone = ?, hourly_wage = ? WHERE mem_id = ?";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, phone);
                pstmt.setInt(2, Integer.parseInt(wage)); // 숫자로 변환하여 저장
                if (isPwUpdate) {
                    pstmt.setString(3, newPw);
                    pstmt.setString(4, userId);
                } else {
                    pstmt.setString(3, userId);
                }
                
                pstmt.executeUpdate();
                out.print("{\"status\":\"success\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 에러 원인을 정확히 화면에 띄우도록 수정
            out.print("{\"status\":\"error\", \"message\":\"DB 오류: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
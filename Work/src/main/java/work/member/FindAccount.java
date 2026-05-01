package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/FindAccount")
public class FindAccount extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String mode = request.getParameter("mode"); // "findId" 또는 "findPw"
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String id = request.getParameter("id");

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";

        try {
            // 🚨 [필수] JDBC 드라이버 수동 로딩 (No suitable driver 에러 해결)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            try (Connection conn = DriverManager.getConnection(dbUrl, "WorkUser", "pass")) {
                
                // 1. 아이디 찾기
                if ("findId".equals(mode)) {
                    String sql = "SELECT mem_id FROM tb_member WHERE mem_name = ? AND mem_phone = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, name);
                        pstmt.setString(2, phone);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                out.print("{\"status\":\"success\", \"userId\":\"" + rs.getString(1) + "\"}");
                            } else {
                                out.print("{\"status\":\"fail\", \"message\":\"일치하는 정보가 없습니다.\"}");
                            }
                        }
                    }
                } 
                
                // 2. 비밀번호 재설정 (임시 비번 발급)
                else if ("findPw".equals(mode)) {
                    String sql = "SELECT count(*) FROM tb_member WHERE mem_id = ? AND mem_phone = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, id);
                        pstmt.setString(2, phone);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                // 임시 비번 생성 및 암호화 처리
                                String tempPw = UUID.randomUUID().toString().substring(0, 6);
                                String encryptedPw = encryptSHA256(tempPw);
                                
                                String updateSql = "UPDATE tb_member SET mem_pw = ? WHERE mem_id = ?";
                                try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateSql)) {
                                    pstmtUpdate.setString(1, encryptedPw);
                                    pstmtUpdate.setString(2, id);
                                    pstmtUpdate.executeUpdate();
                                }
                                out.print("{\"status\":\"success\", \"tempPassword\":\"" + tempPw + "\"}");
                            } else {
                                out.print("{\"status\":\"fail\", \"message\":\"정보가 일치하지 않습니다.\"}");
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"드라이버 로딩 실패\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류 발생\"}");
        }
    }

    private String encryptSHA256(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes("UTF-8"));
            byte[] byteData = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : byteData) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            return sb.toString();
        } catch (Exception e) { return str; }
    }
}
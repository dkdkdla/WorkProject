package work.attendance;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Attendance")
public class Attendance extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String userId = (String)request.getSession().getAttribute("userId");
        String storeId = request.getParameter("storeId");
        String type = request.getParameter("type"); // 프론트에서 "in" 또는 "out"으로 전달

        if (userId == null || storeId == null || type == null) {
            out.print("{\"status\":\"fail\", \"message\":\"필수 정보가 누락되었습니다.\"}");
            return;
        }

        // DB에 저장할 타입 결정 (대문자로 통일: IN / OUT)
        String attType = type.equalsIgnoreCase("in") ? "IN" : "OUT";

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        String dbId = "WorkUser";
        String dbPw = "pass";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            // 🚨 팩트체크: tb_attendance는 모든 기록을 신규 삽입(INSERT)하는 구조입니다.
            String sql = "INSERT INTO tb_attendance (mem_id, store_id, att_type, att_time) VALUES (?, ?, ?, GETDATE())";
            
            try (Connection conn = DriverManager.getConnection(dbUrl, dbId, dbPw);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, userId);
                pstmt.setString(2, storeId);
                pstmt.setString(3, attType);
                
                pstmt.executeUpdate();
                
                String msg = attType.equals("IN") ? "출근 처리가 완료되었습니다." : "퇴근 처리가 완료되었습니다.";
                out.print("{\"status\":\"success\", \"message\":\"" + msg + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
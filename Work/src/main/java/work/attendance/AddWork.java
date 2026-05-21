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

@WebServlet("/AddWork")
public class AddWork extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 세션 체크
        javax.servlet.http.HttpSession session = request.getSession(false);
        String role = session != null ? (String)session.getAttribute("userRole") : null;
        if (role == null || !"A".equals(role.trim())) {
            out.print("{\"status\":\"fail\", \"message\":\"권한이 없습니다.\"}");
            return;
        }

        // 2. 파라미터 수신
        String id = request.getParameter("id");       
        String storeId = request.getParameter("storeId"); 
        String dateTime = request.getParameter("dateTime"); 
        String type = request.getParameter("type");   

        // 3. DB 정보 설정
        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        String dbId = "WorkUser";
        String dbPw = "pass";

        // 4. 로직 실행 (DB Insert)
        String sql = "INSERT INTO tb_attendance (mem_id, store_id, att_time, att_type) VALUES (?, ?, ?, ?)";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            try (Connection conn = DriverManager.getConnection(dbUrl, dbId, dbPw);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, id);
                pstmt.setString(2, storeId);
                pstmt.setString(3, dateTime); // 'yyyy-MM-dd HH:mm:ss' 형식 권장
                pstmt.setString(4, type);
                
                int result = pstmt.executeUpdate();

                if(result > 0) {
                    out.print("{\"status\":\"success\", \"message\":\"기록이 추가되었습니다.\"}");
                } else {
                    out.print("{\"status\":\"fail\", \"message\":\"추가에 실패했습니다.\"}");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
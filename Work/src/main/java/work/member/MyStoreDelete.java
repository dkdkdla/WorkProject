package work.member;

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

@WebServlet("/MyStoreDelete")
public class MyStoreDelete extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 🚨 마이페이지에서 fetch로 GET 방식을 호출하므로 doGet을 오버라이딩합니다.
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        // 쿼리 스트링에서 삭제할 대상(아이디, 매장코드)을 가져옵니다.
        String userId = request.getParameter("id");
        String storeId = request.getParameter("storeId");

        if (userId == null || storeId == null) {
            out.print("{\"status\":\"fail\", \"message\":\"필수 파라미터가 누락되었습니다.\"}");
            return;
        }

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        String dbId = "WorkUser";
        String dbPw = "pass";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            try (Connection conn = DriverManager.getConnection(dbUrl, dbId, dbPw)) {
                
                // 🚨 팩트체크: SSMS 확인 결과 테이블 이름은 'tb_link'입니다.
                String sql = "DELETE FROM tb_link WHERE mem_id = ? AND store_id = ?";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, userId);
                    pstmt.setString(2, storeId);
                    
                    int result = pstmt.executeUpdate();
                    
                    if (result > 0) {
                        out.print("{\"status\":\"success\", \"message\":\"매장이 삭제되었습니다.\"}");
                    } else {
                        out.print("{\"status\":\"fail\", \"message\":\"삭제할 대상을 찾지 못했습니다.\"}");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
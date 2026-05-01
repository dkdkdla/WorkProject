package work.board;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/BoardList")
public class BoardList extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 인코딩 및 JSON 응답 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String storeId = request.getParameter("storeId");

        // 파라미터 유효성 검사
        if (storeId == null || storeId.trim().isEmpty()) {
            out.print("{\"status\":\"fail\", \"message\":\"매장 ID가 전달되지 않았습니다.\"}");
            return;
        }

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        String dbId = "WorkUser";
        String dbPw = "pass";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            String sql = "SELECT post_id, writer_id, title, reg_date FROM tb_board WHERE store_id = ? ORDER BY post_id DESC";
            
            // DB 자원 자동 해제 (try-with-resources)
            try (Connection conn = DriverManager.getConnection(dbUrl, dbId, dbPw);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, storeId.trim());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("{\"status\":\"success\", \"data\":[");
                    
                    boolean isFirst = true;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    while (rs.next()) {
                        if (!isFirst) sb.append(",");
                        
                        // 데이터 추출 및 JSON 포맷팅
                        int postId = rs.getInt("post_id");
                        String writer = rs.getString("writer_id");
                        String title = rs.getString("title");
                        if (title != null) {
                            title = title.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
                        } else {
                            title = "";
                        }
                        String date = sdf.format(rs.getTimestamp("reg_date"));

                        sb.append("{");
                        sb.append("\"id\":\"").append(postId).append("\",");
                        sb.append("\"writer\":\"").append(writer).append("\",");
                        sb.append("\"title\":\"").append(title).append("\",");
                        sb.append("\"date\":\"").append(date).append("\"");
                        sb.append("}");
                        
                        isFirst = false;
                    }
                    
                    sb.append("]}");
                    out.print(sb.toString());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            // JSON 파싱 에러 방지를 위한 특수문자 치환
            String errMsg = e.getMessage().replace("\"", "'").replace("\n", " ").replace("\\", "/");
            out.print("{\"status\":\"error\", \"message\":\"" + errMsg + "\"}");
        }
    }
}
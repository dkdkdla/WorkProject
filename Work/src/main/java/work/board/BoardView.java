package work.board;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 🚨 프론트엔드에서 호출할 URL 주소를 정의합니다.
@WebServlet("/BoardView")
public class BoardView extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 응답 데이터 형식을 JSON으로 명확히 지정하여 한글 깨짐 방지
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idStr = request.getParameter("id");

        // 파라미터 유효성 검사
        if (idStr == null || idStr.trim().isEmpty()) {
            out.print("{\"status\":\"fail\", \"message\":\"파라미터가 없습니다.\"}");
            return;
        }

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        String dbId = "WorkUser";
        String dbPw = "pass";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            // 2. DB 연결 및 자원 자동 해제 (try-with-resources)
            try (Connection conn = DriverManager.getConnection(dbUrl, dbId, dbPw);
                 PreparedStatement pstmt = conn.prepareStatement("SELECT post_id, title, writer_id, reg_date, content, img_data, filename FROM tb_board WHERE post_id = ?")) {
                
                pstmt.setInt(1, Integer.parseInt(idStr.trim()));

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String title = rs.getString("title");
                        String writer = rs.getString("writer_id");
                        String date = rs.getString("reg_date");
                        String content = rs.getString("content");
                        String imgData = rs.getString("img_data");
                        String fileName = rs.getString("filename");

                        // 3. Null 처리 및 JSON 이스케이프 포맷팅
                        if (date != null && date.length() > 16) date = date.substring(0, 16);
                        if (title != null) title = title.replace("\"", "\\\"").replace("\n", " ");
                        if (content != null) content = content.replace("\"", "\\\"").replace("\r\n", "\\n").replace("\n", "\\n");
                        if (imgData == null) imgData = "";
                        if (fileName == null) fileName = "";

                        // 4. JSON 문자열 조립 후 출력
                        StringBuilder sb = new StringBuilder();
                        sb.append("{");
                        sb.append("\"status\":\"success\",");
                        sb.append("\"data\": {");
                        sb.append("\"title\":\"").append(title).append("\",");
                        sb.append("\"writer\":\"").append(writer).append("\",");
                        sb.append("\"date\":\"").append(date).append("\",");
                        sb.append("\"content\":\"").append(content).append("\",");
                        sb.append("\"imgData\":\"").append(imgData).append("\",");
                        sb.append("\"fileName\":\"").append(fileName).append("\",");
                        sb.append("\"orgName\":\"").append(fileName).append("\"");
                        sb.append("}");
                        sb.append("}");

                        out.print(sb.toString());
                    } else {
                        out.print("{\"status\":\"fail\", \"message\":\"해당 번호의 글이 없습니다.\"}");
                    }
                }
            }
        } catch (Exception e) {
            String errMsg = e.getMessage().replace("\"", "'").replace("\n", " ");
            out.print("{\"status\":\"error\", \"message\":\"서버 에러: " + errMsg + "\"}");
        }
    }
}
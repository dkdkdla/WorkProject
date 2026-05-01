package work.board;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Types;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

@WebServlet("/BoardWrite")
public class BoardWrite extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 인코딩 및 JSON 응답 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 업로드 경로 및 폴더 생성 로직
        String savePath = request.getServletContext().getRealPath("upload");
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        int sizeLimit = 100 * 1024 * 1024; // 100MB

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        String dbId = "WorkUser";
        String dbPw = "pass";

        try {
            // 파일 업로드 처리 (이 시점에 파일은 이미 지정된 경로에 저장됨)
            MultipartRequest multi = new MultipartRequest(
                    request, 
                    savePath, 
                    sizeLimit, 
                    "UTF-8", 
                    new DefaultFileRenamePolicy() 
            );

            // 파라미터 추출
            String storeId = multi.getParameter("storeId");
            String writerId = multi.getParameter("writerId");
            String title = multi.getParameter("title");
            String content = multi.getParameter("content");

            String fileName = multi.getFilesystemName("file");       
            String originalName = multi.getOriginalFileName("file"); 

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // 🚨 주의: original_filename 컬럼이 DB에 없으면 여기서 에러 발생
            String sql = "INSERT INTO tb_board (store_id, writer_id, title, content, filename, original_filename) VALUES (?, ?, ?, ?, ?, ?)";
            
            // DB 연결 및 쿼리 실행
            try (Connection conn = DriverManager.getConnection(dbUrl, dbId, dbPw);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, storeId);
                pstmt.setString(2, writerId);
                pstmt.setString(3, title);
                pstmt.setString(4, content);
                
                if (fileName != null) {
                    pstmt.setString(5, fileName);
                    pstmt.setString(6, originalName);
                } else {
                    pstmt.setNull(5, Types.VARCHAR);
                    pstmt.setNull(6, Types.VARCHAR);
                }

                int count = pstmt.executeUpdate();

                if (count > 0) {
                    out.print("{\"status\":\"success\", \"message\":\"게시글(파일포함) 등록 완료\"}");
                } else {
                    out.print("{\"status\":\"fail\", \"message\":\"DB 저장 실패\"}");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // JSON 문자열이 깨지지 않도록 예외 메시지 내부의 따옴표 및 줄바꿈 치환
            String errMsg = e.getMessage().replace("\"", "'").replace("\\", "/").replace("\n", " ");
            out.print("{\"status\":\"error\", \"message\":\"업로드 오류: " + errMsg + "\"}");
        }
    }
}
package work.store;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * [코드 설계 설명]
 * 1. 데이터 필터링: JOIN을 통해 tb_my_stores(연결 테이블)에서 현재 사용자(mem_id)가 등록된 매장만 가져옵니다.
 * 2. 규격화된 JSON: 안드로이드가 바로 처리할 수 있도록 [ { "id": "...", "name": "..." } ] 형태의 배열을 반환합니다.
 */
@WebServlet("/StoreList")
public class StoreList extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 안드로이드에서 ?id=... 로 보낸 값을 수신
        String userId = request.getParameter("id");

        try {
            // MSSQL 드라이버 로드
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false", "WorkUser", "pass")) {
                
                // 🚨 팩트체크: tb_my_stores 테이블과 조인하여 내 매장만 조회
                String sql = "SELECT s.store_id, s.store_name " +
                             "FROM tb_store s " +
                             "JOIN tb_my_stores m ON s.store_id = m.store_id " +
                             "WHERE m.mem_id = ? " +
                             "ORDER BY s.store_name ASC";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, userId);
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("["); // JSON 배열 시작
                        boolean first = true;
                        
                        while(rs.next()) {
                            if(!first) sb.append(",");
                            // 안드로이드 Toast 에러에서 본 키값 "id", "name" 유지
                            sb.append("{\"id\":\"").append(rs.getString(1)).append("\", ");
                            sb.append("\"name\":\"").append(rs.getString(2)).append("\"}");
                            first = false;
                        }
                        sb.append("]"); // JSON 배열 끝
                        out.print(sb.toString());
                    }
                }
            }
        } catch (Exception e) { 
            e.printStackTrace();
            out.print("[]"); // 에러 시 빈 배열 반환하여 안드로이드 크래시 방지
        }
    }
}
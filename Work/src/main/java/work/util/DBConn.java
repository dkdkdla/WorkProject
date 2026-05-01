package work.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 파일명 : DBConn.java
 * 설명 : SQL Server 데이터베이스 연결 및 자원 해제 공통 클래스
 */
public class DBConn {
    
    // DB 연결 정보 (보안을 위해 final 설정 유지)
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false;trustServerCertificate=true;";
    private static final String DB_ID = "WorkUser"; 
    private static final String DB_PW = "pass";     

    // 1. DB 연결 객체를 가져오는 메서드
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // 드라이버 로딩
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // 연결 시도
            conn = DriverManager.getConnection(DB_URL, DB_ID, DB_PW);
            // 성공 로그 (개발 시에만 확인용)
            // System.out.println("✅ DB 연결 성공");
        } catch (Exception e) {
            System.err.println("❌ DB 연결 실패! 주소/아이디/비번을 확인하세요.");
            e.printStackTrace();
        }
        return conn; 
    }
    
    // 2. [추가] 모든 자원(Connection, Statement, ResultSet)을 한 번에 닫는 메서드
    public static void closeAll(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    // 3. 단순 연결 해제용 (오버로딩)
    public static void close(Connection conn) {
        try { if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
    }
}
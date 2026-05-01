package work.admin;

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

@WebServlet("/AdminStore")
public class AdminStoreManager extends HttpServlet {
    
    // 1. 매장 목록 조회 (GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false", "WorkUser", "pass")) {
            String sql = "SELECT store_id, store_name FROM tb_store ORDER BY store_name ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("{\"status\":\"success\", \"data\":[");
                boolean first = true;
                while(rs.next()){
                    if(!first) sb.append(",");
                    sb.append("{\"id\":\"").append(rs.getString(1)).append("\", \"name\":\"").append(rs.getString(2)).append("\"}");
                    first = false;
                }
                sb.append("]}");
                out.print(sb.toString());
            }
        } catch (Exception e) { out.print("{\"status\":\"error\"}"); }
    }

    // 2. 매장 추가 및 삭제 (POST)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        String mode = request.getParameter("mode"); // "add" 또는 "delete"
        String storeId = request.getParameter("storeId");
        String storeName = request.getParameter("storeName");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false", "WorkUser", "pass")) {
            if("add".equals(mode)) {
                String sql = "INSERT INTO tb_store (store_id, store_name) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, storeId);
                    pstmt.setString(2, storeName);
                    pstmt.executeUpdate();
                    out.print("{\"status\":\"success\", \"message\":\"매장이 추가되었습니다.\"}");
                }
            } else if("delete".equals(mode)) {
                String sql = "DELETE FROM tb_store WHERE store_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, storeId);
                    pstmt.executeUpdate();
                    out.print("{\"status\":\"success\", \"message\":\"매장이 삭제되었습니다.\"}");
                }
            }
        } catch (Exception e) { out.print("{\"status\":\"error\", \"message\":\"이미 존재하거나 사용 중인 매장입니다.\"}"); }
    }
}
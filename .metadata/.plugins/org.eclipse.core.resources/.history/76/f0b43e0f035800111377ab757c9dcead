<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, work.util.DBConn" %>

<%
    // 1. 세션에서 현재 사용자의 매장 ID 가져오기 (매장별 게시글 분리)
    String userStoreId = (String)session.getAttribute("userStoreId");
    
    // 2. 해당 매장의 최신 게시글 5개만 가져오는 쿼리
    String sql = "SELECT TOP 5 title, reg_date FROM tb_board " +
                 "WHERE store_id = ? ORDER BY reg_date DESC";
    
    try (Connection conn = DBConn.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, userStoreId); // 매장 ID 바인딩
        
        try (ResultSet rs = pstmt.executeQuery()) {
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                String title = rs.getString("title");
                // 날짜 포맷 (년-월-일만 출력)
                String date = rs.getString("reg_date").substring(0, 10); 
%>
                <tr>
                    <td class="text-truncate" style="max-width: 180px;"><%= title %></td>
                    <td class="text-end text-muted small"><%= date %></td>
                </tr>
<%
            }
            
            if(!hasData) {
%>
                <tr><td colspan="2" class="text-center text-muted py-3 small">등록된 게시글이 없습니다.</td></tr>
<%
            }
        }
    } catch(Exception e) {
        e.printStackTrace();
%>
        <tr><td colspan="2" class="text-center text-danger small py-3">게시판 데이터를 불러올 수 없습니다.</td></tr>
<%
    }
%>
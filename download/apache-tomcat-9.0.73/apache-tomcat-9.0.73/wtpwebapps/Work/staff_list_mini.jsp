<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, work.util.DBConn" %>

<%
    // 1. 세션에서 현재 관리자의 매장 ID 가져오기
    String adminStoreId = (String)session.getAttribute("userStoreId");
    
    if (adminStoreId == null || adminStoreId.isEmpty()) {
%>
        <tr><td colspan="4" class="text-center text-muted py-3">매장 정보가 없습니다. 매장을 선택해주세요.</td></tr>
<%
    } else {
        // 2. 해당 매장에 소속된 직원('S') 목록 조회
        String sql = "SELECT mem_name, mem_phone, hourly_wage FROM tb_member " +
                     "WHERE store_id = ? AND mem_role = 'S' ORDER BY mem_name ASC";
        
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, adminStoreId);
            try (ResultSet rs = pstmt.executeQuery()) {
                
                boolean hasStaff = false;
                while(rs.next()) {
                    hasStaff = true;
                    String name = rs.getString("mem_name");
                    String phone = rs.getString("mem_phone");
                    int wage = rs.getInt("hourly_wage");
%>
                    <tr>
                        <td class="fw-bold text-dark"><%= name %></td>
                        <td class="small"><%= phone %></td>
                        <td class="small text-primary fw-bold"><%= String.format("%,d원", wage) %></td>
                        <td><span class="badge bg-success-subtle text-success border border-success-subtle" style="font-size: 0.7rem;">정직원</span></td>
                    </tr>
<%
                }
                
                if (!hasStaff) {
%>
                    <tr><td colspan="4" class="text-center text-muted py-3 small">등록된 직원이 없습니다.</td></tr>
<%
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
%>
            <tr><td colspan="4" class="text-center text-danger py-3 small">직원 목록을 불러오는 중 오류가 발생했습니다.</td></tr>
<%
        }
    }
%>
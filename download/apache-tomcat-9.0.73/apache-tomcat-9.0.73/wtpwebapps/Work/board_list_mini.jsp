<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, work.util.DBConn" %>

<%
    String userStoreId = (String)session.getAttribute("userStoreId");

    String sql = "SELECT TOP 5 post_id, title, reg_date FROM tb_board " +
                 "WHERE store_id = ? ORDER BY reg_date DESC";

    try (Connection conn = DBConn.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, userStoreId);

        try (ResultSet rs = pstmt.executeQuery()) {
            boolean hasData = false;
            while(rs.next()) {
                hasData = true;
                int    postId = rs.getInt("post_id");
                String title  = rs.getString("title");
                String date   = rs.getString("reg_date").substring(0, 10);
%>
                <tr>
                    <td class="text-truncate" style="max-width:180px;">
                        <a href="board_view.jsp?id=<%=postId%>&storeId=<%=userStoreId%>"
                            class="text-dark fw-bold text-decoration-none"
                            onmouseover="this.style.color='#4e73df'"
                            onmouseout="this.style.color='#212121'">
                            <%=title%>
                        </a>
                    </td>
                    <td class="text-end text-muted small"><%=date%></td>
                </tr>
<%
            }
            if (!hasData) {
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

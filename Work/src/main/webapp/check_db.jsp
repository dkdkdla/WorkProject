<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dao.MemberDAO" %>
<%@ page import="work.dto.MemberDTO" %>
<%@ page import="java.util.ArrayList" %>
<%
    // 🚨 전체관리자(SA)만 접근 가능
    String _role = (String) session.getAttribute("userRole");
    if (_role == null || !"SA".equals(_role.trim())) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404로 숨김 처리
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>DB 확인 - AlbaPass</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f4f4f4; }
        .store-id { color: red; font-weight: bold; }
    </style>
</head>
<body>
    <h2>전체 회원 목록 (매장 ID 및 시급 확인용)</h2>
    <table>
        <thead>
            <tr>
                <th>이름 (mem_name)</th>
                <th>아이디 (mem_id)</th>
                <th>소속 매장 (store_id)</th>
                <th>시급 (hourly_wage)</th>
            </tr>
        </thead>
        <tbody>
<%
    MemberDAO dao = new MemberDAO();
    ArrayList<MemberDTO> list = dao.getAllMembers();

    for(MemberDTO m : list) {
%>
            <tr>
                <td><%= m.getName() %></td>
                <td><%= m.getId() %></td>
                <td class="store-id"><%= (m.getStoreId() == null) ? "없음" : m.getStoreId() %></td>
                <td><%= String.format("%,d원", m.getHourlyWage()) %></td>
            </tr>
<%
    }
%>
        </tbody>
    </table>
    <p style="margin-top: 20px; color: blue;">※ 이 페이지는 개발 확인용이므로 배포 시 반드시 삭제하거나 접근을 제한해야 한다.</p>
</body>
</html>
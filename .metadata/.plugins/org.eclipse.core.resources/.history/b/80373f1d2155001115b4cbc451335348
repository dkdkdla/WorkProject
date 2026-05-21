<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.MemberDTO, java.util.ArrayList" %>
<%
    String userRole = (String) session.getAttribute("userRole");
    if (!"SA".equals(userRole)) {
        response.sendRedirect("login.jsp");
        return;
    }
    ArrayList<MemberDTO> pendingList   = (ArrayList<MemberDTO>) request.getAttribute("pendingList");
    ArrayList<String[]>  pendingStores = (ArrayList<String[]>)  request.getAttribute("pendingStores");
    if (pendingList   == null) pendingList   = new ArrayList<>();
    if (pendingStores == null) pendingStores = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>전체관리자 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container mt-5 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0">
            <i class="fa-solid fa-shield-halved me-2 text-danger"></i>전체관리자 대시보드
        </h3>
        <form action="<%=request.getContextPath()%>/Logout" method="post" class="m-0">
            <button type="submit" class="btn btn-danger btn-sm">로그아웃</button>
        </form>
    </div>

    <%-- 점장 가입 승인 대기 --%>
    <div class="card border-0 shadow-sm p-4 mb-4">
        <h5 class="fw-bold mb-3">
            <i class="fa-solid fa-user-clock me-2 text-warning"></i>점장 가입 승인 대기
            <span class="badge bg-warning text-dark ms-2"><%=pendingList.size()%></span>
        </h5>

        <% if (pendingList.isEmpty()) { %>
            <div class="text-center text-muted py-3">
                <i class="fa-solid fa-check-circle fa-2x mb-2 text-success"></i>
                <p class="mb-0 small">대기 중인 가입 신청이 없습니다.</p>
            </div>
        <% } else { %>
        <div class="table-responsive">
            <table class="table align-middle mb-0">
                <thead class="table-light">
                    <tr><th>아이디</th><th>이름</th><th>전화번호</th><th class="text-center">처리</th></tr>
                </thead>
                <tbody>
                    <% for (MemberDTO m : pendingList) { %>
                    <tr id="member-row-<%=m.getId()%>">
                        <td class="fw-bold"><%=m.getId()%></td>
                        <td><%=m.getName()%></td>
                        <td class="text-muted"><%=m.getPhone()%></td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-success fw-bold me-1"
                                onclick="handleAction('<%=m.getId()%>', 'member', 'approve')">
                                <i class="fa-solid fa-check me-1"></i>승인
                            </button>
                            <button class="btn btn-sm btn-danger fw-bold"
                                onclick="handleAction('<%=m.getId()%>', 'member', 'reject')">
                                <i class="fa-solid fa-xmark me-1"></i>거절
                            </button>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </div>

    <%-- 매장 생성 승인 대기 --%>
    <div class="card border-0 shadow-sm p-4">
        <h5 class="fw-bold mb-3">
            <i class="fa-solid fa-store me-2 text-primary"></i>매장 생성 승인 대기
            <span class="badge bg-primary ms-2"><%=pendingStores.size()%></span>
        </h5>

        <% if (pendingStores.isEmpty()) { %>
            <div class="text-center text-muted py-3">
                <i class="fa-solid fa-check-circle fa-2x mb-2 text-success"></i>
                <p class="mb-0 small">대기 중인 매장 생성 신청이 없습니다.</p>
            </div>
        <% } else { %>
        <div class="table-responsive">
            <table class="table align-middle mb-0">
                <thead class="table-light">
                    <tr><th>매장 코드</th><th>매장 이름</th><th>신청 점장</th><th class="text-center">처리</th></tr>
                </thead>
                <tbody>
                    <% for (String[] s : pendingStores) { %>
                    <tr id="store-row-<%=s[0]%>">
                        <td class="fw-bold"><%=s[0]%></td>
                        <td><%=s[1]%></td>
                        <td class="text-muted"><%=s[2]%> (<%=s[3]%>)</td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-success fw-bold me-1"
                                onclick="handleAction('<%=s[0]%>', 'store', 'approve')">
                                <i class="fa-solid fa-check me-1"></i>승인
                            </button>
                            <button class="btn btn-sm btn-danger fw-bold"
                                onclick="handleAction('<%=s[0]%>', 'store', 'reject')">
                                <i class="fa-solid fa-xmark me-1"></i>거절
                            </button>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </div>
</div>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
async function handleAction(targetId, type, mode) {
    const label  = type === 'store' ? '매장' : '회원';
    const action = mode === 'approve' ? '승인' : '거절';
    if (!confirm('"' + targetId + '" ' + label + '을(를) ' + action + '하시겠습니까?')) return;

    const params = new URLSearchParams();
    params.append('targetId', targetId);
    params.append('type', type);
    params.append('mode', mode);

    try {
        const res  = await fetch('SuperAdminApprove', { method: 'POST', body: params });
        const data = await res.json();
        alert(data.message);
        if (data.status === 'success') {
            const rowId = (type === 'store' ? 'store-row-' : 'member-row-') + targetId;
            const row = document.getElementById(rowId);
            if (row) row.remove();

            // 뱃지 카운트 자동 갱신
            const remainRows = type === 'store'
                ? document.querySelectorAll('[id^="store-row-"]').length
                : document.querySelectorAll('[id^="member-row-"]').length;

            const allBadges = document.querySelectorAll('.badge');
            allBadges.forEach(b => {
                if (type === 'member' && b.classList.contains('bg-warning')) b.innerText = remainRows;
                if (type === 'store'  && b.classList.contains('bg-primary'))  b.innerText = remainRows;
            });
        }
    } catch (e) {
        alert('통신 오류가 발생했습니다.');
    }
}
</script>
</body>
</html>

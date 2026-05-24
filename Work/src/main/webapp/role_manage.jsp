<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList" %>
<%
    String userRole = (String) session.getAttribute("userRole");
    String storeId  = (String) session.getAttribute("userStoreId");
    if (!"A".equals(userRole != null ? userRole.trim() : "")) {
        response.sendRedirect("login.jsp");
        return;
    }
    ArrayList<String[]> roleList = (ArrayList<String[]>) request.getAttribute("roleList");
    if (roleList == null) roleList = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>역할 관리 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container mt-5 mb-5" style="max-width:700px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0">
            <i class="fa-solid fa-tags me-2 text-primary"></i>역할 관리
        </h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드로
        </a>
    </div>

    <%-- 역할 추가 --%>
    <div class="card border-0 shadow-sm p-4 mb-4" style="border-top: 4px solid #4e73df !important;">
        <h5 class="fw-bold mb-3"><i class="fa-solid fa-plus-circle me-2 text-primary"></i>새 역할 추가</h5>
        <div class="row g-3">
            <div class="col-md-5">
                <input type="text" id="newRoleName" class="form-control" placeholder="역할 이름 (예: 홀 직원)">
            </div>
            <div class="col-md-4">
                <div class="input-group">
                    <input type="number" id="newWage" class="form-control" placeholder="시급" min="0">
                    <span class="input-group-text">원/시</span>
                </div>
                <small class="text-muted" style="font-size:11px;">예: 10320 (2026년 최저임금)</small>
            </div>
            <div class="col-md-3">
                <button type="button" onclick="addRole()" class="btn btn-primary fw-bold w-100">추가</button>
            </div>
        </div>
    </div>

    <%-- 역할 목록 --%>
    <div class="card border-0 shadow-sm p-4">
        <h5 class="fw-bold mb-3"><i class="fa-solid fa-list me-2 text-muted"></i>역할 목록</h5>

        <% if (roleList.isEmpty()) { %>
            <div class="text-center text-muted py-4">
                <i class="fa-solid fa-tag fa-2x mb-2"></i>
                <p class="mb-0 small">등록된 역할이 없습니다.</p>
            </div>
        <% } else { %>
        <div class="table-responsive">
            <table class="table align-middle mb-0">
                <thead class="table-light">
                    <tr><th>역할 이름</th><th>시급</th><th class="text-center">관리</th></tr>
                </thead>
                <tbody id="roleTableBody">
                    <% for (String[] r : roleList) { %>
                    <tr id="role-row-<%=r[0]%>">
                        <td>
                            <input type="text" class="form-control form-control-sm border-0 bg-transparent fw-bold"
                                id="roleName-<%=r[0]%>" value="<%=r[1]%>">
                        </td>
                        <td>
                            <div class="input-group input-group-sm" style="max-width:160px;">
                                <input type="number" class="form-control border-0 bg-transparent fw-bold text-primary"
                                    id="roleWage-<%=r[0]%>" value="<%=r[2]%>">
                                <span class="input-group-text bg-transparent border-0 text-muted">원/시</span>
                            </div>
                            <small class="text-muted" style="font-size:11px;">
                                월 160h 기준: <%=String.format("%,d", Integer.parseInt(r[2]) * 160)%>원
                            </small>
                        </td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-primary fw-bold me-1"
                                onclick="editRole('<%=r[0]%>')">
                                <i class="fa-solid fa-check"></i> 저장
                            </button>
                            <button class="btn btn-sm btn-outline-danger fw-bold"
                                onclick="deleteRole('<%=r[0]%>', '<%=r[1]%>')">
                                <i class="fa-solid fa-trash"></i>
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
// 역할 추가
async function addRole() {
    const roleName = document.getElementById('newRoleName').value.trim();
    const wage     = document.getElementById('newWage').value.trim();
    if (!roleName) { alert('역할 이름을 입력해주세요.'); return; }

    const params = new URLSearchParams();
    params.append('mode', 'add');
    params.append('roleName', roleName);
    params.append('wage', wage || '0');

    const res  = await fetch('RoleManage', { method: 'POST', body: params });
    const data = await res.json();
    alert(data.message);
    if (data.status === 'success') location.reload();
}

// 역할 수정
async function editRole(roleId) {
    const roleName = document.getElementById('roleName-' + roleId).value.trim();
    const wage     = document.getElementById('roleWage-' + roleId).value.trim();

    const params = new URLSearchParams();
    params.append('mode', 'edit');
    params.append('roleId', roleId);
    params.append('roleName', roleName);
    params.append('wage', wage || '0');

    const res  = await fetch('RoleManage', { method: 'POST', body: params });
    const data = await res.json();
    alert(data.message);
}

// 역할 삭제
async function deleteRole(roleId, roleName) {
    if (!confirm('[' + roleName + '] 역할을 삭제하시겠습니까?')) return;

    const params = new URLSearchParams();
    params.append('mode', 'delete');
    params.append('roleId', roleId);

    const res  = await fetch('RoleManage', { method: 'POST', body: params });
    const data = await res.json();
    alert(data.message);
    if (data.status === 'success') {
        const row = document.getElementById('role-row-' + roleId);
        if (row) row.remove();
    }
}
</script>
</body>
</html>

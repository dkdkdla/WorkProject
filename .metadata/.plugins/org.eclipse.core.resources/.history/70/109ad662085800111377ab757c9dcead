<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.MemberDTO" %>
<%@ page import="java.util.ArrayList" %>
<%
    String loggedInId   = (String)session.getAttribute("userId");
    String userRolesess = (String)session.getAttribute("userRole");
    String currentStoreId = (String)session.getAttribute("userStoreId");
    if (currentStoreId == null) currentStoreId = "";

    if (!"A".equals(userRolesess != null ? userRolesess.trim() : "")) {
        response.sendRedirect("login.jsp");
        return;
    }

    ArrayList<MemberDTO> list = (ArrayList<MemberDTO>)request.getAttribute("memberList");
    if (list == null) {
        response.sendRedirect("AdminMemberList");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>직원 관리 - AlbaPass</title>
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
            <i class="fa-solid fa-user-gear me-2 text-info"></i>직원 관리
        </h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드로
        </a>
    </div>

    <!-- 검색 + 필터 -->
    <div class="card custom-card p-4 mb-4 shadow-sm border-0">
        <div class="row g-2">
            <div class="col-md-6">
                <div class="input-group">
                    <span class="input-group-text bg-white border-end-0">
                        <i class="fa-solid fa-magnifying-glass text-muted"></i>
                    </span>
                    <input type="text" id="searchInput" class="form-control border-start-0 ps-0"
                        placeholder="이름이나 전화번호로 검색..." oninput="filterTable()">
                </div>
            </div>
            <div class="col-md-3">
                <select id="roleFilter" class="form-select" onchange="filterTable()">
                    <option value="">전체 역할</option>
                    <option value="unassigned">역할 미지정</option>
                </select>
            </div>
            <div class="col-md-3">
                <select id="typeFilter" class="form-select" onchange="filterTable()">
                    <option value="">전체 (점장+직원)</option>
                    <option value="staff">직원만</option>
                    <option value="admin">점장만</option>
                </select>
            </div>
        </div>
    </div>

    <!-- 직원 목록 -->
    <div class="card custom-card p-0 overflow-hidden shadow-sm border-0">
        <div class="table-responsive">
            <table class="table custom-table mb-0 align-middle" id="memberTable">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">이름</th>
                        <th>전화번호</th>
                        <th>역할 / 시급</th>
                        <th>근무 요일</th>
                        <th class="text-center">관리</th>
                    </tr>
                </thead>
                <tbody>
<%
    if (list.isEmpty()) {
%>
                    <tr>
                        <td colspan="5" class="text-center text-muted py-5">현재 등록된 직원이 없습니다.</td>
                    </tr>
<%
    } else {
        for (MemberDTO m : list) {
            boolean isSelf  = m.getId().equals(loggedInId);
            boolean isAdmin = "A".equals(m.getRole() != null ? m.getRole().trim() : "");
            boolean noRole  = !isAdmin && m.getRoleId() == 0;
            String rowClass = isSelf ? "table-info-subtle" : (noRole ? "table-warning" : "");
%>
                    <tr class="<%=rowClass%>">
                        <td class="ps-4">
                            <span class="fw-bold text-dark"><%=m.getName()%></span>
                            <%if (isAdmin) {%>
                                <span class="badge bg-primary ms-1" style="font-size:0.7rem;">점장</span>
                            <%} else {%>
                                <span class="badge bg-warning text-dark ms-1" style="font-size:0.7rem;">직원</span>
                            <%}%>
                            <%if (noRole) {%>
                                <span class="badge bg-danger ms-1" style="font-size:0.7rem;">역할 미지정</span>
                            <%}%>
                            <%if (isSelf) {%>
                                <span class="badge bg-dark ms-1" style="font-size:0.7rem;">나</span>
                            <%}%>
                        </td>
                        <td class="text-muted"><%=m.getPhone()%></td>
                        <td style="min-width:200px;">
                            <div class="d-flex align-items-center gap-2">
                                <select class="form-select form-select-sm role-select"
                                    id="roleSelect-<%=m.getId()%>"
                                    onchange="assignRole('<%=m.getId()%>', this)"
                                    data-current="<%=m.getRoleId()%>"
                                    style="max-width:160px;">
                                    <option value="0">-- 역할 미지정 --</option>
                                </select>
                                <small class="text-primary fw-bold text-nowrap" id="wage-<%=m.getId()%>">
                                    <%=m.getHourlyWage() > 0 ? String.format("%,d", m.getHourlyWage()) + "원" : "-"%>
                                </small>
                            </div>
                        </td>
                        <td>
                            <%if (m.getWorkDays() != null && !m.getWorkDays().isEmpty()) {
                                for (String day : m.getWorkDays().split(",")) {%>
                                    <span class="badge bg-primary me-1"><%=day.trim()%></span>
                            <%  }
                            } else {%>
                                <span class="text-muted small">미정</span>
                            <%}%>
                        </td>
                        <td class="text-center">
                            <%if (!isSelf) {%>
                                <a href="javascript:void(0);"
                                    onclick="removeStaff('<%=m.getId()%>', '<%=m.getName()%>')"
                                    class="btn btn-sm btn-danger fw-bold">
                                    <i class="fa-solid fa-user-minus"></i> 제외
                                </a>
                            <%} else {%>
                                <button class="btn btn-sm btn-secondary fw-bold" disabled>
                                    <i class="fa-solid fa-lock"></i> 고정
                                </button>
                            <%}%>
                        </td>
                    </tr>
<%
        } // end for
    } // end else
%>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>

<script>
document.addEventListener('DOMContentLoaded', function() {
    loadRoleOptions();
});

async function loadRoleOptions() {
    try {
        const storeId = '<%=currentStoreId%>';
        const res  = await fetch('GetRoles?storeId=' + encodeURIComponent(storeId));
        const data = await res.json();

        if (data.status === 'success') {
            const roleFilterEl = document.getElementById('roleFilter');
            document.querySelectorAll('.role-select').forEach(function(select) {
                const currentVal = select.dataset.current || '0';
                data.list.forEach(role => {
                    const opt = document.createElement('option');
                    opt.value       = role.roleId;
                    opt.text        = role.roleName;
                    opt.dataset.wage = role.wage;
                    if (String(role.roleId) === String(currentVal)) opt.selected = true;
                    select.appendChild(opt);
                });
            });
            // 역할 필터 드롭다운에도 추가
            data.list.forEach(role => {
                const opt = document.createElement('option');
                opt.value = role.roleName;
                opt.text  = role.roleName;
                roleFilterEl.appendChild(opt);
            });
        }
    } catch (e) { console.error(e); }
}

async function assignRole(memId, select) {
    const roleId   = select.value;
    const roleName = select.options[select.selectedIndex].text;

    if (!confirm('"' + roleName + '" 역할을 지정하시겠습니까?')) {
        select.value = select.dataset.current || '0';
        return;
    }

    const params = new URLSearchParams();
    params.append('memId',  memId);
    params.append('roleId', roleId);

    try {
        const res  = await fetch('RoleAssign', { method: 'POST', body: params });
        const data = await res.json();

        if (data.status === 'success') {
            select.dataset.current = roleId;
            const wageEl = document.getElementById('wage-' + memId);
            if (wageEl) {
                wageEl.innerText = data.wage > 0 ? Number(data.wage).toLocaleString() + '원' : '-';
                const badge = document.createElement('span');
                badge.className = 'badge bg-success ms-1';
                badge.innerText = '저장됨';
                wageEl.after(badge);
                setTimeout(() => badge.remove(), 2000);
            }
        } else {
            showToast(data.message, 'danger');
            select.value = select.dataset.current || '0';
        }
    } catch (e) {
        showToast('통신 오류가 발생했습니다.', 'danger');
        select.value = select.dataset.current || '0';
    }
}

function filterTable() {
    const nameFilter = document.getElementById('searchInput').value.toUpperCase();
    const roleFilter = document.getElementById('roleFilter').value;
    const typeFilter = document.getElementById('typeFilter').value;
    const rows = document.querySelectorAll('#memberTable tbody tr');

    rows.forEach(tr => {
        if (tr.cells.length < 2) return;
        const name    = tr.cells[0] ? tr.cells[0].innerText : '';
        const phone   = tr.cells[1] ? tr.cells[1].innerText : '';
        const roleSelect = tr.querySelector('.role-select');
        const isAdmin = tr.cells[0].innerText.includes('점장');

        // 이름/전화 검색
        const nameMatch = name.toUpperCase().includes(nameFilter) || phone.toUpperCase().includes(nameFilter);

        // 역할 필터
        let roleMatch = true;
        if (roleFilter === 'unassigned') {
            roleMatch = roleSelect && roleSelect.value === '0';
        } else if (roleFilter !== '') {
            roleMatch = roleSelect && roleSelect.options[roleSelect.selectedIndex].text === roleFilter;
        }

        // 타입 필터
        let typeMatch = true;
        if (typeFilter === 'staff')  typeMatch = !isAdmin;
        if (typeFilter === 'admin')  typeMatch = isAdmin;

        tr.style.display = (nameMatch && roleMatch && typeMatch) ? '' : 'none';
    });
}

function removeStaff(id, name) {
    if (confirm("'" + name + "' 직원을 현재 매장에서 제외하시겠습니까?\n계정은 유지되지만 매장 리스트에서 사라집니다.")) {
        location.href = 'AdminMemberDelete?id=' + id;
    }
}
</script>
</body>
</html>

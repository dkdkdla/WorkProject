<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.MemberDTO" %>
<%@ page import="java.util.ArrayList" %>
<%
    // 세션에서 로그인한 내 아이디 가져오기
    String loggedInId = (String)session.getAttribute("userId");
    String currentStoreId = (String)session.getAttribute("userStoreId");
    if (currentStoreId == null) currentStoreId = "";

    // 서블릿에서 넘겨준 리스트 데이터 수신
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
        <h3 class="fw-bold text-dark mb-0"><i class="fa-solid fa-user-gear me-2 text-info"></i>직원 관리</h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드로 돌아가기
        </a>
    </div>

    <div class="card custom-card p-4 mb-4 shadow-sm border-0">
        <div class="input-group">
            <span class="input-group-text bg-white border-end-0"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
            <input type="text" id="searchInput" class="form-control border-start-0 ps-0" placeholder="이름이나 전화번호로 직원을 검색하세요..." oninput="filterTable()">
            <button class="btn btn-primary px-4 fw-bold" type="button" onclick="filterTable()">
                <i class="fa-solid fa-magnifying-glass me-1"></i> 검색
            </button>
        </div>
    </div>

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
                    <% if (list.isEmpty()) { %>
                        <tr><td colspan='5' class='text-center text-muted py-5'>현재 등록된 직원이 없습니다.</td></tr>
                    <% } else {
                        for (MemberDTO m : list) { 
                            boolean isSelf = m.getId().equals(loggedInId);
                            boolean isAdmin = "A".equals(m.getRole());
                    %>
                        <tr <%= isSelf ? "class='table-info-subtle'" : (m.getRoleId() == 0 && !"A".equals(m.getRole()) ? "class='table-warning'" : "") %>>
                            <td class="ps-4">
                                <span class="fw-bold text-dark"><%=m.getName()%></span>
                                <% if (isAdmin) { %>
                                    <span class="badge bg-primary ms-1" style="font-size: 0.7rem;">점장</span>
                                <% } else { %>
                                    <span class="badge bg-warning text-dark ms-1" style="font-size: 0.7rem;">직원</span>
                                <% } %>
                                <% if (!isAdmin && m.getRoleId() == 0) { %>
                                <span class="badge bg-danger ms-1" style="font-size:0.7rem;">역할 미지정</span>
                                <% } %>
                                <% if (isSelf) { %>
                                    <span class="badge bg-dark text-white ms-1" style="font-size: 0.7rem;">나</span>
                                <% } %>
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
                                <% if (m.getWorkDays() != null && !m.getWorkDays().isEmpty()) {
                                    String[] days = m.getWorkDays().split(",");
                                    for (String day : days) { %>
                                        <span class="badge bg-primary me-1"><%=day.trim()%></span>
                                    <% } %>
                                <% } else { %>
                                    <span class="text-muted small">미정</span>
                                <% } %>
                            </td>
                            <td class="text-center">
                                <a href="AdminMemberUpdate?id=<%=m.getId()%>" class="btn btn-sm btn-warning text-white fw-bold me-1">
                                    <i class="fa-solid fa-pen"></i> 수정
                                </a>
                                <%-- 🚨 본인 계정이 아닐 때만 제외 버튼 표시 --%>
                                <% if (!isSelf) { %>
                                    <a href="javascript:void(0);" onclick="removeStaff('<%=m.getId()%>', '<%=m.getName()%>')" class="btn btn-sm btn-danger fw-bold">
                                        <i class="fa-solid fa-user-minus"></i> 제외
                                    </a>
                                <% } else { %>
                                    <button class="btn btn-sm btn-secondary fw-bold" disabled>
                                        <i class="fa-solid fa-lock"></i> 고정
                                    </button>
                                <% } %>
                            </td>
                        </tr>
                    <%  }
                       } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%-- JavaScript에서 안전하게 값을 가져가기 위한 hidden 태그 추가 --%>
<input type="hidden" id="currentStoreId" value="<%=currentStoreId%>">

<script>
// 페이지 로드 시 역할 목록 로드
document.addEventListener('DOMContentLoaded', function() {
    loadRoleOptions();
});

// 현재 매장의 역할 목록 로드 후 모든 드롭다운에 적용
async function loadRoleOptions() {
    try {
        // JSP 표현식 대신 hidden input 요소의 값을 읽어옴 (Eclipse 에디터 오류 방지)
        const storeId = document.getElementById('currentStoreId').value;
        const res  = await fetch('GetRoles?storeId=' + encodeURIComponent(storeId));
        const data = await res.json();

        if (data.status === 'success') {
            document.querySelectorAll('.role-select').forEach(function(select) {
                // 기존 옵션 유지하고 역할 추가
                const currentVal = select.dataset.current || '0';
                data.list.forEach(role => {
                    const opt = document.createElement('option');
                    opt.value = role.roleId;
                    opt.text  = role.roleName;
                    opt.dataset.wage = role.wage;
                    if (String(role.roleId) === String(currentVal)) opt.selected = true;
                    select.appendChild(opt);
                });
            });
        }
    } catch (e) { console.error(e); }
}

// 역할 지정
async function assignRole(memId, select) {
    const roleId = select.value;
    const roleName = select.options[select.selectedIndex].text;

    if (!confirm('"' + roleName + '" 역할을 지정하시겠습니까?')) {
        // 취소 시 원래 값으로 복원
        select.value = select.dataset.current || '0';
        return;
    }

    const params = new URLSearchParams();
    params.append('memId', memId);
    params.append('roleId', roleId);

    try {
        const res  = await fetch('RoleAssign', { method: 'POST', body: params });
        const data = await res.json();

        if (data.status === 'success') {
            select.dataset.current = roleId;
            // 시급 업데이트
            const wageEl = document.getElementById('wage-' + memId);
            if (wageEl) {
                wageEl.innerText = data.wage > 0 ? data.wage.toLocaleString() + '원' : '-';
            }
            // 성공 토스트 (alert 대신 조용히)
            const badge = document.createElement('span');
            badge.className = 'badge bg-success ms-1';
            badge.innerText = '저장됨';
            wageEl.after(badge);
            setTimeout(() => badge.remove(), 2000);
        } else {
            showToast(data.message, data.status === 'success' ? 'success' : 'danger');
            select.value = select.dataset.current || '0';
        }
    } catch (e) {
        showToast('통신 오류가 발생했습니다.', 'warning');
        select.value = select.dataset.current || '0';
    }
}

function filterTable() {
    let input = document.getElementById("searchInput");
    let filter = input.value.toUpperCase();
    let table = document.getElementById("memberTable");
    let tr = table.getElementsByTagName("tr");

    for (let i = 1; i < tr.length; i++) {
        let tdName = tr[i].getElementsByTagName("td")[0]; 
        let tdPhone = tr[i].getElementsByTagName("td")[1]; 
        
        if (tdName && tdPhone) {
            let name = tdName.textContent || tdName.innerText;
            let phone = tdPhone.textContent || tdPhone.innerText;
            
            tr[i].style.display = (name.toUpperCase().includes(filter) || phone.toUpperCase().includes(filter)) ? "" : "none";
        }
    }
}

function removeStaff(id, name) {
    // 영구 삭제가 아닌 '매장 제외'임을 안내
    if (confirm("정말 '" + name + "' 직원을 현재 매장에서 제외하시겠습니까?\n이 직원의 계정은 유지되지만, 현재 매장 리스트에서는 사라집니다.")) {
        location.href = 'AdminMemberDelete?id=' + id;
    }
}
</script>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
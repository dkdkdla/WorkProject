<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList" %>
<%
    String userRole  = (String) session.getAttribute("userRole");
    String userId    = (String) session.getAttribute("userId");

    if (userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    if (!"A".equals(userRole) && !"S".equals(userRole)) {
        response.sendRedirect("default.jsp");
        return;
    }

    ArrayList<String[]> myStores    = (ArrayList<String[]>) request.getAttribute("myStores");
    ArrayList<String[]> pendingJoins = (ArrayList<String[]>) request.getAttribute("pendingJoins");
    if (myStores     == null) myStores     = new ArrayList<>();
    if (pendingJoins == null) pendingJoins = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>매장 관리 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container mt-5 mb-5" style="max-width: 700px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0">
            <i class="fa-solid fa-store me-2 text-primary"></i>매장 관리
        </h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드로
        </a>
    </div>

    <%-- ====== 점장 전용: 직원 소속 신청 승인 대기 ====== --%>
    <% if ("A".equals(userRole)) { %>
    <div class="card border-0 shadow-sm p-4 mb-4" style="border-top: 4px solid #e74a3b !important;">
        <h5 class="fw-bold mb-3">
            <i class="fa-solid fa-user-clock me-2 text-danger"></i>직원 소속 신청 승인 대기
            <span class="badge bg-danger ms-2"><%=pendingJoins.size()%></span>
        </h5>
        <% if (pendingJoins.isEmpty()) { %>
            <div class="text-center text-muted py-3">
                <i class="fa-solid fa-check-circle fa-2x mb-2 text-success"></i>
                <p class="mb-0 small">대기 중인 소속 신청이 없습니다.</p>
            </div>
        <% } else { %>
        <div class="table-responsive">
            <table class="table align-middle mb-0">
                <thead class="table-light">
                    <tr><th>아이디</th><th>이름</th><th>전화번호</th><th>신청 매장</th><th class="text-center">처리</th></tr>
                </thead>
                <tbody>
                    <% for (String[] join : pendingJoins) { %>
                    <tr id="join-row-<%=join[0]%>-<%=join[3]%>">
                        <td class="fw-bold"><%=join[0]%></td>
                        <td><%=join[1]%></td>
                        <td class="text-muted"><%=join[2]%></td>
                        <td><span class="badge bg-primary"><%=join[4]%></span></td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-success fw-bold me-1"
                                onclick="handleJoin('<%=join[0]%>', '<%=join[3]%>', 'approveJoin')">
                                <i class="fa-solid fa-check me-1"></i>승인
                            </button>
                            <button class="btn btn-sm btn-danger fw-bold"
                                onclick="handleJoin('<%=join[0]%>', '<%=join[3]%>', 'rejectJoin')">
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
    <% } %>

    <%-- ====== 점장 전용: 새 매장 생성 신청 ====== --%>
    <% if ("A".equals(userRole)) { %>
    <div class="card border-0 shadow-sm p-4 mb-4" style="border-top: 4px solid #4e73df !important;">
        <h5 class="fw-bold mb-1">
            <i class="fa-solid fa-plus-circle me-2 text-primary"></i>새 매장 생성 신청
            <span class="badge bg-primary ms-2 small">점장 전용</span>
        </h5>
        <p class="text-muted small mb-4">매장 생성 신청 후 전체관리자 승인이 완료되면 사용 가능합니다.</p>
        <div class="row g-3">
            <div class="col-md-5">
                <label class="form-label small fw-bold text-secondary">매장 코드 (ID)</label>
                <input type="text" id="storeId" class="form-control fw-bold" placeholder="예: store01" maxlength="20">
                <small class="text-muted">영문/숫자 조합, 중복 불가</small>
            </div>
            <div class="col-md-5">
                <label class="form-label small fw-bold text-secondary">매장 이름</label>
                <input type="text" id="storeName" class="form-control" placeholder="예: 강남점">
            </div>
            <div class="col-md-2 d-flex align-items-end">
                <button type="button" onclick="requestStore()" class="btn btn-primary fw-bold w-100">신청</button>
            </div>
        </div>
    </div>
    <% } %>

    <%-- ====== 직원 전용: 매장 소속 신청 ====== --%>
    <% if ("S".equals(userRole)) { %>
    <div class="card border-0 shadow-sm p-4 mb-4" style="border-top: 4px solid #f6c23e !important;">
        <h5 class="fw-bold mb-1">
            <i class="fa-solid fa-magnifying-glass me-2 text-warning"></i>매장 소속 신청
            <span class="badge bg-warning text-dark ms-2 small">직원 전용</span>
        </h5>
        <p class="text-muted small mb-4">근무할 매장을 검색하여 소속을 신청하세요. <strong>점장 승인 후 소속이 확정됩니다.</strong></p>
        <div class="row g-3" style="position:relative;">
            <div class="col-md-8">
                <label class="form-label small fw-bold text-secondary">매장 이름 또는 코드 검색</label>
                <div class="input-group">
                    <input type="text" id="searchKeyword" class="form-control"
                        placeholder="매장 이름 또는 코드 입력"
                        oninput="searchStore(this.value)">
                    <button class="btn btn-outline-secondary" type="button"
                        onclick="searchStore(document.getElementById('searchKeyword').value)">
                        <i class="fa-solid fa-magnifying-glass"></i>
                    </button>
                </div>
                <div id="searchResult" class="list-group shadow-sm mt-1"
                    style="display:none; position:absolute; z-index:100; width:calc(100% - 24px);"></div>
            </div>
            <div class="col-md-4 d-flex align-items-end">
                <button type="button" onclick="joinStore()" class="btn btn-warning fw-bold w-100 text-dark">소속 신청</button>
            </div>
        </div>
        <input type="hidden" id="selectedStoreId">
        <div id="selectedStoreInfo" class="mt-2" style="display:none;">
            <small class="text-primary fw-bold"><i class="fa-solid fa-check me-1"></i>선택된 매장: <span id="selectedStoreName"></span></small>
        </div>
    </div>
    <% } %>

    <%-- ====== 공통: 내 매장 목록 ====== --%>
    <div class="card border-0 shadow-sm p-4">
        <h5 class="fw-bold mb-3">
            <i class="fa-solid fa-list me-2 text-muted"></i>내 매장 목록
        </h5>
        <% if (myStores.isEmpty()) { %>
            <div class="text-center text-muted py-4">
                <i class="fa-solid fa-store-slash fa-2x mb-2"></i>
                <p class="mb-0 small">등록된 매장이 없습니다.</p>
            </div>
        <% } else { %>
        <div class="list-group list-group-flush border rounded-3 overflow-hidden">
            <% for (String[] store : myStores) {
                String joinStatus  = store.length > 2 ? store[2] : "ACTIVE";
                String storeStatus = store.length > 3 ? store[3] : "ACTIVE";
            %>
            <div class="list-group-item d-flex justify-content-between align-items-center p-3">
                <div>
                    <span class="fw-bold text-dark"><%=store[1]%></span>
                    <small class="text-muted ms-2">(<%=store[0]%>)</small>
                    <% if ("PENDING".equals(joinStatus)) { %>
                        <span class="badge bg-warning text-dark ms-1">소속 승인 대기</span>
                    <% } else if ("PENDING".equals(storeStatus)) { %>
                        <span class="badge bg-info text-dark ms-1">매장 승인 대기</span>
                    <% } else { %>
                        <span class="badge bg-success ms-1">활성</span>
                    <% } %>
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger border-0"
                    onclick="deleteStore('<%=userId%>', '<%=store[0]%>')">
                    <i class="fa-solid fa-trash-can"></i>
                </button>
            </div>
            <% } %>
        </div>
        <% } %>
    </div>
</div>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
let searchTimer = null;

// 매장 검색 (직원용)
function searchStore(keyword) {
    clearTimeout(searchTimer);
    const resultBox = document.getElementById('searchResult');
    if (!keyword || keyword.trim().length < 1) {
        resultBox.style.display = 'none';
        return;
    }
    searchTimer = setTimeout(async () => {
        try {
            const res  = await fetch('SearchStore?keyword=' + encodeURIComponent(keyword.trim()));
            const data = await res.json();
            resultBox.innerHTML = '';
            if (data.status === 'success' && data.list.length > 0) {
                data.list.forEach(store => {
                    const item = document.createElement('button');
                    item.type = 'button';
                    item.className = 'list-group-item list-group-item-action';
                    item.innerHTML = '<span class="fw-bold">' + store.name + '</span> <small class="text-muted">(' + store.id + ')</small>';
                    item.onclick = () => selectStore(store.id, store.name);
                    resultBox.appendChild(item);
                });
                resultBox.style.display = 'block';
            } else {
                resultBox.innerHTML = '<div class="list-group-item text-muted small">검색 결과가 없습니다.</div>';
                resultBox.style.display = 'block';
            }
        } catch (e) { console.error(e); }
    }, 300);
}

// 매장 선택
function selectStore(id, name) {
    document.getElementById('selectedStoreId').value = id;
    document.getElementById('searchKeyword').value   = name + ' (' + id + ')';
    document.getElementById('selectedStoreName').innerText = name + ' (' + id + ')';
    document.getElementById('selectedStoreInfo').style.display = 'block';
    document.getElementById('searchResult').style.display = 'none';
}

// 직원: 매장 소속 신청
async function joinStore() {
    const storeId = document.getElementById('selectedStoreId').value.trim();
    if (!storeId) { alert('매장을 검색하여 선택해주세요.'); return; }
    if (!confirm('해당 매장에 소속 신청하시겠습니까?\n점장 승인 후 소속이 확정됩니다.')) return;

    const params = new URLSearchParams();
    params.append('add_store_id', storeId);

    try {
        const res  = await fetch('MyStoreAdd', { method: 'POST', body: params });
        const data = await res.json();
        alert(data.message);
        if (data.status === 'success') location.reload();
    } catch (e) { alert('서버 통신 오류가 발생했습니다.'); }
}

// 점장: 직원 소속 신청 승인/거절
async function handleJoin(memId, storeId, mode) {
    const action = mode === 'approveJoin' ? '승인' : '거절';
    if (!confirm('"' + memId + '" 직원의 소속 신청을 ' + action + '하시겠습니까?')) return;

    const params = new URLSearchParams();
    params.append('mode', mode);
    params.append('memId', memId);
    params.append('storeId', storeId);

    try {
        const res  = await fetch('StoreManage', { method: 'POST', body: params });
        const data = await res.json();
        alert(data.message);
        if (data.status === 'success') {
            const row = document.getElementById('join-row-' + memId + '-' + storeId);
            if (row) row.remove();
        }
    } catch (e) { alert('서버 통신 오류가 발생했습니다.'); }
}

// 점장: 새 매장 생성 신청
async function requestStore() {
    const storeId   = document.getElementById('storeId').value.trim();
    const storeName = document.getElementById('storeName').value.trim();
    if (!storeId || !storeName) { alert('매장 코드와 이름을 모두 입력해주세요.'); return; }
    if (!confirm('[' + storeId + '] 매장 생성을 신청하시겠습니까?')) return;

    const params = new URLSearchParams();
    params.append('storeId', storeId);
    params.append('storeName', storeName);

    try {
        const res  = await fetch('StoreManage', { method: 'POST', body: params });
        const data = await res.json();
        alert(data.message);
        if (data.status === 'success') location.reload();
    } catch (e) { alert('서버 통신 오류가 발생했습니다.'); }
}

// 매장 삭제
async function deleteStore(userId, storeId) {
    if (!confirm('해당 매장을 목록에서 삭제하시겠습니까?')) return;
    try {
        const res  = await fetch('MyStoreDelete?id=' + userId + '&storeId=' + storeId);
        const data = await res.json();
        alert(data.message);
        if (data.status === 'success') location.reload();
    } catch (e) { alert('서버 통신 오류가 발생했습니다.'); }
}

// 검색창 외부 클릭 시 닫기
document.addEventListener('click', function(e) {
    const resultBox = document.getElementById('searchResult');
    if (resultBox && !resultBox.contains(e.target) && e.target.id !== 'searchKeyword') {
        resultBox.style.display = 'none';
    }
});
</script>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList" %>
<%
    String userId   = (String) session.getAttribute("userId");
    String userRole = (String) session.getAttribute("userRole");

    if (userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    ArrayList<String[]> storeList = (ArrayList<String[]>) request.getAttribute("storeList");
    if (storeList == null) storeList = new ArrayList<>();
    // storeList 구조: [0]=store_id, [1]=store_name, [2]=join_status(PENDING/ACTIVE), [3]=store_status
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

<div class="container mt-5 mb-5" style="max-width:680px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0">
            <i class="fa-solid fa-store me-2 text-primary"></i>매장 관리
        </h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드로
        </a>
    </div>

    <%-- 소속 매장 목록 --%>
    <div class="card border-0 shadow-sm mb-4">
        <div class="card-header bg-white border-0 pt-4 pb-2 px-4">
            <h6 class="fw-bold text-dark mb-0">
                <i class="fa-solid fa-list me-2 text-primary"></i>내 소속 매장
                <span class="badge bg-primary ms-1"><%=storeList.size()%></span>
            </h6>
        </div>
        <div class="card-body px-4 pb-4">
            <% if (storeList.isEmpty()) { %>
                <div class="text-center text-muted py-4">
                    <i class="fa-solid fa-store-slash fa-2x mb-2 d-block"></i>
                    <small>소속된 매장이 없습니다. 아래에서 매장을 검색해 신청해보세요!</small>
                </div>
            <% } else {
                for (String[] s : storeList) {
                    String joinStatus  = s[2]; // PENDING / ACTIVE / REJECTED
                    String storeStatus = s[3]; // PENDING / ACTIVE
                    boolean isActive   = "ACTIVE".equals(joinStatus) && "ACTIVE".equals(storeStatus);
                    boolean isPending  = "PENDING".equals(joinStatus);
                    boolean isRejected = "REJECTED".equals(joinStatus);
            %>
            <div class="d-flex align-items-center justify-content-between py-3 border-bottom">
                <div>
                    <div class="fw-bold text-dark"><%=s[1]%></div>
                    <small class="text-muted"><%=s[0]%></small>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <% if (isActive) { %>
                        <span class="badge bg-success px-3 py-2">
                            <i class="fa-solid fa-circle-check me-1"></i>소속 중
                        </span>
                    <% } else if (isPending) { %>
                        <span class="badge bg-warning text-dark px-3 py-2">
                            <i class="fa-solid fa-clock me-1"></i>승인 대기
                        </span>
                    <% } else if (isRejected) { %>
                        <span class="badge bg-danger px-3 py-2">
                            <i class="fa-solid fa-circle-xmark me-1"></i>거절됨
                        </span>
                    <% } else { %>
                        <span class="badge bg-secondary px-3 py-2">매장 승인 대기</span>
                    <% } %>
                    <% if (!isActive) { %>
                        <button class="btn btn-sm btn-outline-danger fw-bold"
                            onclick="cancelStore('<%=s[0]%>', '<%=s[1]%>')">
                            <i class="fa-solid fa-xmark"></i>
                        </button>
                    <% } %>
                </div>
            </div>
            <% } } %>
        </div>
    </div>

    <%-- 매장 검색 & 신청 --%>
    <div class="card border-0 shadow-sm" style="border-top: 4px solid #4e73df !important;">
        <div class="card-body p-4">
            <h6 class="fw-bold mb-3">
                <i class="fa-solid fa-magnifying-glass me-2 text-primary"></i>매장 검색 후 소속 신청
            </h6>

            <div class="input-group mb-3">
                <input type="text" id="searchKeyword" class="form-control"
                    placeholder="매장 이름 또는 코드 입력"
                    oninput="searchStore(this.value)">
                <button class="btn btn-primary fw-bold" type="button"
                    onclick="searchStore(document.getElementById('searchKeyword').value)">
                    <i class="fa-solid fa-magnifying-glass"></i>
                </button>
            </div>

            <div id="searchResult">
                <div class="text-center text-muted py-4 small">
                    <i class="fa-solid fa-magnifying-glass fa-2x mb-2 d-block"></i>
                    매장 이름이나 코드를 입력해 검색하세요.
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
var searchTimer = null;

function searchStore(keyword) {
    clearTimeout(searchTimer);
    var resultBox = document.getElementById('searchResult');

    if (!keyword || keyword.trim().length < 1) {
        resultBox.innerHTML = '<div class="text-center text-muted py-4 small"><i class="fa-solid fa-magnifying-glass fa-2x mb-2 d-block"></i>매장 이름이나 코드를 입력해 검색하세요.</div>';
        return;
    }

    searchTimer = setTimeout(async function() {
        try {
            var res  = await fetch('SearchStore?keyword=' + encodeURIComponent(keyword.trim()));
            var data = await res.json();
            resultBox.innerHTML = '';

            if (data.status === 'success' && data.list.length > 0) {
                var table = document.createElement('table');
                table.className = 'table table-hover align-middle mb-0';
                table.innerHTML = '<thead class="table-light"><tr><th>매장 이름</th><th>매장 코드</th><th></th></tr></thead>';
                var tbody = document.createElement('tbody');

                data.list.forEach(function(store) {
                    var tr = document.createElement('tr');
                    tr.innerHTML =
                        '<td class="fw-bold">' + store.name + '</td>' +
                        '<td class="text-muted">' + store.id + '</td>' +
                        '<td class="text-end">' +
                            '<button type="button" class="btn btn-sm btn-primary fw-bold" ' +
                            'onclick="requestJoin(\'' + store.id + '\', \'' + store.name + '\')">소속 신청</button>' +
                        '</td>';
                    tbody.appendChild(tr);
                });

                table.appendChild(tbody);
                resultBox.appendChild(table);
            } else {
                resultBox.innerHTML = '<div class="text-center text-muted py-4 small"><i class="fa-solid fa-circle-xmark fa-2x mb-2 d-block text-danger"></i>검색 결과가 없습니다.</div>';
            }
        } catch (e) {
            resultBox.innerHTML = '<div class="text-center text-muted py-3 small">검색 중 오류가 발생했습니다.</div>';
        }
    }, 300);
}

async function requestJoin(storeId, storeName) {
    if (!confirm('[' + storeName + '] 매장에 소속 신청하시겠습니까?\n점장 승인 후 활성화됩니다.')) return;

    var params = new URLSearchParams();
    params.append('add_store_id', storeId);

    try {
        var res  = await fetch('MyStoreAdd', { method: 'POST', body: params });
        var data = await res.json();
        alert(data.message);
        if (data.status === 'success') location.reload();
    } catch (e) {
        alert('통신 오류가 발생했습니다.');
    }
}

async function cancelStore(storeId, storeName) {
    if (!confirm('[' + storeName + '] 소속 신청을 취소하시겠습니까?')) return;

    var params = new URLSearchParams();
    params.append('storeId', storeId);

    try {
        var res  = await fetch('MyStoreDelete', { method: 'POST', body: params });
        var data = await res.json();
        alert(data.message);
        if (data.status === 'success') location.reload();
    } catch (e) {
        alert('통신 오류가 발생했습니다.');
    }
}
</script>
</body>
</html>

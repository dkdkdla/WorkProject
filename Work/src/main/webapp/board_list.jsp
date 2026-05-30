<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 1. 세션 및 권한 체크
    String userId = (String)session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // 2. 매장 ID 처리 (파라미터가 없으면 세션에서 찾고, 그래도 없으면 기본값 store1)
    String storeId = request.getParameter("storeId");
    if (storeId == null || storeId.trim().isEmpty()) {
        storeId = (String)session.getAttribute("userStoreId");
        if (storeId == null) storeId = ""; 
    }
%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>소통 게시판 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container my-5">
    
    <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
        <div>
            <h3 class="fw-bold text-dark mb-1">
                <i class="fa-solid fa-comments text-primary me-2"></i>소통 게시판
            </h3>
            <p class="text-muted small mb-0">매장 내 공지사항 및 건의사항을 자유롭게 나누세요.</p>
        </div>
        
        <a href="board_write.jsp?storeId=<%=storeId%>" class="btn btn-primary px-4 fw-bold shadow-sm">
            <i class="fa-solid fa-pen-to-square me-1"></i> 새 글 쓰기
        </a>
    </div>

    <div class="card custom-card border-0 shadow-sm overflow-hidden">
        <div class="table-responsive">
            <table class="table custom-table mb-0 text-center table-hover">
                <colgroup>
                    <col width="10%">
                    <col width="55%">
                    <col width="15%">
                    <col width="20%">
                </colgroup>
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>제목</th>
                        <th>작성자</th>
                        <th>작성일</th>
                    </tr>
                </thead>
                <tbody id="boardListBody">
                    <tr>
                        <td colspan="4" class="py-5 text-center text-muted">
                            <div class="spinner-border text-primary mb-3" role="status"></div>
                            <p>게시글을 불러오는 중입니다...</p>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="text-center mt-5">
        <a href="default.jsp" class="btn btn-light px-4 py-2 fw-bold border shadow-sm">
            <i class="fa-solid fa-house me-1"></i> 메인화면
        </a>
    </div>

    <%-- 페이지네이션 --%>
    <div id="pagination" class="d-flex justify-content-center mt-4 gap-1"></div>
</div>

<%@ include file="footer.jsp" %>

<script>
const PAGE_SIZE = 10;
let allData   = [];
let curPage   = 1;

document.addEventListener('DOMContentLoaded', function() {
    loadBoardList();
});

async function loadBoardList() {
    const storeId = '<%=storeId%>';
    const tbody   = document.getElementById('boardListBody');

    try {
        const response = await fetch("BoardList?storeId=" + storeId + "&_nocache=" + new Date().getTime());
        const result   = await response.json();

        if (result.status === 'success') {
            allData = result.data;
            renderPage(1);
        } else {
            tbody.innerHTML = "<tr><td colspan='4' class='py-5 text-center text-danger'>데이터를 불러오지 못했습니다.<br>" + result.message + "</td></tr>";
        }
    } catch (error) {
        console.error('Fetch Error:', error);
        tbody.innerHTML = "<tr><td colspan='4' class='py-5 text-center text-danger'>서버 통신 중 오류가 발생했습니다.</td></tr>";
    }
}

function renderPage(page) {
    curPage = page;
    const tbody      = document.getElementById('boardListBody');
    const totalCount = allData.length;
    const totalPages = Math.ceil(totalCount / PAGE_SIZE);

    const start = (page - 1) * PAGE_SIZE;
    const end   = Math.min(start + PAGE_SIZE, totalCount);
    const pageData = allData.slice(start, end);

    if (totalCount === 0) {
        tbody.innerHTML =
            '<tr>' +
                '<td colspan="4" class="py-5 text-center">' +
                    '<i class="fa-solid fa-folder-open fa-3x text-light mb-3"></i>' +
                    '<p class="text-muted mb-0">작성된 게시글이 없습니다.<br>첫 번째 소통의 주인공이 되어보세요!</p>' +
                '</td>' +
            '</tr>';
        document.getElementById('pagination').innerHTML = '';
        return;
    }

    // 목록 렌더링 (전체 기준 순번)
    let html = '';
    for (let i = 0; i < pageData.length; i++) {
        let item   = pageData[i];
        let rowNum = totalCount - (start + i); // 전체 기준 순번
        html +=
            '<tr onclick="location.href=\'board_view.jsp?id=' + item.id + '\'" style="cursor:pointer;">' +
                '<td class="text-secondary">' + rowNum + '</td>' +
                '<td class="text-start ps-4 fw-bold text-dark">' + item.title + '</td>' +
                '<td>' +
                    '<span class="badge bg-light text-dark border fw-normal">' + item.writer + '</span>' +
                '</td>' +
                '<td class="small text-muted">' + item.date + '</td>' +
            '</tr>';
    }
    tbody.innerHTML = html;

    // 페이지네이션 버튼 렌더링
    let pagHtml = '';

    // 이전 버튼
    pagHtml += '<button class="btn btn-sm btn-outline-secondary px-3" ' +
        (page <= 1 ? 'disabled' : 'onclick="renderPage(' + (page - 1) + ')"') + '>' +
        '<i class="fa-solid fa-chevron-left"></i></button>';

    // 페이지 번호 (최대 5개 표시)
    const startPage = Math.max(1, page - 2);
    const endPage   = Math.min(totalPages, startPage + 4);

    for (let p = startPage; p <= endPage; p++) {
        if (p === page) {
            pagHtml += '<button class="btn btn-sm btn-primary px-3 fw-bold">' + p + '</button>';
        } else {
            pagHtml += '<button class="btn btn-sm btn-outline-secondary px-3" onclick="renderPage(' + p + ')">' + p + '</button>';
        }
    }

    // 다음 버튼
    pagHtml += '<button class="btn btn-sm btn-outline-secondary px-3" ' +
        (page >= totalPages ? 'disabled' : 'onclick="renderPage(' + (page + 1) + ')"') + '>' +
        '<i class="fa-solid fa-chevron-right"></i></button>';

    document.getElementById('pagination').innerHTML = pagHtml;
}
</script>

</body>
</html>
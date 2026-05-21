<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 파라미터 체크 및 현재 세션 정보 가져오기
    String id = request.getParameter("id");
    if (id == null) {
        response.sendRedirect("board_list.jsp");
        return;
    }
    
    String currentUserId = (String)session.getAttribute("userId");
    
    // 세션에서 사용자의 소속 매장 ID 가져오기 (세션 만료 대비 파라미터 백업)
    String userStoreId = (String)session.getAttribute("userStoreId");
    if (userStoreId == null) userStoreId = request.getParameter("storeId"); 
%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>게시글 보기 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container my-5" style="max-width: 900px;">
    
    <div class="d-flex justify-content-between align-items-center mb-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="default.jsp" class="text-primary">홈</a></li>
                <li class="breadcrumb-item"><a href="board_list.jsp?storeId=<%=userStoreId%>" class="text-primary">소통 게시판</a></li>
                <li class="breadcrumb-item active">상세 보기</li>
            </ol>
        </nav>
        <a href="board_list.jsp?storeId=<%=userStoreId%>" class="btn btn-outline-secondary btn-sm px-3 fw-bold">
            <i class="fa-solid fa-list me-1"></i> 목록으로
        </a>
    </div>

    <div id="boardDetailContainer" class="card custom-card border-0 shadow-sm overflow-hidden">
        <div class="p-5 text-center text-muted">
            <div class="spinner-border text-primary mb-3" role="status"></div>
            <p>게시글을 불러오는 중입니다...</p>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>

<script>
document.addEventListener('DOMContentLoaded', function() {
    loadBoardDetail();
});

async function loadBoardDetail() {
    const urlParams = new URLSearchParams(window.location.search);
    const postId = urlParams.get('id');

    if (!postId) {
        alert("게시글 번호를 찾을 수 없습니다.");
        location.href = 'board_list.jsp';
        return;
    }

    const currentUserId = '<%=currentUserId != null ? currentUserId : ""%>';
    const userStoreId = '<%=userStoreId != null ? userStoreId : ""%>'; 
    const container = document.getElementById('boardDetailContainer');

    try {
        // 🚨 핵심 설계 1: API 요청 주소를 자바 파일(@WebServlet("/BoardView"))로 변경
        // 🚨 핵심 설계 2: 브라우저 캐시를 무시하기 위해 무작위 시간값(_nocache)을 지속 전달
        const response = await fetch("BoardView?id=" + postId + "&_nocache=" + new Date().getTime());
        const result = await response.json();

        if (result.status === 'success') {
            const data = result.data;
            
            // 🚨 핵심 설계 3: 톰캣 EL 태그 충돌 에러 방지를 위해 백틱(`)을 제거하고 순수 문자열 결합(+) 사용
            let htmlTemplate = 
                '<div class="card-header bg-white border-bottom-0 p-4 p-md-5 pb-0">' +
                    '<h2 class="fw-bold text-dark mb-3">' + data.title + '</h2>' +
                    '<div class="d-flex justify-content-between align-items-center text-muted small border-bottom pb-4">' +
                        '<div class="d-flex gap-3">' +
                            '<span><i class="fa-solid fa-user-circle me-1"></i>작성자: <strong>' + data.writer + '</strong></span>' +
                            '<span><i class="fa-regular fa-clock me-1"></i>' + data.date + '</span>' +
                        '</div>' +
                    '</div>' +
                '</div>' +
                '<div class="card-body p-4 p-md-5 py-4">' +
                    '<div class="post-content mb-5" style="min-height: 250px; line-height: 1.8; white-space: pre-wrap; font-size: 1.05rem;">' +
                        data.content +
                    '</div>';

            if (data.fileName && data.fileName.trim() !== "") {
                htmlTemplate += 
                    '<div class="bg-light rounded-4 p-4 border mt-5">' +
                        '<h6 class="fw-bold text-dark mb-3"><i class="fa-solid fa-paperclip me-2 text-primary"></i>첨부파일</h6>' +
                        '<div class="d-flex align-items-center flex-wrap gap-3">' +
                            '<a href="upload/' + data.fileName + '" download="' + data.orgName + '" class="btn btn-success fw-bold shadow-sm px-4 py-2 rounded-pill">' +
                                '<i class="fa-solid fa-file-arrow-down me-2"></i>' + data.orgName + ' 내려받기' +
                            '</a>' +
                        '</div>';

                const ext = data.fileName.split('.').pop().toLowerCase();
                if (['jpg', 'jpeg', 'png', 'gif'].includes(ext)) {
                    htmlTemplate += 
                        '<div class="mt-4 pt-4 border-top">' +
                            '<p class="small text-muted mb-3"><i class="fa-regular fa-image me-1"></i>이미지 미리보기</p>' +
                            '<img src="upload/' + data.fileName + '" class="img-fluid rounded-3 shadow-sm border" alt="첨부 이미지" style="max-height: 600px;">' +
                        '</div>';
                }
                htmlTemplate += '</div>'; 
            }
            htmlTemplate += '</div>'; 

            htmlTemplate += 
                '<div class="card-footer bg-light border-top-0 p-4 text-center d-flex justify-content-center gap-2">';
            
            if (currentUserId === data.writer) {
                htmlTemplate += 
                    '<a href="BoardEdit?id=' + postId + '&storeId=' + userStoreId + '" class="btn btn-outline-primary px-4 fw-bold">수정하기</a>' +
                    '<a href="BoardDelete?id=' + postId + '&storeId=' + userStoreId + '" class="btn btn-outline-danger px-4 fw-bold" onclick="return confirm(\'정말로 삭제하시겠습니까?\');">삭제하기</a>';
            }
            
            htmlTemplate += 
                    '<a href="board_list.jsp?storeId=' + userStoreId + '" class="btn btn-secondary px-4 fw-bold">목록</a>' +
                '</div>';

            container.innerHTML = htmlTemplate;

        } else {
            container.innerHTML = "<div class='p-5 text-center'><h3>글을 찾을 수 없습니다.</h3><p class='text-danger mt-3'>에러: " + result.message + "</p><a href='board_list.jsp?storeId=" + userStoreId + "' class='btn btn-primary mt-4'>목록으로</a></div>";
        }
    } catch (error) {
        container.innerHTML = "<div class='p-5 text-center text-danger'><h4>서버 통신 중 오류가 발생했습니다.</h4><p>" + error.message + "</p></div>";
    }
}
</script>

</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String userId   = (String) session.getAttribute("userId");
    String postId   = (String) request.getAttribute("postId");
    String title    = (String) request.getAttribute("title");
    String content  = (String) request.getAttribute("content");
    String storeId  = (String) request.getAttribute("storeId");
    String fileName = (String) request.getAttribute("fileName");
    String orgName  = (String) request.getAttribute("orgName");

    if (userId == null || postId == null) {
        response.sendRedirect("board_list.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>게시글 수정 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container my-5" style="max-width: 700px;">

    <div class="text-center mb-5">
        <h3 class="fw-bold text-dark mb-1">
            <i class="fa-solid fa-pen-to-square text-primary me-2"></i>게시글 수정
        </h3>
        <p class="text-muted small">내용을 수정하고 저장하세요.</p>
    </div>

    <div class="card custom-card border-0 shadow-sm p-4 p-md-5">
        <form id="editForm">
            <input type="hidden" name="postId"  value="<%=postId%>">
            <input type="hidden" name="storeId" value="<%=storeId%>">

            <div class="mb-4">
                <label class="form-label fw-bold text-secondary small">제목</label>
                <input type="text" name="title" class="form-control form-control-lg border-2 shadow-none"
                       value="<%=title != null ? title : ""%>" required
                       style="border-radius: 12px; font-size: 1.1rem;">
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold text-secondary small">내용</label>
                <textarea name="content" class="form-control border-2 shadow-none" required
                          style="height: 300px; border-radius: 12px; resize: none;"><%=content != null ? content : ""%></textarea>
            </div>

            <% if (fileName != null && !fileName.isEmpty()) { %>
            <div class="mb-4 p-3 bg-light rounded-3 border">
                <p class="small text-muted mb-1"><i class="fa-solid fa-paperclip me-1"></i>현재 첨부파일</p>
                <span class="fw-bold text-dark"><%=orgName != null ? orgName : fileName%></span>
                <small class="text-muted ms-2">(파일 변경은 삭제 후 새 글 작성)</small>
            </div>
            <% } %>

            <div class="d-flex gap-2">
                <button type="button" onclick="history.back()" class="btn btn-light flex-fill py-3 fw-bold border">
                    <i class="fa-solid fa-xmark me-1"></i> 취소
                </button>
                <button type="submit" class="btn btn-primary flex-fill py-3 fw-bold shadow-sm" id="submitBtn">
                    <i class="fa-solid fa-check me-1"></i> 수정 완료
                </button>
            </div>
        </form>
    </div>
</div>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
document.getElementById('editForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const submitBtn = document.getElementById('submitBtn');
    const originalHtml = submitBtn.innerHTML;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>저장 중...';
    submitBtn.disabled = true;

    const params = new URLSearchParams(new FormData(this));

    try {
        const res  = await fetch('BoardEdit', { method: 'POST', body: params });
        const data = await res.json();

        if (data.status === 'success') {
            showToast(data.message, data.status === 'success' ? 'success' : 'danger');
            location.href = 'board_view.jsp?id=<%=postId%>&storeId=<%=storeId%>';
        } else {
            showToast(data.message, data.status === 'success' ? 'success' : 'danger');
        }
    } catch (e) {
        showToast('서버 통신 오류가 발생했습니다.', 'warning');
    } finally {
        submitBtn.innerHTML = originalHtml;
        submitBtn.disabled  = false;
    }
});
</script>
</body>
</html>

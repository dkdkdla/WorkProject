<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 1. 보안 체크 (세션 확인 로직만 남깁니다)
    String userId = (String)session.getAttribute("userId");
    String userStoreId = (String)session.getAttribute("userStoreId");

    if(userId == null) {
        out.println("<script>alert('로그인이 필요합니다.'); location.href='login.jsp';</script>");
        return;
    }
%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>글 쓰기 - AlbaPass</title>
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
            <i class="fa-solid fa-pen-nib text-primary me-2"></i>게시글 작성
        </h3>
        <p class="text-muted small">매장 동료들과 공유할 소중한 의견을 남겨주세요.</p>
    </div>

    <div class="card custom-card border-0 shadow-sm p-4 p-md-5">
        <form id="writeForm">
            
            <div class="mb-4">
                <label class="form-label fw-bold text-secondary small">제목</label>
                <input type="text" name="title" class="form-control form-control-lg border-2 shadow-none" 
                       placeholder="게시글 제목을 입력하세요" required 
                       style="border-radius: 12px; font-size: 1.1rem;">
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold text-secondary small">내용</label>
                <textarea name="content" class="form-control border-2 shadow-none" 
                          placeholder="매장 동료들과 나누고 싶은 내용을 입력해 주세요." required 
                          style="height: 300px; border-radius: 12px; resize: none;"></textarea>
            </div>

            <div class="mb-5">
                <label class="form-label fw-bold text-secondary small">파일 첨부 (이미지, PDF, ZIP 등)</label>
                <div class="input-group">
                    <input type="file" name="file" class="form-control border-2" id="inputFile" style="border-radius: 12px;">
                </div>
                <p class="text-muted mt-2 small">
                    <i class="fa-solid fa-circle-info me-1"></i> 최대 100MB까지 업로드 가능합니다.
                </p>
            </div>

            <div class="d-flex gap-2">
                <button type="button" onclick="history.back()" class="btn btn-light flex-fill py-3 fw-bold border">
                    <i class="fa-solid fa-xmark me-1"></i> 취소
                </button>
                <button type="submit" class="btn btn-primary flex-fill py-3 fw-bold shadow-sm" id="submitBtn">
                    <i class="fa-solid fa-check me-1"></i> 등록 완료
                </button>
            </div>
            
        </form>
    </div>
</div>

<%@ include file="footer.jsp" %>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const writeForm = document.getElementById('writeForm');
    const submitBtn = document.getElementById('submitBtn');

    writeForm.addEventListener('submit', async function(e) {
        // 브라우저의 기본 제출(새로고침) 기능을 막습니다.
        e.preventDefault(); 

        // 🚨 다중 클릭 방지 UI: 버튼을 스피너로 바꾸고 클릭을 막습니다.
        const originalBtnHtml = submitBtn.innerHTML;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>등록 중...';
        submitBtn.disabled = true;

        try {
            // 🚨 핵심 수정 2: FormData를 사용하여 텍스트와 첨부파일을 한 덩어리로 묶습니다.
            const formData = new FormData(writeForm);

            // 세션에 있는 아이디와 매장 정보를 백엔드(BoardWrite)로 같이 넘겨줍니다.
            formData.append('writerId', '<%=userId%>');
            formData.append('storeId', '<%=userStoreId%>');

            // 🚨 핵심 수정 3: 새로 만든 자바 서블릿으로 통신을 쏩니다.
            const response = await fetch('BoardWrite', {
                method: 'POST',
                body: formData
            });

            const result = await response.json();

            if (result.status === 'success') {
                alert('게시글이 성공적으로 등록되었습니다.');
                location.href = 'board_list.jsp?storeId=<%=userStoreId%>';
            } else {
                alert('등록 실패: ' + result.message);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        } finally {
            // 통신이 끝나면 버튼 상태를 원래대로 복구합니다.
            submitBtn.innerHTML = originalBtnHtml;
            submitBtn.disabled = false;
        }
    });
});
</script>

</body>
</html>
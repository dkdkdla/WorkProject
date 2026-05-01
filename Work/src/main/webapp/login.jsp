<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>로그인 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/login.css">
</head>
<body>

<jsp:include page="navbar.jsp" />

<main class="login-main">
    <div class="container">
        <div class="card login-card p-4 p-md-5">
            
            <div class="text-center mb-4">
                <a href="<%=request.getContextPath()%>/default.jsp" class="d-inline-block overflow-hidden mb-2" 
                   style="width: 240px; height: 100px; border-radius: 12px; background-color: #fff;">
                    <img src="<%=request.getContextPath()%>/logo.png" alt="AlbaPass 로고" 
                         style="width: 100%; height: 100%; object-fit: contain; transform: scale(1); transform-origin: center;">
                </a>
                <h3 class="fw-bold text-dark mt-2">로그인</h3>
            </div>

            <form id="loginForm">
                <div class="mb-3">
                    <div class="input-group input-group-lg border-bottom">
                        <span class="input-group-text"><i class="fa-solid fa-user text-muted"></i></span>
                        <input type="text" name="id" class="form-control" placeholder="아이디를 입력하세요" required>
                    </div>
                </div>

                <div class="mb-4">
                    <div class="input-group input-group-lg border-bottom">
                        <span class="input-group-text"><i class="fa-solid fa-lock text-muted"></i></span>
                        <input type="password" name="pw" id="loginPw" class="form-control" placeholder="비밀번호를 입력하세요" required>
                        <button class="btn border-0 bg-transparent shadow-none" type="button" onclick="togglePasswordVisibility()">
                            <i class="fa-solid fa-eye text-muted" id="pwIcon"></i>
                        </button>
                    </div>
                </div>

                <button type="submit" id="loginBtn" class="btn btn-primary w-100 py-3 fw-bold shadow-sm mb-4" style="border-radius: 8px;">
                    로그인
                </button>

                <div class="find-links">
                    <a href="find_id.jsp">아이디 찾기</a>
                    <span class="divider"></span>
                    <a href="find_pw.jsp">비밀번호 찾기</a>
                    <span class="divider"></span>
                    <a href="register.jsp" class="fw-bold text-primary">회원가입</a>
                    <span class="divider"></span>
                    <a href="default.jsp" class="back-home"><i class="fa-solid fa-house-user me-1"></i>메인</a>
                </div>
            </form>
            
        </div>
    </div>
</main>

<jsp:include page="footer.jsp" />

<script>
function togglePasswordVisibility() {
    const pwInput = document.getElementById("loginPw");
    const icon = document.getElementById("pwIcon");
    if (pwInput.type === "password") {
        pwInput.type = "text";
        icon.classList.replace("fa-eye", "fa-eye-slash");
    } else {
        pwInput.type = "password";
        icon.classList.replace("fa-eye-slash", "fa-eye");
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault(); 
        const originalBtnText = loginBtn.innerText;
        loginBtn.disabled = true;
        loginBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>로그인 중...';

        try {
            const formData = new FormData(loginForm);
            const params = new URLSearchParams(formData);

            const response = await fetch('Login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });

            const result = await response.json();

            if (result.status === 'success') {
                location.href = 'default.jsp';
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error('Login Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        } finally {
            loginBtn.disabled = false;
            loginBtn.innerText = originalBtnText;
        }
    });
});
</script>

</body>
</html>
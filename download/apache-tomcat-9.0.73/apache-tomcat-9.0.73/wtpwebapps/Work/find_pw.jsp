<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>비밀번호 찾기 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/login.css">

    <style>
        .result-box { display: none; }
        .temp-pw-box {
            background-color: #fff5f5;
            border: 1px dashed #e74a3b;
            border-radius: 12px;
            padding: 2rem;
            margin: 1.5rem 0;
        }
        .alert-warning-custom { 
            background-color: #f8f9fc; 
            color: #4e73df; 
            border: none; 
            font-size: 0.85rem;
            border-radius: 10px;
        }
    </style>
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
                <h4 class="fw-bold text-dark mt-2">비밀번호 찾기</h4>
                <div class="alert alert-warning-custom py-2 mt-2">
                    <i class="fa-solid fa-circle-info me-1"></i> 정보 일치 시 임시 비밀번호가 발급됩니다.
                </div>
            </div>

            <form id="findPwForm">
                <input type="hidden" name="mode" value="findPw">
                
                <div class="mb-4">
                    <label class="form-label fw-bold text-primary small">아이디</label>
                    <div class="input-group input-group-lg border-bottom">
                        <span class="input-group-text bg-transparent border-0"><i class="fa-solid fa-user text-muted"></i></span>
                        <input type="text" name="id" class="form-control border-0 bg-transparent shadow-none" placeholder="아이디 입력" required>
                    </div>
                </div>

                <div class="mb-5">
                    <label class="form-label fw-bold text-primary small">전화번호</label>
                    <div class="input-group input-group-lg border-bottom">
                        <span class="input-group-text bg-transparent border-0"><i class="fa-solid fa-phone text-muted"></i></span>
                        <input type="text" name="phone" class="form-control border-0 bg-transparent shadow-none" placeholder="010-0000-0000" required>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary w-100 py-3 fw-bold shadow-sm mb-3" style="border-radius: 8px;">
                    비밀번호 재발급
                </button>
                <a href="login.jsp" class="btn btn-link w-100 text-muted text-decoration-none small">로그인 페이지로 돌아가기</a>
            </form>

            <div id="resultArea" class="result-box text-center">
                <div class="temp-pw-box">
                    <small class="text-muted d-block mb-2">임시 비밀번호가 발급되었습니다.</small>
                    <strong id="tempPw" class="h4 text-danger d-block mb-2"></strong>
                    <p class="text-muted small mb-0">로그인 후 반드시 비밀번호를 변경하세요.</p>
                </div>
                <div class="d-grid gap-2">
                    <a href="login.jsp" class="btn btn-primary py-3 fw-bold shadow-sm" style="border-radius: 8px;">로그인하러 가기</a>
                    <a href="<%=request.getContextPath()%>/default.jsp" class="btn btn-outline-secondary py-2 small border-0">메인으로 이동</a>
                </div>
            </div>
            
        </div>
    </div>
</main>

<jsp:include page="footer.jsp" />

<script>
    document.getElementById('findPwForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new URLSearchParams(new FormData(this));
        
        // 로딩 상태 표시
        const btn = e.target.querySelector('button[type="submit"]');
        const originalText = btn.innerText;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>조회 중...';

        fetch('FindAccount', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            const resultArea = document.getElementById('resultArea');
            const form = document.getElementById('findPwForm');
            
            if (data.status === 'success') {
                document.getElementById('tempPw').innerText = data.tempPassword;
                resultArea.style.display = 'block';
                form.style.display = 'none'; 
            } else {
                alert(data.message || '정보가 일치하지 않습니다.');
                btn.disabled = false;
                btn.innerText = originalText;
            }
        })
        .catch(err => {
            alert('서버 오류 발생');
            btn.disabled = false;
            btn.innerText = originalText;
        });
    });
</script>

</body>
</html>
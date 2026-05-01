<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 1. 보안 체크: 로그인 여부 및 매장 정보 가져오기
    String userId = (String)session.getAttribute("userId");
    String userStoreId = (String)session.getAttribute("userStoreId"); // 🚨 세션에서 매장 ID 추출

    if (userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>QR 출퇴근 시뮬레이션 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
	<link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <style>
        body { background-color: #f8f9fc; font-family: 'Pretendard', sans-serif; }
        .qr-container { max-width: 500px; margin: 80px auto; }
        .scanner-box { 
            border: 2px dashed #4e73df; 
            border-radius: 20px; 
            background-color: #fff;
            transition: all 0.3s ease;
        }
        .qr-input {
            font-size: 2rem;
            font-weight: 800;
            letter-spacing: 5px;
            text-align: center;
            border: none;
            border-bottom: 3px solid #eaecf4;
            border-radius: 0;
            color: #4e73df;
            text-transform: uppercase; /* 소문자로 입력해도 대문자로 보이게 설정 */
        }
        .qr-input:focus {
            box-shadow: none;
            border-bottom-color: #4e73df;
        }
    </style>
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container qr-container">
    <div class="card border-0 shadow-sm p-4 text-center">
        <div class="mb-4">
            <div class="icon-box bg-primary-subtle text-primary mx-auto mb-3" style="width: 70px; height: 70px; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                <i class="fa-solid fa-camera fa-2x"></i>
            </div>
            <h4 class="fw-bold text-dark">QR 출퇴근 인증</h4>
            <p class="text-muted small">현재 접속한 매장 코드가 자동으로 입력되어 있습니다.</p>
        </div>

        <div class="scanner-box p-5 mb-4">
            <label class="form-label small fw-bold text-secondary mb-3">STORE CODE</label>
            <input type="text" id="qrInput" 
                   value="<%= (userStoreId != null) ? userStoreId : "" %>" 
                   class="form-control qr-input mb-4" placeholder="CODE">
            
            <div class="d-grid gap-2">
                <button onclick="processQr('IN')" class="btn btn-primary py-3 fw-bold">
                    <i class="fa-solid fa-right-to-bracket me-2"></i>출근 처리하기
                </button>
                <button onclick="processQr('OUT')" class="btn btn-danger py-3 fw-bold">
                    <i class="fa-solid fa-right-from-bracket me-2"></i>퇴근 처리하기
                </button>
            </div>
        </div>

        <div class="alert alert-light border-0 py-2 small text-muted">
            <i class="fa-solid fa-circle-info me-1"></i> 매장 코드가 다를 경우 직접 수정하여 인증할 수 있습니다.
        </div>
        
        <a href="default.jsp" class="btn btn-link text-decoration-none text-muted mt-3">
            <i class="fa-solid fa-house me-1"></i> 메인으로 돌아가기
        </a>
    </div>
</div>

<script>
    async function processQr(type) {
        const storeId = document.getElementById("qrInput").value.trim();
        
        if(!storeId) {
            alert("매장 코드를 입력해주세요.");
            return;
        }

        const params = new URLSearchParams();
        params.append('storeId', storeId);
        params.append('type', type);

        try {
            const response = await fetch('QrCheck', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });
            
            const result = await response.json();

            if(result.status === 'success') {
                alert(result.message);
                location.href = 'default.jsp';
            } else {
                alert("인증 실패: " + result.message);
            }
        } catch (error) {
            alert("서버 통신 중 오류가 발생했습니다.");
            console.error(error);
        }
    }
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
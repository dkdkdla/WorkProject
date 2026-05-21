<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>404 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <style>
        body {
            font-family: 'Pretendard', sans-serif;
            background-color: #f8f9fc;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
            justify-content: center;
            align-items: center;
        }
        .error-code {
            font-size: 8rem;
            font-weight: 800;
            color: #e3e6f0;
            line-height: 1;
        }
        .error-icon {
            font-size: 4rem;
            color: #4e73df;
        }
    </style>
</head>
<body>
    <div class="text-center px-4">
        <div class="error-code">404</div>
        <div class="error-icon my-3">
            <i class="fa-solid fa-map-location-dot"></i>
        </div>
        <h3 class="fw-bold text-dark mb-2">페이지를 찾을 수 없습니다</h3>
        <p class="text-muted mb-4">요청하신 페이지가 존재하지 않거나 이동되었습니다.</p>
        <div class="d-flex justify-content-center gap-2">
            <a href="<%=request.getContextPath()%>/default.jsp" class="btn btn-primary fw-bold px-4">
                <i class="fa-solid fa-house me-1"></i> 메인으로
            </a>
            <button onclick="history.back()" class="btn btn-outline-secondary fw-bold px-4">
                <i class="fa-solid fa-arrow-left me-1"></i> 이전 페이지
            </button>
        </div>
        <p class="text-muted small mt-5">AlbaPass &copy; 2026</p>
    </div>
</body>
</html>

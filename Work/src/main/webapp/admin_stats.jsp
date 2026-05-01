<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>매장 통계 Dashboard - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container mt-5 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0">
            <i class="fa-solid fa-chart-pie me-2 text-primary"></i>매장 통계 리포트
        </h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드
        </a>
    </div>

    <div class="card custom-card p-4 mb-4 border-top border-4 border-primary shadow-sm">
        <form action="AdminStats" method="get" class="row g-3 align-items-end">
            <div class="col-md-4">
                <label class="form-label small fw-bold text-muted">시작 날짜</label>
                <input type="date" name="startDate" value="${startDate}" class="form-control">
            </div>
            <div class="col-md-4">
                <label class="form-label small fw-bold text-muted">종료 날짜</label>
                <input type="date" name="endDate" value="${endDate}" class="form-control">
            </div>
            <div class="col-md-4">
                <button type="submit" class="btn btn-primary w-100 fw-bold shadow-sm">
                    <i class="fa-solid fa-magnifying-glass me-2"></i>통계 데이터 조회
                </button>
            </div>
        </form>
    </div>

    <div class="row g-4 mb-4">
        <div class="col-md-4">
            <div class="card custom-card p-4 border-start border-4 border-info h-100 shadow-sm">
                <div class="d-flex align-items-center">
                    <div class="icon-box bg-info-subtle text-info me-3"><i class="fa-solid fa-users fa-lg"></i></div>
                    <div>
                        <div class="small fw-bold text-muted text-uppercase">총 직원 수</div>
                        <div class="h4 fw-bold mb-0">${totalEmployees}명</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card custom-card p-4 border-start border-4 border-success h-100 shadow-sm">
                <div class="d-flex align-items-center">
                    <div class="icon-box bg-success-subtle text-success me-3"><i class="fa-solid fa-user-check fa-lg"></i></div>
                    <div>
                        <div class="small fw-bold text-muted text-uppercase">오늘 실시간 출근</div>
                        <div class="h4 fw-bold mb-0 text-success">${todayAttendance}명</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card custom-card p-4 border-start border-4 border-primary h-100 shadow-sm">
                <div class="d-flex align-items-center">
                    <div class="icon-box bg-primary-subtle text-primary me-3"><i class="fa-solid fa-check-double fa-lg"></i></div>
                    <div>
                        <div class="small fw-bold text-muted text-uppercase">조회 기간 누적 출근</div>
                        <div class="h4 fw-bold mb-0 text-primary">${periodTotal}회</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card custom-card p-4 shadow-sm border-0">
        <div class="d-flex align-items-center mb-4">
            <h5 class="fw-bold mb-0">
                <i class="fa-solid fa-chart-line me-2 text-primary"></i>출근 현황 시각화
            </h5>
            <span class="badge bg-light text-dark ms-auto">조회 기간: ${startDate} ~ ${endDate}</span>
        </div>
        <div style="position: relative; height: 400px; width: 100%;">
            <canvas id="myChart"></canvas>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>

<script>
    // 서블릿에서 보낸 데이터를 자바스크립트 배열로 변환
    const labels = ${chartLabels};
    const dataValues = ${chartData};

    const ctx = document.getElementById('myChart').getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(78, 115, 223, 0.4)');
    gradient.addColorStop(1, 'rgba(78, 115, 223, 0.02)');

    new Chart(ctx, {
        type: 'line', 
        data: {
            labels: labels,
            datasets: [{
                label: '출근 인원(명)',
                data: dataValues,
                borderColor: '#4e73df',
                backgroundColor: gradient,
                borderWidth: 4,
                pointBackgroundColor: '#fff',
                pointBorderColor: '#4e73df',
                pointBorderWidth: 2,
                pointRadius: 5,
                fill: true, 
                tension: 0.4 
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0 } },
                x: { grid: { display: false } }
            }
        }
    });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
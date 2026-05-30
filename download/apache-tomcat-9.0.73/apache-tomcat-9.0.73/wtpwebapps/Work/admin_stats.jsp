<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%
    String _role = (String) session.getAttribute("userRole");
    if (_role == null || !"A".equals(_role.trim())) {
        response.sendRedirect("login.jsp"); return;
    }
    String startDate       = (String)request.getAttribute("startDate");
    String endDate         = (String)request.getAttribute("endDate");
    int totalEmployees     = (Integer)request.getAttribute("totalEmployees");
    int todayAttendance    = (Integer)request.getAttribute("todayAttendance");
    int periodTotal        = (Integer)request.getAttribute("periodTotal");
    int totalPayEstimate   = (Integer)request.getAttribute("totalPayEstimate");
    String chartLabels     = (String)request.getAttribute("chartLabels");
    String chartData       = (String)request.getAttribute("chartData");
    List<String[]> ranking = (List<String[]>)request.getAttribute("empRanking");
    if (startDate == null) startDate = "";
    if (endDate   == null) endDate   = "";
    if (ranking   == null) ranking   = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>매장 통계 - AlbaPass</title>
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

<div class="container-fluid mt-4 mb-5 px-4" style="max-width:1100px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0">
            <i class="fa-solid fa-chart-pie me-2 text-primary"></i>매장 통계 리포트
        </h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드
        </a>
    </div>

    <%-- 기간 선택 --%>
    <div class="card border-0 shadow-sm p-4 mb-4">
        <form action="AdminStats" method="get" class="row g-2 align-items-end">
            <div class="col-auto">
                <label class="form-label small fw-bold text-muted mb-1">기간</label>
                <div class="d-flex gap-1 align-items-center">
                    <button type="button" class="btn btn-outline-primary btn-sm fw-bold" onclick="setPreset('thisWeek')">이번주</button>
                    <button type="button" class="btn btn-outline-primary btn-sm fw-bold" onclick="setPreset('lastWeek')">지난주</button>
                    <button type="button" class="btn btn-outline-secondary btn-sm fw-bold" onclick="setPreset('thisMonth')">이번달</button>
                    <button type="button" class="btn btn-outline-secondary btn-sm fw-bold" onclick="setPreset('lastMonth')">지난달</button>
                </div>
            </div>
            <div class="col-auto">
                <input type="date" id="startInput" name="startDate" value="<%=startDate%>" class="form-control form-control-sm" style="width:140px;">
            </div>
            <div class="col-auto"><span class="text-muted fw-bold">~</span></div>
            <div class="col-auto">
                <input type="date" id="endInput" name="endDate" value="<%=endDate%>" class="form-control form-control-sm" style="width:140px;">
            </div>
            <div class="col-auto">
                <button type="submit" class="btn btn-primary btn-sm fw-bold px-4">
                    <i class="fa-solid fa-magnifying-glass me-1"></i>조회
                </button>
            </div>
        </form>
    </div>

    <%-- 요약 카드 4개 --%>
    <div class="row g-3 mb-4">
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm p-3 h-100 border-start border-4 border-info">
                <div class="small text-muted fw-bold mb-1">총 직원 수</div>
                <div class="h3 fw-bold mb-0 text-info"><%=totalEmployees%>명</div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm p-3 h-100 border-start border-4 border-success">
                <div class="small text-muted fw-bold mb-1">오늘 출근</div>
                <div class="h3 fw-bold mb-0 text-success"><%=todayAttendance%>명</div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm p-3 h-100 border-start border-4 border-primary">
                <div class="small text-muted fw-bold mb-1">기간 누적 출근</div>
                <div class="h3 fw-bold mb-0 text-primary"><%=periodTotal%>회</div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm p-3 h-100 border-start border-4 border-warning">
                <div class="small text-muted fw-bold mb-1">예상 총 급여 지출</div>
                <div class="h4 fw-bold mb-0 text-warning"><%=String.format("%,d", totalPayEstimate)%>원</div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <%-- 출근 현황 차트 --%>
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm p-4 h-100">
                <h6 class="fw-bold mb-3">
                    <i class="fa-solid fa-chart-line me-2 text-primary"></i>일별 출근 인원
                    <small class="text-muted fw-normal ms-2"><%=startDate%> ~ <%=endDate%></small>
                </h6>
                <% if (chartLabels == null || chartLabels.equals("[]")) { %>
                <div class="text-center text-muted py-5">
                    <i class="fa-solid fa-chart-bar fa-3x mb-3 d-block text-light"></i>
                    <p>조회된 출근 기록이 없습니다.</p>
                </div>
                <% } else { %>
                <div style="position:relative; height:300px;">
                    <canvas id="attendChart"></canvas>
                </div>
                <% } %>
            </div>
        </div>

        <%-- 직원별 근무시간 랭킹 --%>
        <div class="col-lg-4">
            <div class="card border-0 shadow-sm p-4 h-100">
                <h6 class="fw-bold mb-3">
                    <i class="fa-solid fa-ranking-star me-2 text-warning"></i>직원별 근무시간 TOP
                </h6>
                <% if (ranking.isEmpty()) { %>
                <div class="text-center text-muted py-4">
                    <i class="fa-solid fa-users fa-2x mb-2 d-block text-light"></i>
                    <small>데이터가 없습니다.</small>
                </div>
                <% } else {
                    int maxMins = Integer.parseInt(ranking.get(0)[2]);
                    for (int i = 0; i < ranking.size(); i++) {
                        String[] r = ranking.get(i);
                        int pct = maxMins > 0 ? (int)(Integer.parseInt(r[2]) * 100.0 / maxMins) : 0;
                        String[] colors = {"text-warning", "text-secondary", "text-danger"};
                        String color = i < 3 ? colors[i] : "text-muted";
                %>
                <div class="mb-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <div class="d-flex align-items-center gap-2">
                            <span class="fw-bold <%=color%>" style="font-size:16px;"><%=i+1%></span>
                            <span class="fw-bold text-dark"><%=r[0]%></span>
                        </div>
                        <span class="text-muted small fw-bold"><%=r[1]%></span>
                    </div>
                    <div class="progress" style="height:6px;">
                        <div class="progress-bar <%=i==0?"bg-warning":i==1?"bg-secondary":"bg-primary"%>"
                            style="width:<%=pct%>%"></div>
                    </div>
                </div>
                <%  }
                } %>
            </div>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
<% if (chartLabels != null && !chartLabels.equals("[]")) { %>
var ctx = document.getElementById('attendChart').getContext('2d');
var grad = ctx.createLinearGradient(0, 0, 0, 300);
grad.addColorStop(0, 'rgba(78,115,223,0.4)');
grad.addColorStop(1, 'rgba(78,115,223,0.02)');
new Chart(ctx, {
    type: 'line',
    data: {
        labels: <%=chartLabels%>,
        datasets: [{
            label: '출근 인원(명)',
            data: <%=chartData%>,
            borderColor: '#4e73df',
            backgroundColor: grad,
            borderWidth: 3,
            pointBackgroundColor: '#fff',
            pointBorderColor: '#4e73df',
            pointRadius: 5,
            fill: true,
            tension: 0.4
        }]
    },
    options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
            y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0 } },
            x: { grid: { display: false } }
        }
    }
});
<% } %>

function setPreset(type) {
    var today = new Date();
    var start, end;
    if (type === 'thisWeek') {
        var d = today.getDay(), diff = d===0?-6:1-d;
        start = new Date(today); start.setDate(today.getDate()+diff);
        end   = new Date(start); end.setDate(start.getDate()+6);
    } else if (type === 'lastWeek') {
        var d = today.getDay(), diff = d===0?-6:1-d;
        start = new Date(today); start.setDate(today.getDate()+diff-7);
        end   = new Date(start); end.setDate(start.getDate()+6);
    } else if (type === 'thisMonth') {
        start = new Date(today.getFullYear(), today.getMonth(), 1);
        end   = new Date(today.getFullYear(), today.getMonth()+1, 0);
    } else {
        start = new Date(today.getFullYear(), today.getMonth()-1, 1);
        end   = new Date(today.getFullYear(), today.getMonth(), 0);
    }
    function fmt(d) {
        return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');
    }
    document.getElementById('startInput').value = fmt(start);
    document.getElementById('endInput').value   = fmt(end);
}
</script>
</body>
</html>

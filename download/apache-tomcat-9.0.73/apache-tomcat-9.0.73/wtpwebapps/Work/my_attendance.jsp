<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.AttendanceDTO" %>
<%@ page import="java.util.ArrayList" %>
<%
    // 서블릿에서 넘겨준 데이터 수신
    String searchMonth = (String)request.getAttribute("searchMonth");
    if(searchMonth == null) { response.sendRedirect("MyAttendance"); return; } // 서블릿을 거치지 않은 경우 강제 이동

    int hourlyWage = (Integer)request.getAttribute("hourlyWage");
    double recognizedHours = (Double)request.getAttribute("recognizedHours");
    int estimatedSalary = (Integer)request.getAttribute("estimatedSalary");
    ArrayList<AttendanceDTO> list = (ArrayList<AttendanceDTO>)request.getAttribute("attendanceList");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>내 근무 및 급여 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container my-5" style="max-width: 800px;">
    
    <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
        <div>
            <h3 class="fw-bold text-dark mb-1">
                <i class="fa-solid fa-wallet text-primary me-2"></i>내 근무 및 급여
            </h3>
            <p class="text-muted small mb-0">해당 월의 예상 급여와 상세 기록을 확인하세요.</p>
        </div>
        
        <form action="MyAttendance" method="get" class="d-flex gap-2">
            <input type="month" name="searchMonth" value="<%=searchMonth%>" class="form-control form-control-sm shadow-sm" style="width:160px;">
            <button type="submit" class="btn btn-primary btn-sm px-3 shadow-sm fw-bold">조회</button>
        </form>
    </div>

    <div class="card custom-card mb-5 border-0 shadow-sm overflow-hidden">
        <div class="card-body p-4 p-md-5 text-center">
            <h6 class="text-uppercase text-primary fw-bold mb-3" style="letter-spacing: 1px;">
                <%=searchMonth.substring(0,4)%>년 <%=searchMonth.substring(5)%>월 예상 수령액
            </h6>
            <h1 class="display-5 fw-bold text-dark mb-4">
                ￦ <%=String.format("%,d", estimatedSalary)%>
            </h1>
            
            <div class="row g-3 justify-content-center">
                <div class="col-6 col-md-4">
                    <div class="p-3 bg-light rounded-3">
                        <small class="text-muted d-block mb-1">인정 근무시간</small>
                        <span class="fw-bold text-dark"><%=recognizedHours%>시간</span>
                    </div>
                </div>
                <div class="col-6 col-md-4">
                    <div class="p-3 bg-light rounded-3">
                        <small class="text-muted d-block mb-1">나의 시급</small>
                        <span class="fw-bold text-dark"><%=String.format("%,d", hourlyWage)%>원</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="bg-primary-subtle py-2 text-center">
            <small class="text-primary fw-bold" style="font-size: 11px;">
                <i class="fa-solid fa-circle-info me-1"></i> 급여는 30분 단위(0.5h)로 합산하여 계산되었습니다.
            </small>
        </div>
    </div>

    <div class="d-flex align-items-center mb-3">
        <h5 class="fw-bold text-dark mb-0">상세 근무 기록</h5>
        <span class="badge bg-secondary ms-2"><%=list.size()%>건</span>
    </div>

    <% if(list.isEmpty()) { %>
        <div class="card custom-card border-0 p-5 text-center">
            <i class="fa-solid fa-calendar-xmark fa-3x text-light mb-3"></i>
            <p class="text-muted mb-0">해당 월의 근무 기록이 존재하지 않습니다.</p>
        </div>
    <% } else { %>
        <div class="card custom-card border-0 shadow-sm overflow-hidden">
            <div class="table-responsive">
                <table class="table custom-table mb-0 text-center">
                    <thead>
                        <tr>
                            <th>근무 일자</th>
                            <th>매장명</th>
                            <th>출퇴근 시간</th>
                            <th>상태</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for(AttendanceDTO dto : list) { 
                            String fullDate = dto.getAttTime();
                            String datePart = fullDate.substring(0, 10);
                            String timePart = fullDate.substring(11, 16);
                        %>
                        <tr>
                            <td class="fw-bold text-dark"><%=datePart%></td>
                            <td class="small text-secondary"><%=dto.getStoreName()%></td>
                            <td class="fw-bold"><%=timePart%></td>
                            <td>
                                <% if("출근".equals(dto.getAttType())) { %>
                                    <span class="badge-in">출근</span>
                                <% } else { %>
                                    <span class="badge-out">퇴근</span>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    <% } %>

    <div class="text-center mt-5">
        <a href="default.jsp" class="btn btn-light px-4 py-2 fw-bold border shadow-sm">
            <i class="fa-solid fa-house me-2"></i>메인으로 돌아가기
        </a>
    </div>
</div>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
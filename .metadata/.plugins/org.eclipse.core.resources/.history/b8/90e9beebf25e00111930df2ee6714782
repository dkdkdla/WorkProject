<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.AttendanceDTO" %>
<%@ page import="java.util.*" %>
<%
    String searchMonth = (String)request.getAttribute("searchMonth");
    if (searchMonth == null) { response.sendRedirect("MyAttendance"); return; }

    int    hourlyWage      = (Integer)request.getAttribute("hourlyWage");
    double recognizedHours = (Double) request.getAttribute("recognizedHours");
    int    estimatedSalary = (Integer)request.getAttribute("estimatedSalary");
    Integer _hp = (Integer)request.getAttribute("holidayPay");
    int holidayPay = (_hp != null) ? _hp : 0;
    int basePay    = estimatedSalary - holidayPay;
    java.util.Map<String,Object> sd = (java.util.Map<String,Object>)request.getAttribute("salaryDetail");
    if (sd == null) sd = new java.util.HashMap<>();

    ArrayList<AttendanceDTO> list = (ArrayList<AttendanceDTO>)request.getAttribute("attendanceList");
    String startDate  = (String)request.getAttribute("startDate");
    String endDate    = (String)request.getAttribute("endDate");
    if (startDate == null) startDate = searchMonth + "-01";
    if (endDate   == null) endDate   = searchMonth + "-31";

    // 날짜별 그룹핑
    LinkedHashMap<String, ArrayList<AttendanceDTO>> grouped = new LinkedHashMap<>();
    for (AttendanceDTO dto : list) {
        String date = dto.getAttTime().substring(0, 10);
        if (!grouped.containsKey(date)) grouped.put(date, new ArrayList<>());
        grouped.get(date).add(dto);
    }
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
    <style>
        .working-now { background:#fff3cd; color:#856404; padding:3px 10px;
                       border-radius:20px; font-size:12px; font-weight:700;
                       animation:pulse 1.5s infinite; display:inline-block; }
        @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.6} }
    </style>
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container-fluid mt-4 mb-5 px-4" style="max-width:960px;">

    <%-- 헤더 --%>
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <h3 class="fw-bold text-dark mb-0">
            <i class="fa-solid fa-wallet text-primary me-2"></i>내 근무 및 급여
        </h3>
        <div class="d-flex align-items-center gap-2 flex-wrap">
            <%-- 기간 프리셋 --%>
            <div class="d-flex gap-1">
                <button type="button" class="btn btn-outline-primary btn-sm fw-bold" onclick="setPreset('thisWeek')">이번주</button>
                <button type="button" class="btn btn-outline-primary btn-sm fw-bold" onclick="setPreset('lastWeek')">지난주</button>
                <button type="button" class="btn btn-outline-secondary btn-sm fw-bold" onclick="setPreset('thisMonth')">이번달</button>
                <button type="button" class="btn btn-outline-secondary btn-sm fw-bold" onclick="setPreset('lastMonth')">지난달</button>
            </div>
            <form action="MyAttendance" method="get" class="d-flex gap-1 align-items-center">
                <input type="date" id="startDateInput" name="startDate" value="<%=startDate%>"
                    class="form-control form-control-sm" style="width:135px;">
                <span class="text-muted fw-bold">~</span>
                <input type="date" id="endDateInput" name="endDate" value="<%=endDate%>"
                    class="form-control form-control-sm" style="width:135px;">
                <button type="submit" class="btn btn-primary btn-sm px-3 fw-bold">조회</button>
            </form>
            <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
                <i class="fa-solid fa-arrow-left me-1"></i>메인
            </a>
        </div>
    </div>

    <%-- 급여 요약 바 --%>
    <div class="rounded-3 shadow-sm text-white p-4 mb-4"
        style="background:linear-gradient(135deg,#4e73df,#224abe);">
        <div class="row align-items-center g-3">
            <div class="col-md-4 text-center text-md-start">
                <div class="small opacity-75 mb-1"><%=startDate%> ~ <%=endDate%> 예상 급여</div>
                <div class="fw-bold" style="font-size:28px;">￦<%=String.format("%,d", estimatedSalary)%></div>
            </div>
            <div class="col-md-8">
                <div class="row g-2">
                    <div class="col-6 col-md-3">
                        <div class="text-center p-2 rounded-3" style="background:rgba(255,255,255,0.15);">
                            <div class="small opacity-75">기본급</div>
                            <div class="fw-bold"><%=String.format("%,d",basePay)%>원</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="text-center p-2 rounded-3" style="background:rgba(255,255,255,0.15);">
                            <div class="small opacity-75">주휴수당</div>
                            <div class="fw-bold <%=holidayPay>0?"":"opacity-50"%>">
                                <%=holidayPay>0 ? String.format("%,d",holidayPay)+"원" : "-"%>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="text-center p-2 rounded-3" style="background:rgba(255,255,255,0.15);">
                            <div class="small opacity-75">총 근무시간</div>
                            <div class="fw-bold"><%=String.format("%.1f",recognizedHours)%>h</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="text-center p-2 rounded-3" style="background:rgba(255,255,255,0.15);">
                            <div class="small opacity-75">근무일수</div>
                            <div class="fw-bold"><%=grouped.size()%>일</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <% if (!sd.isEmpty()) { %>
        <div class="text-end mt-2">
            <button type="button" class="btn btn-light btn-sm fw-bold"
                style="color:#224abe;" onclick="openSalaryModal()">
                <i class="fa-solid fa-file-invoice-dollar me-1"></i>급여 명세서 보기
            </button>
        </div>
        <% } %>
    </div>

    <%-- 근무 기록 테이블 --%>
    <div class="card border-0 shadow-sm">
        <div class="card-header bg-white border-0 pt-3 pb-2 px-4">
            <h6 class="fw-bold text-dark mb-0">
                <i class="fa-solid fa-list-check me-2 text-primary"></i>상세 근무 기록
                <span class="badge bg-secondary ms-1"><%=grouped.size()%>일</span>
            </h6>
        </div>
        <div class="card-body p-0">
            <% if (list.isEmpty()) { %>
            <div class="text-center text-muted py-5">
                <i class="fa-solid fa-calendar-xmark fa-3x mb-3 d-block text-light"></i>
                <p class="mb-0">해당 월의 근무 기록이 없습니다.</p>
            </div>
            <% } else { %>
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">날짜</th>
                            <th>매장</th>
                            <th class="text-center">출근</th>
                            <th class="text-center">퇴근</th>
                            <th class="text-center">근무시간</th>
                            <th class="text-center">상태</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                    for (Map.Entry<String, ArrayList<AttendanceDTO>> entry : grouped.entrySet()) {
                        String date    = entry.getKey();
                        ArrayList<AttendanceDTO> dayList = entry.getValue();

                        boolean hasIn = false, hasOut = false;
                        String inTime = "-", outTime = "-", storeName = "";
                        for (AttendanceDTO d : dayList) {
                            String t = d.getAttType();
                            if ("출근".equals(t) || "IN".equals(t)) {
                                hasIn = true;
                                inTime = d.getAttTime().substring(11,16);
                                storeName = d.getStoreName();
                            }
                            if ("퇴근".equals(t) || "OUT".equals(t)) {
                                hasOut = true;
                                outTime = d.getAttTime().substring(11,16);
                            }
                        }

                        // 근무시간 계산
                        String workHours = "-";
                        if (hasIn && hasOut) {
                            try {
                                java.time.LocalTime st = java.time.LocalTime.parse(inTime);
                                java.time.LocalTime et = java.time.LocalTime.parse(outTime);
                                long mins = java.time.Duration.between(st, et).toMinutes();
                                if (mins < 0) mins += 1440;
                                workHours = String.format("%dh %dm", mins/60, mins%60);
                            } catch(Exception e) {}
                        }

                        // 요일
                        String dayOfWeek = "";
                        try {
                            java.time.LocalDate ld = java.time.LocalDate.parse(date);
                            String[] days = {"월","화","수","목","금","토","일"};
                            dayOfWeek = days[ld.getDayOfWeek().getValue()-1];
                        } catch(Exception e) {}
                        boolean isWeekend = "토".equals(dayOfWeek) || "일".equals(dayOfWeek);
                    %>
                    <tr>
                        <td class="ps-4">
                            <span class="fw-bold text-dark"><%=date%></span>
                            <span class="badge <%=isWeekend?"bg-warning text-dark":"bg-light text-muted"%> ms-1" style="font-size:11px;"><%=dayOfWeek%></span>
                        </td>
                        <td><small class="text-muted"><%=storeName%></small></td>
                        <td class="text-center">
                            <% if (hasIn) { %>
                            <span class="badge bg-success-subtle text-success px-2 py-1 fw-bold">
                                <i class="fa-solid fa-arrow-right-to-bracket me-1" style="font-size:10px;"></i><%=inTime%>
                            </span>
                            <% } else { %><span class="text-muted small">-</span><% } %>
                        </td>
                        <td class="text-center">
                            <% if (hasOut) { %>
                            <span class="badge bg-secondary-subtle text-secondary px-2 py-1 fw-bold">
                                <i class="fa-solid fa-arrow-right-from-bracket me-1" style="font-size:10px;"></i><%=outTime%>
                            </span>
                            <% } else if (hasIn) { %>
                            <span class="working-now"><i class="fa-solid fa-circle-dot me-1"></i>근무중</span>
                            <% } else { %><span class="text-muted small">-</span><% } %>
                        </td>
                        <td class="text-center fw-bold text-dark"><%=workHours%></td>
                        <td class="text-center">
                            <% if (hasIn && hasOut) { %>
                                <span class="badge bg-success px-2">완료</span>
                            <% } else if (hasIn) { %>
                                <span class="badge bg-warning text-dark px-2">진행중</span>
                            <% } else { %>
                                <span class="badge bg-light text-muted px-2">미기록</span>
                            <% } %>
                        </td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
            <% } %>
        </div>
        <div class="card-footer bg-white border-0 py-2 text-center">
            <small class="text-muted" style="font-size:11px;">
                <i class="fa-solid fa-circle-info me-1"></i>
                예상 급여이며 4대보험 공제 전 금액입니다.
            </small>
        </div>
    </div>
</div>

<%-- 급여 명세서 모달 --%>
<% if (!sd.isEmpty()) {
    int sdWorkDays  = (Integer)sd.getOrDefault("workDays",0);
    long sdTotalMin = (Long)sd.getOrDefault("totalMin",0L);
    long sdWddMin=(Long)sd.getOrDefault("wddMin",0L); int sdWddWage=(Integer)sd.getOrDefault("wddWage",0); int sdWddPay=(Integer)sd.getOrDefault("wddPay",0);
    long sdWdnMin=(Long)sd.getOrDefault("wdnMin",0L); int sdWdnWage=(Integer)sd.getOrDefault("wdnWage",0); int sdWdnPay=(Integer)sd.getOrDefault("wdnPay",0);
    long sdHddMin=(Long)sd.getOrDefault("hddMin",0L); int sdHddWage=(Integer)sd.getOrDefault("hddWage",0); int sdHddPay=(Integer)sd.getOrDefault("hddPay",0);
    long sdHdnMin=(Long)sd.getOrDefault("hdnMin",0L); int sdHdnWage=(Integer)sd.getOrDefault("hdnWage",0); int sdHdnPay=(Integer)sd.getOrDefault("hdnPay",0);
    int sdBasePay=(Integer)sd.getOrDefault("basePay",0);
    int sdHolidayPay=(Integer)sd.getOrDefault("holidayPay",0);
    int sdTotalPay=(Integer)sd.getOrDefault("totalPay",0);
%>
<div class="modal fade" id="salaryModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" style="max-width:460px;">
        <div class="modal-content border-0 shadow-lg">
            <div class="modal-header border-0 pb-1" style="background:linear-gradient(135deg,#4e73df,#224abe);">
                <div class="text-white">
                    <h5 class="fw-bold mb-0"><i class="fa-solid fa-file-invoice-dollar me-2"></i>급여 명세서</h5>
                    <small class="opacity-75"><%=searchMonth.substring(0,4)%>년 <%=searchMonth.substring(5)%>월</small>
                </div>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body p-4" id="salaryModalBody">
                <div class="d-flex gap-3 mb-3">
                    <div class="flex-fill text-center p-3 bg-light rounded-3">
                        <div class="fw-bold text-primary fs-4"><%=sdWorkDays%>일</div>
                        <small class="text-muted">근무일수</small>
                    </div>
                    <div class="flex-fill text-center p-3 bg-light rounded-3">
                        <div class="fw-bold text-primary fs-4"><%=String.format("%.1f",sdTotalMin/60.0)%>h</div>
                        <small class="text-muted">총 근무시간</small>
                    </div>
                </div>
                <table class="table table-sm table-borderless mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="small text-muted">구분</th>
                            <th class="small text-muted text-end">시간</th>
                            <th class="small text-muted text-end">시급</th>
                            <th class="small text-muted text-end">금액</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr><td><span class="badge bg-primary">평일주간</span></td>
                            <td class="text-end small"><%=String.format("%.1f",sdWddMin/60.0)%>h</td>
                            <td class="text-end small"><%=String.format("%,d",sdWddWage)%>원</td>
                            <td class="text-end fw-bold"><%=String.format("%,d",sdWddPay)%>원</td></tr>
                        <tr><td><span class="badge bg-dark">평일야간</span></td>
                            <td class="text-end small"><%=String.format("%.1f",sdWdnMin/60.0)%>h</td>
                            <td class="text-end small"><%=String.format("%,d",sdWdnWage)%>원</td>
                            <td class="text-end fw-bold"><%=String.format("%,d",sdWdnPay)%>원</td></tr>
                        <tr><td><span class="badge bg-warning text-dark">휴일주간</span></td>
                            <td class="text-end small"><%=String.format("%.1f",sdHddMin/60.0)%>h</td>
                            <td class="text-end small"><%=String.format("%,d",sdHddWage)%>원</td>
                            <td class="text-end fw-bold"><%=String.format("%,d",sdHddPay)%>원</td></tr>
                        <tr><td><span class="badge bg-danger">휴일야간</span></td>
                            <td class="text-end small"><%=String.format("%.1f",sdHdnMin/60.0)%>h</td>
                            <td class="text-end small"><%=String.format("%,d",sdHdnWage)%>원</td>
                            <td class="text-end fw-bold"><%=String.format("%,d",sdHdnPay)%>원</td></tr>
                        <tr class="border-top">
                            <td colspan="3" class="fw-bold text-dark">기본급 합계</td>
                            <td class="text-end fw-bold text-primary"><%=String.format("%,d",sdBasePay)%>원</td></tr>
                        <tr>
                            <td colspan="3" class="text-muted small">주휴수당</td>
                            <td class="text-end fw-bold text-success"><%=String.format("%,d",sdHolidayPay)%>원</td></tr>
                    </tbody>
                    <tfoot>
                        <tr style="background:#f0f4ff;">
                            <td colspan="3" class="fw-bold py-2 ps-2">예상 총 급여</td>
                            <td class="text-end fw-bold text-primary py-2 pe-2" style="font-size:18px;"><%=String.format("%,d",sdTotalPay)%>원</td>
                        </tr>
                    </tfoot>
                </table>
                <small class="text-muted d-block mt-2">
                    <i class="fa-solid fa-circle-info me-1"></i>4대보험 공제 전 금액입니다.
                </small>
            </div>
            <div class="modal-footer border-0 pt-0">
                <button type="button" class="btn btn-outline-secondary fw-bold px-4" onclick="printSalary()">
                    <i class="fa-solid fa-print me-1"></i>인쇄
                </button>
                <button type="button" class="btn btn-primary fw-bold px-4" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>
<% } %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
function setPreset(type) {
    var today = new Date();
    var start, end;
    if (type === 'thisWeek') {
        var day = today.getDay();
        var diffToMon = (day === 0) ? -6 : 1 - day;
        start = new Date(today); start.setDate(today.getDate() + diffToMon);
        end   = new Date(start); end.setDate(start.getDate() + 6);
    } else if (type === 'lastWeek') {
        var day = today.getDay();
        var diffToMon = (day === 0) ? -6 : 1 - day;
        start = new Date(today); start.setDate(today.getDate() + diffToMon - 7);
        end   = new Date(start); end.setDate(start.getDate() + 6);
    } else if (type === 'thisMonth') {
        start = new Date(today.getFullYear(), today.getMonth(), 1);
        end   = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    } else if (type === 'lastMonth') {
        start = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        end   = new Date(today.getFullYear(), today.getMonth(), 0);
    }
    document.getElementById('startDateInput').value = toDateStr(start);
    document.getElementById('endDateInput').value   = toDateStr(end);
}
function toDateStr(d) {
    var mm = String(d.getMonth()+1).padStart(2,'0');
    var dd = String(d.getDate()).padStart(2,'0');
    return d.getFullYear() + '-' + mm + '-' + dd;
}

function openSalaryModal() {
    var el = document.getElementById('salaryModal');
    if (el) new bootstrap.Modal(el).show();
}
function printSalary() {
    var body = document.getElementById('salaryModalBody').innerHTML;
    var w = window.open('', '_blank', 'width=500,height=700');
    w.document.write('<html><head><title>급여 명세서</title>');
    w.document.write('<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">');
    w.document.write('</head><body class="p-4">');
    w.document.write(body);
    w.document.write('</body></html>');
    w.document.close();
    w.focus();
    setTimeout(function(){ w.print(); }, 500);
}
</script>

<%@ include file="footer.jsp" %>
</body>
</html>

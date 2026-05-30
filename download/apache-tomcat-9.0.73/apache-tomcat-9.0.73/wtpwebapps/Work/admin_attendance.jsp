<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.AttendanceDTO" %>
<%@ page import="work.dto.MemberDTO" %>
<%@ page import="work.util.PayCalcUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="java.time.*" %>
<%
    // 세션 체크
    String _userId = (String) session.getAttribute("userId");
    String _role   = (String) session.getAttribute("userRole");
    if (_userId == null || !"A".equals(_role != null ? _role.trim() : "")) {
        response.sendRedirect("login.jsp");
        return;
    }

    // 1. 서블릿(AdminHistory)으로부터 데이터 수신
    ArrayList<AttendanceDTO> fullList   = (ArrayList<AttendanceDTO>) request.getAttribute("fullList");
    ArrayList<MemberDTO>     memberList = (ArrayList<MemberDTO>)     request.getAttribute("memberList");

    // 🚨 팩트체크: 만약 서블릿을 거치지 않고 직접 JSP에 접속했다면 서블릿으로 강제 이동시킵니다.
    if (fullList == null || memberList == null) {
        response.sendRedirect("AdminHistory");
        return;
    }

    // 서블릿이 다시 보내준 검색 파라미터 복구
    String startDate      = (String)request.getAttribute("startDate");
    String endDate        = (String)request.getAttribute("endDate");
    String searchMemId    = (String)request.getAttribute("searchMemId");
    String currentStoreId = (String)request.getAttribute("currentStoreId");
    java.util.ArrayList<String[]> myStores = (java.util.ArrayList<String[]>)request.getAttribute("myStores");

    if (startDate == null)      startDate = java.time.LocalDate.now().withDayOfMonth(1).toString();
    if (endDate == null)        endDate   = java.time.LocalDate.now().toString();
    if (searchMemId == null)    searchMemId = "";
    if (currentStoreId == null) currentStoreId = "";
    if (myStores == null)       myStores = new java.util.ArrayList<>();
    String attFilter = (String)request.getAttribute("attFilter");
    if (attFilter == null) attFilter = "";
    java.util.Map<String,Object> sd = (java.util.Map<String,Object>)request.getAttribute("salaryDetail");
    if (sd == null) sd = new java.util.HashMap<>();

    // 2. 페이징 처리 (전달받은 fullList 기준)
    int currentPage = 1;
    String pageParam = request.getParameter("page");
    if (pageParam != null && !pageParam.isEmpty()) {
        currentPage = Integer.parseInt(pageParam);
    }
    int itemsPerPage = 10;
    int totalItems = fullList.size();
    int totalPages = (int)Math.ceil((double)totalItems / itemsPerPage);
    if (totalPages == 0) totalPages = 1;

    int startIndex = (currentPage - 1) * itemsPerPage;
    int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
    List<AttendanceDTO> list = (totalItems > 0) ? fullList.subList(startIndex, endIndex) : new ArrayList<>();

    // 3. 급여 계산 로직 (기존 로직 유지하되 fullList 기반으로 작동)
    long totalMinutes = 0;      
    double basePay = 0;         
    double holidayPay = 0;      
    int hourlyWage = 0;
    String targetName = "";
    int totalEstimatedSalary = 0;
    
    if (!searchMemId.equals("") && fullList.size() > 0) {
        // 직원 이름 조회
        for (MemberDTO m : memberList) {
            if (m.getId().equals(searchMemId)) {
                hourlyWage = m.getHourlyWage();
                targetName = m.getName();
                break;
            }
        }

        // IN/OUT 쌍을 매칭하여 PayCalcUtil로 급여 계산
        // calcList는 오래된 순서(오름차순)로 정렬
        ArrayList<AttendanceDTO> calcList = new ArrayList<>(fullList);
        Collections.reverse(calcList);

        // storeKey → 마지막 IN 기록 매핑
        Map<String, AttendanceDTO> lastInMap = new HashMap<>();
        Map<String, Long> weeklyMap = new HashMap<>();

        for (AttendanceDTO dto : calcList) {
            String storeKey = dto.getStoreName();
            try {
                if ("출근".equals(dto.getAttType()) || "IN".equals(dto.getAttType())) {
                    lastInMap.put(storeKey, dto);
                } else if ("퇴근".equals(dto.getAttType()) || "OUT".equals(dto.getAttType())) {
                    if (lastInMap.containsKey(storeKey)) {
                        AttendanceDTO inDto  = lastInMap.get(storeKey);
                        String inTimeStr  = inDto.getAttTime();   // "yyyy-MM-dd HH:mm"
                        String outTimeStr = dto.getAttTime();

                        LocalDate  workDate  = LocalDate.parse(inTimeStr.substring(0, 10));
                        LocalTime  startTime = LocalTime.parse(inTimeStr.substring(11, 16));
                        LocalTime  endTime   = LocalTime.parse(outTimeStr.substring(11, 16));
                        int rid = inDto.getRoleId();

                        // PayCalcUtil로 급여 계산 (주간/야간/주말 자동 분리)
                        int pay = PayCalcUtil.calcPay(rid, workDate, startTime, endTime);
                        basePay += pay;

                        // 주휴수당용 주간 근무 분 누적
                        long diffMin = java.time.Duration.between(startTime, endTime).toMinutes();
                        if (diffMin < 0) diffMin += 24 * 60; // 자정 넘기는 경우
                        totalMinutes += diffMin;
                        String weekKey = workDate.getYear() + "-" + workDate.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
                        weeklyMap.put(weekKey, weeklyMap.getOrDefault(weekKey, 0L) + diffMin);

                        lastInMap.remove(storeKey);
                    }
                }
            } catch (Exception e) {}
        }

        // 주휴수당 계산 (주 15시간 이상 시 적용)
        for (String key : weeklyMap.keySet()) {
            if (weeklyMap.get(key) >= 900) { // 900분 = 15시간
                double weekHours = Math.min(weeklyMap.get(key) / 60.0, 40);
                holidayPay += (weekHours / 40.0) * 8 * hourlyWage;
            }
        }
        totalEstimatedSalary = (int)(basePay + holidayPay);
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>근태 관리 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <style>
        .form-check-input[type="radio"] { display: none; }
        .form-check-input[type="radio"]:checked + label.btn-outline-success { background-color: #198754 !important; color: white !important; }
        .form-check-input[type="radio"]:checked + label.btn-outline-secondary { background-color: #6c757d !important; color: white !important; }
    </style>
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container-fluid mt-4 mb-5 px-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0"><i class="fa-solid fa-clock-rotate-left me-2 text-primary"></i>근태 관리 및 정산</h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드
        </a>
    </div>

    <div class="row g-4">
        <%-- 왼쪽: 매장 목록 --%>
        <div class="col-12 col-lg-2">
            <div class="card border-0 shadow-sm h-100">
                <div class="card-header bg-white border-0 pt-3 pb-2 px-3">
                    <h6 class="fw-bold text-dark mb-0 small">
                        <i class="fa-solid fa-store me-1 text-primary"></i>내 매장
                        <span class="badge bg-primary ms-1"><%=myStores.size()%></span>
                    </h6>
                </div>
                <div class="card-body p-0">
                    <% for (String[] s : myStores) {
                        boolean isActive = s[0].equals(currentStoreId); %>
                    <div class="p-2 px-3 border-bottom"
                        style="cursor:pointer; <%=isActive ? "background:#e8edff; border-left:3px solid #4e73df; font-weight:700;" : "border-left:3px solid transparent;"%>"
                        onclick="location.href='AdminHistory?storeId=<%=s[0]%>&startDate=<%=startDate%>&endDate=<%=endDate%>'">
                        <div class="fw-bold text-dark small"><%=s[1]%></div>
                        <small class="text-muted" style="font-size:11px;"><%=s[0]%></small>
                    </div>
                    <% } %>
                </div>
            </div>
        </div>

        <%-- 필터 + 테이블 영역 --%>
        <div class="col-12 col-lg-10">
        <div class="row g-4">
        <div class="col-lg-3">
            <div class="card custom-card mb-4 border-top border-4 border-primary shadow-sm">
                <div class="d-flex justify-content-between align-items-center px-4 pt-3 pb-2"
                    style="cursor:pointer;" onclick="toggleFilter()">
                    <h6 class="fw-bold mb-0"><i class="fa-solid fa-filter me-2 text-primary"></i>조회 필터</h6>
                    <i class="fa-solid fa-chevron-up text-muted" id="filterChevron"></i>
                </div>
                <div id="filterBody" class="px-4 pb-4">
                <form action="AdminHistory" method="get">
                    <%-- 기간 프리셋 버튼 --%>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">기간 빠른 선택</label>
                        <div class="d-flex gap-1">
                            <button type="button" class="btn btn-outline-primary fw-bold flex-fill" style="font-size:11px;padding:4px 2px;" onclick="setPreset('thisWeek')">이번주</button>
                            <button type="button" class="btn btn-outline-primary fw-bold flex-fill" style="font-size:11px;padding:4px 2px;" onclick="setPreset('lastWeek')">지난주</button>
                            <button type="button" class="btn btn-outline-secondary fw-bold flex-fill" style="font-size:11px;padding:4px 2px;" onclick="setPreset('thisMonth')">이번달</button>
                            <button type="button" class="btn btn-outline-secondary fw-bold flex-fill" style="font-size:11px;padding:4px 2px;" onclick="setPreset('lastMonth')">지난달</button>
                        </div>
                    </div>
                    <div class="mb-2">
                        <label class="form-label small fw-bold text-secondary">시작일</label>
                        <input type="date" id="startDateInput" name="startDate" value="<%=startDate%>" class="form-control form-control-sm">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">종료일</label>
                        <input type="date" id="endDateInput" name="endDate" value="<%=endDate%>" class="form-control form-control-sm">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">직원 선택</label>
                        <select name="searchMemId" class="form-select form-select-sm">
                            <option value="">전체 직원</option>
                            <% for(MemberDTO m : memberList) { %>
                                <option value="<%=m.getId()%>" <%=m.getId().equals(searchMemId)?"selected":""%>><%=m.getName()%></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="mb-4">
                        <label class="form-label small fw-bold text-secondary">출퇴근 구분</label>
                        <div class="d-flex gap-2">
                            <div class="flex-fill">
                                <input type="radio" class="btn-check" name="attFilter" id="filterAll" value="" <%="".equals(attFilter)?"checked":""%>>
                                <label class="btn btn-outline-secondary btn-sm w-100 fw-bold" for="filterAll">전체</label>
                            </div>
                            <div class="flex-fill">
                                <input type="radio" class="btn-check" name="attFilter" id="filterIn" value="IN" <%="IN".equals(attFilter)?"checked":""%>>
                                <label class="btn btn-outline-success btn-sm w-100 fw-bold" for="filterIn">
                                    <i class="fa-solid fa-arrow-right-to-bracket me-1"></i>출근
                                </label>
                            </div>
                            <div class="flex-fill">
                                <input type="radio" class="btn-check" name="attFilter" id="filterOut" value="OUT" <%="OUT".equals(attFilter)?"checked":""%>>
                                <label class="btn btn-outline-secondary btn-sm w-100 fw-bold" for="filterOut">
                                    <i class="fa-solid fa-arrow-right-from-bracket me-1"></i>퇴근
                                </label>
                            </div>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 fw-bold mb-2">
                        <i class="fa-solid fa-search me-1"></i>조회하기
                    </button>
                    <button type="button" class="btn btn-success w-100 fw-bold" onclick="openAddModal()">
                        <i class="fa-solid fa-plus me-1"></i> 기록 수동 추가
                    </button>
                </form>
                </div><%-- filterBody --%>
            </div>

        </div>

        <div class="col-lg-9">
            <%-- 급여 요약 바 - 항상 표 위에 표시 --%>
            <% if (!searchMemId.equals("") && fullList.size() > 0) { %>
            <div class="d-flex align-items-center justify-content-between px-4 py-3 mb-3 rounded-3 shadow-sm text-white"
                style="background:linear-gradient(135deg,#4e73df,#224abe);">
                <div class="d-flex align-items-center gap-3">
                    <i class="fa-solid fa-user-circle fa-lg opacity-75"></i>
                    <div>
                        <div class="small opacity-75">예상 급여</div>
                        <div class="fw-bold" style="font-size:18px;"><%=targetName%>님 &nbsp;·&nbsp; ￦<%=String.format("%,d", totalEstimatedSalary)%></div>
                    </div>
                </div>
                <button type="button" class="btn btn-light btn-sm fw-bold px-3"
                    style="color:#224abe;" onclick="openSalaryModal()">
                    <i class="fa-solid fa-file-invoice-dollar me-1"></i>명세서 보기
                </button>
            </div>
            <% } %>
            <div class="card custom-card p-0 overflow-hidden shadow-sm">
                <div class="table-responsive">
                    <table class="table custom-table mb-0 align-middle">
                        <thead class="table-light">
                            <tr>
                                <th class="ps-4">직원명</th>
                                <th>날짜 / 시간</th>
                                <th class="text-center">상태</th>
                                <th class="text-center d-none d-md-table-cell">관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if(list.isEmpty()) { %>
                                <tr><td colspan="4" class="text-center py-5 text-muted">기록이 없습니다.</td></tr>
                            <% } %>
                            <% for(AttendanceDTO dto : list) {
                                String fullDate = dto.getAttTime();
                                String dDate = fullDate.substring(0, 10);
                                String dTime = fullDate.substring(11, 16);
                                boolean isIn = "출근".equals(dto.getAttType()) || "IN".equals(dto.getAttType());
                            %>
                            <tr class="<%=isIn ? "" : ""%>">
                                <td class="ps-4">
                                    <span class="fw-bold text-dark"><%=dto.getStoreName()%></span>
                                </td>
                                <td class="text-nowrap">
                                    <span class="fw-bold text-dark"><%=dDate%></span>
                                    <span class="text-muted ms-1"><%=dTime%></span>
                                </td>
                                <td class="text-center">
                                    <%if (isIn) {%>
                                        <span class="badge bg-success-subtle text-success px-3 py-2 rounded-pill fw-bold">
                                            <i class="fa-solid fa-arrow-right-to-bracket me-1"></i>출근
                                        </span>
                                    <%} else {%>
                                        <span class="badge bg-secondary-subtle text-secondary px-3 py-2 rounded-pill fw-bold">
                                            <i class="fa-solid fa-arrow-right-from-bracket me-1"></i>퇴근
                                        </span>
                                    <%}%>
                                    <%-- 모바일에서는 버튼을 상태 아래에 --%>
                                    <div class="d-md-none mt-1">
                                        <button class="btn btn-xs btn-outline-warning border-0 me-1" onclick="openEditModal('<%=dto.getIdx()%>', '<%=dDate%>', '<%=dTime%>', '<%=dto.getAttType()%>', '<%=dto.getMemberId()%>')">✏️</button>
                                        <button class="btn btn-xs btn-outline-danger border-0" onclick="deleteRecord('<%=dto.getIdx()%>')">🗑️</button>
                                    </div>
                                </td>
                                <td class="text-center d-none d-md-table-cell">
                                    <button class="btn btn-sm btn-outline-warning border-0 me-1" onclick="openEditModal('<%=dto.getIdx()%>', '<%=dDate%>', '<%=dTime%>', '<%=dto.getAttType()%>', '<%=dto.getMemberId()%>')">✏️</button>
                                    <button class="btn btn-sm btn-outline-danger border-0" onclick="deleteRecord('<%=dto.getIdx()%>')">🗑️</button>
                                </td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
                
                <div class="card-footer bg-white py-3 border-0">
                    <nav>
                        <ul class="pagination pagination-sm justify-content-center mb-0">
                            <li class="page-item <%= (currentPage == 1) ? "disabled" : "" %>">
                                <a class="page-link" href="AdminHistory?startDate=<%=startDate%>&endDate=<%=endDate%>&searchMemId=<%=searchMemId%>&page=<%=currentPage-1%>">이전</a>
                            </li>
                            <% for(int i=1; i<=totalPages; i++) { %>
                                <li class="page-item <%= (i == currentPage) ? "active" : "" %>">
                                    <a class="page-link" href="AdminHistory?startDate=<%=startDate%>&endDate=<%=endDate%>&searchMemId=<%=searchMemId%>&page=<%=i%>"><%=i%></a>
                                </li>
                            <% } %>
                            <li class="page-item <%= (currentPage == totalPages) ? "disabled" : "" %>">
                                <a class="page-link" href="AdminHistory?startDate=<%=startDate%>&endDate=<%=endDate%>&searchMemId=<%=searchMemId%>&page=<%=currentPage+1%>">다음</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </div>
        </div>
    </div>
</div>

<%-- 급여 명세서 모달 --%>
<% if (!searchMemId.equals("") && sd != null && !sd.isEmpty()) {
    int sdWorkDays   = (Integer)sd.getOrDefault("workDays",0);
    long sdTotalMin  = (Long)sd.getOrDefault("totalMin",0L);
    long sdWddMin=(Long)sd.getOrDefault("wddMin",0L); int sdWddWage=(Integer)sd.getOrDefault("wddWage",0); int sdWddPay=(Integer)sd.getOrDefault("wddPay",0);
    long sdWdnMin=(Long)sd.getOrDefault("wdnMin",0L); int sdWdnWage=(Integer)sd.getOrDefault("wdnWage",0); int sdWdnPay=(Integer)sd.getOrDefault("wdnPay",0);
    long sdHddMin=(Long)sd.getOrDefault("hddMin",0L); int sdHddWage=(Integer)sd.getOrDefault("hddWage",0); int sdHddPay=(Integer)sd.getOrDefault("hddPay",0);
    long sdHdnMin=(Long)sd.getOrDefault("hdnMin",0L); int sdHdnWage=(Integer)sd.getOrDefault("hdnWage",0); int sdHdnPay=(Integer)sd.getOrDefault("hdnPay",0);
    int sdBasePay=(Integer)sd.getOrDefault("basePay",0);
    int sdHolidayPay=(Integer)sd.getOrDefault("holidayPay",0);
    int sdTotalPay=(Integer)sd.getOrDefault("totalPay",0);
    double sdTotalHours = sdTotalMin/60.0;
%>
<div class="modal fade" id="salaryModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" style="max-width:460px;">
        <div class="modal-content border-0 shadow-lg">
            <div class="modal-header border-0 pb-1" style="background:linear-gradient(135deg,#4e73df,#224abe);">
                <div class="text-white">
                    <h5 class="fw-bold mb-0"><i class="fa-solid fa-file-invoice-dollar me-2"></i>급여 명세서</h5>
                    <small class="opacity-75"><%=targetName%>님 &nbsp;·&nbsp; <%=startDate%> ~ <%=endDate%></small>
                </div>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body p-4" id="salaryModalBody">

                <%-- 근무 요약 --%>
                <div class="d-flex gap-3 mb-3">
                    <div class="flex-fill text-center p-3 bg-light rounded-3">
                        <div class="fw-bold text-primary fs-4"><%=sdWorkDays%>일</div>
                        <small class="text-muted">근무일수</small>
                    </div>
                    <div class="flex-fill text-center p-3 bg-light rounded-3">
                        <div class="fw-bold text-primary fs-4"><%=String.format("%.1f",sdTotalHours)%>h</div>
                        <small class="text-muted">총 근무시간</small>
                    </div>
                </div>

                <%-- 시급 상세 --%>
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
                        <tr>
                            <td><span class="badge bg-primary">평일주간</span></td>
                            <td class="text-end small"><%=String.format("%.1f",sdWddMin/60.0)%>h</td>
                            <td class="text-end small"><%=String.format("%,d",sdWddWage)%>원</td>
                            <td class="text-end fw-bold"><%=String.format("%,d",sdWddPay)%>원</td>
                        </tr>
                        <tr>
                            <td><span class="badge bg-dark">평일야간</span></td>
                            <td class="text-end small"><%=String.format("%.1f",sdWdnMin/60.0)%>h</td>
                            <td class="text-end small"><%=String.format("%,d",sdWdnWage)%>원</td>
                            <td class="text-end fw-bold"><%=String.format("%,d",sdWdnPay)%>원</td>
                        </tr>
                        <tr>
                            <td><span class="badge bg-warning text-dark">휴일주간</span></td>
                            <td class="text-end small"><%=String.format("%.1f",sdHddMin/60.0)%>h</td>
                            <td class="text-end small"><%=String.format("%,d",sdHddWage)%>원</td>
                            <td class="text-end fw-bold"><%=String.format("%,d",sdHddPay)%>원</td>
                        </tr>
                        <tr>
                            <td><span class="badge bg-danger">휴일야간</span></td>
                            <td class="text-end small"><%=String.format("%.1f",sdHdnMin/60.0)%>h</td>
                            <td class="text-end small"><%=String.format("%,d",sdHdnWage)%>원</td>
                            <td class="text-end fw-bold"><%=String.format("%,d",sdHdnPay)%>원</td>
                        </tr>
                        <tr class="border-top">
                            <td colspan="3" class="fw-bold text-dark">기본급 합계</td>
                            <td class="text-end fw-bold text-primary"><%=String.format("%,d",sdBasePay)%>원</td>
                        </tr>
                        <tr>
                            <td colspan="3" class="text-muted small">주휴수당 <span class="text-muted" style="font-size:10px;">(주 15h 이상)</span></td>
                            <td class="text-end fw-bold text-success"><%=String.format("%,d",sdHolidayPay)%>원</td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr style="background:#f0f4ff;">
                            <td colspan="3" class="fw-bold text-dark py-2 ps-2 rounded-start">예상 총 급여</td>
                            <td class="text-end fw-bold text-primary py-2 pe-2 rounded-end" style="font-size:18px;"><%=String.format("%,d",sdTotalPay)%>원</td>
                        </tr>
                    </tfoot>
                </table>

                <small class="text-muted d-block mt-2">
                    <i class="fa-solid fa-circle-info me-1"></i>
                    실제 지급액은 4대보험 공제 후 달라질 수 있습니다.
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

<div class="modal fade" id="attModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold" id="modalTitle">
                    <i class="fa-solid fa-clock-rotate-left me-2 text-primary"></i>근태 기록 추가
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="AdminAttendanceAction" method="post">
                <div class="modal-body pt-3">
                    <input type="hidden" name="mode" id="modalMode" value="add">
                    <input type="hidden" name="idx" id="modalIdx">
                    <input type="hidden" name="returnStartDate" value="<%=startDate%>">
                    <input type="hidden" name="returnEndDate" value="<%=endDate%>">
                    <input type="hidden" name="returnSearchMemId" value="<%=searchMemId%>">

                    <%-- 직원 선택 --%>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">직원 선택</label>
                        <select name="memId" id="modalMemId" class="form-select" required>
                            <% for(MemberDTO m : memberList) { %>
                                <option value="<%=m.getId()%>"><%=m.getName()%> (<%=m.getId()%>)</option>
                            <% } %>
                        </select>
                    </div>

                    <%-- 출퇴근 구분 --%>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">구분</label>
                        <div class="d-flex gap-2">
                            <div class="form-check form-check-inline flex-fill">
                                <input class="form-check-input" type="radio" name="type" id="typeIn" value="출근" checked>
                                <label class="form-check-label btn btn-outline-success w-100 fw-bold" for="typeIn">
                                    <i class="fa-solid fa-arrow-right-to-bracket me-1"></i>출근
                                </label>
                            </div>
                            <div class="form-check form-check-inline flex-fill">
                                <input class="form-check-input" type="radio" name="type" id="typeOut" value="퇴근">
                                <label class="form-check-label btn btn-outline-secondary w-100 fw-bold" for="typeOut">
                                    <i class="fa-solid fa-arrow-right-from-bracket me-1"></i>퇴근
                                </label>
                            </div>
                        </div>
                    </div>

                    <%-- 날짜+시간 한 줄로 --%>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">날짜 및 시간</label>
                        <div class="input-group">
                            <input type="date" name="date" id="modalDate" class="form-control" required>
                            <input type="time" name="time" id="modalTime" class="form-control" required>
                        </div>
                        <small class="text-muted">날짜와 시간을 함께 선택하세요.</small>
                    </div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-light fw-bold px-4" data-bs-dismiss="modal">취소</button>
                    <button type="submit" class="btn btn-primary fw-bold px-4">
                        <i class="fa-solid fa-check me-1"></i>저장하기
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<form id="deleteForm" action="AdminAttendanceAction" method="post" style="display:none;">
    <input type="hidden" name="mode" value="delete">
    <input type="hidden" name="idx" id="deleteIdx">
    <input type="hidden" name="returnStartDate" value="<%=startDate%>">
    <input type="hidden" name="returnEndDate" value="<%=endDate%>">
    <input type="hidden" name="returnSearchMemId" value="<%=searchMemId%>">
</form>

        </div><%-- col-lg-10 --%>
        </div><%-- row (필터+테이블) --%>
        </div><%-- container-fluid row --%>
</div><%-- container-fluid --%>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    // 기간 프리셋
    function setPreset(type) {
        var today = new Date();
        var start, end;

        if (type === 'thisWeek') {
            var day = today.getDay(); // 0=일, 1=월 ...
            var diffToMon = (day === 0) ? -6 : 1 - day; // 이번 주 월요일
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
        var mm = String(d.getMonth() + 1).padStart(2, '0');
        var dd = String(d.getDate()).padStart(2, '0');
        return d.getFullYear() + '-' + mm + '-' + dd;
    }

    const attModal = new bootstrap.Modal(document.getElementById('attModal'));

    function downloadExcel() {
        var start   = document.getElementById('startDateInput').value;
        var end     = document.getElementById('endDateInput').value;
        var storeId = '<%=currentStoreId%>';
        if (!start || !end || !storeId) {
            showToast('날짜와 매장을 선택 후 다운로드하세요.', 'warning');
            return;
        }
        var url = 'api_excel_download.jsp?storeId=' + encodeURIComponent(storeId) +
                  '&startDate=' + start + '&endDate=' + end;
        window.location.href = url;
    }

    function toggleFilter() {
        var body    = document.getElementById('filterBody');
        var chevron = document.getElementById('filterChevron');
        if (body.style.display === 'none') {
            body.style.display = '';
            chevron.classList.replace('fa-chevron-down', 'fa-chevron-up');
        } else {
            body.style.display = 'none';
            chevron.classList.replace('fa-chevron-up', 'fa-chevron-down');
        }
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

    function openAddModal() {
        document.getElementById('modalTitle').innerText = "기록 수동 추가";
        document.getElementById('modalMode').value = "add";
        document.getElementById('modalIdx').value = "";
        document.getElementById('modalMemId').disabled = false;

        // 오늘 날짜 + 현재 시각 기본값 설정
        var now = new Date();
        var yyyy = now.getFullYear();
        var mm   = String(now.getMonth() + 1).padStart(2, '0');
        var dd   = String(now.getDate()).padStart(2, '0');
        var hh   = String(now.getHours()).padStart(2, '0');
        var min  = String(now.getMinutes()).padStart(2, '0');
        document.getElementById('modalDate').value = yyyy + '-' + mm + '-' + dd;
        document.getElementById('modalTime').value = hh + ':' + min;

        attModal.show();
    }

    function openEditModal(idx, date, time, type, memId) {
        document.getElementById('modalTitle').innerText = "기록 수정";
        document.getElementById('modalMode').value = "update";
        document.getElementById('modalIdx').value = idx;
        document.getElementById('modalDate').value = date;
        document.getElementById('modalTime').value = time;
        // 라디오 버튼 선택 (IN/출근, OUT/퇴근 모두 처리)
        var isIn = (type === 'IN' || type === '출근');
        document.getElementById('typeIn').checked  = isIn;
        document.getElementById('typeOut').checked = !isIn;
        document.getElementById('modalMemId').value = memId;
        document.getElementById('modalMemId').disabled = true;
        attModal.show();
    }

    function deleteRecord(idx) {
        if(confirm("정말 이 근태 기록을 삭제하시겠습니까?")) {
            document.getElementById('deleteIdx').value = idx;
            document.getElementById('deleteForm').submit();
        }
    }
</script>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.AttendanceDTO" %>
<%@ page import="work.dto.MemberDTO" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
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
    String startDate = (String)request.getAttribute("startDate");
    String endDate = (String)request.getAttribute("endDate");
    String searchMemId = (String)request.getAttribute("searchMemId");
    
    // 기본값 방어 코드
    if (startDate == null) startDate = "";
    if (endDate == null) endDate = "";
    if (searchMemId == null) searchMemId = "";

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
        // 직원 정보는 이미 memberList에 있으므로 거기서 찾거나 필요시 전용 로직 수행
        for(MemberDTO m : memberList) {
            if(m.getId().equals(searchMemId)) {
                hourlyWage = m.getHourlyWage();
                targetName = m.getName();
                break;
            }
        }

        ArrayList<AttendanceDTO> calcList = new ArrayList<>(fullList);
        Collections.reverse(calcList); 

        SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar cal = Calendar.getInstance();
        Map<String, Long> weeklyMap = new HashMap<>(); 
        Map<String, Date> lastInTimeMap = new HashMap<>(); 

        for (AttendanceDTO dto : calcList) {
            String storeKey = dto.getStoreName(); 
            try {
                if ("출근".equals(dto.getAttType())) {
                    lastInTimeMap.put(storeKey, sdfFull.parse(dto.getAttTime()));
                } 
                else if ("퇴근".equals(dto.getAttType())) {
                    if (lastInTimeMap.containsKey(storeKey)) {
                        Date inTime = lastInTimeMap.get(storeKey);
                        Date outTime = sdfFull.parse(dto.getAttTime());
                        long diff = outTime.getTime() - inTime.getTime();
                        totalMinutes += (diff / (1000 * 60));
                        cal.setTime(inTime);
                        String weekKey = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.WEEK_OF_YEAR);
                        weeklyMap.put(weekKey, weeklyMap.getOrDefault(weekKey, 0L) + (diff / (1000 * 60)));
                        lastInTimeMap.remove(storeKey); 
                    }
                }
            } catch(Exception e){}
        }
        double recognizedHours = (totalMinutes / 30) * 0.5;
        basePay = recognizedHours * hourlyWage;
        for (String key : weeklyMap.keySet()) {
            if (weeklyMap.get(key) >= 900) { 
                double weekHours = Math.min(weeklyMap.get(key)/60.0, 40);
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
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container mt-5 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0"><i class="fa-solid fa-clock-rotate-left me-2 text-primary"></i>근태 관리 및 정산</h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 대시보드
        </a>
    </div>

    <div class="row g-4">
        <div class="col-lg-4">
            <div class="card custom-card p-4 mb-4 border-top border-4 border-primary shadow-sm">
                <h6 class="fw-bold mb-3"><i class="fa-solid fa-filter me-2 text-primary"></i>조회 필터</h6>
                <form action="AdminHistory" method="get">
                    <div class="mb-2">
                        <label class="form-label small fw-bold text-secondary">시작일</label>
                        <input type="date" name="startDate" value="<%=startDate%>" class="form-control form-control-sm">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">종료일</label>
                        <input type="date" name="endDate" value="<%=endDate%>" class="form-control form-control-sm">
                    </div>
                    <div class="mb-4">
                        <label class="form-label small fw-bold text-secondary">직원 선택</label>
                        <select name="searchMemId" class="form-select form-select-sm">
                            <option value="">전체 직원</option>
                            <% for(MemberDTO m : memberList) { %>
                                <option value="<%=m.getId()%>" <%=m.getId().equals(searchMemId)?"selected":""%>><%=m.getName()%></option>
                            <% } %>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 fw-bold mb-2">
                        <i class="fa-solid fa-search me-1"></i>조회하기
                    </button>
                    <button type="button" class="btn btn-success w-100 fw-bold" onclick="openAddModal()">
                        <i class="fa-solid fa-plus me-1"></i> 기록 수동 추가
                    </button>
                </form>
            </div>
            <% if (!searchMemId.equals("") && fullList.size() > 0) { %>
                <div class="card custom-card p-4 text-white border-0 shadow-lg" style="background: linear-gradient(135deg, #4e73df 0%, #224abe 100%);">
                    <small class="opacity-75"><%=targetName%>님 예상 급여</small>
                    <h2 class="fw-bold mb-3">￦ <%=String.format("%,d", totalEstimatedSalary)%></h2>
                    <hr class="opacity-25">
                    <div class="d-flex justify-content-between small"><span>기본급</span><span>￦<%=String.format("%,d", (int)basePay)%></span></div>
                    <div class="d-flex justify-content-between small mt-1"><span>주휴수당</span><span>￦<%=String.format("%,d", (int)holidayPay)%></span></div>
                </div>
            <% } %>
        </div>

        <div class="col-lg-8">
            <div class="card custom-card p-0 overflow-hidden shadow-sm">
                <div class="table-responsive">
                    <table class="table custom-table mb-0 align-middle text-center">
                        <thead class="table-light">
                            <tr>
                                <th class="ps-4 text-start">날짜 / 시간</th>
                                <th>직원명</th>
                                <th>상태</th>
                                <th>관리</th>
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
                            %>
                            <tr>
                                <td class="ps-4 text-start">
                                    <div class="fw-bold text-dark"><%=dDate%></div>
                                    <small class="text-muted"><%=dTime%></small>
                                </td>
                                <td class="fw-bold"><%=dto.getStoreName()%></td>
                                <td>
                                    <% if("출근".equals(dto.getAttType())) { %>
                                        <span class="badge bg-primary-subtle text-primary px-3 py-2 rounded-pill">출근</span>
                                    <% } else { %>
                                        <span class="badge bg-danger-subtle text-danger px-3 py-2 rounded-pill">퇴근</span>
                                    <% } %>
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-outline-warning border-0" onclick="openEditModal('<%=dto.getIdx()%>', '<%=dDate%>', '<%=dTime%>', '<%=dto.getAttType()%>', '<%=dto.getMemberId()%>')">✏️</button>
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

<div class="modal fade" id="attModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold" id="modalTitle">근태 기록 관리</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="AdminAttendanceAction" method="post">
                <div class="modal-body">
                    <input type="hidden" name="mode" id="modalMode" value="add">
                    <input type="hidden" name="idx" id="modalIdx">
                    
                    <input type="hidden" name="returnStartDate" value="<%=startDate%>">
                    <input type="hidden" name="returnEndDate" value="<%=endDate%>">
                    <input type="hidden" name="returnSearchMemId" value="<%=searchMemId%>">

                    <div class="mb-3">
                        <label class="form-label small fw-bold">직원 선택</label>
                        <select name="memId" id="modalMemId" class="form-select" required>
                            <% for(MemberDTO m : memberList) { %>
                                <option value="<%=m.getId()%>"><%=m.getName()%>(<%=m.getId()%>)</option>
                            <% } %>
                        </select>
                    </div>
                    <div class="row g-2">
                        <div class="col-6">
                            <label class="form-label small fw-bold">날짜</label>
                            <input type="date" name="date" id="modalDate" class="form-control" required>
                        </div>
                        <div class="col-6">
                            <label class="form-label small fw-bold">시간</label>
                            <input type="time" name="time" id="modalTime" class="form-control" required>
                        </div>
                    </div>
                    <div class="mt-3">
                        <label class="form-label small fw-bold">상태</label>
                        <select name="type" id="modalType" class="form-select">
                            <option value="출근">출근</option>
                            <option value="퇴근">퇴근</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="submit" class="btn btn-primary fw-bold px-4">저장하기</button>
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

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    const attModal = new bootstrap.Modal(document.getElementById('attModal'));

    function openAddModal() {
        document.getElementById('modalTitle').innerText = "기록 수동 추가";
        document.getElementById('modalMode').value = "add";
        document.getElementById('modalIdx').value = "";
        document.getElementById('modalMemId').disabled = false;
        attModal.show();
    }

    function openEditModal(idx, date, time, type, memId) {
        document.getElementById('modalTitle').innerText = "기록 수정";
        document.getElementById('modalMode').value = "update";
        document.getElementById('modalIdx').value = idx;
        document.getElementById('modalDate').value = date;
        document.getElementById('modalTime').value = time;
        document.getElementById('modalType').value = type;
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
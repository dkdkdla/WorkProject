<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.MemberDTO" %>
<%
    // 서블릿에서 보낸 member 객체를 가져옴
    MemberDTO member = (MemberDTO)request.getAttribute("member");
    if (member == null) {
        response.sendRedirect("admin_member_list.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>직원 정보 수정 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container mt-5 mb-5" style="max-width: 600px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0">
            <i class="fa-solid fa-user-pen me-2 text-primary"></i>직원 정보 수정
        </h3>
        <a href="admin_member_list.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 목록으로
        </a>
    </div>

    <div class="card custom-card p-4 shadow-sm border-0">
        <div class="text-center mb-4">
            <div class="icon-box bg-primary-subtle text-primary mx-auto mb-3" style="width: 70px; height: 70px;">
                <i class="fa-solid fa-user-check fa-2x"></i>
            </div>
            <h5 class="fw-bold text-dark"><%=member.getName()%> 직원님</h5>
            <small class="text-muted">직원의 기본 인적사항 및 시급을 수정합니다.</small>
        </div>

        <form action="AdminMemberUpdate" method="post">
            <input type="hidden" name="id" value="<%=member.getId()%>">
            
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label small fw-bold text-secondary">이름</label>
                    <div class="input-group">
                        <span class="input-group-text bg-light border-end-0"><i class="fa-solid fa-signature text-muted"></i></span>
                        <input type="text" value="<%=member.getName()%>" class="form-control bg-light" readonly>
                    </div>
                </div>

                <div class="col-md-6">
                    <label class="form-label small fw-bold text-secondary">전화번호</label>
                    <div class="input-group">
                        <span class="input-group-text bg-light border-end-0"><i class="fa-solid fa-phone text-muted"></i></span>
                        <input type="text" value="<%=member.getPhone()%>" class="form-control bg-light" readonly>
                    </div>
                </div>

                <div class="col-12 mt-4">
                    <label class="form-label small fw-bold text-primary">시급 설정 (원)</label>
                    <div class="input-group input-group-lg">
                        <span class="input-group-text bg-white border-end-0 text-primary"><i class="fa-solid fa-won-sign"></i></span>
                        <input type="number" name="wage" value="<%=member.getHourlyWage()%>" class="form-control border-start-0 ps-0 fw-bold text-primary" placeholder="숫자만 입력" required>
                    </div>
                    <p class="text-muted mt-2 small">
                        <i class="fa-solid fa-circle-info me-1"></i> 시급 변경 시, 이후 기록되는 급여 정산부터 반영됩니다.
                    </p>
                </div>
            </div>

            <div class="mt-5 d-flex gap-2">
                <a href="admin_member_list.jsp" class="btn btn-light flex-fill py-3 fw-bold border">취소</a>
                <button type="submit" class="btn btn-primary flex-fill py-3 fw-bold shadow-sm">수정 완료</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="footer.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="work.dto.MemberDTO" %>
<%@ page import="java.util.ArrayList" %>
<%
    // 서블릿(MyPage)에서 넘겨준 데이터 수신
    MemberDTO dto = (MemberDTO)request.getAttribute("memberDto");
    ArrayList<String[]> myStores = (ArrayList<String[]>)request.getAttribute("myStores");
    
    // 권한 확인용 (세션에서 직접 가져옴)
    String userRole = (String)session.getAttribute("userRole");

    // 직접 접근 방지
    if (dto == null) {
        response.sendRedirect("MyPage");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>내 정보 수정 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <style>
        .input-group-text { min-width: 65px; justify-content: center; }
        .custom-card { border-radius: 12px; border: none; }
    </style>
</head>
<body>

<%@ include file="navbar.jsp" %>

<div class="container mt-5 mb-5" style="max-width: 700px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold text-dark mb-0"><i class="fa-solid fa-user-gear me-2 text-primary"></i>내 정보 수정</h3>
        <a href="default.jsp" class="btn btn-outline-secondary btn-sm fw-bold px-3">
            <i class="fa-solid fa-arrow-left me-1"></i> 메인으로
        </a>
    </div>

    <div class="card custom-card p-4 mb-4 shadow-sm">
        <h5 class="fw-bold mb-4"><i class="fa-solid fa-id-card me-2 text-muted"></i>기본 정보</h5>
        <form id="profileForm">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label small fw-bold text-secondary">아이디</label>
                    <input type="text" class="form-control bg-light" value="<%=dto.getId()%>" readonly>
                </div>
                <div class="col-md-6">
                    <label class="form-label small fw-bold text-secondary">이름</label>
                    <input type="text" class="form-control bg-light" value="<%=dto.getName()%>" readonly>
                </div>
                <div class="col-md-6">
                    <label class="form-label small fw-bold text-secondary">연락처</label>
                    <input type="text" name="phone" class="form-control" value="<%=dto.getPhone()%>" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label small fw-bold text-secondary">시급 (원)</label>
                    <input type="number" name="wage" class="form-control" value="<%=dto.getHourlyWage()%>">
                </div>
            </div>
            <div class="mt-4 p-4 rounded-3 border bg-light-subtle">
                <label class="form-label small fw-bold text-primary">새 비밀번호 (변경 시에만 입력)</label>
                <div class="input-group">
                    <input type="password" name="new_pw" id="new_pw" class="form-control" placeholder="비워두면 기존 비밀번호 유지">
                    <button class="btn btn-outline-secondary border-start-0 bg-white" type="button" onclick="toggleNewPw()">
                        <i class="fa-solid fa-eye text-muted" id="toggleIcon"></i>
                    </button>
                </div>
            </div>
            <button type="submit" class="btn btn-primary w-100 fw-bold py-3 mt-4 shadow-sm">정보 수정 완료</button>
        </form>
    </div>

    <% if ("A".equals(userRole)) { %>
    <div class="card custom-card p-4 mb-4 shadow-sm border-start border-4 border-primary">
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <h5 class="fw-bold mb-1 text-primary"><i class="fa-solid fa-store me-2"></i>매장 관리</h5>
                <small class="text-muted">새 매장 생성 신청 및 소속 매장 관리</small>
            </div>
            <a href="StoreManage" class="btn btn-primary fw-bold px-4">
                <i class="fa-solid fa-arrow-right me-1"></i>매장 관리 페이지로
            </a>
        </div>
    </div>
    <% } %>

    <div class="card custom-card p-4 shadow-sm">
        <h5 class="fw-bold mb-2"><i class="fa-solid fa-store me-2 text-muted"></i>내 근무지 관리</h5>
        <div class="list-group list-group-flush border rounded-3 mb-4 overflow-hidden mt-3">
            <% if(myStores == null || myStores.size() == 0) { %>
                <div class="list-group-item text-center py-4 text-muted small">등록된 근무지가 없습니다.</div>
            <% } else { %>
                <% for(String[] store : myStores) { %>
                    <div class="list-group-item d-flex justify-content-between align-items-center p-3">
                        <div>
                            <span class="fw-bold text-dark"><%=store[1]%></span>
                            <small class="text-muted ms-2">(ID: <%=store[0]%>)</small>
                        </div>
                        <button type="button" class="btn btn-sm btn-outline-danger border-0" onclick="deleteStore('<%=dto.getId()%>', '<%=store[0]%>')">
                            <i class="fa-solid fa-trash-can"></i> 삭제
                        </button>
                    </div>
                <% } %>
            <% } %>
        </div>

        <form id="addStoreForm">
            <div class="input-group">
                <span class="input-group-text bg-white small fw-bold text-secondary">매장 연결</span>
                <input type="text" name="add_store_id" class="form-control shadow-none" placeholder="기존 매장 코드 입력" required>
                <button type="submit" class="btn btn-dark fw-bold px-4">추가</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="footer.jsp" %>

<script>
    // 비밀번호 토글 (기본 정보용)
    function toggleNewPw() {
        const pwInput = document.getElementById("new_pw");
        const icon = document.getElementById("toggleIcon");
        pwInput.type = (pwInput.type === "password") ? "text" : "password";
        icon.classList.toggle("fa-eye");
        icon.classList.toggle("fa-eye-slash");
    }

    // 기본 정보 수정 (MyPageUpdate 호출)
    document.getElementById('profileForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        if(!confirm("정보를 수정하시겠습니까?")) return;
        const formData = new URLSearchParams(new FormData(this));
        const res = await fetch('MyPageUpdate', { method: 'POST', body: formData });
        const data = await res.json();
        alert(data.message);
        if(data.status === 'success') location.reload();
    });

    // 기존 매장 추가 (MyStoreAdd 호출)
    document.getElementById('addStoreForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const formData = new URLSearchParams(new FormData(this));
        formData.append("id", "<%=dto.getId()%>");
        const res = await fetch('MyStoreAdd', { method: 'POST', body: formData });
        const data = await res.json();
        alert(data.message);
        if(data.status === 'success') location.reload();
    });

    // 매장 삭제 (MyStoreDelete 호출)
    async function deleteStore(userId, storeId) {
        if (!confirm("해당 근무지를 목록에서 삭제하시겠습니까?")) return;
        const res = await fetch('MyStoreDelete?id=' + userId + '&storeId=' + storeId);
        const data = await res.json();
        alert(data.message);
        if(data.status === 'success') location.reload();
    }
</script>
</body>
</html>
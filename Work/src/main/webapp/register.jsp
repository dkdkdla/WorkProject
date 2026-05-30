<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>회원가입 - AlbaPass</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css">
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/jlogo.png">
    <style>
        body { 
            background-color: #f8f9fc; 
            font-family: 'Pretendard', sans-serif; 
            min-height: 100vh; 
            display: flex; 
            flex-direction: column; 
        }
        
        main {
            flex: 1;
            display: flex;
            align-items: center;
            padding: 40px 0;
        }

        .register-card { 
            max-width: 500px; 
            margin: 0 auto; 
            border: none; 
            border-radius: 20px; 
            box-shadow: 0 10px 30px rgba(0,0,0,0.08); 
            background: white;
        }
        
        .form-label { font-size: 0.85rem; font-weight: 700; color: #4e73df; }
        .required-mark { color: #e74a3b; }
        .role-selector .btn-check:checked + .btn { background-color: #4e73df; color: white; border-color: #4e73df; }
        #adminCodeArea { display: none; transition: all 0.3s ease-in-out; }
        #adminNotice { display: none; }
        .hint-text { font-size: 11px; margin-top: 4px; display: block; }
        
        .logo-wrapper {
            display: inline-block;
            overflow: hidden;
            width: 240px;
            height: 100px;
            border-radius: 12px;
            background-color: #fff;
            transition: transform 0.3s ease;
        }
        .logo-wrapper:hover { transform: scale(1.02); }
    </style>
</head>
<body>

<jsp:include page="navbar.jsp" />

<main>
    <div class="container">
        <div class="card register-card p-4 p-md-5">
            <div class="text-center mb-4">
                <a href="<%=request.getContextPath()%>/default.jsp" class="logo-wrapper mb-2">
                    <img src="<%=request.getContextPath()%>/logo.png" alt="AlbaPass 로고" 
                         style="width: 100%; height: 100%; object-fit: contain; transform: scale(1); transform-origin: center;">
                </a>
                <h3 class="fw-bold text-dark mt-2">회원가입</h3>
            </div>

            <form id="regForm">
                <div class="mb-4 text-center">
                    <div class="btn-group role-selector w-100" role="group">
                        <input type="radio" class="btn-check" name="role" id="roleStaff" value="S" checked onclick="handleRoleChange('S')">
                        <label class="btn btn-outline-primary py-2" for="roleStaff"><i class="fa-solid fa-user me-2"></i>직원</label>
                        
                        <input type="radio" class="btn-check" name="role" id="roleAdmin" value="A" onclick="handleRoleChange('A')">
                        <label class="btn btn-outline-primary py-2" for="roleAdmin"><i class="fa-solid fa-user-tie me-2"></i>관리자</label>
                    </div>
                </div>

                <div id="adminNotice" class="mb-3 p-3 bg-warning-subtle rounded-3">
                    <i class="fa-solid fa-circle-info me-2 text-warning"></i>
                    <small class="text-dark fw-bold">점장 가입 신청 후 전체관리자 승인이 완료되면 로그인 가능합니다.</small>
                </div>

                <div class="mb-3">
                    <label class="form-label">아이디 <span class="required-mark">*</span></label>
                    <div class="input-group">
                        <input type="text" name="id" id="inputId" class="form-control" placeholder="아이디 입력" required
                            oninput="resetIdCheck()">
                        <button type="button" class="btn btn-outline-secondary fw-bold" onclick="checkDuplicateId()">
                            중복확인
                        </button>
                    </div>
                    <small id="idCheckMsg" class="hint-text"></small>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-6">
                        <label class="form-label">비밀번호 <span class="required-mark">*</span></label>
                        <input type="password" id="pw" name="pw" class="form-control" placeholder="입력" required onkeyup="checkPwMatch()">
                    </div>
                    <div class="col-6">
                        <label class="form-label">비밀번호 확인 <span class="required-mark">*</span></label>
                        <input type="password" id="pwCheck" class="form-control" placeholder="재입력" required onkeyup="checkPwMatch()">
                    </div>
                    <div class="col-12 mt-1">
                        <small id="pwMsg" style="font-size: 11px;"></small>
                    </div>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-6">
                        <label class="form-label">이름 <span class="required-mark">*</span></label>
                        <input type="text" name="name" class="form-control" placeholder="실명" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label">전화번호 <span class="required-mark">*</span></label>
                        <input type="text" name="phone" class="form-control" placeholder="010-0000-0000"
                            maxlength="13" oninput="autoHyphen(this)" required>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">생년월일 <span class="required-mark">*</span></label>
                    <input type="date" name="birth" id="birth" class="form-control" required>
                    <small class="text-muted hint-text">실제 생년월일을 입력해주세요.</small>
                </div>

                <div class="mb-4" id="storeWrap">
                    <label class="form-label" id="storeLabel">소속 매장 <span class="text-muted">(선택, 여러 개 가능)</span></label>

                    <%-- 선택된 매장 태그 목록 --%>
                    <div id="selectedStoreTags" class="d-flex flex-wrap gap-2 mb-2" style="min-height:32px;"></div>

                    <div class="input-group">
                        <input type="text" id="storeSearchInput" class="form-control bg-white"
                            placeholder="매장을 검색해서 선택하세요" readonly>
                        <button class="btn btn-outline-primary fw-bold" type="button"
                            data-bs-toggle="modal" data-bs-target="#storeSearchModal">
                            <i class="fa-solid fa-magnifying-glass me-1"></i>검색
                        </button>
                    </div>
                    <input type="hidden" name="storeIds" id="storeIdsHidden">
                    <small id="storeHint" class="text-muted hint-text">없으면 나중에 매장 관리에서 신청 가능합니다.</small>
                </div>

                <%-- 매장별 근무 요일은 JS로 동적 생성 --%>
                <div id="storeWorkDaysContainer" class="mb-4"></div>
                <input type="hidden" name="workDays" id="workDaysHidden">
                <input type="hidden" name="storeWorkDays" id="storeWorkDaysHidden">

                <button type="submit" class="btn btn-primary w-100 py-3 fw-bold shadow-sm mb-3">가입하기</button>
            </form>

            <div class="text-center">
                <a href="login.jsp" class="small text-decoration-none text-muted">이미 계정이 있으신가요? <strong>로그인</strong></a>
            </div>
        </div>
    </div>
</main>

<%-- 매장 검색 모달 --%>
<div class="modal fade" id="storeSearchModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content border-0 shadow">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">
                    <i class="fa-solid fa-store me-2 text-primary"></i>매장 검색
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="input-group mb-3">
                    <input type="text" id="modalStoreKeyword" class="form-control"
                        placeholder="매장 이름 또는 코드 입력"
                        oninput="searchStoreModal(this.value)">
                    <button class="btn btn-primary fw-bold" type="button"
                        onclick="searchStoreModal(document.getElementById('modalStoreKeyword').value)">
                        <i class="fa-solid fa-magnifying-glass"></i>
                    </button>
                </div>
                <div id="modalStoreResult">
                    <div class="text-center text-muted py-4 small">
                        <i class="fa-solid fa-magnifying-glass fa-2x mb-2 d-block"></i>
                        매장 이름이나 코드를 입력해서 검색하세요.
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp" />

<script>
    // 생년월일 기본값: 오늘 날짜
    window.addEventListener('DOMContentLoaded', function() {
        var today = new Date();
        var yyyy = today.getFullYear();
        var mm = String(today.getMonth() + 1).padStart(2, '0');
        var dd = String(today.getDate()).padStart(2, '0');
        document.getElementById('birth').value = yyyy + '-' + mm + '-' + dd;
    });

    function handleRoleChange(role) {
        var adminNotice = document.getElementById("adminNotice");
        var storeLabel  = document.getElementById("storeLabel");
        var storeWrap   = document.getElementById("storeWrap");
        var storeHint   = document.getElementById("storeHint");

        if (role === 'A') {
            adminNotice.style.display = "block";
            storeWrap.style.display   = "none";
            selectedStores = [];
            document.getElementById('storeIdsHidden').value = "";
            document.getElementById('selectedStoreTags').innerHTML = "";
            document.getElementById('storeWorkDaysContainer').innerHTML = "";
            document.getElementById('storeWorkDaysHidden').value = "";
            document.getElementById('workDaysHidden').value = "";
        } else {
            adminNotice.style.display = "none";
            storeWrap.style.display   = "block";
            storeLabel.innerHTML      = '소속 매장 <span class="text-muted">(선택)</span>';
            storeHint.innerText       = "없으면 나중에 매장 관리에서 신청 가능합니다.";
            storeHint.className       = "text-muted hint-text";
        }
    }

    function autoHyphen(input) {
        var val = input.value.replace(/[^0-9]/g, '');
        if (val.length <= 3) {
            input.value = val;
        } else if (val.length <= 7) {
            input.value = val.slice(0, 3) + '-' + val.slice(3);
        } else {
            input.value = val.slice(0, 3) + '-' + val.slice(3, 7) + '-' + val.slice(7, 11);
        }
    }

    var idChecked = false;

    async function checkDuplicateId() {
        var id = document.getElementById('inputId').value.trim();
        var msgEl = document.getElementById('idCheckMsg');

        if (!id) {
            msgEl.className = 'hint-text text-danger';
            msgEl.innerText = '아이디를 입력해주세요.';
            return;
        }

        try {
            var res  = await fetch('CheckId?id=' + encodeURIComponent(id));
            var data = await res.json();

            if (data.status === 'available') {
                msgEl.className = 'hint-text text-success fw-bold';
                msgEl.innerText = '✓ ' + data.message;
                idChecked = true;
            } else {
                msgEl.className = 'hint-text text-danger fw-bold';
                msgEl.innerText = '✗ ' + data.message;
                idChecked = false;
            }
        } catch (e) {
            msgEl.className = 'hint-text text-danger';
            msgEl.innerText = '서버 오류가 발생했습니다.';
        }
    }

    function resetIdCheck() {
        idChecked = false;
        var msgEl = document.getElementById('idCheckMsg');
        msgEl.innerText = '';
        msgEl.className = 'hint-text';
    }

    var regSearchTimer = null;

    async function searchStoreModal(keyword) {
        clearTimeout(regSearchTimer);
        var resultBox = document.getElementById('modalStoreResult');

        if (!keyword || keyword.trim().length < 1) {
            resultBox.innerHTML = '<div class="text-center text-muted py-4 small"><i class="fa-solid fa-magnifying-glass fa-2x mb-2 d-block"></i>매장 이름이나 코드를 입력해서 검색하세요.</div>';
            return;
        }

        regSearchTimer = setTimeout(async function() {
            try {
                var res  = await fetch('SearchStore?keyword=' + encodeURIComponent(keyword.trim()));
                var data = await res.json();
                resultBox.innerHTML = '';

                if (data.status === 'success' && data.list.length > 0) {
                    var table = document.createElement('table');
                    table.className = 'table table-hover align-middle mb-0';
                    table.innerHTML = '<thead class="table-light"><tr><th>매장 이름</th><th>매장 코드</th><th></th></tr></thead>';
                    var tbody = document.createElement('tbody');

                    data.list.forEach(function(store) {
                        var tr = document.createElement('tr');
                        tr.style.cursor = 'pointer';
                        tr.innerHTML =
                            '<td class="fw-bold">' + store.name + '</td>' +
                            '<td class="text-muted">' + store.id + '</td>' +
                            '<td class="text-end"><button type="button" class="btn btn-sm btn-primary fw-bold">선택</button></td>';
                        tr.onclick = function() { selectStoreReg(store.id, store.name); };
                        tbody.appendChild(tr);
                    });

                    table.appendChild(tbody);
                    resultBox.appendChild(table);
                } else {
                    resultBox.innerHTML = '<div class="text-center text-muted py-4 small"><i class="fa-solid fa-circle-xmark fa-2x mb-2 d-block text-danger"></i>검색 결과가 없습니다.</div>';
                }
            } catch (e) {
                resultBox.innerHTML = '<div class="text-center text-muted py-3 small">검색 중 오류가 발생했습니다.</div>';
            }
        }, 300);
    }

    var selectedStores = [];

    function selectStoreReg(id, name) {
        if (selectedStores.find(function(s) { return s.id === id; })) {
            showToast('이미 선택된 매장입니다.', 'warning');
            return;
        }

        selectedStores.push({id: id, name: name});
        renderStoreTags();

        var modal = bootstrap.Modal.getInstance(document.getElementById('storeSearchModal'));
        if (modal) modal.hide();
        document.getElementById('modalStoreKeyword').value = '';
        document.getElementById('modalStoreResult').innerHTML = '<div class="text-center text-muted py-4 small"><i class="fa-solid fa-magnifying-glass fa-2x mb-2 d-block"></i>매장 이름이나 코드를 입력해서 검색하세요.</div>';
    }

    // 선택된 매장 태그 + 요일 렌더링
    // ※ JSP EL 충돌 방지: 백틱(template literal) 대신 문자열 연결(+) 사용
    function renderStoreTags() {
        var tagContainer  = document.getElementById('selectedStoreTags');
        var daysContainer = document.getElementById('storeWorkDaysContainer');
        tagContainer.innerHTML  = '';
        daysContainer.innerHTML = '';

        var allDays = ['월','화','수','목','금','토','일'];

        selectedStores.forEach(function(store) {
            // 매장 태그
            var tag = document.createElement('span');
            tag.className = 'badge bg-primary d-flex align-items-center gap-1 px-3 py-2';
            tag.style.fontSize = '13px';
            tag.innerHTML = store.name + ' (' + store.id + ')' +
                '<button type="button" class="btn-close btn-close-white ms-1" ' +
                'style="font-size:10px;" onclick="removeStore(\'' + store.id + '\')"></button>';
            tagContainer.appendChild(tag);

            // 요일 선택 섹션
            var section = document.createElement('div');
            section.className = 'p-3 bg-light rounded-3 mb-2';
            section.id = 'days-section-' + store.id;

            // 요일 버튼 HTML (문자열 연결로 생성 - JSP EL 충돌 방지)
            var btnHtml = '';
            allDays.forEach(function(day) {
                var inputId = 'day-' + store.id + '-' + day;
                btnHtml +=
                    '<div class="m-0">' +
                        '<input class="btn-check store-day-check" type="checkbox"' +
                            ' id="' + inputId + '"' +
                            ' value="' + day + '"' +
                            ' data-store="' + store.id + '"' +
                            ' onchange="updateStoreWorkDays()">' +
                        '<label class="btn btn-sm btn-outline-primary fw-bold"' +
                            ' style="width:38px;padding:6px 0;text-align:center;"' +
                            ' for="' + inputId + '">' + day + '</label>' +
                    '</div>';
            });

            section.innerHTML =
                '<div class="d-flex align-items-center justify-content-between mb-2">' +
                    '<small class="fw-bold text-dark">' +
                        '<i class="fa-solid fa-store me-1 text-primary"></i>' +
                        store.name + ' 근무 요일' +
                    '</small>' +
                    '<small class="text-muted">선택 안 하면 협의 후 결정</small>' +
                '</div>' +
                '<div class="d-flex flex-nowrap gap-1">' + btnHtml + '</div>';

            daysContainer.appendChild(section);
        });

        if (selectedStores.length === 0) {
            daysContainer.innerHTML = '';
        }

        document.getElementById('storeIdsHidden').value = selectedStores.map(function(s) { return s.id; }).join(',');
        updateStoreWorkDays();
    }

    // 매장별 요일 → hidden input 업데이트
    // 형식: store01:월,화,수|store02:목,금
    function updateStoreWorkDays() {
        var result = selectedStores.map(function(store) {
            var checked = Array.from(
                document.querySelectorAll('.store-day-check[data-store="' + store.id + '"]:checked')
            ).map(function(c) { return c.value; });
            return store.id + ':' + checked.join(',');
        }).join('|');

        document.getElementById('storeWorkDaysHidden').value = result;

        var firstStore = selectedStores[0];
        if (firstStore) {
            var firstChecked = Array.from(
                document.querySelectorAll('.store-day-check[data-store="' + firstStore.id + '"]:checked')
            ).map(function(c) { return c.value; });
            document.getElementById('workDaysHidden').value = firstChecked.join(',');
        }
    }

    function removeStore(id) {
        selectedStores = selectedStores.filter(function(s) { return s.id !== id; });
        renderStoreTags();
    }

    document.getElementById('storeSearchModal').addEventListener('shown.bs.modal', function() {
        document.getElementById('modalStoreKeyword').focus();
    });

    function checkPwMatch() {
        var pw = document.getElementById("pw").value;
        var pwCheck = document.getElementById("pwCheck").value;
        var msg = document.getElementById("pwMsg");
        if (!pw || !pwCheck) { msg.innerText = ""; return; }
        if (pw === pwCheck) {
            msg.style.color = "#1cc88a";
            msg.innerText = "✓ 비밀번호가 일치합니다.";
        } else {
            msg.style.color = "#e74a3b";
            msg.innerText = "✕ 비밀번호가 일치하지 않습니다.";
        }
    }

    document.getElementById('regForm').addEventListener('submit', function(e) {
        e.preventDefault();

        if (!idChecked) {
            showToast('아이디 중복확인을 해주세요.', 'warning');
            document.getElementById('inputId').focus();
            return;
        }
        var pw = document.getElementById("pw").value;
        var pwCheck = document.getElementById("pwCheck").value;
        if (pw !== pwCheck) {
            showToast('비밀번호가 일치하지 않습니다.', 'danger');
            return;
        }
        
        var formData = new URLSearchParams(new FormData(this));

        var selectedRole = document.querySelector('input[name="role"]:checked');
        if (selectedRole) {
            formData.set('role', selectedRole.value);
        }
        
        fetch('Register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data.status === 'success') {
                alert(data.message);
                location.href = 'login.jsp';
            } else {
                alert(data.message);
            }
        })
        .catch(function(err) { showToast('서버 통신 오류가 발생했습니다.', 'danger'); });
    });
</script>

</body>
</html>

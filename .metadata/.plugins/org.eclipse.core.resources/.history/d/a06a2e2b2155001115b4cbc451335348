<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<footer class="text-center py-5 text-muted bg-white mt-auto border-top no-print">
    <div class="container">
        <p class="mb-1 fw-bold text-dark">AlbaPass</p>
        <small>© 2026 AlbaPass Project. All rights reserved.</small>
    </div>
</footer>

<%-- 공통 토스트 알림 --%>
<div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 9999;">
    <div id="appToast" class="toast align-items-center border-0 shadow" role="alert" aria-live="assertive">
        <div class="d-flex">
            <div class="toast-body fw-bold" id="appToastMsg">
                메시지
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
// 공통 토스트 함수
function showToast(message, type) {
    type = type || 'success'; // success, danger, warning, info
    const toast    = document.getElementById('appToast');
    const toastMsg = document.getElementById('appToastMsg');

    // 색상 설정
    toast.className = 'toast align-items-center border-0 shadow text-white';
    if (type === 'success') toast.classList.add('bg-success');
    else if (type === 'danger')  toast.classList.add('bg-danger');
    else if (type === 'warning') toast.classList.add('bg-warning', 'text-dark');
    else if (type === 'info')    toast.classList.add('bg-primary');

    toastMsg.innerText = message;
    const bsToast = new bootstrap.Toast(toast, { delay: 3000 });
    bsToast.show();
}

// alert 대체 함수 (기존 코드와 호환)
function showAlert(message, type) {
    showToast(message, type || 'info');
}
</script>

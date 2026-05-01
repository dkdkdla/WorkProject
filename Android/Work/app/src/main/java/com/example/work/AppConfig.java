package com.example.work;

/**
 * [설계 설명]
 * 이 클래스는 앱의 모든 네트워크 설정 및 API 엔드포인트를 중앙 집중 관리합니다.
 * 1. BASE_URL: 에뮬레이터 환경(10.0.2.2) 또는 실기기(서버 PC IP) 환경에 맞춰 수정합니다.
 * 2. ENDPOINTS: 이클립스 서블릿(@WebServlet)에 정의된 경로와 1:1로 매핑됩니다.
 */
public class AppConfig {

    // 🚨 서버 접속 기본 주소 (에뮬레이터 전용 IP)
    // 실기기 테스트 시에는 본인 PC의 실제 IP(예: 192.168.0.5)로 수정해야 합니다.
    public static final String BASE_URL = "http://10.0.2.2:8080/Work/";

    // --- [카테고리별 API 엔드포인트 명세] ---

    // 1. 인증 및 계정 (Auth)
    public static final String API_LOGIN = BASE_URL + "Login";
    public static final String API_REGISTER = BASE_URL + "Register";
    public static final String API_FIND_ACCOUNT = BASE_URL + "FindAccount";
    public static final String API_GET_MEMBER_INFO = BASE_URL + "GetMemberInfo";
    public static final String API_MY_PAGE_UPDATE = BASE_URL + "MyPageUpdate";

    // 2. 근무 및 출퇴근 (Work/Attendance)
    public static final String API_ATTENDANCE = BASE_URL + "Attendance";
    public static final String API_QR_CHECK = BASE_URL + "QrCheck";
    public static final String API_MY_ATTENDANCE = BASE_URL + "MyAttendance";
    public static final String API_ADD_WORK = BASE_URL + "AddWork";

    // 3. 매장 관리 (Store)
    public static final String API_STORE_LIST = BASE_URL + "StoreList";
    public static final String API_CHANGE_STORE = BASE_URL + "ChangeStore";
    public static final String API_MY_STORE_ADD = BASE_URL + "MyStoreAdd";
    public static final String API_MY_STORE_DELETE = BASE_URL + "MyStoreDelete";

    // 4. 게시판 (Board)
    public static final String API_BOARD_LIST = BASE_URL + "BoardList";
    public static final String API_BOARD_VIEW = BASE_URL + "BoardView";
    public static final String API_BOARD_WRITE = BASE_URL + "BoardWrite";
    public static final String API_UPLOAD_PATH = BASE_URL + "upload/"; // 첨부파일 경로

    // 5. 관리자 기능 (Admin)
    public static final String API_ADMIN_MEMBER_LIST = BASE_URL + "AdminMemberList";
    public static final String API_ADMIN_MEMBER_UPDATE = BASE_URL + "AdminMemberUpdate";
    public static final String API_ADMIN_ATTENDANCE_ACTION = BASE_URL + "AdminAttendanceAction";
    public static final String API_GET_SALARY_INFO = BASE_URL + "GetSalaryInfo";

}
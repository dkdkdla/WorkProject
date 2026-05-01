package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;

@WebServlet("/StoreManage")
public class StoreManage extends HttpServlet {

    // GET: 매장 생성 페이지로 이동
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        // 점장(A)과 직원(S) 모두 접근 가능
        if (!"A".equals(userRole) && !"S".equals(userRole)) {
            response.sendRedirect("default.jsp");
            return;
        }

        // 내 매장 목록도 같이 전달
        String userId = (String) session.getAttribute("userId");
        MemberDAO dao = new MemberDAO();
        request.setAttribute("myStores", dao.getMyStoreListAll(userId));

        // 점장인 경우 전체 매장 대기 목록 (매장 선택 무관)
        if ("A".equals(userRole)) {
            request.setAttribute("pendingJoins", dao.getPendingJoinsByOwner(userId));
        }
        request.getRequestDispatcher("store_manage.jsp").forward(request, response);
    }

    // POST: 매장 생성 신청
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");
        String userId   = (String) session.getAttribute("userId");

        if (userId == null || (!"A".equals(userRole) && !"S".equals(userRole))) {
            out.print("{\"status\":\"fail\", \"message\":\"권한이 없습니다.\"}");
            return;
        }

        String mode = request.getParameter("mode");
        MemberDAO dao = new MemberDAO();

        // 점장: 직원 소속 신청 승인/거절
        if ("approveJoin".equals(mode) || "rejectJoin".equals(mode)) {
            if (!"A".equals(userRole)) {
                out.print("{\"status\":\"fail\", \"message\":\"권한이 없습니다.\"}");
                return;
            }
            String memId   = request.getParameter("memId");
            String storeId = request.getParameter("storeId"); // 파라미터로 받음
            boolean success = "approveJoin".equals(mode)
                    ? dao.approveJoin(memId, storeId)
                    : dao.rejectJoin(memId, storeId);
            String msg = "approveJoin".equals(mode) ? "승인되었습니다." : "거절되었습니다.";
            out.print("{\"status\":\"" + (success ? "success" : "fail") + "\", \"message\":\"" + msg + "\"}");
            return;
        }

        // 점장: 새 매장 생성 신청
        if ("A".equals(userRole)) {
            String storeId   = request.getParameter("storeId");
            String storeName = request.getParameter("storeName");

            if (storeId == null || storeId.trim().isEmpty() ||
                storeName == null || storeName.trim().isEmpty()) {
                out.print("{\"status\":\"fail\", \"message\":\"매장 코드와 이름을 모두 입력해주세요.\"}");
                return;
            }
            if (dao.isStoreExists(storeId.trim())) {
                out.print("{\"status\":\"fail\", \"message\":\"이미 사용 중인 매장 코드입니다.\"}");
                return;
            }
            boolean success = dao.requestCreateStore(userId, storeId.trim(), storeName.trim());
            if (success) {
                dao.addMyStoreActive(userId, storeId.trim()); // 점장은 ACTIVE로 바로 연결
                out.print("{\"status\":\"success\", \"message\":\"매장 생성 신청이 완료되었습니다. 전체관리자 승인 후 사용 가능합니다.\"}");
            } else {
                out.print("{\"status\":\"fail\", \"message\":\"매장 생성 신청 중 오류가 발생했습니다.\"}");
            }
            return;
        }

        // 직원: 매장 소속 신청 (MyStoreAdd에서 처리하므로 여기선 불필요하지만 안전망)
        out.print("{\"status\":\"fail\", \"message\":\"잘못된 요청입니다.\"}");
    }
}
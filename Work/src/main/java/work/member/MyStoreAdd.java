package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;

@WebServlet("/MyStoreAdd")
public class MyStoreAdd extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession();
        String userId  = (String) session.getAttribute("userId");
        String storeId = request.getParameter("add_store_id");

        // 앱에서 호출 시 세션 없으므로 파라미터로도 받음
        if (userId == null || userId.isEmpty()) {
            userId = request.getParameter("userId");
        }

        if (userId == null || userId.trim().isEmpty()) {
            out.print("{\"status\":\"fail\", \"message\":\"세션이 만료되었습니다. 다시 로그인해주세요.\"}");
            return;
        }
        
        if (storeId == null || storeId.trim().isEmpty()) {
            out.print("{\"status\":\"fail\", \"message\":\"매장 코드를 입력해주세요.\"}");
            return;
        }

        storeId = storeId.trim(); // 공백 제거
        MemberDAO dao = new MemberDAO();

        try {
            // 2. [검사 1] 매장 존재 여부 확인
            if (!dao.isStoreExists(storeId)) {
                out.print("{\"status\":\"fail\", \"message\":\"존재하지 않는 매장 코드입니다. 코드를 다시 확인해주세요.\"}");
                return;
            }

            // 3. [검사 2] 중복 등록 여부 확인 (핵심 포인트!)
            // 이미 내 매장 목록에 있다면 더 이상 진행할 필요가 없습니다.
            if (dao.isAlreadyJoined(userId, storeId)) {
                out.print("{\"status\":\"fail\", \"message\":\"이미 근무지 목록에 등록된 매장입니다.\"}");
                return;
            }

            boolean success = dao.addMyStore(userId, storeId);

            if (success) {
                out.print("{\"status\":\"success\", \"message\":\"소속 신청이 완료되었습니다. 점장 승인 후 매장이 활성화됩니다.\"}");
            } else {
                out.print("{\"status\":\"fail\", \"message\":\"매장 추가 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 에러 메시지 내 따옴표 처리로 JSON 형식을 유지합니다.
            String errMsg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "알 수 없는 서버 오류";
            out.print("{\"status\":\"error\", \"message\":\"서버 오류 발생: " + errMsg + "\"}");
        }
    }
}
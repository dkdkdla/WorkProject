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

@WebServlet("/ChangeStore")
public class ChangeStore extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 인코딩 및 응답 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession();
        String userId = (String)session.getAttribute("userId");
        String userRole = (String)session.getAttribute("userRole"); 
        String newStoreId = request.getParameter("storeId");

        // 1. 세션 체크
        if (userId == null || newStoreId == null) {
            out.print("{\"status\":\"fail\", \"message\":\"세션이 만료되었습니다. 다시 로그인해주세요.\"}");
            return;
        }

        MemberDAO dao = new MemberDAO();

        // 2. [보안 강화] 해당 매장이 실제 존재하며, '내가 등록한 매장'인지 확인
        // 만약 단순히 존재 여부만 체크하고 싶다면 기존처럼 isStoreExists를 유지해도 됩니다.
        if (!dao.isStoreExists(newStoreId)) {
            if ("A".equals(userRole)) {
                out.print("{\"status\":\"fail\", \"message\":\"존재하지 않는 매장ID입니다. 내 정보 수정에서 매장을 먼저 생성해주세요.\"}");
            } else {
                out.print("{\"status\":\"fail\", \"message\":\"존재하지 않는 매장ID입니다. 정확한 코드를 확인해주세요.\"}");
            }
            return;
        }

        // 3. 매장 변경 처리 (DB 업데이트)
        // 이전에 만든 updateStore 메서드를 호출합니다.
        if (dao.updateStore(userId, newStoreId)) {
            // 🚨 4. 세션 정보 갱신 (가장 중요!)
            // 네비바와 각 페이지 상단바는 세션의 userStoreId를 보고 현재 매장을 표시합니다.
            session.setAttribute("userStoreId", newStoreId);
            
            out.print("{\"status\":\"success\", \"message\":\"매장이 [" + newStoreId + "]로 변경되었습니다.\"}");
        } else {
            out.print("{\"status\":\"fail\", \"message\":\"시스템 오류로 매장 변경에 실패했습니다.\"}");
        }
    }
}
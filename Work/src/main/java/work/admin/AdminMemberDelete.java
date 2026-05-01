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

@WebServlet("/AdminMemberDelete")
public class AdminMemberDelete extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 파라미터 및 세션 정보 수신
        String staffId = request.getParameter("id"); // 삭제(제외) 대상의 ID
        HttpSession session = request.getSession();
        
        String loginId = (String) session.getAttribute("userId"); // 🚨 현재 로그인한 나의 ID
        String currentStoreId = (String) session.getAttribute("userStoreId");
        String userRole = (String) session.getAttribute("userRole");

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 2. 권한 및 데이터 유효성 기본 체크
        if (staffId == null || currentStoreId == null || !"A".equals(userRole)) {
            out.println("<script>alert('권한이 없거나 잘못된 접근입니다.'); history.back();</script>");
            return;
        }

        // 🚨 3. [핵심 추가] 관리자 본인 삭제 방지 체크
        // 삭제하려는 대상(staffId)이 현재 로그인한 나(loginId)와 같다면 차단합니다.
        if (staffId.equals(loginId)) {
            out.println("<script>alert('관리자 본인은 현재 매장에서 제외할 수 없습니다.'); history.back();</script>");
            return;
        }

        MemberDAO dao = new MemberDAO();
        
        // 4. 매장과의 연결만 끊는 removeStaffFromStore 호출
        // (tb_my_stores 삭제 + tb_member의 store_id를 NULL로 업데이트)
        boolean success = dao.removeStaffFromStore(staffId, currentStoreId);
        
        if (success) {
            // 삭제 성공 시, 다시 직원 목록 페이지로 이동
            response.sendRedirect("AdminMemberList"); 
        } else {
            out.println("<script>alert('매장 제외 처리 중 오류가 발생했습니다.'); history.back();</script>");
        }
    }
}
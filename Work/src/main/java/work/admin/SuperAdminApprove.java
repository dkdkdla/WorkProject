package work.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;

@WebServlet("/SuperAdminApprove")
public class SuperAdminApprove extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        // SA(전체관리자)만 접근 가능
        if (!"SA".equals(userRole)) {
            response.getWriter().print("{\"status\":\"fail\", \"message\":\"권한이 없습니다.\"}");
            return;
        }

        String mode     = request.getParameter("mode");    // "approve" 또는 "reject"
        String type     = request.getParameter("type");    // "member" 또는 "store"
        String targetId = request.getParameter("targetId"); // memId 또는 storeId

        if (targetId == null || targetId.trim().isEmpty()) {
            response.getWriter().print("{\"status\":\"fail\", \"message\":\"대상 정보가 없습니다.\"}");
            return;
        }

        MemberDAO dao = new MemberDAO();
        boolean success = false;

        if ("approve".equals(mode)) {
            success = "store".equals(type) ? dao.approveStore(targetId) : dao.approveMember(targetId);
        } else if ("reject".equals(mode)) {
            success = "store".equals(type) ? dao.rejectStore(targetId) : dao.rejectMember(targetId);
        }

        if (success) {
            String msg = "approve".equals(mode) ? "승인되었습니다." : "거절되었습니다.";
            response.getWriter().print("{\"status\":\"success\", \"message\":\"" + msg + "\"}");
        } else {
            response.getWriter().print("{\"status\":\"fail\", \"message\":\"처리에 실패했습니다.\"}");
        }
    }
}
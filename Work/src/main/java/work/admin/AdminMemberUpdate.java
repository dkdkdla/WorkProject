package work.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;
import work.dto.MemberDTO;

@WebServlet("/AdminMemberUpdate")
public class AdminMemberUpdate extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 1. 수정할 회원의 데이터를 불러와서 화면(JSP)으로 전달
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userRole = (String)session.getAttribute("userRole");

        // 권한 체크
        if (userRole == null || !"A".equals(userRole)) {
            response.sendRedirect("default.jsp");
            return;
        }

        String id = request.getParameter("id");
        MemberDAO dao = new MemberDAO();
        MemberDTO member = dao.getMember(id);

        if (member != null) {
            request.setAttribute("member", member);
            request.getRequestDispatcher("admin_member_edit.jsp").forward(request, response);
        } else {
            response.sendRedirect("admin_member_list.jsp");
        }
    }

    // 2. 화면에서 넘어온 데이터를 DB에 업데이트
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        String id = request.getParameter("id");
        String wage = request.getParameter("wage");
        // 현재 화면에서 이름과 전화번호는 readonly이므로 시급 위주로 업데이트 설계
        
        MemberDAO dao = new MemberDAO();
        // 🚨 팩트체크: MemberDAO에 updateWage(id, wage) 또는 비슷한 메서드가 있어야 합니다.
        boolean success = dao.updateMemberWage(id, Integer.parseInt(wage));

        response.setContentType("text/html; charset=UTF-8");
        if (success) {
            response.getWriter().println("<script>alert('성공적으로 수정되었습니다.'); location.href='admin_member_list.jsp';</script>");
        } else {
            response.getWriter().println("<script>alert('수정에 실패했습니다.'); history.back();</script>");
        }
    }
}
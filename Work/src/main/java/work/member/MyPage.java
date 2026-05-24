package work.member;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;
import work.dto.MemberDTO;

@WebServlet("/MyPage")
public class MyPage extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userId = (String)session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String storeId = (String)session.getAttribute("userStoreId");

        MemberDAO dao = new MemberDAO();
        MemberDTO dto = dao.getMember(userId);

        // 현재 매장 기준 역할명/시급 조회
        String roleName  = "";
        int    storeWage = 0;
        if (storeId != null && !storeId.isEmpty()) {
            String[] roleInfo = dao.getStoreRoleInfo(userId, storeId);
            roleName  = roleInfo[0];
            try { storeWage = Integer.parseInt(roleInfo[1]); } catch (Exception e) {}
        }

        request.setAttribute("memberDto", dto);
        request.setAttribute("roleName",  roleName);
        request.setAttribute("storeWage", storeWage);

        request.getRequestDispatcher("my_page.jsp").forward(request, response);
    }
}
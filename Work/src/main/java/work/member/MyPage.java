package work.member;

import java.io.IOException;
import java.util.ArrayList;
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

        MemberDAO dao = new MemberDAO();
        // 1. 내 기본 정보 가져오기
        MemberDTO dto = dao.getMember(userId);
        // 2. 내 근무지 목록 가져오기
        ArrayList<String[]> myStores = dao.getMyStoreList(userId);

        // 3. JSP로 데이터 전달
        request.setAttribute("memberDto", dto);
        request.setAttribute("myStores", myStores);

        request.getRequestDispatcher("my_page.jsp").forward(request, response);
    }
}
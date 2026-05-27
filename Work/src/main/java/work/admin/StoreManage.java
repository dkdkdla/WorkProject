package work.admin;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;

@WebServlet("/StoreManage")
public class StoreManage extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userId   = (String) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        // 로그인 체크 (점장·직원 모두 접근 가능)
        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        // 전체관리자(SA)는 접근 불필요
        if ("SA".equals(userRole != null ? userRole.trim() : "")) {
            response.sendRedirect("default.jsp");
            return;
        }

        MemberDAO dao = new MemberDAO();
        // PENDING 포함 전체 매장 목록 조회
        ArrayList<String[]> storeList = dao.getMyStoreListAll(userId);

        request.setAttribute("storeList", storeList);
        request.getRequestDispatcher("store_manage.jsp").forward(request, response);
    }
}
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
import work.dto.MemberDTO;

@WebServlet("/SuperAdminMain")
public class SuperAdminMain extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if (!"SA".equals(userRole)) {
            response.sendRedirect("login.jsp");
            return;
        }

        MemberDAO dao = new MemberDAO();
        request.setAttribute("pendingList", dao.getPendingMembers());
        request.setAttribute("pendingStores", dao.getPendingStores());
        request.getRequestDispatcher("superadmin_main.jsp").forward(request, response);
    }
}
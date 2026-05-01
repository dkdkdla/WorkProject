package work.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/AdminQrManage")
public class AdminQrManage extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        // 1. 보안 체크: 관리자('A') 권한 확인
        String userRole = (String)session.getAttribute("userRole");
        if (userRole == null || !"A".equals(userRole)) {
            response.sendRedirect("default.jsp"); // 권한 없으면 튕김
            return;
        }

        // 2. 매장 선택 여부 확인
        String userStoreId = (String)session.getAttribute("userStoreId");
        if (userStoreId == null || userStoreId.isEmpty()) {
            request.setAttribute("errorMsg", "매장을 먼저 선택해야 QR 코드를 관리할 수 있습니다.");
        }

        // 3. JSP 화면으로 이동
        request.getRequestDispatcher("admin_qr_manage.jsp").forward(request, response);
    }
}
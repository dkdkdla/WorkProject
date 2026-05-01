package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import work.dao.MemberDAO;
import work.dto.MemberDTO;

@WebServlet("/AdminMemberList")
public class AdminMemberList extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        // 1. 매장 ID 파악 (안드로이드는 파라미터로, 웹은 세션으로 보냅니다)
        String storeIdParam = request.getParameter("storeId");
        String storeIdSession = (String) session.getAttribute("userStoreId");
        
        // 최종적으로 사용할 storeId 결정
        String targetStoreId = (storeIdParam != null) ? storeIdParam : storeIdSession;

        // 2. DAO를 통해 데이터 조회
        MemberDAO dao = new MemberDAO();
        ArrayList<MemberDTO> list = dao.getStoreMembers(targetStoreId);

        // 3. [핵심] 응답 방식 결정
        // 안드로이드 앱에서 요청한 경우 (파라미터가 있는 경우) -> JSON 출력
        if (storeIdParam != null) {
            response.setContentType("application/json; charset=UTF-8");
            PrintWriter out = response.getWriter();
            
            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"success\", \"members\":[");
            for (int i = 0; i < list.size(); i++) {
                MemberDTO m = list.get(i);
                sb.append("{\"id\":\"").append(m.getId()).append("\",");
                sb.append("\"name\":\"").append(m.getName()).append("\",");
                sb.append("\"phone\":\"").append(m.getPhone()).append("\",");
                sb.append("\"wage\":").append(m.getHourlyWage()).append("}");
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]}");
            out.print(sb.toString());
            
        } else {
            // 웹 브라우저에서 요청한 경우 (파라미터가 없는 경우) -> JSP로 포워딩
            if (targetStoreId == null) {
                response.sendRedirect("login.jsp"); // 로그인 안 되어 있으면 쫓겨남
                return;
            }
            request.setAttribute("memberList", list);
            request.getRequestDispatcher("admin_member_list.jsp").forward(request, response);
        }
    }
}
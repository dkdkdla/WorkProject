package work.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.AttendanceDAO;

@WebServlet("/AdminAttendanceAction")
public class AdminAttendanceAction extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        // 1. 권한 체크
        String userRole = (String)session.getAttribute("userRole");
        if (!"A".equals(userRole)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String mode = request.getParameter("mode");
        String storeId = (String)session.getAttribute("userStoreId");
        AttendanceDAO dao = new AttendanceDAO();

        // 2. 모드별 처리 (추가, 수정, 삭제)
        try {
            if ("add".equals(mode)) {
                String memId = request.getParameter("memId");
                String type = request.getParameter("type");
                String fullTime = request.getParameter("date") + " " + request.getParameter("time") + ":00";
                dao.insertManualAttendance(memId, storeId, type, fullTime);
                
            } else if ("update".equals(mode)) {
                int idx = Integer.parseInt(request.getParameter("idx"));
                String type = request.getParameter("type");
                String fullTime = request.getParameter("date") + " " + request.getParameter("time") + ":00";
                dao.updateAttendance(idx, fullTime, type);
                
            } else if ("delete".equals(mode)) {
                int idx = Integer.parseInt(request.getParameter("idx"));
                dao.deleteAttendance(idx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 리다이렉트 (원래 보던 검색 조건 유지)
        String rStartDate = request.getParameter("returnStartDate");
        String rEndDate = request.getParameter("returnEndDate");
        String rSearchMemId = request.getParameter("returnSearchMemId");

        response.sendRedirect("admin_attendance.jsp?startDate=" + (rStartDate != null ? rStartDate : "") 
                            + "&endDate=" + (rEndDate != null ? rEndDate : "") 
                            + "&searchMemId=" + (rSearchMemId != null ? rSearchMemId : ""));
    }
}
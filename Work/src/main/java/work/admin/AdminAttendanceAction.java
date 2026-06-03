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
                // 단일 수동 추가 유지
                String memId = request.getParameter("memId");
                String type = request.getParameter("type");
                String fullTime = request.getParameter("date") + " " + request.getParameter("time") + ":00";
                dao.insertManualAttendance(memId, storeId, type, fullTime);
                
            } else if ("update".equals(mode)) {
                // 출퇴근 동시 다중 수정
                String date = request.getParameter("date");
                String inIdx = request.getParameter("inIdx");
                String outIdx = request.getParameter("outIdx");
                String inTime = request.getParameter("inTime");
                String outTime = request.getParameter("outTime");
                
                if (inIdx != null && !inIdx.trim().isEmpty() && inTime != null && !inTime.trim().isEmpty()) {
                    String fullInTime = date + " " + inTime + ":00";
                    dao.updateAttendance(Integer.parseInt(inIdx), fullInTime, "출근");
                }
                
                if (outIdx != null && !outIdx.trim().isEmpty() && outTime != null && !outTime.trim().isEmpty()) {
                    String fullOutTime = date + " " + outTime + ":00";
                    dao.updateAttendance(Integer.parseInt(outIdx), fullOutTime, "퇴근");
                }
                
            } else if ("delete".equals(mode)) {
                // 출퇴근 동시 다중 삭제
                String inIdx = request.getParameter("inIdx");
                String outIdx = request.getParameter("outIdx");
                
                if (inIdx != null && !inIdx.trim().isEmpty()) {
                    dao.deleteAttendance(Integer.parseInt(inIdx));
                }
                if (outIdx != null && !outIdx.trim().isEmpty()) {
                    dao.deleteAttendance(Integer.parseInt(outIdx));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 리다이렉트 (원래 보던 검색 조건 유지)
        String rStartDate = request.getParameter("returnStartDate");
        String rEndDate = request.getParameter("returnEndDate");
        String rSearchMemId = request.getParameter("returnSearchMemId");

        response.sendRedirect("AdminHistory?startDate=" + (rStartDate != null ? rStartDate : "") 
                            + "&endDate=" + (rEndDate != null ? rEndDate : "") 
                            + "&searchMemId=" + (rSearchMemId != null ? rSearchMemId : ""));
    }
}
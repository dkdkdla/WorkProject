package work.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import work.dao.AttendanceDAO;
import work.dto.AttendanceDTO;
import work.dao.MemberDAO;
import work.dto.MemberDTO;

/**
 * [코드 설계 설명]
 * 1. 컨트롤러 역할: 모든 근태 조회 요청(앱/웹)을 여기서 먼저 받습니다.
 * 2. 데이터 준비: DAO를 통해 DB 데이터를 가져와서 바구니(request)에 담습니다.
 * 3. 화면 연결: 앱 요청이면 JSON을 쏘고, 웹 요청이면 admin_attendance.jsp로 토스합니다.
 */
@WebServlet("/AdminHistory")
public class AdminHistory extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String storeId = (String) session.getAttribute("userStoreId");
        
        // 파라미터 수신
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String searchMemId = request.getParameter("searchMemId");
        if (searchMemId == null) searchMemId = "";

        try {
            // 데이터 조회 (기존 JSP에 있던 로직을 이리로 가져옴)
            AttendanceDAO attDao = new AttendanceDAO();
            ArrayList<AttendanceDTO> fullList = attDao.getAttendanceByRange(storeId, startDate, endDate, searchMemId);
            
            MemberDAO memDao = new MemberDAO();
            ArrayList<MemberDTO> memberList = memDao.getStoreMembers(storeId);

            // 앱인지 웹인지 판단
            String isApp = request.getParameter("isApp");

            if ("true".equals(isApp)) {
                // [안드로이드 응답]
                response.setContentType("application/json; charset=UTF-8");
                PrintWriter out = response.getWriter();
                StringBuilder sb = new StringBuilder();
                sb.append("{\"status\":\"success\", \"data\":[");
                if (fullList != null) {
                    for (int i = 0; i < fullList.size(); i++) {
                        AttendanceDTO dto = fullList.get(i);
                        sb.append("{\"idx\":\"").append(dto.getIdx()).append("\",")
                          .append("\"mem_id\":\"").append(dto.getMemberId()).append("\",")
                          .append("\"name\":\"").append(dto.getStoreName()).append("\",")
                          .append("\"type\":\"").append(dto.getAttType()).append("\",")
                          .append("\"time\":\"").append(dto.getAttTime()).append("\"}");
                        if (i < fullList.size() - 1) sb.append(",");
                    }
                }
                sb.append("]}");
                out.print(sb.toString());
            } else {
                // [JSP 웹 응답] 데이터를 request에 담아서 전달
                request.setAttribute("fullList", fullList);
                request.setAttribute("memberList", memberList);
                request.setAttribute("startDate", startDate);
                request.setAttribute("endDate", endDate);
                request.setAttribute("searchMemId", searchMemId);
                
                // 다시 admin_attendance.jsp 화면을 띄워줌
                request.getRequestDispatcher("admin_attendance.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package work.member;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.dao.AttendanceDAO;
import work.dao.MemberDAO;
import work.dto.AttendanceDTO;
import work.dto.MemberDTO;

@WebServlet("/MyAttendance")
public class MyAttendance extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userId = (String)session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 1. 기간 설정
        String searchMonth = request.getParameter("searchMonth");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
        if (searchMonth == null || searchMonth.isEmpty()) {
            searchMonth = sdfMonth.format(new Date());
        }

        // 2. 데이터 조회
        MemberDAO memDao = new MemberDAO();
        MemberDTO myInfo = memDao.getMember(userId);
        int hourlyWage = (myInfo != null) ? myInfo.getHourlyWage() : 0;

        AttendanceDAO attDao = new AttendanceDAO();
        ArrayList<AttendanceDTO> list = attDao.getMyAttendanceList(userId, searchMonth);

        // 3. 🚨 급여 계산 로직 (서블릿으로 이전)
        long totalMinutes = 0;
        Map<String, Date> lastInTimeMap = new HashMap<>();
        ArrayList<AttendanceDTO> calcList = new ArrayList<>(list);
        Collections.reverse(calcList); 

        SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (AttendanceDTO dto : calcList) {
            try {
                if ("출근".equals(dto.getAttType())) {
                    lastInTimeMap.put(dto.getStoreName(), sdfFull.parse(dto.getAttTime()));
                } else if ("퇴근".equals(dto.getAttType())) {
                    Date inTime = lastInTimeMap.get(dto.getStoreName());
                    if (inTime != null) {
                        Date outTime = sdfFull.parse(dto.getAttTime());
                        totalMinutes += (outTime.getTime() - inTime.getTime()) / (1000 * 60);
                        lastInTimeMap.remove(dto.getStoreName());
                    }
                }
            } catch(Exception e){ }
        }

        double recognizedHours = (totalMinutes / 30) * 0.5; // 30분 단위 절삭
        int estimatedSalary = (int)(recognizedHours * hourlyWage);

        // 4. JSP로 결과 전달
        request.setAttribute("searchMonth", searchMonth);
        request.setAttribute("hourlyWage", hourlyWage);
        request.setAttribute("attendanceList", list);
        request.setAttribute("recognizedHours", recognizedHours);
        request.setAttribute("estimatedSalary", estimatedSalary);

        request.getRequestDispatcher("my_attendance.jsp").forward(request, response);
    }
}
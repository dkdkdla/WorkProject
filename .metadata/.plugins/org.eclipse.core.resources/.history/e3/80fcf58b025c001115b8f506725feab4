package work.admin;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/AdminStats")
public class AdminStats extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String storeId = (String)session.getAttribute("userStoreId");
        String userRole = (String)session.getAttribute("userRole");

        if(storeId == null || !"A".equals(userRole)) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 1. 기간 설정 로직
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        if(endDate == null || endDate.isEmpty()) endDate = sdf.format(new java.util.Date());
        if(startDate == null || startDate.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -6);
            startDate = sdf.format(cal.getTime());
        }

        // 2. 데이터 조회
        int totalEmployees = 0;
        int todayAttendance = 0;
        int periodTotal = 0;
        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        try (Connection conn = DriverManager.getConnection(dbUrl, "WorkUser", "pass")) {
            // 총 직원 수
            String sql1 = "SELECT COUNT(*) FROM tb_member WHERE store_id = ? AND mem_role != 'A'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                pstmt.setString(1, storeId);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()) totalEmployees = rs.getInt(1);
            }

            // 오늘 출근 인원
            String sql2 = "SELECT COUNT(DISTINCT mem_id) FROM tb_attendance WHERE store_id = ? AND att_type = '출근' AND CAST(att_time AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                pstmt.setString(1, storeId);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()) todayAttendance = rs.getInt(1);
            }

            // 차트 데이터 (기간 내 통계)
            String sql3 = "SELECT FORMAT(att_time, 'MM-dd') as work_date, COUNT(DISTINCT mem_id) as cnt FROM tb_attendance " +
                          "WHERE store_id = ? AND att_type = '출근' AND CAST(att_time AS DATE) BETWEEN ? AND ? " +
                          "GROUP BY FORMAT(att_time, 'MM-dd') ORDER BY work_date ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql3)) {
                pstmt.setString(1, storeId);
                pstmt.setString(2, startDate);
                pstmt.setString(3, endDate);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    labels.add("'" + rs.getString("work_date") + "'");
                    int cnt = rs.getInt("cnt");
                    counts.add(cnt);
                    periodTotal += cnt;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 3. JSP로 데이터 전달
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("totalEmployees", totalEmployees);
        request.setAttribute("todayAttendance", todayAttendance);
        request.setAttribute("periodTotal", periodTotal);
        request.setAttribute("chartLabels", labels.toString());
        request.setAttribute("chartData", counts.toString());

        request.getRequestDispatcher("admin_stats.jsp").forward(request, response);
    }
}
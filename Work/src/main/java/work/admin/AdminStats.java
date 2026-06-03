package work.admin;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import work.util.DBConn;

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

        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if(endDate == null || endDate.isEmpty()) endDate = sdf.format(new java.util.Date());
        if(startDate == null || startDate.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -6);
            startDate = sdf.format(cal.getTime());
        }

        int totalEmployees = 0, todayAttendance = 0, periodTotal = 0;
        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        try (Connection conn = DBConn.getConnection()) {
            // 총 직원 수 - tb_my_stores 기준으로 변경
            String sql1 = "SELECT COUNT(*) FROM tb_my_stores ms " +
                          "JOIN tb_member m ON ms.mem_id = m.mem_id " +
                          "WHERE ms.store_id = ? AND ms.join_status = 'ACTIVE' AND m.mem_role = 'S'";
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, storeId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) totalEmployees = rs.getInt(1);
            }

            String sql2 = "SELECT COUNT(DISTINCT mem_id) FROM tb_attendance " +
                          "WHERE store_id = ? AND att_type IN ('출근','IN') " +
                          "AND CAST(att_time AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setString(1, storeId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) todayAttendance = rs.getInt(1);
            }

            String sql3 = "SELECT FORMAT(att_time,'MM-dd') as work_date, COUNT(DISTINCT mem_id) as cnt " +
                          "FROM tb_attendance WHERE store_id = ? AND att_type IN ('출근','IN') " +
                          "AND CAST(att_time AS DATE) BETWEEN ? AND ? " +
                          "GROUP BY FORMAT(att_time,'MM-dd') ORDER BY work_date ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql3)) {
                ps.setString(1, storeId); ps.setString(2, startDate); ps.setString(3, endDate);
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    labels.add("'" + rs.getString("work_date") + "'");
                    int cnt = rs.getInt("cnt");
                    counts.add(cnt);
                    periodTotal += cnt;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

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
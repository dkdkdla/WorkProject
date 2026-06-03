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
        String storeId  = (String)session.getAttribute("userStoreId");
        String userRole = (String)session.getAttribute("userRole");

        if (storeId == null || !"A".equals(userRole)) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 기간 설정 (기본: 이번 달 1일 ~ 오늘)
        String startDate = request.getParameter("startDate");
        String endDate   = request.getParameter("endDate");
        java.time.LocalDate today = java.time.LocalDate.now();
        if (startDate == null || startDate.isEmpty())
            startDate = today.withDayOfMonth(1).toString();
        if (endDate == null || endDate.isEmpty())
            endDate = today.toString();

        int totalEmployees   = 0;
        int todayAttendance  = 0;
        int periodTotal      = 0;
        int totalPayEstimate = 0;

        List<String>  chartLabels = new ArrayList<>();
        List<Integer> chartData   = new ArrayList<>();

        // 직원별 근무시간 랭킹 [이름, 시간(분)]
        List<String[]> empRanking = new ArrayList<>();

        try (Connection conn = DBConn.getConnection()) {

            // 1. 총 직원 수 (tb_my_stores 기반)
            String sql1 = "SELECT COUNT(*) FROM tb_my_stores ms " +
                          "JOIN tb_member m ON ms.mem_id = m.mem_id " +
                          "WHERE ms.store_id = ? AND ms.join_status = 'ACTIVE' AND m.mem_role = 'S'";
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, storeId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) totalEmployees = rs.getInt(1);
            }

            // 2. 오늘 출근 인원 (IN/출근 모두 체크)
            String sql2 = "SELECT COUNT(DISTINCT mem_id) FROM tb_attendance " +
                          "WHERE store_id = ? AND (att_type = 'IN' OR att_type = '출근') " +
                          "AND CAST(att_time AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setString(1, storeId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) todayAttendance = rs.getInt(1);
            }

            // 3. 기간 내 일별 출근 인원 차트
            String sql3 = "SELECT FORMAT(att_time,'MM-dd') as d, COUNT(DISTINCT mem_id) as cnt " +
                          "FROM tb_attendance " +
                          "WHERE store_id = ? AND (att_type='IN' OR att_type='출근') " +
                          "AND CAST(att_time AS DATE) BETWEEN ? AND ? " +
                          "GROUP BY FORMAT(att_time,'MM-dd') ORDER BY d ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql3)) {
                ps.setString(1, storeId);
                ps.setString(2, startDate);
                ps.setString(3, endDate);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    chartLabels.add("'" + rs.getString("d") + "'");
                    int cnt = rs.getInt("cnt");
                    chartData.add(cnt);
                    periodTotal += cnt;
                }
            }

            // 4. 직원별 총 근무 분 랭킹 (IN/OUT 쌍 매칭)
            String sql4 = "SELECT m.mem_name, " +
                          "SUM(DATEDIFF(MINUTE, i.att_time, o.att_time)) as total_min " +
                          "FROM tb_attendance i " +
                          "JOIN tb_attendance o ON i.mem_id = o.mem_id AND i.store_id = o.store_id " +
                          "   AND (o.att_type='OUT' OR o.att_type='퇴근') " +
                          "   AND CAST(i.att_time AS DATE) = CAST(o.att_time AS DATE) " +
                          "JOIN tb_member m ON i.mem_id = m.mem_id " +
                          "WHERE i.store_id = ? AND (i.att_type='IN' OR i.att_type='출근') " +
                          "AND CAST(i.att_time AS DATE) BETWEEN ? AND ? " +
                          "GROUP BY m.mem_name ORDER BY total_min DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql4)) {
                ps.setString(1, storeId);
                ps.setString(2, startDate);
                ps.setString(3, endDate);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int mins = rs.getInt("total_min");
                    int h = mins / 60, m = mins % 60;
                    empRanking.add(new String[]{
                        rs.getString("mem_name"),
                        String.format("%dh %dm", h, m),
                        String.valueOf(mins)
                    });
                }
            }

            // 5. 기간 내 예상 총 급여 (기본 시급 × 근무시간 합산)
            String sql5 = "SELECT SUM(DATEDIFF(MINUTE, i.att_time, o.att_time) / 60.0 * ms.hourly_wage) " +
                          "FROM tb_attendance i " +
                          "JOIN tb_attendance o ON i.mem_id = o.mem_id AND i.store_id = o.store_id " +
                          "   AND (o.att_type='OUT' OR o.att_type='퇴근') " +
                          "   AND CAST(i.att_time AS DATE) = CAST(o.att_time AS DATE) " +
                          "JOIN (SELECT ms2.mem_id, m2.hourly_wage FROM tb_my_stores ms2 " +
                          "      JOIN tb_member m2 ON ms2.mem_id = m2.mem_id " +
                          "      WHERE ms2.store_id = ?) ms ON i.mem_id = ms.mem_id " +
                          "WHERE i.store_id = ? AND (i.att_type='IN' OR i.att_type='출근') " +
                          "AND CAST(i.att_time AS DATE) BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(sql5)) {
                ps.setString(1, storeId);
                ps.setString(2, storeId);
                ps.setString(3, startDate);
                ps.setString(4, endDate);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) totalPayEstimate = (int)rs.getDouble(1);
            }

        } catch (Exception e) { e.printStackTrace(); }

        request.setAttribute("startDate",       startDate);
        request.setAttribute("endDate",         endDate);
        request.setAttribute("totalEmployees",  totalEmployees);
        request.setAttribute("todayAttendance", todayAttendance);
        request.setAttribute("periodTotal",     periodTotal);
        request.setAttribute("totalPayEstimate",totalPayEstimate);
        request.setAttribute("chartLabels",     chartLabels.toString());
        request.setAttribute("chartData",       chartData.toString());
        request.setAttribute("empRanking",      empRanking);

        request.getRequestDispatcher("admin_stats.jsp").forward(request, response);
    }
}
package work.attendance;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import work.util.DBConn;

@WebServlet("/Attendance")
public class Attendance extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String userId  = (String) request.getSession().getAttribute("userId");
        String storeId = request.getParameter("storeId");
        String type    = request.getParameter("type"); // "in" or "out"

        if (userId == null || storeId == null || type == null) {
            out.print("{\"status\":\"fail\", \"message\":\"필수 정보가 누락되었습니다.\"}");
            return;
        }

        String attType = type.equalsIgnoreCase("in") ? "IN" : "OUT";

        try {
            // 출근 시 tb_my_stores에서 role_id 조회
            Integer roleId = null;
            if ("IN".equals(attType)) {
                String roleSql = "SELECT role_id FROM tb_my_stores " +
                                 "WHERE mem_id = ? AND store_id = ? AND join_status = 'ACTIVE'";
                try (Connection conn = DBConn.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(roleSql)) {
                    pstmt.setString(1, userId);
                    pstmt.setString(2, storeId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int rid = rs.getInt("role_id");
                            if (!rs.wasNull()) roleId = rid;
                        }
                    }
                }
            }

            // tb_attendance에 저장 (role_id 포함)
            String sql = "INSERT INTO tb_attendance (mem_id, store_id, att_type, att_time, role_id) " +
                         "VALUES (?, ?, ?, GETDATE(), ?)";
            try (Connection conn = DBConn.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, storeId);
                pstmt.setString(3, attType);
                if (roleId != null) {
                    pstmt.setInt(4, roleId);
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER);
                }
                pstmt.executeUpdate();
            }

            String msg = "IN".equals(attType) ? "출근 처리가 완료되었습니다." : "퇴근 처리가 완료되었습니다.";
            out.print("{\"status\":\"success\", \"message\":\"" + msg + "\"}");

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
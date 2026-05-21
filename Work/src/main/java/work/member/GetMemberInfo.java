package work.member;

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

@WebServlet("/GetMemberInfo")
public class GetMemberInfo extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String id = request.getParameter("id");
        if (id == null || id.isEmpty()) {
            out.print("{\"status\":\"fail\", \"message\":\"ID가 누락되었습니다.\"}");
            return;
        }

        String sql = "SELECT mem_name, mem_phone, hourly_wage, store_id FROM tb_member WHERE mem_id = ?";

        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name    = rs.getString("mem_name");
                    String phone   = rs.getString("mem_phone");
                    int    wage    = rs.getInt("hourly_wage");
                    String storeId = rs.getString("store_id");

                    out.print("{");
                    out.print("\"status\":\"success\",");
                    out.print("\"name\":\"" + (name   != null ? name   : "") + "\",");
                    out.print("\"phone\":\"" + (phone  != null ? phone  : "") + "\",");
                    out.print("\"wage\":\"" + wage + "\",");
                    out.print("\"storeId\":\"" + (storeId != null ? storeId : "") + "\"");
                    out.print("}");
                } else {
                    out.print("{\"status\":\"fail\", \"message\":\"정보를 찾을 수 없습니다.\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"서버 오류가 발생했습니다.\"}");
        }
    }
}
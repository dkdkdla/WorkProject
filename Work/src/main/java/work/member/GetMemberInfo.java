package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetMemberInfo")
public class GetMemberInfo extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 요청 및 응답 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 2. 파라미터 수신
        String id = request.getParameter("id");
        if (id == null || id.isEmpty()) {
            out.print("{\"status\":\"fail\", \"message\":\"ID가 누락되었습니다.\"}");
            return;
        }

        // 3. DB 정보 설정
        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
        String dbId = "WorkUser";
        String dbPw = "pass";

        String sql = "SELECT mem_name, mem_phone, hourly_wage, store_id FROM tb_member WHERE mem_id = ?";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DriverManager.getConnection(dbUrl, dbId, dbPw);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("mem_name");
                        String phone = rs.getString("mem_phone");
                        int wage = rs.getInt("hourly_wage");
                        String storeId = rs.getString("store_id");

                        // 🚨 팩트체크: JSON 문자열을 수동으로 조립하여 반환한다.
                        out.print("{");
                        out.print("\"status\":\"success\", ");
                        out.print("\"name\":\"" + name + "\", ");
                        out.print("\"phone\":\"" + phone + "\", ");
                        out.print("\"wage\":\"" + wage + "\", ");
                        out.print("\"storeId\":\"" + storeId + "\"");
                        out.print("}");
                    } else {
                        out.print("{\"status\":\"fail\", \"message\":\"정보를 찾을 수 없습니다.\"}");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
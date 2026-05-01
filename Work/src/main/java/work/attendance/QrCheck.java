package work.attendance;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;

@WebServlet("/QrCheck")
public class QrCheck extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession();
        String userId = (String)session.getAttribute("userId");
        String storeId = request.getParameter("storeId"); // qr_check.jsp에서 보낸 매장ID
        String type = request.getParameter("type");       // 🚨 핵심: 'IN' 또는 'OUT'

        if (userId == null || storeId == null || type == null) {
            out.print("{\"status\":\"fail\", \"message\":\"필수 정보가 누락되었습니다.\"}");
            return;
        }

        MemberDAO dao = new MemberDAO();
        // 🚨 DAO 메서드에 type(출근/퇴근 구분)을 함께 전달한다.
        boolean result = dao.insertAttendance(userId, storeId, type);

        if (result) {
            String msg = type.equals("IN") ? "출근 처리가 완료되었습니다." : "퇴근 처리가 완료되었습니다.";
            out.print("{\"status\":\"success\", \"message\":\"" + msg + "\"}");
        } else {
            out.print("{\"status\":\"fail\", \"message\":\"DB 기록 중 오류가 발생했습니다.\"}");
        }
    }
}
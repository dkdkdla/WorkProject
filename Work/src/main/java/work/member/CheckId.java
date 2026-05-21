package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import work.dao.MemberDAO;

@WebServlet("/CheckId")
public class CheckId extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String id = request.getParameter("id");

        if (id == null || id.trim().isEmpty()) {
            out.print("{\"status\":\"fail\", \"message\":\"아이디를 입력해주세요.\"}");
            return;
        }

        MemberDAO dao = new MemberDAO();

        if (dao.getMember(id.trim()) != null) {
            out.print("{\"status\":\"duplicate\", \"message\":\"이미 사용 중인 아이디입니다.\"}");
        } else {
            out.print("{\"status\":\"available\", \"message\":\"사용 가능한 아이디입니다.\"}");
        }
    }
}
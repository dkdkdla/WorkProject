package work.member;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Logout")
public class Logout extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 세션 가져오기 (false: 세션이 없으면 새로 만들지 마라)
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // 2. 세션 무효화 (안에 든 모든 데이터 삭제)
            session.invalidate(); 
            System.out.println(">>> 로그아웃 처리 완료");
        }

        // 3. 메인 또는 로그인 페이지로 이동
        // 프로젝트 경로를 포함하여 정확하게 리다이렉트 한다.
        response.sendRedirect(request.getContextPath() + "/default.jsp"); 
    }

    // 버튼이 폼(POST) 형태일 경우를 대비해 doPost도 doGet을 호출하게 한다.
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
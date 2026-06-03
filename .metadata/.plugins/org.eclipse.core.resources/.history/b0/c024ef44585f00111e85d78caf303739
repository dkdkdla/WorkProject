package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;
import work.dto.MemberDTO;

@WebServlet("/Login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String id = request.getParameter("id");
        String pw = request.getParameter("pw");

        // 1. 입력받은 비밀번호 SHA-256 암호화
        String encryptedPw = "";
        try {
            if (pw != null && !pw.isEmpty()) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(pw.getBytes("UTF-8"));
                byte[] byteData = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : byteData) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
                encryptedPw = sb.toString();
            }
        } catch(Exception e) {
            e.printStackTrace();
            encryptedPw = ""; 
        }

        // 2. DAO를 통해 회원 정보 조회
        MemberDAO dao = new MemberDAO();
        MemberDTO user = dao.getMember(id);

        // 3. 로그인 검증 및 세션 처리
        if (user != null && encryptedPw.equals(user.getPw())) {

            // 🚨 PENDING 상태 체크 (승인 대기 중인 점장)
            MemberDAO statusDao = new MemberDAO();
            String status = statusDao.getMemberStatus(user.getId());

            if ("PENDING".equals(status)) {
                out.print("{\"status\":\"fail\", \"message\":\"가입 승인 대기 중입니다. 전체관리자의 승인을 기다려주세요.\"}");
                return;
            }
            if ("REJECTED".equals(status)) {
                out.print("{\"status\":\"fail\", \"message\":\"가입이 거절되었습니다. 관리자에게 문의해주세요.\"}");
                return;
            }

            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userStoreId", user.getStoreId());

            // SA(전체관리자)는 별도 페이지로 이동
            String redirectPage = "SA".equals(user.getRole()) ? "superadmin_main.jsp" : "default.jsp";

            out.print("{\"status\":\"success\""
                + ", \"message\":\"환영합니다, " + user.getName() + "님!\""
                + ", \"role\":\"" + user.getRole() + "\""
                + ", \"name\":\"" + user.getName() + "\""
                + ", \"id\":\"" + user.getId() + "\""
                + ", \"storeId\":\"" + (user.getStoreId() != null ? user.getStoreId() : "") + "\""
                + ", \"redirect\":\"" + redirectPage + "\"}");
        } else {
            out.print("{\"status\":\"fail\", \"message\":\"아이디 또는 비밀번호가 일치하지 않습니다.\"}");
        }
    }
}
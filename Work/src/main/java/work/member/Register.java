package work.member;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import work.dao.MemberDAO;
import work.dto.MemberDTO;

@WebServlet("/Register")
public class Register extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String id = request.getParameter("id");
        String pw = request.getParameter("pw");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role"); 
        String storeId = request.getParameter("storeId");
        String wageStr = request.getParameter("wage");

        if (id == null) id = "";
        if (phone == null) phone = "";
        if (name == null) name = "";
        if (storeId != null) storeId = storeId.trim();

        int wage = 0;
        try {
            if (wageStr != null && !wageStr.trim().isEmpty()) {
                wage = Integer.parseInt(wageStr.trim());
            }
        } catch (NumberFormatException e) { wage = 0; }

        MemberDAO dao = new MemberDAO();

        // 1. 공통 필수 체크
        if (id.isEmpty() || pw == null || pw.isEmpty()) {
            out.print("{\"status\":\"fail\", \"message\":\"아이디와 비밀번호를 입력해주세요.\"}");
            return;
        }

        // 역할별 체크
        if ("S".equals(role)) {
            // 직원은 storeId 없이도 가입 가능 (나중에 매장 관리에서 소속 신청)
            if (storeId != null && !storeId.trim().isEmpty()) {
                if (!dao.isStoreExists(storeId)) {
                    out.print("{\"status\":\"fail\", \"message\":\"존재하지 않는 매장 ID입니다. 관리자에게 확인해주세요.\"}");
                    return;
                }
            }
        }
        // 점장은 adminCode 없이 가입 후 PENDING 상태로 저장됨

        // 2. 비밀번호 암호화
        String encryptedPw = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(pw.getBytes("UTF-8"));
            byte[] byteData = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : byteData) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            encryptedPw = sb.toString();
        } catch(Exception e) { encryptedPw = pw; }

        // 3. DTO 세팅
        MemberDTO dto = new MemberDTO();
        dto.setId(id);
        dto.setPw(encryptedPw);
        dto.setName(name);
        dto.setPhone(phone);
        dto.setRole(role);
        dto.setStoreId(storeId);
        dto.setHourlyWage(wage);

        // 4. 아이디 중복 체크
        if (dao.getMember(id) != null) {
            out.print("{\"status\":\"fail\", \"message\":\"이미 사용 중인 아이디입니다.\"}");
            return;
        }

        // 5. 최종 가입
        boolean success = dao.registerMember(dto, storeId);

        if (success) {
            // 점장은 승인 대기 안내, 직원은 바로 완료
            if ("A".equals(role)) {
                out.print("{\"status\":\"success\", \"message\":\"가입 신청이 완료되었습니다! 전체관리자 승인 후 로그인 가능합니다.\"}");
            } else {
                out.print("{\"status\":\"success\", \"message\":\"회원가입이 완료되었습니다!\"}");
            }
        } else {
            out.print("{\"status\":\"fail\", \"message\":\"가입 처리 중 오류가 발생했습니다. DB 정보를 확인하세요.\"}");
        }
    }
}
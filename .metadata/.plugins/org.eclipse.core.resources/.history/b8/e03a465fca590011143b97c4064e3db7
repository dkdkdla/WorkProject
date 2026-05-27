package work.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import work.dao.MemberDAO;
import work.dto.MemberDTO;
import work.util.DBConn;

@WebServlet("/StoreMemberManage")
public class StoreMemberManage extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");
        String userId   = (String) session.getAttribute("userId");
        String sessionStoreId = (String) session.getAttribute("userStoreId");

        if (!"A".equals(userRole != null ? userRole.trim() : "")) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 파라미터로 매장 선택 시 해당 매장으로 변경
        String selectedStoreId = request.getParameter("storeId");
        if (selectedStoreId != null && !selectedStoreId.isEmpty()) {
            sessionStoreId = selectedStoreId;
            session.setAttribute("userStoreId", selectedStoreId);
        }

        // 점장의 매장 목록 조회
        ArrayList<String[]> myStores = getMyStores(userId);

        // 선택된 매장 직원 목록 조회
        MemberDAO dao = new MemberDAO();
        ArrayList<MemberDTO> memberList = new ArrayList<>();
        if (sessionStoreId != null && !sessionStoreId.isEmpty()) {
            memberList = dao.getStoreMembers(sessionStoreId);
        }

        // 소속 신청 대기 목록
        ArrayList<String[]> pendingList = dao.getPendingJoinsByOwner(userId);

        request.setAttribute("myStores",    myStores);
        request.setAttribute("memberList",  memberList);
        request.setAttribute("pendingList", pendingList);
        request.getRequestDispatcher("store_member_manage.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (!"A".equals(userRole != null ? userRole.trim() : "")) {
            out.print("{\"status\":\"fail\", \"message\":\"권한이 없습니다.\"}");
            return;
        }

        String action = request.getParameter("action");

        // 소속 신청 승인
        if ("approve".equals(action)) {
            String memId   = request.getParameter("memId");
            String storeId = request.getParameter("storeId");
            try {
                MemberDAO dao = new MemberDAO();
                dao.approveJoin(memId, storeId);
                out.print("{\"status\":\"success\", \"message\":\"승인되었습니다.\"}");
            } catch (Exception e) {
                e.printStackTrace();
                out.print("{\"status\":\"error\", \"message\":\"서버 오류가 발생했습니다.\"}");
            }
        }
        // 소속 신청 거절
        else if ("reject".equals(action)) {
            String memId   = request.getParameter("memId");
            String storeId = request.getParameter("storeId");
            try {
                MemberDAO dao = new MemberDAO();
                dao.rejectJoin(memId, storeId);
                out.print("{\"status\":\"success\", \"message\":\"거절되었습니다.\"}");
            } catch (Exception e) {
                e.printStackTrace();
                out.print("{\"status\":\"error\", \"message\":\"서버 오류가 발생했습니다.\"}");
            }
        }
        // 새 매장 생성 신청
        else if ("createStore".equals(action)) {
            String storeId   = request.getParameter("storeId");
            String storeName = request.getParameter("storeName");
            try {
                MemberDAO dao = new MemberDAO();
                boolean success = dao.requestCreateStore(userId, storeId.trim(), storeName.trim());
                if (success) {
                    dao.addMyStoreActive(userId, storeId.trim());
                    out.print("{\"status\":\"success\", \"message\":\"매장 생성 신청이 완료되었습니다. 전체관리자 승인 후 활성화됩니다.\"}");
                } else {
                    out.print("{\"status\":\"fail\", \"message\":\"이미 존재하는 매장 코드입니다.\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                out.print("{\"status\":\"error\", \"message\":\"서버 오류가 발생했습니다.\"}");
            }
        } else {
            out.print("{\"status\":\"fail\", \"message\":\"알 수 없는 요청입니다.\"}");
        }
    }

    // 점장의 ACTIVE 매장 목록
    private ArrayList<String[]> getMyStores(String userId) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT s.store_id, s.store_name " +
                     "FROM tb_store s " +
                     "JOIN tb_my_stores m ON s.store_id = m.store_id " +
                     "WHERE m.mem_id = ? AND m.join_status = 'ACTIVE' AND s.store_status = 'ACTIVE' " +
                     "ORDER BY s.store_name ASC";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{rs.getString("store_id"), rs.getString("store_name")});
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}

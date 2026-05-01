<%@ page language="java" contentType="application/vnd.ms-excel; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%
    
    String storeId = request.getParameter("storeId");
    String year = request.getParameter("year");
    String month = request.getParameter("month");

    
    String fileName = "salary_" + year + "_" + month + ".xls";
    
    
    String userAgent = request.getHeader("User-Agent");
    if (userAgent != null && userAgent.indexOf("MSIE") > -1) {
        fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
    } else {
        fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
    }
    
    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    response.setHeader("Content-Description", "JSP Generated Data");
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>
    table { border-collapse: collapse; width: 100%; }
    th { background-color: #f0f0f0; border: 1px solid #000; padding: 10px; font-weight: bold; }
    td { border: 1px solid #000; padding: 10px; text-align: center; }
    .money { text-align: right; }
</style>
</head>
<body>
    <h2><%=year%>년 <%=month%>월 급여 정산 내역</h2>
    <table>
        <thead>
            <tr>
                <th>이름 (아이디)</th>
                <th>총 근무시간</th>
                <th>시급</th>
                <th>총 급여</th>
            </tr>
        </thead>
        <tbody>
<%
    
    String startDate = year + "-" + (month.length()==1 ? "0"+month : month) + "-01 00:00:00";
    int nextMonth = Integer.parseInt(month) + 1;
    String nextYear = year;
    if(nextMonth > 12) { nextMonth = 1; nextYear = String.valueOf(Integer.parseInt(year)+1); }
    String endDate = nextYear + "-" + (nextMonth<10 ? "0"+nextMonth : nextMonth) + "-01 00:00:00";

    String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Work;encrypt=false";
    String dbId = "WorkUser";
    String dbPw = "pass";

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        conn = DriverManager.getConnection(dbUrl, dbId, dbPw);

        
        String memSql = "SELECT mem_id, mem_name, hourly_wage FROM tb_member WHERE store_id = ?";
        pstmt = conn.prepareStatement(memSql);
        pstmt.setString(1, storeId);
        rs = pstmt.executeQuery();

        ArrayList<Map<String, String>> members = new ArrayList<>();
        while(rs.next()) {
            Map<String, String> m = new HashMap<>();
            m.put("id", rs.getString("mem_id"));
            m.put("name", rs.getString("mem_name"));
            m.put("wage", rs.getString("hourly_wage"));
            members.add(m);
        }
        rs.close(); pstmt.close();

        
        for(Map<String, String> member : members) {
            String memId = member.get("id");
            String name = member.get("name");
            int wage = 9860;
            try { wage = Integer.parseInt(member.get("wage")); } catch(Exception e){}

            String attSql = "SELECT att_type, att_time FROM tb_attendance " +
                            "WHERE mem_id = ? AND store_id = ? " +
                            "AND att_time >= ? AND att_time < ? " +
                            "ORDER BY att_time ASC";
            
            pstmt = conn.prepareStatement(attSql);
            pstmt.setString(1, memId);
            pstmt.setString(2, storeId);
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);
            rs = pstmt.executeQuery();

            long totalMin = 0;
            java.util.Date lastIn = null;

            while(rs.next()) {
                String type = rs.getString("att_type");
                Timestamp ts = rs.getTimestamp("att_time");
                java.util.Date curTime = new java.util.Date(ts.getTime());

                if("출근".equals(type)) {
                    lastIn = curTime;
                } else if("퇴근".equals(type) && lastIn != null) {
                    long diff = curTime.getTime() - lastIn.getTime();
                    totalMin += (diff / (1000 * 60)); 
                    lastIn = null;
                }
            }
            rs.close(); pstmt.close();

            if(totalMin > 0) {
                long totalPay = (long)((totalMin / 60.0) * wage);
%>
            <tr>
                <td><%=name%> (<%=memId%>)</td>
                <td><%=totalMin/60%>시간 <%=totalMin%60%>분</td>
                <td><%=String.format("%,d", wage)%></td>
                <td class="money"><%=String.format("%,d", totalPay)%>원</td>
            </tr>
<%
            }
        }

    } catch(Exception e) {
        e.printStackTrace();
%>
        <tr><td colspan="4">에러 발생: <%=e.getMessage()%></td></tr>
<%
    } finally {
        if(rs!=null) try{ rs.close(); } catch(Exception e){}
        if(pstmt!=null) try{ pstmt.close(); } catch(Exception e){}
        if(conn!=null) try{ conn.close(); } catch(Exception e){}
    }
%>
        </tbody>
    </table>
</body>
</html>
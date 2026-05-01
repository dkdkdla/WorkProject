/**
 * 파일명 : AttendanceDAO.java
 * 작성일 : 2025. 12. 15.
 * 설명 :
 */
package work.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import work.util.DBConn;

/**
 * @author user
 *
 */
public class AttendanceDAO {

    
    public String checkInOut(String memId, String storeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String type = "출근"; 

        try {
            conn = DBConn.getConnection();

            
            
            String sqlCheck = "SELECT TOP 1 att_type FROM tb_attendance WHERE mem_id = ? ORDER BY idx DESC";
            
            pstmt = conn.prepareStatement(sqlCheck);
            pstmt.setString(1, memId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String lastType = rs.getString("att_type");
                
                if ("출근".equals(lastType)) {
                    type = "퇴근";
                }
                
            }
            
            rs.close();
            pstmt.close();

            
            String sqlInsert = "INSERT INTO tb_attendance (mem_id, store_id, att_type) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sqlInsert);
            pstmt.setString(1, memId);
            pstmt.setString(2, storeId);
            pstmt.setString(3, type);
            
            int result = pstmt.executeUpdate();
            
            if(result > 0) return type; 
            else return null; 

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConn.close(conn);
        }
    }
    public java.util.ArrayList<work.dto.AttendanceDTO> getMyList(String memId) {
        java.util.ArrayList<work.dto.AttendanceDTO> list = new java.util.ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        
        String sql = "SELECT a.att_type, a.att_time, s.store_name " +
                     "FROM tb_attendance a " +
                     "JOIN tb_store s ON a.store_id = s.store_id " +
                     "WHERE a.mem_id = ? " +
                     "ORDER BY a.idx DESC"; 
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                work.dto.AttendanceDTO dto = new work.dto.AttendanceDTO();
                dto.setAttType(rs.getString("att_type"));
                
                dto.setAttTime(rs.getString("att_time").substring(0, 19)); 
                dto.setStoreName(rs.getString("store_name"));
                
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(rs != null) try{ rs.close(); } catch(Exception e){}
            if(pstmt != null) try{ pstmt.close(); } catch(Exception e){}
            DBConn.close(conn);
        }
        return list;
    }
    
    public java.util.ArrayList<work.dto.AttendanceDTO> getMyListByDate(String memId, String startDate, String endDate) {
        java.util.ArrayList<work.dto.AttendanceDTO> list = new java.util.ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        
        
        String sql = "SELECT a.att_type, a.att_time, ISNULL(s.store_name, '삭제된 매장') as store_name " +
                     "FROM tb_attendance a " +
                     "LEFT OUTER JOIN tb_store s ON a.store_id = s.store_id " +
                     "WHERE a.mem_id = ? " +
                     "AND a.att_time BETWEEN ? AND ? " + 
                     "ORDER BY a.att_time DESC"; 
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memId);
            pstmt.setString(2, startDate + " 00:00:00"); 
            pstmt.setString(3, endDate + " 23:59:59");   
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                work.dto.AttendanceDTO dto = new work.dto.AttendanceDTO();
                dto.setAttType(rs.getString("att_type"));
                dto.setAttTime(rs.getString("att_time").substring(0, 19)); 
                dto.setStoreName(rs.getString("store_name"));
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(rs != null) try{ rs.close(); } catch(Exception e){}
            if(pstmt != null) try{ pstmt.close(); } catch(Exception e){}
            DBConn.close(conn);
        }
        return list;
    }
    
    public boolean isEmployee(String memId, String storeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String sql = "SELECT idx FROM tb__link WHERE mem_id = ? AND store_id = ?";
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memId);
            pstmt.setString(2, storeId);
            rs = pstmt.executeQuery();
            
            
            return rs.next(); 
            
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        } finally {
            if(rs != null) try{ rs.close(); } catch(Exception e){}
            if(pstmt != null) try{ pstmt.close(); } catch(Exception e){}
            DBConn.close(conn);
        }
    }
    
    public java.util.ArrayList<work.dto.AttendanceDTO> getStoreAttendanceList(String storeId, String date) {
        java.util.ArrayList<work.dto.AttendanceDTO> list = new java.util.ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        
        
        String sql = "SELECT m.mem_name, m.mem_phone, a.att_type, a.att_time " +
                     "FROM tb_attendance a " +
                     "JOIN tb_member m ON a.mem_id = m.mem_id " +
                     "WHERE a.store_id = ? " +
                     "AND a.att_time BETWEEN ? AND ? " +
                     "ORDER BY a.att_time DESC"; 
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, storeId);
            pstmt.setString(2, date + " 00:00:00");
            pstmt.setString(3, date + " 23:59:59");
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                work.dto.AttendanceDTO dto = new work.dto.AttendanceDTO();
                
                
                
                
                
                dto.setStoreName(rs.getString("mem_name")); 
                dto.setAttType(rs.getString("att_type"));
                dto.setAttTime(rs.getString("att_time").substring(0, 19)); 
                
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(rs!=null) try{rs.close();}catch(Exception e){}
            if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
            DBConn.close(conn);
        }
        return list;
    }
    
    public java.util.ArrayList<work.dto.AttendanceDTO> getAttendanceByRange(String storeId, String startDate, String endDate, String searchMemId) {
        java.util.ArrayList<work.dto.AttendanceDTO> list = new java.util.ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        
        String sql = "SELECT a.idx, m.mem_name, a.att_type, a.att_time " +
                     "FROM tb_attendance a " +
                     "JOIN tb_member m ON a.mem_id = m.mem_id " +
                     "WHERE a.store_id = ? " +
                     "AND a.att_time BETWEEN ? AND ? "; 
        
        
        if (searchMemId != null && !searchMemId.equals("")) {
            sql += "AND a.mem_id = ? ";
        }
        
        sql += "ORDER BY a.att_time DESC"; 
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, storeId);
            pstmt.setString(2, startDate + " 00:00:00");
            pstmt.setString(3, endDate + " 23:59:59");
            
            
            if (searchMemId != null && !searchMemId.equals("")) {
                pstmt.setString(4, searchMemId);
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                work.dto.AttendanceDTO dto = new work.dto.AttendanceDTO();
                
                dto.setIdx(rs.getInt("idx"));
                dto.setStoreName(rs.getString("mem_name")); 
                dto.setAttType(rs.getString("att_type"));
                dto.setAttTime(rs.getString("att_time").substring(0, 16)); 
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(rs!=null) try{rs.close();}catch(Exception e){}
            if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
            DBConn.close(conn);
        }
        return list;
    }
    
    private void closeAll(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        if(rs != null) try{ rs.close(); } catch(Exception e){}
        if(pstmt != null) try{ pstmt.close(); } catch(Exception e){}
        work.util.DBConn.close(conn);
    }
    
    
    public boolean insertManualAttendance(String memId, String storeId, String type, String time) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        String sql = "INSERT INTO tb_attendance (mem_id, store_id, att_type, att_time) VALUES (?, ?, ?, ?)";
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memId);
            pstmt.setString(2, storeId);
            pstmt.setString(3, type);
            pstmt.setString(4, time);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; } 
        finally { closeAll(conn, pstmt, null); }
    }

    
    public boolean updateAttendance(int idx, String newTime, String newType) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE tb_attendance SET att_time = ?, att_type = ? WHERE idx = ?";
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newTime);
            pstmt.setString(2, newType);
            pstmt.setInt(3, idx);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; } 
        finally { closeAll(conn, pstmt, null); }
    }

    
    public boolean deleteAttendance(int idx) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM tb_attendance WHERE idx = ?";
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idx);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; } 
        finally { closeAll(conn, pstmt, null); }
    }
    
    public java.util.ArrayList<work.dto.AttendanceDTO> getMyAttendanceList(String memId, String month) {
        java.util.ArrayList<work.dto.AttendanceDTO> list = new java.util.ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        
        String sql = "SELECT a.att_time, a.att_type, ISNULL(s.store_name, '삭제된 매장') as store_name " +
                     "FROM tb_attendance a " +
                     "LEFT JOIN tb_store s ON a.store_id = s.store_id " +
                     "WHERE a.mem_id = ? AND CONVERT(VARCHAR, a.att_time, 120) LIKE ? " +
                     "ORDER BY a.att_time DESC";
        
        try {
            conn = DBConn.getConnection(); 
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memId);
            pstmt.setString(2, month + "%"); 
            rs = pstmt.executeQuery();
            
            while(rs.next()) {
                work.dto.AttendanceDTO dto = new work.dto.AttendanceDTO();
                
                String timeStr = rs.getString("att_time");
                if(timeStr.length() > 16) timeStr = timeStr.substring(0, 16);
                
                dto.setAttTime(timeStr);
                dto.setAttType(rs.getString("att_type"));
                dto.setStoreName(rs.getString("store_name"));
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, pstmt, rs); 
        }
        return list;
    }
    
    public work.dto.AttendanceDTO getLastRecord(String memId, String storeId) {
        work.dto.AttendanceDTO dto = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        
        String sql = "SELECT TOP 1 att_type, att_time FROM tb_attendance " +
                     "WHERE mem_id = ? AND store_id = ? " +
                     "ORDER BY att_time DESC";
        
        try {
            conn = DBConn.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memId);
            pstmt.setString(2, storeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                dto = new work.dto.AttendanceDTO();
                dto.setAttType(rs.getString("att_type"));
                dto.setAttTime(rs.getString("att_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, pstmt, rs);
        }
        return dto;
    }
}
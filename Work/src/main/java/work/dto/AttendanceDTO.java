package work.dto;

public class AttendanceDTO {

    private String attType;
    private String attTime;
    private String storeName;
    private int    idx;
    private String memberId;
    private int    roleId;
    private String workType;

    public String getAttType()  { return attType; }
    public void setAttType(String attType) { this.attType = attType; }

    public String getAttTime()  { return attTime; }
    public void setAttTime(String attTime) { this.attTime = attTime; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public int getIdx()  { return idx; }
    public void setIdx(int idx) { this.idx = idx; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getWorkType() { return workType != null ? workType : ""; }
    public void setWorkType(String workType) { this.workType = workType; }
}
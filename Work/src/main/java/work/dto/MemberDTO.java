/**
 * 파일명 : MemberDTO.java
 * 작성일 : 2025. 12. 15.
 * 설명 :
 */
package work.dto;

/**
 * @author user
 *
 */
public class MemberDTO {

	private String id;
	private String pw;
	private String name;
	private String phone;
	private String role;
	private int hourlyWage;
	private String storeId;
	private String empName;
	private String birth;
	private String workDays;
	private int roleId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPw() {
		return pw;
	}
	public void setPw(String pw) {
		this.pw = pw;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public int getHourlyWage() {
		return hourlyWage;
	}
	public void setHourlyWage(int hoourlyWage) {
		this.hourlyWage = hoourlyWage;
	}
	public String getStoreId() {
		return storeId;
	}
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	public String getEmpName() {
		return empName;
	}
	public void setEmpName(String empName) {
		this.empName = empName;
	}
	public String getBirth() {
		return birth;
	}
	public void setBirth(String birth) {
		this.birth = birth;
	}
	public String getWorkDays() {
		return workDays;
	}
	public void setWorkDays(String workDays) {
		this.workDays = workDays;
	}
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
}
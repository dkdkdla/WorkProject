package com.example.work;

import java.io.Serializable;

/**
 * [코드 설계 설명]
 * 1. 직렬화(Serializable): 인텐트(Intent)에 객체 자체를 담아 다른 액티비티로 넘길 수 있도록 구현했습니다.
 * 2. 필드 확장: 서버 응답 규격에 맞춰 권한(role)과 매장코드(storeId)를 추가하여 앱의 세션 관리 효율을 높였습니다.
 * 3. 캡슐화: 필드는 private으로 보호하고, Getter/Setter 메서드를 통해 접근하도록 설계했습니다.
 */
public class MemberDTO implements Serializable {
    private String id;          // 아이디
    private String name;        // 이름
    private String phone;       // 전화번호
    private int hourlyWage;     // 시급
    private String role;        // 권한 (A: 점주, S: 스태프)
    private String storeId;     // 소속 매장 코드

    // 기본 생성자 (JSON 파싱 등을 위해 필요)
    public MemberDTO() {}

    // 주요 정보 초기화 생성자
    public MemberDTO(String id, String name, String phone, int hourlyWage) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.hourlyWage = hourlyWage;
    }

    // Getter & Setter 메서드
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getHourlyWage() { return hourlyWage; }
    public void setHourlyWage(int hourlyWage) { this.hourlyWage = hourlyWage; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    /**
     * 권한 확인 편의 메서드
     * @return 관리자 여부
     */
    public boolean isAdmin() {
        return "A".equals(this.role);
    }
}
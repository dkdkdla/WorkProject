package com.example.work;

import java.io.Serializable;

/**
 * [코드 설계 설명]
 * 1. 객체 직렬화(Serializable): Intent를 통해 액티비티 간에 매장 객체를 통째로 넘길 수 있도록 설계했습니다.
 * 2. 불변성 및 유연성: 생성자를 통해 초기 데이터를 강제하고, 필요 시 Setter를 통해 데이터를 수정할 수 있습니다.
 * 3. 서버 데이터 매핑: StoreList 및 ProfileActivity에서 수신하는 JSON 키값(storeId, storeName)과 일치하도록 구성했습니다.
 */
public class StoreData implements Serializable {
    // 인텐트 전달을 위한 직렬화 ID (권장)
    private static final long serialVersionUID = 1L;

    private String storeId;
    private String storeName;
    private String origin; // 매장 등록 출처 (Android, Web 등)

    // 기본 생성자 (JSON 파싱 및 확장성을 위해 추가)
    public StoreData() {}

    // 데이터 초기화를 위한 생성자
    public StoreData(String storeId, String storeName, String origin) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.origin = origin;
    }

    // Getter 및 Setter 메서드
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    /**
     * 디버깅 및 로그 확인을 위한 toString 오버라이드
     */
    @Override
    public String toString() {
        return "StoreData{" +
                "storeId='" + storeId + '\'' +
                ", storeName='" + storeName + '\'' +
                ", origin='" + origin + '\'' +
                '}';
    }
}
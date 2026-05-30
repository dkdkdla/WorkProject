package work.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;

/**
 * 급여 계산 유틸리티
 * - 22:00 기준으로 주간/야간 분리
 * - workType: "평일"=항상 평일 시급 / "휴일"=항상 휴일 시급 / "전체"=실제 날짜 자동 판단
 * - 배율: 평일주간×1.0 / 평일야간×1.5 / 휴일주간×1.5 / 휴일야간×2.0
 */
public class PayCalcUtil {

    /**
     * 급여 계산 메인 메서드
     * @param roleId    역할 ID
     * @param workDate  근무 날짜
     * @param startTime 출근 시간
     * @param endTime   퇴근 시간
     * @param workType  근무 유형 ("평일" / "휴일" / "전체" / null)
     * @return 계산된 급여 (원)
     */
    public static int calcPay(int roleId, LocalDate workDate, LocalTime startTime, LocalTime endTime, String workType) {
        return calcPay(roleId, workDate, startTime, endTime, workType, true, true);
    }

    public static int calcPay(int roleId, LocalDate workDate, LocalTime startTime, LocalTime endTime,
                               String workType, boolean useNight, boolean useWeekend) {
        if (roleId <= 0 || workDate == null || startTime == null || endTime == null) return 0;

        int[] wages = getWages(roleId);
        if (wages == null) return 0;

        int wageWeekdayDay   = wages[0]; // 평일 주간
        int wageWeekdayNight = wages[1]; // 평일 야간
        int wageHolidayDay   = wages[2]; // 휴일 주간 (wage_weekend_day)
        int wageHolidayNight = wages[3]; // 휴일 야간 (wage_weekend_night)

        // 휴일 여부 판단
        // "평일" → 항상 평일 시급
        // "휴일" → 항상 휴일 시급
        // "전체" 또는 null → 실제 날짜(토/일) 기준 자동 판단
        boolean isHoliday;
        if ("휴일".equals(workType)) {
            isHoliday = true;
        } else if ("평일".equals(workType)) {
            isHoliday = false;
        } else {
            // "전체" 또는 미설정 → 실제 날짜 기준
            isHoliday = isWeekend(workDate);
        }

        // 근무 시간을 주간/야간 구간으로 분리
        double dayMinutes   = 0;
        double nightMinutes = 0;

        if (endTime.isBefore(startTime)) {
            // 자정을 넘기는 경우 (예: 22:00 ~ 02:00)
            double[] seg1 = splitDayNight(startTime, LocalTime.of(23, 59));
            dayMinutes   += seg1[0];
            nightMinutes += seg1[1];
            nightMinutes += toMinutes(LocalTime.of(0, 0), endTime);
        } else {
            double[] seg = splitDayNight(startTime, endTime);
            dayMinutes   = seg[0];
            nightMinutes = seg[1];
        }

        int dayWage   = isHoliday ? wageHolidayDay   : wageWeekdayDay;
        int nightWage = isHoliday ? wageHolidayNight : wageWeekdayNight;

        double totalPay = (dayMinutes / 60.0 * dayWage) + (nightMinutes / 60.0 * nightWage);
        return (int) Math.round(totalPay);
    }

    // 하위 호환 - workType 없이 호출 시 날짜 기준 자동 판단
    public static int calcPay(int roleId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        return calcPay(roleId, workDate, startTime, endTime, null);
    }

    private static double[] splitDayNight(LocalTime start, LocalTime end) {
        double dayMin   = 0;
        double nightMin = 0;

        LocalTime dayStart = LocalTime.of(6, 0);
        LocalTime dayEnd   = LocalTime.of(22, 0);

        LocalTime dayOverlapStart = maxTime(start, dayStart);
        LocalTime dayOverlapEnd   = minTime(end, dayEnd);
        if (dayOverlapStart.isBefore(dayOverlapEnd)) {
            dayMin = toMinutes(dayOverlapStart, dayOverlapEnd);
        }

        LocalTime n1OverlapStart = maxTime(start, LocalTime.of(0, 0));
        LocalTime n1OverlapEnd   = minTime(end, LocalTime.of(6, 0));
        if (n1OverlapStart.isBefore(n1OverlapEnd)) {
            nightMin += toMinutes(n1OverlapStart, n1OverlapEnd);
        }

        LocalTime n2OverlapStart = maxTime(start, LocalTime.of(22, 0));
        LocalTime n2OverlapEnd   = minTime(end, LocalTime.of(23, 59));
        if (n2OverlapStart.isBefore(n2OverlapEnd)) {
            nightMin += toMinutes(n2OverlapStart, n2OverlapEnd);
        }

        return new double[]{dayMin, nightMin};
    }

    private static double toMinutes(LocalTime from, LocalTime to) {
        return (to.toSecondOfDay() - from.toSecondOfDay()) / 60.0;
    }
    private static LocalTime maxTime(LocalTime a, LocalTime b) { return a.isAfter(b) ? a : b; }
    private static LocalTime minTime(LocalTime a, LocalTime b) { return a.isBefore(b) ? a : b; }
    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    private static int[] getWages(int roleId) {
        String sql = "SELECT wage_weekday_day, wage_weekday_night, " +
                     "wage_weekend_day, wage_weekend_night FROM tb_role WHERE role_id = ?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new int[]{
                        rs.getInt("wage_weekday_day"),
                        rs.getInt("wage_weekday_night"),
                        rs.getInt("wage_weekend_day"),
                        rs.getInt("wage_weekend_night")
                    };
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * 급여 상세 내역 반환
     * @return [평일주간분, 평일야간분, 휴일주간분, 휴일야간분,
     *          평일주간시급, 평일야간시급, 휴일주간시급, 휴일야간시급]
     */
    public static long[] calcPayDetail(int roleId, LocalDate workDate, LocalTime startTime, LocalTime endTime, String workType) {
        if (roleId <= 0 || workDate == null || startTime == null || endTime == null)
            return new long[]{0,0,0,0,0,0,0,0};

        int[] wages = getWages(roleId);
        if (wages == null) return new long[]{0,0,0,0,0,0,0,0};

        boolean isHoliday;
        if ("휴일".equals(workType))      isHoliday = true;
        else if ("평일".equals(workType)) isHoliday = false;
        else                              isHoliday = isWeekend(workDate);

        double dayMin = 0, nightMin = 0;
        if (endTime.isBefore(startTime)) {
            double[] seg1 = splitDayNight(startTime, LocalTime.of(23, 59));
            dayMin   += seg1[0]; nightMin += seg1[1];
            nightMin += toMinutes(LocalTime.of(0, 0), endTime);
        } else {
            double[] seg = splitDayNight(startTime, endTime);
            dayMin = seg[0]; nightMin = seg[1];
        }

        long wdd = 0, wdn = 0, hdd = 0, hdn = 0;
        if (isHoliday) { hdd = (long)dayMin; hdn = (long)nightMin; }
        else           { wdd = (long)dayMin; wdn = (long)nightMin; }

        return new long[]{wdd, wdn, hdd, hdn,
                          wages[0], wages[1], wages[2], wages[3]};
    }
}
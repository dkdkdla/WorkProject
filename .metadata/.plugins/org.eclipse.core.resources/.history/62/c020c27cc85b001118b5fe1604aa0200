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
 * - 토/일 주말 여부 판단
 * - 배율: 평일주간×1.0 / 평일야간×1.5 / 주말주간×1.5 / 주말야간×2.0
 */
public class PayCalcUtil {

    private static final LocalTime NIGHT_START = LocalTime.of(22, 0); // 야간 시작 22:00
    private static final LocalTime NIGHT_END   = LocalTime.of(6, 0);  // 야간 종료 06:00

    /**
     * 급여 계산 메인 메서드
     * @param roleId    역할 ID
     * @param workDate  근무 날짜
     * @param startTime 출근 시간
     * @param endTime   퇴근 시간
     * @return 계산된 급여 (원)
     */
    public static int calcPay(int roleId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        if (roleId <= 0 || workDate == null || startTime == null || endTime == null) return 0;

        // 역할별 4개 시급 조회
        int[] wages = getWages(roleId);
        if (wages == null) return 0;

        int wageWeekdayDay   = wages[0]; // 평일 주간
        int wageWeekdayNight = wages[1]; // 평일 야간
        int wageWeekendDay   = wages[2]; // 주말 주간
        int wageWeekendNight = wages[3]; // 주말 야간

        boolean isWeekend = isWeekend(workDate);

        // 근무 시간을 주간/야간 구간으로 분리
        double dayMinutes   = 0;
        double nightMinutes = 0;

        // 자정을 넘기는 경우 처리 (예: 22:00 ~ 02:00)
        if (endTime.isBefore(startTime)) {
            // 당일 구간: startTime ~ 23:59
            double[] seg1 = splitDayNight(startTime, LocalTime.of(23, 59));
            dayMinutes   += seg1[0];
            nightMinutes += seg1[1];
            // 익일 구간: 00:00 ~ endTime (익일은 모두 야간으로 처리 - 06:00 이전)
            nightMinutes += toMinutes(LocalTime.of(0, 0), endTime);
        } else {
            double[] seg = splitDayNight(startTime, endTime);
            dayMinutes   = seg[0];
            nightMinutes = seg[1];
        }

        // 시급 선택
        int dayWage   = isWeekend ? wageWeekendDay   : wageWeekdayDay;
        int nightWage = isWeekend ? wageWeekendNight : wageWeekdayNight;

        // 급여 계산 (분 단위 → 시간 단위)
        double totalPay = (dayMinutes / 60.0 * dayWage) + (nightMinutes / 60.0 * nightWage);
        return (int) Math.round(totalPay);
    }

    /**
     * 시작~종료 시간을 주간/야간 분으로 분리
     * 주간: 06:00 ~ 22:00 / 야간: 22:00 ~ 06:00
     * @return [주간분, 야간분]
     */
    private static double[] splitDayNight(LocalTime start, LocalTime end) {
        double dayMin   = 0;
        double nightMin = 0;

        LocalTime dayStart = LocalTime.of(6, 0);   // 06:00
        LocalTime dayEnd   = LocalTime.of(22, 0);  // 22:00

        // 전체 구간을 1분 단위로 계산하는 대신 구간 교집합으로 계산
        // 주간 구간 [06:00, 22:00] 과 근무 구간 [start, end] 의 교집합
        LocalTime dayOverlapStart = maxTime(start, dayStart);
        LocalTime dayOverlapEnd   = minTime(end, dayEnd);
        if (dayOverlapStart.isBefore(dayOverlapEnd)) {
            dayMin = toMinutes(dayOverlapStart, dayOverlapEnd);
        }

        // 야간 구간 1: [00:00, 06:00]
        LocalTime night1Start = LocalTime.of(0, 0);
        LocalTime night1End   = LocalTime.of(6, 0);
        LocalTime n1OverlapStart = maxTime(start, night1Start);
        LocalTime n1OverlapEnd   = minTime(end, night1End);
        if (n1OverlapStart.isBefore(n1OverlapEnd)) {
            nightMin += toMinutes(n1OverlapStart, n1OverlapEnd);
        }

        // 야간 구간 2: [22:00, 23:59]
        LocalTime night2Start = LocalTime.of(22, 0);
        LocalTime night2End   = LocalTime.of(23, 59);
        LocalTime n2OverlapStart = maxTime(start, night2Start);
        LocalTime n2OverlapEnd   = minTime(end, night2End);
        if (n2OverlapStart.isBefore(n2OverlapEnd)) {
            nightMin += toMinutes(n2OverlapStart, n2OverlapEnd);
        }

        return new double[]{dayMin, nightMin};
    }

    private static double toMinutes(LocalTime from, LocalTime to) {
        return (to.toSecondOfDay() - from.toSecondOfDay()) / 60.0;
    }

    private static LocalTime maxTime(LocalTime a, LocalTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalTime minTime(LocalTime a, LocalTime b) {
        return a.isBefore(b) ? a : b;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * DB에서 역할별 4개 시급 조회
     * @return [평일주간, 평일야간, 주말주간, 주말야간]
     */
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
}
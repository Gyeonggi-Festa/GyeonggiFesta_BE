package gyeonggi.gyeonggifesta.calendar.service;

import gyeonggi.gyeonggifesta.event.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

@Component
@Slf4j
public class EventToCalendarPayloadMapper {

    // "10:00~18:00", "10:00 - 18:00", "10:00-18:00" 등
    private static final Pattern TIME_RANGE = Pattern.compile(
        "(?<sh>\\d{1,2}):(?<sm>\\d{2})\\s*[~\\-–—]\\s*(?<eh>\\d{1,2}):(?<em>\\d{2})"
    );

    public Map<String, Object> toPayload(Event e) {
        ZoneId zone = ZoneId.of("Asia/Seoul");

        LocalDate sDate = Optional.ofNullable(e.getStartDate()).orElse(LocalDate.now());
        LocalDate eDate = Optional.ofNullable(e.getEndDate()).orElse(sDate);
        if (eDate.isBefore(sDate)) eDate = sDate;

        // 기본 시간(종일 대용): 10:00 ~ 18:00
        LocalTime startT = LocalTime.of(10, 0);
        LocalTime endT   = LocalTime.of(18, 0);

        String ti = Optional.ofNullable(e.getTimeInfo()).orElse("");
        Matcher m = TIME_RANGE.matcher(ti);
        if (m.find()) {
            try {
                int sh = Integer.parseInt(m.group("sh"));
                int sm = Integer.parseInt(m.group("sm"));
                int eh = Integer.parseInt(m.group("eh"));
                int em = Integer.parseInt(m.group("em"));
                startT = LocalTime.of(Math.min(Math.max(sh, 0), 23), Math.min(Math.max(sm, 0), 59));
                endT   = LocalTime.of(Math.min(Math.max(eh, 0), 23), Math.min(Math.max(em, 0), 59));
            } catch (Exception ignore) {}
        }
        if (sDate.equals(eDate) && !endT.isAfter(startT)) endT = startT.plusHours(1);

        LocalDateTime sdt = LocalDateTime.of(sDate, startT);
        LocalDateTime edt = LocalDateTime.of(eDate, endT);

        DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        String title = Optional.ofNullable(e.getTitle()).orElse("무제 행사");
        String desc = buildDescription(e);

        Map<String, Object> start = Map.of("dateTime", sdt.truncatedTo(java.time.temporal.ChronoUnit.MINUTES).format(ISO),
                                           "timeZone", zone.getId());
        Map<String, Object> end   = Map.of("dateTime", edt.truncatedTo(java.time.temporal.ChronoUnit.MINUTES).format(ISO),
                                           "timeZone", zone.getId());

        Map<String, Object> comp = new LinkedHashMap<>();
        comp.put("summary", title);
        comp.put("description", desc);
        comp.put("start", start);
        comp.put("end", end);
        comp.put("location", Optional.ofNullable(e.getOrgName()).orElse("")); // 장소 정보가 없으니 주최명 사용
        comp.put("reminders", List.of(Map.of("method", "DISPLAY", "trigger", "-PT10M")));

        // 라인웍스 캘린더 이벤트 생성 바디
        return Map.of("eventComponents", List.of(comp));
    }

    private String buildDescription(Event e) {
        return new StringBuilder()
          .append("[행사명] ").append(nz(e.getTitle())).append("\n")
          .append("• 주최: ").append(nz(e.getOrgName())).append("\n")
          .append("• 분류: ").append(nz(e.getCodename())).append("\n")
          .append("• 요금: ").append("Y".equalsIgnoreCase(nz(e.getIsFree())) ? "무료" : nz(e.getUseFee())).append("\n")
          .append("• 시간안내: ").append(nz(e.getTimeInfo())).append("\n")
          .append("• 링크: ").append(nz(e.getOrgLink())).append("\n")
          .append("• 진행상태: ").append(e.getStatus() != null ? e.getStatus().name() : "")
          .append(" (").append(nz(e.getStartDate())).append(" ~ ").append(nz(e.getEndDate())).append(")\n")
          .toString();
    }
    private String nz(Object o) { return o == null ? "" : String.valueOf(o); }
}

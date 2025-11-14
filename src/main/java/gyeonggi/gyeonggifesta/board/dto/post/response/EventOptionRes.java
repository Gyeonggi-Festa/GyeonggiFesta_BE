package gyeonggi.gyeonggifesta.board.dto.post.response;

import gyeonggi.gyeonggifesta.event.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventOptionRes {
        private Long eventId;
        private String title;
        private String mainImage;
        private LocalDate startDate;
        private LocalDate endDate;

        public static EventOptionRes from(Event event) {
                return EventOptionRes.builder()
                                .eventId(event.getId())
                                .title(event.getTitle())
                                .mainImage(event.getMainImg())
                                .startDate(event.getStartDate())
                                .endDate(event.getEndDate())
                                .build();
        }
}

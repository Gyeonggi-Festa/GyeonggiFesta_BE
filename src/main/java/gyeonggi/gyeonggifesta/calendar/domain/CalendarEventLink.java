package gyeonggi.gyeonggifesta.calendar.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "lineworks_event_link",
       uniqueConstraints = @UniqueConstraint(name="uk_lw_member_event", columnNames = {"memberId","eventId"}),
       indexes = @Index(name="idx_lw_member_event", columnList = "memberId,eventId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CalendarEventLink {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long memberId;
    @Column(nullable = false) private Long eventId;

    @Column(length = 128)
    private String externalEventId; // 라인웍스 이벤트 식별자(id/eventId)

    private Instant createdAt = Instant.now();
}

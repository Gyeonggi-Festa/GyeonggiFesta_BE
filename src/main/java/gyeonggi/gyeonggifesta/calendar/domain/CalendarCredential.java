package gyeonggi.gyeonggifesta.calendar.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "lineworks_credential",
       indexes = {
           @Index(name="idx_lw_member", columnList = "memberId", unique = true),
           @Index(name="idx_lw_user", columnList = "lineWorksUserId")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CalendarCredential {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(length = 128)
    private String lineWorksUserId; // 필요 시 '/users/me'로 보충 가능 (지금은 'me' 기본)

    @Column(length = 2048) private String accessToken;
    @Column(length = 2048) private String refreshToken;
    private Instant accessTokenExpiresAt;

    @Column(length = 64)  private String tokenType;
    @Column(length = 256) private String scope;

    private Instant createdAt = Instant.now();
    @UpdateTimestamp private Instant updatedAt;
}

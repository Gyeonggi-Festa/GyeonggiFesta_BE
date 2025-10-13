package gyeonggi.gyeonggifesta.member.entity;

import gyeonggi.gyeonggifesta.member.enums.Role;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	private String verifyId;

	@Setter
	private String username;

	@Setter
	private String email;

	@Enumerated(value = EnumType.STRING)
	@Setter
	private Role role;

	@Setter
	private String gender;

	@Column(name = "birthday")
	@Setter
	private LocalDate birthDay;

	@Builder
	public Member(String verifyId, String username, String email, Role role, String gender, LocalDate birthDay) {
		this.verifyId = verifyId;
		this.username = username;
		this.email = email;
		this.role = role;
		this.gender = gender;
		this.birthDay = birthDay;
	}

	public int getAge() {
		return Period.between(this.birthDay, LocalDate.now()).getYears();
	}
}

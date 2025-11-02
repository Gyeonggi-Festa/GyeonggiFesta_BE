package gyeonggi.gyeonggifesta.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoRes {

	private String verifyId;
	private String username;
	private String gender;
	private String email;
}

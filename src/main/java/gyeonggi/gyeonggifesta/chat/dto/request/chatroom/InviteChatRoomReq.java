package gyeonggi.gyeonggifesta.chat.dto.request.chatroom;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InviteChatRoomReq {

	private Long chatRoomId;
	private String email = null;
	private String verifyId = null;

	@AssertTrue(message = "email 또는 verifyId 중 하나는 반드시 입력해야 합니다")
	public boolean isEmailOrVerifyIdProvided() {
		return (email != null && !email.isEmpty()) ||
			(verifyId != null && !verifyId.isEmpty());
	}

	public boolean hasEmail() {
		return email != null && !email.isEmpty();
	}

	public boolean hasVerifyId() {
		return verifyId != null && !verifyId.isEmpty();
	}
}

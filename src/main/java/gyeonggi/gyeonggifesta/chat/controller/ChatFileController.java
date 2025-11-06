package gyeonggi.gyeonggifesta.chat.controller;

import gyeonggi.gyeonggifesta.board.dto.media.response.PresignedUrlResponse;
import gyeonggi.gyeonggifesta.chat.service.chatting.ChatFileService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅에 첨부할 파일 업로드를 위한 REST 컨트롤러
 * - 서비스 레이어에서 비즈니스 로직 및 예외 처리 수행
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user/chat")
public class ChatFileController {

	private final ChatFileService chatFileService;

	/**
	 * 채팅용 파일 업로드를 위한 Pre-signed URL 발급
	 * - 클라이언트는 발급받은 URL을 사용하여 직접 S3에 파일을 업로드
	 * - 업로드 후 s3Key를 채팅 메시지와 함께 전송
	 */
	@PostMapping("/upload-url")
	public ResponseEntity<Response<PresignedUrlResponse>> getUploadUrl(
		@RequestParam("fileName") String fileName,
		@RequestParam("contentType") String contentType) {

		PresignedUrlResponse presignedUrlResponse = chatFileService.getUploadUrl(fileName, contentType);

		return Response.ok(presignedUrlResponse).toResponseEntity();
	}
}
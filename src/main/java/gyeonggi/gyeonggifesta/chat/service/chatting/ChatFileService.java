package gyeonggi.gyeonggifesta.chat.service.chatting;

import gyeonggi.gyeonggifesta.board.dto.media.response.PresignedUrlResponse;

/**
 * 채팅 파일 서비스 인터페이스
 */
public interface ChatFileService {

	/**
	 * 채팅 파일 업로드를 위한 Pre-signed URL 생성
	 *
	 * @param fileName 업로드할 파일명
	 * @param contentType 파일 타입
	 * @return Pre-signed URL 응답 객체
	 */
	PresignedUrlResponse getUploadUrl(String fileName, String contentType);
}

package gyeonggi.gyeonggifesta.chat.service.chatting;

import gyeonggi.gyeonggifesta.aws.service.S3Service;
import gyeonggi.gyeonggifesta.board.dto.media.response.PresignedUrlResponse;
import gyeonggi.gyeonggifesta.chat.exception.ChatErrorCode;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 채팅 파일 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatFileServiceImpl implements ChatFileService {

	private final S3Service s3Service;

	@Override
	public PresignedUrlResponse getUploadUrl(String fileName, String contentType) {

		validateFileType(contentType);

		try {
			// S3 Pre-signed URL 생성
			return s3Service.generateChatMediaPresignedUrl(fileName, contentType);
		} catch (Exception e) {
			log.error("Pre-signed URL 생성 실패: {}", e.getMessage(), e);
			throw new BusinessException(ChatErrorCode.FILE_UPLOAD_FAILED);
		}
	}

	/**
	 * 파일 타입 유효성 검사
	 *
	 * @param contentType 파일 타입
	 * @throws BusinessException 지원하지 않는 파일 타입일 경우
	 */
	private void validateFileType(String contentType) {
		if (contentType == null || !(
			contentType.startsWith("image/") ||
				contentType.equals("application/pdf") ||
				contentType.equals("application/zip") ||
				contentType.equals("application/x-zip-compressed") ||
				contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
				contentType.equals("application/msword") ||
				contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
				contentType.equals("application/vnd.ms-excel")
		)) {
			throw new BusinessException(ChatErrorCode.INVALID_FILE_TYPE);
		}
	}
}
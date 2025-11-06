package gyeonggi.gyeonggifesta.aws.service;

import gyeonggi.gyeonggifesta.board.dto.media.response.PresignedUrlResponse;

public interface S3Service {

	PresignedUrlResponse generatePostMediaPresignedUrl(String objectKey, String contentType);
	PresignedUrlResponse generateChatMediaPresignedUrl(String objectKey, String contentType);

	PresignedUrlResponse generateReviewMediaPresignedUrl(String objectKey, String contentType);

	void deleteObject(String s3Key);
}

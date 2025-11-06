package gyeonggi.gyeonggifesta.aws.controller;

import gyeonggi.gyeonggifesta.aws.service.S3Service;
import gyeonggi.gyeonggifesta.board.dto.media.response.PresignedUrlResponse;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class S3Controller {

	private final S3Service s3Service;

	@GetMapping("/post/presigned")
	public ResponseEntity<Response<PresignedUrlResponse>> generatePostPresignedUrl(
		@RequestParam String originalFileName,
		@RequestParam String contentType) {

		PresignedUrlResponse presignedUrlResponse = s3Service.generatePostMediaPresignedUrl(originalFileName, contentType);

		return Response.ok(presignedUrlResponse).toResponseEntity();
	}

	@GetMapping("/chat/presigned")
	public ResponseEntity<Response<PresignedUrlResponse>> generateChatPresignedUrl(
		@RequestParam String originalFileName,
		@RequestParam String contentType) {

		PresignedUrlResponse presignedUrlResponse = s3Service.generateChatMediaPresignedUrl(originalFileName, contentType);

		return Response.ok(presignedUrlResponse).toResponseEntity();
	}

	@GetMapping("/review/presigned")
	public ResponseEntity<Response<PresignedUrlResponse>> generateReviewPresignedUrl(
		@RequestParam String originalFileName,
		@RequestParam String contentType) {

		PresignedUrlResponse presignedUrlResponse = s3Service.generateReviewMediaPresignedUrl(originalFileName, contentType);

		return Response.ok(presignedUrlResponse).toResponseEntity();
	}
}

package gyeonggi.gyeonggifesta.aws.service;

import gyeonggi.gyeonggifesta.board.dto.media.response.PresignedUrlResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

	private S3Client s3Client;
	private S3Presigner s3Presigner;

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@PostConstruct
	public void initializeAmazon() {
		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

		this.s3Client = S3Client.builder()
			.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
			.region(Region.of(region))
			.build();

		this.s3Presigner = S3Presigner.builder()
			.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
			.region(Region.of(region))
			.build();
	}

	/**
	 * 파일 업로드를 위한 pre-signed URL을 생성합니다.
	 *
	 * @param contentType 업로드할 파일의 콘텐츠 타입 (예: "image/jpeg", "video/mp4")
	 * @return pre-signed URL 문자열
	 */
	@Override
	public PresignedUrlResponse generatePostMediaPresignedUrl(String originalFileName, String contentType) {

		String extension = getExtensionFromContentType(contentType);

		// 고유한 s3Key 생성 (예: "seoulfest/post/media/20250402123045_uniqueId.jpg")
		String objectKey = generatePostUniqueS3KeyWithExtension(originalFileName, extension);

		// S3에 업로드할 객체의 요청 객체 생성
		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(objectKey)
			.contentType(contentType)
			.build();

		// pre-signed URL 요청 생성 (유효기간 15분)
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(15))
			.putObjectRequest(objectRequest)
			.build();

		// pre-signed URL 생성 및 문자열로 반환
		String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

		// 생성된 s3Key와 pre-signed URL을 함께 반환
		return PresignedUrlResponse.builder()
			.s3Key(objectKey)
			.presignedUrl(presignedUrl)
			.build();
	}

	@Override
	public PresignedUrlResponse generateChatMediaPresignedUrl(String originalFileName, String contentType) {

		String extension = getExtensionFromContentType(contentType);

		// 고유한 s3Key 생성 (예: "seoulfest/chat/media/20250402123045_uniqueId.jpg")
		String objectKey = generateChatUniqueS3KeyWithExtension(originalFileName, extension);

		// S3에 업로드할 객체의 요청 객체 생성
		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(objectKey)
			.contentType(contentType)
			.build();

		// pre-signed URL 요청 생성 (유효기간 15분)
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(15))
			.putObjectRequest(objectRequest)
			.build();

		// pre-signed URL 생성 및 문자열로 반환
		String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

		// 생성된 s3Key와 pre-signed URL을 함께 반환
		return PresignedUrlResponse.builder()
			.s3Key(objectKey)
			.presignedUrl(presignedUrl)
			.build();
	}

	@Override
	public PresignedUrlResponse generateReviewMediaPresignedUrl(String originalFileName, String contentType) {

		String extension = getExtensionFromContentType(contentType);

		// 고유한 s3Key 생성 (예: "seoulfest/review/media/20250402123045_uniqueId.jpg")
		String objectKey = generateReviewUniqueS3KeyWithExtension(originalFileName, extension);

		// S3에 업로드할 객체의 요청 객체 생성
		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(objectKey)
			.contentType(contentType)
			.build();

		// pre-signed URL 요청 생성 (유효기간 15분)
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(15))
			.putObjectRequest(objectRequest)
			.build();

		// pre-signed URL 생성 및 문자열로 반환
		String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

		// 생성된 s3Key와 pre-signed URL을 함께 반환
		return PresignedUrlResponse.builder()
			.s3Key(objectKey)
			.presignedUrl(presignedUrl)
			.build();
	}

	private String generatePostUniqueS3KeyWithExtension(String originalFileName, String extension) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uniqueId = UUID.randomUUID().toString();
		return "gyeonggifesta/post/media/" + timestamp + "_" + uniqueId + extension;
	}

	private String generateChatUniqueS3KeyWithExtension(String originalFileName, String extension) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uniqueId = UUID.randomUUID().toString();
		return "gyeonggifesta/chat/media/" + timestamp + "_" + uniqueId + extension;
	}

	private String generateReviewUniqueS3KeyWithExtension(String originalFileName, String extension) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uniqueId = UUID.randomUUID().toString();
		return "gyeonggifesta/review/media/" + timestamp + "_" + uniqueId + extension;
	}

	@Override
	public void deleteObject(String s3Key) {
		DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(s3Key)
			.build();
		s3Client.deleteObject(deleteRequest);
	}

	private String getExtensionFromContentType(String contentType) {
		if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) {
			return ".jpg";
		} else if (contentType.equals("image/png")) {
			return ".png";
		} else if (contentType.equals("image/gif")) {
			return ".gif";
		} else if (contentType.equals("video/mp4")) {
			return ".mp4";
		}
		// 기본값
		return ".bin";
	}
}

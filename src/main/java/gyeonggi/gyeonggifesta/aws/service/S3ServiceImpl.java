package gyeonggi.gyeonggifesta.aws.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

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

	private String generatePostUniqueS3KeyWithExtension(String originalFileName, String extension) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uniqueId = UUID.randomUUID().toString();
		return "seoulfest/post/media/" + timestamp + "_" + uniqueId + extension;
	}

	private String generateChatUniqueS3KeyWithExtension(String originalFileName, String extension) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uniqueId = UUID.randomUUID().toString();
		return "seoulfest/chat/media/" + timestamp + "_" + uniqueId + extension;
	}

	private String generateReviewUniqueS3KeyWithExtension(String originalFileName, String extension) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uniqueId = UUID.randomUUID().toString();
		return "seoulfest/review/media/" + timestamp + "_" + uniqueId + extension;
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

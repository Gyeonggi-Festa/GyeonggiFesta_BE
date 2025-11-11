package gyeonggi.gyeonggifesta.board.service.comment;

import gyeonggi.gyeonggifesta.board.dto.comment.request.PostCommentReq;
import gyeonggi.gyeonggifesta.board.dto.comment.request.PostCommentUpdateReq;
import gyeonggi.gyeonggifesta.board.dto.comment.response.PostCommentRes;
import gyeonggi.gyeonggifesta.board.entity.Post;
import gyeonggi.gyeonggifesta.board.entity.PostComment;
import gyeonggi.gyeonggifesta.board.exception.BoardErrorCode;
import gyeonggi.gyeonggifesta.board.repository.PostCommentRepository;
import gyeonggi.gyeonggifesta.board.repository.PostRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

	private final SecurityUtil securityUtil;
	private final PostCommentRepository postCommentRepository;
	private final PostRepository postRepository;

	@Override
	@Transactional
	public void createComment(PostCommentReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		// 게시글 조회
		Post post = postRepository.findById(request.getPostId())
			.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_POST));

		// 댓글 내용 안전하게 필터링 (XSS 방지)
		String safeContent = sanitizeContent(request.getContent());

		// 부모 댓글이 있다면 조회, 없으면 null
		PostComment parentComment = null;
		if (request.getParentCommentId() != null) {
			parentComment = postCommentRepository.findById(request.getParentCommentId())
				.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_COMMENT));

			// 중요: 대댓글의 대댓글 작성 방지 (1단계 중첩만 허용)
			if (parentComment.getParent() != null) {
				throw new BusinessException(BoardErrorCode.NESTED_COMMENT_NOT_ALLOWED);
			}

			// 부모 댓글이 현재 게시글에 속하는지 검증
			if (!parentComment.getPost().getId().equals(post.getId())) {
				throw new BusinessException(BoardErrorCode.PARENT_COMMENT_NOT_BELONG_TO_POST);
			}
		}

		// 댓글 생성 (대댓글이면 parent 설정)
		PostComment newComment = PostComment.builder()
			.post(post)
			.member(currentMember)
			.parent(parentComment)
			.content(safeContent)
			.build();

		if (parentComment != null) {
			parentComment.addReply(newComment);
		} else {
			post.addPostComment(newComment);
		}

		currentMember.addPostComment(newComment);
		postCommentRepository.save(newComment);
	}

	@Override
	@Transactional
	public void updateComment(PostCommentUpdateReq request) {
		PostComment comment = postCommentRepository.findById(request.getCommentId())
			.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_COMMENT));

		validateWriter(comment);

		String safeContent = sanitizeContent(request.getContent());
		comment.setContent(safeContent);
	}

	@Override
	@Transactional
	public void deleteComment(Long commentId) {
		PostComment comment = postCommentRepository.findById(commentId)
			.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_COMMENT));

		Member currentMember = validateWriter(comment);

		// 댓글에 대댓글이 있는지 확인
		boolean hasReplies = !comment.getReplies().isEmpty();

		// 대댓글이 있으면 내용만 "삭제된 댓글입니다"로 변경 (소프트 삭제)
		if (hasReplies) {
			comment.markAsDeleted();
		} else {
			// 대댓글이 없는 경우, 연관관계 정리 후 실제 삭제
			PostComment parentComment = comment.getParent();

			// 부모-자식 관계 정리
			if (parentComment != null) {
				parentComment.removeReply(comment);
				comment.setParent(null);
			} else {
				// 최상위 댓글인 경우 게시글에서 제거
				comment.getPost().removePostComment(comment);
			}

			// 작성자와의 연관관계 제거
			currentMember.removePostComment(comment);

			// DB에서 실제 삭제
			postCommentRepository.delete(comment);
		}
	}

	@Override
	public List<PostCommentRes> getComments(Long postId, Pageable pageable) {

		if (!postRepository.existsById(postId)) {
			throw new BusinessException(BoardErrorCode.NOT_EXIST_POST);
		}

		// 게시글의 최상위 댓글만 먼저 조회 (parent가 null인 댓글)
		List<PostComment> rootComments = postCommentRepository.findByPost_IdAndParentIsNull(postId, pageable);

		// 모든 댓글을 계층 구조로 변환
		return rootComments.stream()
			.map(this::convertToCommentResponse)
			.collect(Collectors.toList());
	}

	private PostCommentRes convertToCommentResponse(PostComment comment) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// 하위 댓글들 재귀적으로 변환
		List<PostCommentRes> childResponses = comment.getReplies().stream()
			.map(this::convertToCommentResponse)
			.collect(Collectors.toList());

		LocalDateTime updatedAt = comment.getUpdatedAt();
		if (updatedAt == null) {
			updatedAt = comment.getCreatedAt();
		}

		return PostCommentRes.builder()
			.commentId(comment.getId())
			.postId(comment.getPost().getId())
			.verifyId(comment.getMember().getVerifyId())
			.memberName(comment.getMember().getUsername())
			.content(comment.isDeleted() ? comment.getDisplayContent() : comment.getContent())
			.createdAt(updatedAt.format(formatter))
			.replies(childResponses)  // 대댓글 목록
			.build();
	}

	private String sanitizeContent(String content) {
		return Jsoup.clean(content, Safelist.basic());
	}

	private Member validateWriter(PostComment comment) {
		Member currentMember = securityUtil.getCurrentMember();
		if (!comment.getMember().equals(currentMember)) {
			throw new BusinessException(BoardErrorCode.NOT_WRITER);
		}
		return currentMember;
	}
}

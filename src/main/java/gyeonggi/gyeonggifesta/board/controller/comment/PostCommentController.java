package gyeonggi.gyeonggifesta.board.controller.comment;

import gyeonggi.gyeonggifesta.board.dto.comment.request.PostCommentReq;
import gyeonggi.gyeonggifesta.board.dto.comment.request.PostCommentUpdateReq;
import gyeonggi.gyeonggifesta.board.dto.comment.response.PostCommentRes;
import gyeonggi.gyeonggifesta.board.service.comment.PostCommentService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class PostCommentController {

	private final PostCommentService postCommentService;

	/**
	 * 댓글 등록
	 * 요청 본문: PostCommentReq (postId, parentCommentId (옵션), content)
	 */

	@PostMapping("/posts/comments")
	public ResponseEntity<Response<Void>> createComment(@RequestBody PostCommentReq request) {
		postCommentService.createComment(request);

		return Response.ok().toResponseEntity();
	}

	@PatchMapping("/posts/comments")
	public ResponseEntity<Response<Void>> updateComment(@RequestBody PostCommentUpdateReq request) {
		postCommentService.updateComment(request);

		return Response.ok().toResponseEntity();
	}

	@DeleteMapping("/posts/comments/{commentId}")
	public ResponseEntity<Response<Void>> deleteComment(@PathVariable Long commentId) {
		postCommentService.deleteComment(commentId);

		return Response.ok().toResponseEntity();
	}

	@GetMapping("/posts/{postId}/comments")
	public ResponseEntity<Response<List<PostCommentRes>>> getComments(@PathVariable Long postId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		List<PostCommentRes> comments = postCommentService.getComments(postId, pageable);

		return Response.ok(comments).toResponseEntity();
	}
}

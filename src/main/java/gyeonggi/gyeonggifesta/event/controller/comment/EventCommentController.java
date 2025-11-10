package gyeonggi.gyeonggifesta.event.controller.comment;

import gyeonggi.gyeonggifesta.event.dto.comment.request.EventCommentReq;
import gyeonggi.gyeonggifesta.event.dto.comment.request.EventCommentUpdateReq;
import gyeonggi.gyeonggifesta.event.dto.comment.response.EventCommentRes;
import gyeonggi.gyeonggifesta.event.service.comment.EventCommentService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user/event")
public class EventCommentController {

	private final EventCommentService eventCommentService;

	/**
	 * 댓글 생성
	 *
	 * @param request 댓글 작성 요청
	 */
	@PostMapping("/comment")
	public ResponseEntity<Response<Void>> createComment(@RequestBody EventCommentReq request) {
		eventCommentService.createComment(request);

		return Response.ok().toResponseEntity();
	}

	/**
	 * 댓글 수정
	 *
	 * @param request 댓글 수정 요청
	 */
	@PatchMapping("/comment")
	public ResponseEntity<Response<Void>> updateComment(@RequestBody EventCommentUpdateReq request) {
		eventCommentService.updateComment(request);

		return Response.ok().toResponseEntity();
	}

	/**
	 * 댓글 삭제
	 *
	 * @param commentId 삭제할 댓글 id
	 */
	@DeleteMapping("/comment/{commentId}")
	public ResponseEntity<Response<Void>> deleteComment(@PathVariable Long commentId) {
		eventCommentService.deleteComment(commentId);

		return Response.ok().toResponseEntity();
	}

	/**
	 * 댓글 조회
	 * 특정 이벤트의 댓글을 페이징 및 정렬 조건(최신순/오래된순)에 따라 조회합니다.
	 * 프론트엔드에서는 page 번호를 1부터 전달하며, 기본 정렬은 최신순(내림차순)입니다.
	 *
	 * @param eventId 조회할 이벤트 id
	 * @param page    프론트엔드 기준 페이지 번호 (1부터 시작, 기본값 1)
	 * @param size    페이지당 항목 수 (기본값 10)
	 * @param sort    정렬 방식 ("new" 또는 "old", 기본값 "new")
	 * @return 댓글 정보를 담은 Page 객체
	 */
	@GetMapping("/comment")
	public ResponseEntity<Response<Page<EventCommentRes>>> getComments(
		@RequestParam("eventId") Long eventId,
		@RequestParam(name = "page", defaultValue = "1") int page,
		@RequestParam(name = "size", defaultValue = "10") int size,
		@RequestParam(name = "sort", defaultValue = "new") String sort) {

		Sort sortOrder;
		if ("old".equalsIgnoreCase(sort)) {
			sortOrder = Sort.by("createdAt").ascending();
		} else {
			sortOrder = Sort.by("createdAt").descending();
		}
		Pageable pageable = PageRequest.of(page - 1, size, sortOrder);

		Page<EventCommentRes> commentPage = eventCommentService.getComments(eventId, pageable);
		return Response.ok(commentPage).toResponseEntity();
	}
}

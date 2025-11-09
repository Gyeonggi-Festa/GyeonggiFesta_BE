package gyeonggi.gyeonggifesta.event.service.comment;

import gyeonggi.gyeonggifesta.event.dto.comment.request.EventCommentReq;
import gyeonggi.gyeonggifesta.event.dto.comment.request.EventCommentUpdateReq;
import gyeonggi.gyeonggifesta.event.dto.comment.response.EventCommentRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventCommentService {

	/**
	 * 댓글 생성
	 *
	 * @param request 댓글 작성 요청
	 */
	void createComment(EventCommentReq request);

	/**
	 * 댓글 수정
	 *
	 * @param request 댓글 수정 요청
	 */
	void updateComment(EventCommentUpdateReq request);

	/**
	 * 댓글 삭제
	 *
	 * @param commentId 삭제할 댓글 id
	 */
	void deleteComment(Long commentId);

	/**
	 * 특정 이벤트의 댓글들을 페이징과 정렬 조건에 따라 조회합니다.
	 *
	 * @param eventId  조회할 이벤트 id
	 * @param pageable 페이징 정보 (페이지 번호, 사이즈, 정렬 조건 등)
	 * @return 댓글 정보를 담은 Page 객체
	 */
	Page<EventCommentRes> getComments(Long eventId, Pageable pageable);

}

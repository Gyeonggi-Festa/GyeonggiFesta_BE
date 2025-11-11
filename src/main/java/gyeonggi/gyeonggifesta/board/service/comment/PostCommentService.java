package gyeonggi.gyeonggifesta.board.service.comment;

import gyeonggi.gyeonggifesta.board.dto.comment.request.PostCommentReq;
import gyeonggi.gyeonggifesta.board.dto.comment.request.PostCommentUpdateReq;
import gyeonggi.gyeonggifesta.board.dto.comment.response.PostCommentRes;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostCommentService {

	void createComment(PostCommentReq request);
	void updateComment(PostCommentUpdateReq request);
	void deleteComment(Long commentId);
	List<PostCommentRes> getComments(Long postId, Pageable pageable);
}

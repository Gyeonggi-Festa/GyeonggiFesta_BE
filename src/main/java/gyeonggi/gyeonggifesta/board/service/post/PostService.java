package gyeonggi.gyeonggifesta.board.service.post;

import gyeonggi.gyeonggifesta.board.dto.post.request.CreatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.request.UpdatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostListRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

	// 게시글 생성
	void createPost(CreatePostReq request);

	Page<PostListRes> getPosts(Long boardId, Pageable pageable);

	// 게시글 단건 조회
	PostRes getPost(Long postId);

	// 게시글 수정
	void updatePost(Long postId, UpdatePostReq request);

	// 게시글 삭제
	void deletePost(Long postId);
}

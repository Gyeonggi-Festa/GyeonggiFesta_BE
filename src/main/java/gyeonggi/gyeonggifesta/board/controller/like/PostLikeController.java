package gyeonggi.gyeonggifesta.board.controller.like;

import gyeonggi.gyeonggifesta.board.dto.post.response.PostListRes;
import gyeonggi.gyeonggifesta.board.service.like.PostLikeService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class PostLikeController {

	private final PostLikeService postLikeService;

	@PostMapping("/posts/{postId}/like")
	public ResponseEntity<Response<Void>> likePost(@PathVariable Long postId) {
		postLikeService.likePost(postId);

		return Response.ok().toResponseEntity();
	}

	@DeleteMapping("/posts/{postId}/like")
	public ResponseEntity<Response<Void>> unlikePost(@PathVariable Long postId) {
		postLikeService.unlikePost(postId);

		return Response.ok().toResponseEntity();
	}

	@GetMapping("/posts/likes")
	public ResponseEntity<Response<Page<PostListRes>>> getLikedPosts(Pageable pageable) {
		Page<PostListRes> likedPosts = postLikeService.getLikedPosts(pageable);

		return Response.ok(likedPosts).toResponseEntity();
	}
}

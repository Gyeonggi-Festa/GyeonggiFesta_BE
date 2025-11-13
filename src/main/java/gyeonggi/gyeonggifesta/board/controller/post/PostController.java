package gyeonggi.gyeonggifesta.board.controller.post;

import gyeonggi.gyeonggifesta.board.dto.post.request.CreatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.request.UpdatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostEventOptionRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostListRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostRes;
import gyeonggi.gyeonggifesta.board.service.post.PostService;
import gyeonggi.gyeonggifesta.event.enums.Status;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class PostController {

    private final PostService postService;

    @PostMapping("/posts")
    public ResponseEntity<Response<Void>> createPost(@RequestBody CreatePostReq request) {
        postService.createPost(request);

        return Response.ok().toResponseEntity();
    }

    /**
     * 행사별 게시글 목록 조회 (페이징, 정렬 지원)
     * URL 예시: /api/auth/user/events/{eventId}/posts?sort=latest&page=1&size=10
     * sort 파라미터는 "latest", "likes", "comments" 중 하나
     */
    @GetMapping("/events/{eventId}/posts")
    public ResponseEntity<Page<PostListRes>> getPosts(
            @PathVariable Long eventId,
            @RequestParam(name = "sort", defaultValue = "latest") String sort,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        String sortType = sort.toLowerCase();
        Sort sortCriteria = "latest".equals(sortType)
                ? Sort.by("updatedAt").descending()
                : Sort.unsorted();

        PageRequest pageable = PageRequest.of(page - 1, size, sortCriteria);
        Page<PostListRes> postsPage = postService.getPosts(eventId, pageable, sortType);
        return ResponseEntity.ok(postsPage);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Response<PostRes>> getPost(@PathVariable Long postId) {
        PostRes postRes = postService.getPost(postId);

        return Response.ok(postRes).toResponseEntity();
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<Response<Void>> updatePost(@PathVariable Long postId, @RequestBody UpdatePostReq request) {
        postService.updatePost(postId, request);

        return Response.ok().toResponseEntity();
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Response<Void>> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);

        return Response.ok().toResponseEntity();
    }

    @GetMapping("/posts/events")
    public ResponseEntity<Response<List<PostEventOptionRes>>> getAvailableEvents(
            @RequestParam(name = "status", required = false) Status status
    ) {
        List<PostEventOptionRes> events = postService.getAvailableEvents(status);
        return Response.ok(events).toResponseEntity();
    }
}

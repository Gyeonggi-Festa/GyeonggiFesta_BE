package gyeonggi.gyeonggifesta.board.service.post;

import gyeonggi.gyeonggifesta.board.dto.post.request.CreatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.request.UpdatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.response.EventOptionRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostListRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

        void createPost(CreatePostReq request);

        Page<PostListRes> getPosts(Long eventId, Pageable pageable);

        PostRes getPost(Long postId);

        void updatePost(Long postId, UpdatePostReq request);

        void deletePost(Long postId);

        List<EventOptionRes> getActiveEvents();
}

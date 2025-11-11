package gyeonggi.gyeonggifesta.board.service.media;

import gyeonggi.gyeonggifesta.board.entity.Post;

import java.util.List;

public interface PostMediaService {

	void createPostMedia(Post post, List<String> keyList);

	void updatePostMedia(Post post, List<String> keyList);

	void removePostMedia(Post post);
}

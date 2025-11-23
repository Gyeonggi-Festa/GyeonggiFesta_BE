package gyeonggi.gyeonggifesta.board.service.like;

import gyeonggi.gyeonggifesta.board.dto.post.response.PostListRes;
import gyeonggi.gyeonggifesta.board.entity.Post;
import gyeonggi.gyeonggifesta.board.entity.PostLike;
import gyeonggi.gyeonggifesta.board.exception.BoardErrorCode;
import gyeonggi.gyeonggifesta.board.repository.PostLikeRepository;
import gyeonggi.gyeonggifesta.board.repository.PostRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

	private final PostLikeRepository postLikeRepository;
	private final PostRepository postRepository;
	private final SecurityUtil securityUtil;

	@Override
	@Transactional
	public void likePost(Long postId) {
		Member member = securityUtil.getCurrentMember();

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_POST));

		if (postLikeRepository.existsByPostAndMember(post, member)) {
			throw new BusinessException(BoardErrorCode.ALREADY_LIKED);
		}

		PostLike like = PostLike.builder()
				.post(post)
				.member(member)
				.build();

		member.addPostLike(like);
		postLikeRepository.save(like);
	}

	@Override
	@Transactional
	public void unlikePost(Long postId) {

		Member member = securityUtil.getCurrentMember();
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_POST));

		PostLike like = postLikeRepository.findByPostAndMember(post, member)
				.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_LIKE));

		member.removePostLike(like);
		postLikeRepository.delete(like);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PostListRes> getLikedPosts(Pageable pageable) {

		Member currentMember = securityUtil.getCurrentMember();
		return postLikeRepository.findByMember(currentMember, pageable)
				.map(like -> {
					Post post = like.getPost();
					return PostListRes.builder()
							.postId(post.getId())
							.title(post.getTitle())
							.writer(post.getMember().getUsername())
							.viewCount(post.getViewCount())
							.likes(post.getPostLikes().size())
							.comments(post.getPostComments().size())
							.updatedAt(post.getUpdatedAt().toLocalDate())
							.build();
				});
	}
}

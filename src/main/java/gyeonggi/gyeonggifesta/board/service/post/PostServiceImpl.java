package gyeonggi.gyeonggifesta.board.service.post;

import gyeonggi.gyeonggifesta.board.dto.post.request.CreatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.request.UpdatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostListRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostRes;
import gyeonggi.gyeonggifesta.board.entity.Board;
import gyeonggi.gyeonggifesta.board.entity.Post;
import gyeonggi.gyeonggifesta.board.exception.BoardErrorCode;
import gyeonggi.gyeonggifesta.board.repository.BoardRepository;
import gyeonggi.gyeonggifesta.board.repository.PostRepository;
import gyeonggi.gyeonggifesta.board.service.media.PostMediaService;
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
public class PostServiceImpl implements PostService {

	private final SecurityUtil securityUtil;
	private final PostMediaService postMediaService;
	private final BoardRepository boardRepository;
	private final PostRepository postRepository;

	@Override
	@Transactional
	public void createPost(CreatePostReq request) {

		Member currentMember = securityUtil.getCurrentMember();

		Board board = validateBoard(request.getBoardId());

		Post post = Post.builder()
			.board(board)
			.member(currentMember)
			.title(request.getTitle())
			.content(request.getContent())
			.build();

		currentMember.addPost(post);
		board.addPost(post);

		postMediaService.createPostMedia(post, request.getKeyList());
		postRepository.save(post);

	}

	@Override
	public Page<PostListRes> getPosts(Long boardId, Pageable pageable) {

		validateBoard(boardId);

		Page<Post> postsPage;
		if (pageable.getSort() != null && pageable.getSort().isSorted()) {
			// 정렬 조건 중 첫 번째 필드명을 가져옵니다.
			String property = pageable.getSort().iterator().next().getProperty();
			if ("likesCount".equals(property)) {
				postsPage = postRepository.findByBoardIdOrderByLikesCountDesc(boardId, pageable);
			} else if ("commentsCount".equals(property)) {
				postsPage = postRepository.findByBoardIdOrderByCommentsCountDesc(boardId, pageable);
			} else {
				postsPage = postRepository.findByBoardId(boardId, pageable);
			}
		} else {
			postsPage = postRepository.findByBoardId(boardId, pageable);
		}

		return postsPage.map(post -> PostListRes.builder()
			.postId(post.getId())
			.title(post.getTitle())
			.writer(post.getMember().getUsername())
			.viewCount(post.getViewCount())
			.likes(post.getPostLikes().size())
			.comments(post.getPostComments().size())
			.updatedAt(post.getUpdatedAt().toLocalDate())
			.build());
	}

	@Override
	@Transactional
	public PostRes getPost(Long postId) {
		Post post = validatePost(postId);

		post.increaseViewCount();

		return PostRes.builder()
			.postId(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.writer(post.getMember().getUsername())
			.viewCount(post.getViewCount())
			.likes(post.getPostLikes().size())
			.comments(post.getPostComments().size())
			.updatedAt(post.getUpdatedAt())
			.build();
	}

	/**
	 * 업데이트 시:
	 * - 게시글의 제목/내용 업데이트 후,
	 * - 전달받은 keyList와 기존 DB에 저장된 미디어의 S3Key를 비교하여
	 * - DB에는 있었으나 새 keyList에 없는 미디어는 S3에서 삭제 후 DB에서도 제거
	 * - 새롭게 추가된 key는 PostMedia 엔티티를 생성하여 추가
	 */
	@Override
	@Transactional
	public void updatePost(Long postId, UpdatePostReq request) {
		Post post = validatePost(postId);
		validateMember(post);

		post.setTitle(request.getTitle());
		post.setContent(request.getContent());

		postMediaService.updatePostMedia(post, request.getKeyList());

	}

	/**
	 * 게시글 삭제 시:
	 * - 게시글에 연결된 모든 미디어의 S3Key를 이용해 S3 파일을 삭제한 후,
	 * - DB에서 게시글과 관련된 미디어도 삭제합니다.
	 */
	@Override
	@Transactional
	public void deletePost(Long postId) {

		Post post = validatePost(postId);
		Member member = validateMember(post);
		Board board = post.getBoard();

		postMediaService.removePostMedia(post);

		member.removePost(post);
		board.removePost(post);

		postRepository.delete(post);
	}

	private Board validateBoard(Long boardId) {
		return boardRepository.findById(boardId)
			.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_BOARD));
	}

	private Post validatePost(Long postId) {
		return postRepository.findById(postId)
			.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_POST));
	}

	private Member validateMember(Post post) {
		Member currentMember = securityUtil.getCurrentMember();

		if (!post.getMember().equals(currentMember)) {
			throw new BusinessException(BoardErrorCode.NOT_WRITER);
		}

		return currentMember;
	}
}

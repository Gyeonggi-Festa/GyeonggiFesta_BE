package gyeonggi.gyeonggifesta.recommand.service;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import gyeonggi.gyeonggifesta.recommand.dto.response.AiRecommendRes;
import gyeonggi.gyeonggifesta.recommand.entity.AiRecommendation;
import gyeonggi.gyeonggifesta.recommand.repository.AiRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationSaveService {

	private final MemberRepository memberRepository;
	private final EventRepository eventRepository;
	private final AiRecommendationRepository aiRecommendationRepository;

	/**
	 * AI 추천 결과를 저장하는 메서드
	 *
	 * @param response AI 추천 응답 DTO
	 */
	@Transactional
	public void saveRecommendations(AiRecommendRes response) {
		if (response == null || CollectionUtils.isEmpty(response.getFestivalRecommendations())) {
			log.warn("추천 결과가 비어있습니다.");
			return;
		}

		// 사용자 ID로 멤버 조회
		Member member = findMemberByVerifyId(response.getUserid());
		if (member == null) {
			log.error("사용자 ID {}에 해당하는 회원을 찾을 수 없습니다.", response.getUserid());
			return;
		}

		// 추천 결과 저장
		List<AiRecommendation> savedRecommendations = new ArrayList<>();

		// festivalRecommendations 리스트의 첫 번째 항목에 있는 eventid 목록 처리
		if (!response.getFestivalRecommendations().isEmpty()) {
			List<String> eventIds = response.getFestivalRecommendations().get(0).getEventid();

			for (String eventId : eventIds) {
				try {
					// 이벤트 ID로 이벤트 조회
					Event event = findEventById(eventId);
					if (event == null) {
						log.warn("이벤트 ID {}에 해당하는 이벤트를 찾을 수 없습니다.", eventId);
						continue;
					}

					// 추천 정보 저장
					AiRecommendation recommendation = createRecommendation(member, event);
					savedRecommendations.add(aiRecommendationRepository.save(recommendation));
				} catch (Exception e) {
					log.error("이벤트 ID {}의 추천 정보 저장 중 오류 발생: {}", eventId, e.getMessage());
				}
			}
		}

		log.info("사용자 {}의 추천 정보 {}건 저장 완료", member.getVerifyId(), savedRecommendations.size());
	}

	/**
	 * 사용자 ID(verifyId)로 회원을 조회
	 *
	 * @param userId 사용자 ID(verifyId)
	 * @return 회원 객체
	 */
	private Member findMemberByVerifyId(String userId) {
		return memberRepository.findByVerifyId(userId).orElse(null);
	}

	/**
	 * 이벤트 ID로 이벤트를 조회
	 *
	 * @param eventId 이벤트 ID
	 * @return 이벤트 객체
	 */
	private Event findEventById(String eventId) {
		try {
			Long id = Long.parseLong(eventId);
			return eventRepository.findById(id).orElse(null);
		} catch (NumberFormatException e) {
			log.error("이벤트 ID {} 변환 중 오류 발생", eventId);
			return null;
		}
	}

	/**
	 * 회원과 이벤트로 추천 정보 엔티티 생성
	 *
	 * @param member 회원
	 * @param event 이벤트
	 * @return 추천 정보 엔티티
	 */
	private AiRecommendation createRecommendation(Member member, Event event) {
		return AiRecommendation.builder()
			.member(member)
			.event(event)
			.build();
	}
}
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
		if (response == null) {
			log.warn("AI 추천 응답이 null 입니다. 저장을 건너뜁니다.");
			return;
		}

		if (CollectionUtils.isEmpty(response.getFestivalRecommendations())) {
			log.warn("사용자 {}의 추천 결과(festivalRecommendations)가 비어 있습니다. 저장을 건너뜁니다.", response.getUserid());
			return;
		}

		// 사용자 ID로 멤버 조회
		Member member = findMemberByVerifyId(response.getUserid());
		if (member == null) {
			log.error("사용자 ID {}에 해당하는 회원을 찾을 수 없습니다. 추천 결과 저장을 건너뜁니다.", response.getUserid());
			return;
		}

		List<AiRecommendation> savedRecommendations = new ArrayList<>();

		// 여러 FestivalRecommendation 이 올 수 있으니 전부 순회
		response.getFestivalRecommendations().forEach(fr -> {
			List<String> eventIds = fr.getEventid();

			// 🔥 여기서 null/빈 리스트 방어
			if (CollectionUtils.isEmpty(eventIds)) {
				log.warn("사용자 {}의 추천 결과 중 eventid 리스트가 비어 있습니다. 이 항목은 건너뜁니다.", member.getVerifyId());
				return;
			}

			for (String eventId : eventIds) {
				try {
					Event event = findEventById(eventId);
					if (event == null) {
						log.warn("이벤트 ID {}에 해당하는 이벤트를 찾을 수 없습니다. 저장을 건너뜁니다.", eventId);
						continue;
					}

					AiRecommendation recommendation = createRecommendation(member, event);
					savedRecommendations.add(aiRecommendationRepository.save(recommendation));
				} catch (Exception e) {
					log.error("이벤트 ID {}의 추천 정보 저장 중 오류 발생: {}", eventId, e.getMessage());
				}
			}
		});

		if (savedRecommendations.isEmpty()) {
			log.info("사용자 {}의 유효한 추천 정보가 없어 저장된 추천이 없습니다.", member.getVerifyId());
		} else {
			log.info("사용자 {}의 추천 정보 {}건 저장 완료", member.getVerifyId(), savedRecommendations.size());
		}
	}

	/**
	 * 사용자 ID(verifyId)로 회원을 조회
	 */
	private Member findMemberByVerifyId(String userId) {
		return memberRepository.findByVerifyId(userId).orElse(null);
	}

	/**
	 * 이벤트 ID로 이벤트를 조회
	 */
	private Event findEventById(String eventId) {
		try {
			Long id = Long.parseLong(eventId);
			return eventRepository.findById(id).orElse(null);
		} catch (NumberFormatException e) {
			log.error("이벤트 ID {}를 Long 타입으로 변환하는 중 오류 발생", eventId);
			return null;
		}
	}

	/**
	 * 회원과 이벤트로 추천 정보 엔티티 생성
	 */
	private AiRecommendation createRecommendation(Member member, Event event) {
		return AiRecommendation.builder()
				.member(member)
				.event(event)
				.build();
	}
}

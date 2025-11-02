package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.enums.Status;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event 엔티티에 대한 검색 조건 명세를 정의하는 클래스
 */
public class EventSpecifications {

	/**
	 * 이벤트 상태 필터링 명세 생성
	 */
	public static Specification<Event> hasStatus(Status status) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("status"), status);
	}

	/**
	 * 유료/무료 필터링 명세 생성
	 */
	public static Specification<Event> hasIsFree(String isFree) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("isFree"), isFree);
	}

	/**
	 * 카테고리(codename) 필터링 명세 생성
	 */
	public static Specification<Event> hasCodename(String codename) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("codename"), codename);
	}

	/**
	 * 구 이름 필터링 명세 생성
	 */
	public static Specification<Event> hasGuName(String guName) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("guName"), guName);
	}

	/**
	 * 제목 검색어를 포함하는 명세 생성
	 */
	public static Specification<Event> titleContains(String keyword) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
				"%" + keyword.toLowerCase() + "%");
	}

	/**
	 * 이벤트 기간이 주어진 날짜 범위와 겹치는지 확인하는 명세 생성
	 * LocalDate를 사용하여 날짜만 비교 (시간 정보 무시)
	 *
	 * @param startDate 검색 시작 날짜
	 * @param endDate 검색 종료 날짜
	 * @return 생성된 명세
	 */
	public static Specification<Event> dateRangeOverlaps(LocalDate startDate, LocalDate endDate) {
		return (root, query, criteriaBuilder) -> {
			if (startDate == null && endDate == null) {
				return criteriaBuilder.conjunction();
			}

			// startDate를 LocalDateTime의 시작 시간(00:00:00)으로 변환
			Expression<LocalDateTime> eventStartDate = root.get("startDate");
			Expression<LocalDateTime> eventEndDate = root.get("endDate");

			// 시작일만 존재하는 경우: 이벤트 종료일 >= 검색 시작일
			if (startDate != null && endDate == null) {
				LocalDateTime startOfDay = startDate.atStartOfDay();
				return criteriaBuilder.greaterThanOrEqualTo(eventEndDate, startOfDay);
			}

			// 종료일만 존재하는 경우: 이벤트 시작일 <= 검색 종료일의 끝
			if (startDate == null && endDate != null) {
				LocalDateTime endOfDay = endDate.atTime(23, 59, 59);
				return criteriaBuilder.lessThanOrEqualTo(eventStartDate, endOfDay);
			}

			// 두 날짜 범위가 겹치는 조건:
			// (이벤트 시작일 <= 검색 종료일의 끝) AND (이벤트 종료일 >= 검색 시작일의 시작)
			LocalDateTime startOfDay = startDate.atStartOfDay();
			LocalDateTime endOfDay = endDate.atTime(23, 59, 59);

			return criteriaBuilder.and(
				criteriaBuilder.lessThanOrEqualTo(eventStartDate, endOfDay),
				criteriaBuilder.greaterThanOrEqualTo(eventEndDate, startOfDay)
			);
		};
	}
}
package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.enums.Status;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
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
		return (root, query, cb) -> {
			if (startDate == null && endDate == null) {
				return cb.conjunction();
			}

			// Event 엔티티의 컬럼 타입이 LocalDate 이므로 LocalDate로 다뤄야 안전
			Path<LocalDate> evStart = root.get("startDate");
			Path<LocalDate> evEnd   = root.get("endDate");

			// 검색 시작만 지정: evEnd >= startDate  (evEnd == null -> open ended 로 간주: 매칭)
			if (startDate != null && endDate == null) {
				return cb.or(
						cb.isNull(evEnd),
						cb.greaterThanOrEqualTo(evEnd, startDate)
				);
			}

			// 검색 종료만 지정: evStart <= endDate  (evStart == null -> open started 로 간주: 매칭)
			if (startDate == null) { // endDate != null
				return cb.or(
						cb.isNull(evStart),
						cb.lessThanOrEqualTo(evStart, endDate)
				);
			}

			// 둘 다 지정: (evStart <= endDate) AND (evEnd >= startDate)
			// evStart/evEnd 가 null일 수 있으므로 각각 open-ended 로 허용
			return cb.and(
					cb.or(
							cb.isNull(evStart),
							cb.lessThanOrEqualTo(evStart, endDate)
					),
					cb.or(
							cb.isNull(evEnd),
							cb.greaterThanOrEqualTo(evEnd, startDate)
					)
			);
		};
	}
}
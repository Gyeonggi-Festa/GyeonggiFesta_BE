// path: src/main/java/gyeonggi/gyeonggifesta/event/repository/EventRepository.java
package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

	// 스키마 기준: 제목 + 등록일 + 카테고리 로 중복체크
	Optional<Event> findByTitleAndRegisterDateAndCodename(String title, LocalDate registerDate, String codename);

	List<Event> findAllByStatus(Status status);
}

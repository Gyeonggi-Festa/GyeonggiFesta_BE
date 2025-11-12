package gyeonggi.gyeonggifesta.event.repository.projection;

public interface TopLikedEventView {
    Long getEventId();
    Long getLikeCount(); // COUNT → Long 매핑
}

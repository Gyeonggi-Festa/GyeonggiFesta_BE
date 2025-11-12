package gyeonggi.gyeonggifesta.event.repository.projection;

public interface TopLikedEventView {
    Long getEventId();
    long getLikeCount();
}

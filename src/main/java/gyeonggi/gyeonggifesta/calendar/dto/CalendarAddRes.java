package gyeonggi.gyeonggifesta.calendar.dto;

public class CalendarAddRes {
    private final String eventId;

    public CalendarAddRes(String eventId) {
        this.eventId = eventId == null ? "" : eventId;
    }

    public String getEventId() {
        return eventId;
    }
}
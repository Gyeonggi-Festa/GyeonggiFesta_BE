package gyeonggi.gyeonggifesta.board.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderPreference {
    FEMALE("여성"),
    MALE("남성"),
    ANY("상관없음");

    private final String description;
}

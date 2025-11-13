package gyeonggi.gyeonggifesta.board.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeRange {
        TWENTIES("20대"),
        TWENTY_TO_THIRTY_FIVE("20-35세"),
        THIRTY_FIVE_TO_FORTY("35-40세"),
        FORTY_TO_FIFTY_FIVE("40-55세"),
        ANY("상관없음");

        private final String description;
}

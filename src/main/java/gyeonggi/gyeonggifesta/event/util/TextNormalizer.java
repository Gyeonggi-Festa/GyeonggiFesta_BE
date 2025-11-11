package gyeonggi.gyeonggifesta.event.util;

public class TextNormalizer {

  /**
   * 문자열 정규화:
   * - 전각 공백/nbsp 제거
   * - 다중 공백 1칸
   * - 좌우 trim
   * - 일부 전각/특수 괄호 통일
   */
  public static String norm(String s) {
    if (s == null) return "";
    String t = s.replace('\u3000',' ')
            .replace('\u00A0',' ')
            .replaceAll("\\s+", " ")
            .trim();
    t = t.replace('〈','<').replace('〉','>')
            .replace('《','<').replace('》','>')
            .replace('「','\"').replace('」','\"')
            .replace('『','\"').replace('』','\"');
    return t;
  }

  /**
   * URL 길이 방어: null 허용, 너무 길면 앞쪽 2048자로 클램프
   * (DB가 TEXT여도 과도한 길이로 인한 다운스트림 문제 방지용)
   */
  public static String clampUrl(String url) {
    if (url == null) return null;
    final int MAX = 2048;
    return (url.length() > MAX) ? url.substring(0, MAX) : url;
  }

  private TextNormalizer() {}
}

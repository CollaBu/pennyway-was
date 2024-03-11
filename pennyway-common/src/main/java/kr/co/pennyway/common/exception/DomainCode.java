package kr.co.pennyway.common.exception;

/**
 * 도메인 코드
 * <br>
 * 도메인 코드는 각 도메인별로 고유한 코드를 가지고 있으며, 해당 코드는 각 도메인별로 고유한 에러를 구분하기 위해 사용된다.
 * <br>
 * ZERO(0)는 기본 코드로 사용되며, 도메인 코드를 사용하지 않는 경우에 사용한다.
 * <pre>
 * ZERO 필드는 모든 BaseErrorCode를 구현하는 모든 도메인 코드에서 정의되어야 한다.
 * {@code
 * @RequiredArgsConstructor
 * public enum DomainCode implements BaseErrorCode {
 *      ZERO(0),
 *      USER(1),
 *      PRODUCT(2),
 *      ORDER(3);
 *
 *      private final int code;
 *
 *      @Override
 *      public int getCode() {
 *          return code;
 *      }
 *
 *      @Override
 *      public String getDomainName() {
 *          return name().toLowerCase();
 *      }
 * }
 * }
 * </pre>
 * @author YANG JAESEO
 */
public interface DomainCode {
    int getCode();
    String getDomainName();
}

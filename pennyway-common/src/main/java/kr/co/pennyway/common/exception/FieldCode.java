package kr.co.pennyway.common.exception;

/**
 * 도메인의 필드 코드
 * <pre>
 * {@code
 * @RequiredArgsConstructor
 * public enum UserFieldCode implements FieldCode {
 *    ID(1),
 *    PASSWORD(2),
 *    NAME(3);
 *
 *    private final int code;
 *    private final String fieldName;
 *
 *    @Override
 *    public int getCode() {
 *      return code;
 *    }
 *
 *    @Override
 *    public String getFieldName() {
 *      return name().toLowerCase();
 *    }
 * }
 * }
 * </pre>
 * @author YANG JAESEO
 */
public interface FieldCode {
    int getCode();
    String getFieldName();
}

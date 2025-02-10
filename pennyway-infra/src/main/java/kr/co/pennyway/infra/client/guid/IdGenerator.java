package kr.co.pennyway.infra.client.guid;

/**
 * Global Unique Identifier 생성 인터페이스
 * <p>
 * IdGenerator는 Integer, Long 또는 String과 같은 Wrapper 타입으로만 구현해야 한다.
 */
public interface IdGenerator<T> {
    T generate();
}

package kr.co.pennyway.infra.client.guid;

/**
 * Global Unique Identifier 생성 인터페이스
 */
public interface IdGenerator<T> {
    T generate();
}

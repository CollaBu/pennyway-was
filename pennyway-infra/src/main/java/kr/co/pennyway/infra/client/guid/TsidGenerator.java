package kr.co.pennyway.infra.client.guid;

import com.github.f4b6a3.tsid.TsidCreator;

/**
 * Time-Sorted ID 생성 클래스
 */
public class TsidGenerator implements IdGenerator<Long> {

    /**
     * TSID 알고리즘 기반의 timestamp(42bit) + node(8bit) + counter(14bit)로 구성되는 64bit 정수를 반환한다.
     * 생성된 ID는 ms당 16,383개의 Unique Id를 생성할 수 있으며, 정렬 순서는 생성 순서와 동일하다.
     *
     * @see <a href="https://github.com/psychology50/high-concurrency-unique-id-generator-test">GUID별 성능 지표</a>
     */
    @Override
    public Long generate() {
        return TsidCreator.getTsid256().toLong();
    }
}

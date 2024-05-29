package kr.co.pennyway.domain.common.aop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CallTransactionFactory {
    private final RedissonCallNewTransaction redissonCallNewTransaction;
    private final RedissonCallSameTransaction redissonCallSameTransaction;

    public CallTransaction getCallTransaction(boolean isNewTransaction) {
        return isNewTransaction ? redissonCallNewTransaction : redissonCallSameTransaction;
    }
}

package top.meethigher.light.retry;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 重试器
 * 每次执行，需要实例化一个新的重试器
 *
 * @author chenchuancheng
 * @since 2023/09/24 20:08
 */
public class RetryHolder<T> {

    /**
     * 最大重试次数
     */
    private final int maxRetries;

    /**
     * 重试延迟时间
     */
    private final long delayMills;

    /**
     * 验证执行结果
     */
    private final Predicate<T> verifyResult;

    /**
     * 注入异常处理器
     */
    private final Consumer<Exception> exceptionConsumer;

    private RetryHolder(int maxRetries, long delayMills, Predicate<T> verifyResult) {
        this.maxRetries = maxRetries;
        this.delayMills = delayMills;
        this.verifyResult = verifyResult;
        this.exceptionConsumer = null;
    }


    private RetryHolder(int maxRetries, long delayMills, Predicate<T> verifyResult, Consumer<Exception> exceptionConsumer) {
        this.maxRetries = maxRetries;
        this.delayMills = delayMills;
        this.verifyResult = verifyResult;
        this.exceptionConsumer = exceptionConsumer;
    }

    private int getMaxRetries() {
        return maxRetries;
    }

    private long getDelayMills() {
        return delayMills;
    }

    private Predicate<T> getVerifyResult() {
        return verifyResult;
    }

    private Consumer<Exception> getExceptionConsumer() {
        return exceptionConsumer;
    }

    public static <S> RetryHolder<S> getRetryHolder(int maxRetries, long delayMills, Predicate<S> verifyResult) {
        return new RetryHolder<>(maxRetries, delayMills, verifyResult);
    }

    public static <S> RetryHolder<S> getRetryHolder(int maxRetries, long delayMills, Predicate<S> verifyResult, Consumer<Exception> exceptionConsumer) {
        return new RetryHolder<>(maxRetries, delayMills, verifyResult, exceptionConsumer);
    }

    /**
     * 操作出错时，会根据配置进行重试
     *
     * @param operation 操作
     * @return 执行操作后返回的结果
     * @throws Exception 执行过程中的异常
     */
    public T executeWithRetry(Supplier<T> operation) throws Exception {
        int retryCount = 0;
        while (retryCount < getMaxRetries()) {
            try {
                T result = operation.get();
                if (getVerifyResult().test(result)) {
                    return result;
                } else {
                    throw new RuntimeException("The execution result is different from the expected result");
                }
            } catch (Exception e) {
                // 发生异常，增加重试计数
                retryCount++;
                // 打印错误信息，可根据需要记录日志
                if (getExceptionConsumer() != null) {
                    getExceptionConsumer().accept(e);
                }
            }
            // 等待一段时间后进行下一次重试，可以根据需要进行调整
            try {
                TimeUnit.MILLISECONDS.sleep(getDelayMills());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new Exception("Thread interrupted while sleeping");
            }
        }
        throw new Exception("Max retries exceeded");
    }

}

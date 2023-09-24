下载依赖地址https://mvnrepository.com/artifact/top.meethigher/light-retry

使用示例

```java
public static void main(String[] args) throws Exception {
    RetryHolder<Integer> retryHolder = RetryHolder.getRetryHolder(3, 1000L, s -> s == 1, e -> e.printStackTrace());
    retryHolder.executeWithRetry(() -> 4);
}
```


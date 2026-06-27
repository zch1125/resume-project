package com.resume.util;

/**
 * AI 调用重试工具类。
 * 当 AI 大模型调用失败时，采用指数退避策略自动重试，直到达到最大重试次数或总超时时间。
 * 
 * <p>重试参数：</p>
 * <ul>
 *   <li>最大重试次数： {@value #MAX_RETRIES} 次（首次失败 + 3 次重试）</li>
 *   <li>最大总等待时间： {@value #MAX_TOTAL_WAIT_MS}ms</li>
 *   <li>退避策略：基础延迟 {@value #BASE_DELAY_MS}ms ，每次翻倍</li>
 * </ul>
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class AiRetryUtil {

    private static final Logger log = LoggerFactory.getLogger(AiRetryUtil.class);

    private static final int MAX_RETRIES = 3;
    private static final long MAX_TOTAL_WAIT_MS = 60000;
    private static final long BASE_DELAY_MS = 2000;

    public static String callWithRetry(Supplier<String> aiCall, String operationName) {
        long startTime = System.currentTimeMillis();
        Exception lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                return aiCall.get();
            } catch (Exception e) {
                lastException = e;
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = MAX_TOTAL_WAIT_MS - elapsed;

                if (attempt >= MAX_RETRIES || remaining <= 0) {
                    log.error("{} failed after {} attempts (elapsed={}ms)", operationName, attempt + 1, elapsed);
                    break;
                }

                long delay = Math.min(BASE_DELAY_MS * (1L << attempt), remaining);
                log.warn("{} failed (attempt {}/{}), retrying in {}ms: {}",
                        operationName, attempt + 1, MAX_RETRIES + 1, delay, e.getMessage());

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        throw new RuntimeException(operationName + " failed after " + (MAX_RETRIES + 1) + " attempts", lastException);
    }
}

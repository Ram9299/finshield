package com.finshield.fraud;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSlidingWindowCounter {

  private final StringRedisTemplate redis;

  /**
   * Adds an event into a time window and returns current count in the window. Uses ZSET with
   * timestamp score.
   */
  public long addAndCount(String key, long nowMillis, Duration window) {
    String member = nowMillis + "-" + Thread.currentThread().getId();

    long minScore = nowMillis - window.toMillis();

    // ZADD
    redis.opsForZSet().add(key, member, nowMillis);

    // remove old
    redis.opsForZSet().removeRangeByScore(key, 0, minScore);

    // count in window
    Long count = redis.opsForZSet().zCard(key);

    // keep key from living forever
    redis.expire(key, window.plusSeconds(10));

    return count == null ? 0 : count;
  }
}

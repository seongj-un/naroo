package com.example.naroo.auth.adapter.`out`.token

import com.example.naroo.auth.port.`out`.StoredToken
import com.example.naroo.auth.port.`out`.TokenStorePort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration

@Component
class RedisTokenStoreAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val clock: Clock,
) : TokenStorePort {
    override fun save(token: StoredToken) {
        val ttl = Duration.between(clock.instant(), token.expiresAt)
        if (ttl.isNegative || ttl.isZero) {
            return
        }

        redisTemplate.opsForValue().set(
            accessTokenKey(token.tokenId),
            token.userId,
            ttl,
        )
    }

    private fun accessTokenKey(tokenId: String): String {
        return "auth:access-token:$tokenId"
    }
}

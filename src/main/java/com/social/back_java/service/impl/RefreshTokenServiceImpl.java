package com.social.back_java.service.impl;

import com.social.back_java.model.RefreshToken;
import com.social.back_java.model.User;
import com.social.back_java.repository.RefreshTokenRepository;
import com.social.back_java.repository.UserRepository;
import com.social.back_java.service.IRefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.refresh.expiration.days}")
    private int refreshExpirationDays;

    @Override
    public String generateRefreshToken(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate random token
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Calculate expiry date
        Date expiresAt = new Date(System.currentTimeMillis() + (refreshExpirationDays * 24L * 60 * 60 * 1000));

        // Create and save refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setCreatedByIp(ipAddress);

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!refreshToken.isActive()) {
            throw new RuntimeException("Refresh token is no longer active");
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token, String ipAddress, String replacedByToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        refreshToken.setRevokedAt(new Date());
        refreshToken.setRevokedByIp(ipAddress);
        if (replacedByToken != null) {
            refreshToken.setReplacedByToken(replacedByToken);
        }

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllUserTokens(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.findByUser(user).forEach(token -> {
            if (token.isActive()) {
                token.setRevokedAt(new Date());
                token.setRevokedByIp(ipAddress);
                refreshTokenRepository.save(token);
            }
        });
    }
}

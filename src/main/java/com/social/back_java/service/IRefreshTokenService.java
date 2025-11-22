package com.social.back_java.service;

import com.social.back_java.model.RefreshToken;

public interface IRefreshTokenService {
    String generateRefreshToken(Long userId, String ipAddress);
    RefreshToken verifyRefreshToken(String token);
    void revokeRefreshToken(String token, String ipAddress, String replacedByToken);
    void revokeAllUserTokens(Long userId, String ipAddress);
}

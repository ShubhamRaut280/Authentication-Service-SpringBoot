package com.shubham.Expense.Tracker.services;

import com.shubham.Expense.Tracker.models.entities.RefreshToken;
import com.shubham.Expense.Tracker.models.entities.UserInfo;
import com.shubham.Expense.Tracker.repository.RefreshTokenRepository;
import com.shubham.Expense.Tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    UserRepository userRepository;


    public RefreshToken createRefreshToken(String username){
        UserInfo extractedUser = userRepository.findByUsername(username);

        RefreshToken refreshToken = RefreshToken.builder()
                .userInfo(extractedUser)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(600000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + "Refresh token is expired, Please make a new login!");
        }

        return token;
    }
}

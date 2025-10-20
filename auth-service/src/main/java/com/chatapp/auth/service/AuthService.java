package com.chatapp.auth.service;

import com.chatapp.auth.dto.AuthRequest;
import com.chatapp.auth.dto.AuthResponse;
import com.chatapp.auth.dto.RegisterRequest;
import com.chatapp.auth.model.User;
import com.chatapp.auth.repository.UserRepository;
import com.chatapp.common.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public BaseResponse<AuthResponse> register(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            return BaseResponse.error("Phone number already registered", "USER_ALREADY_EXISTS");
        }

        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .build();

        userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("role", user.getRole().name());

        String accessToken = jwtService.generateToken(user.getPhoneNumber(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getPhoneNumber());

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .displayName(user.getDisplayName())
                .build();

        return BaseResponse.success(response, "User registered successfully");
    }

    public BaseResponse<AuthResponse> login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setLastLoginAt(java.time.LocalDateTime.now());
            userRepository.save(user);

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("phoneNumber", user.getPhoneNumber());
            claims.put("role", user.getRole().name());

            String accessToken = jwtService.generateToken(user.getPhoneNumber(), claims);
            String refreshToken = jwtService.generateRefreshToken(user.getPhoneNumber());

            AuthResponse response = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .userId(user.getId())
                    .phoneNumber(user.getPhoneNumber())
                    .displayName(user.getDisplayName())
                    .build();

            return BaseResponse.success(response, "Login successful");
        } catch (Exception e) {
            return BaseResponse.error("Invalid credentials", "INVALID_CREDENTIALS");
        }
    }

    public BaseResponse<AuthResponse> refreshToken(String refreshToken) {
        try {
            if (jwtService.isTokenExpired(refreshToken)) {
                return BaseResponse.error("Refresh token expired", "REFRESH_TOKEN_EXPIRED");
            }

            String username = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByPhoneNumber(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("phoneNumber", user.getPhoneNumber());
            claims.put("role", user.getRole().name());

            String newAccessToken = jwtService.generateToken(user.getPhoneNumber(), claims);
            String newRefreshToken = jwtService.generateRefreshToken(user.getPhoneNumber());

            AuthResponse response = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .userId(user.getId())
                    .phoneNumber(user.getPhoneNumber())
                    .displayName(user.getDisplayName())
                    .build();

            return BaseResponse.success(response, "Token refreshed successfully");
        } catch (Exception e) {
            return BaseResponse.error("Invalid refresh token", "INVALID_REFRESH_TOKEN");
        }
    }

    public BaseResponse<String> logout(String token) {
        // In a production system, you would blacklist the token in Redis
        return BaseResponse.success("Logout successful");
    }
}

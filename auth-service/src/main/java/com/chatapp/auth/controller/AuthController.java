package com.chatapp.auth.controller;

import com.chatapp.auth.dto.AuthRequest;
import com.chatapp.auth.dto.AuthResponse;
import com.chatapp.auth.dto.RegisterRequest;
import com.chatapp.auth.service.AuthService;
import com.chatapp.common.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        BaseResponse<AuthResponse> response = authService.register(request);
        return ResponseEntity.status(response.isSuccess() ? 201 : 400).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        BaseResponse<AuthResponse> response = authService.login(request);
        return ResponseEntity.status(response.isSuccess() ? 200 : 401).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(@RequestHeader("Authorization") String token) {
        String refreshToken = token.replace("Bearer ", "");
        BaseResponse<AuthResponse> response = authService.refreshToken(refreshToken);
        return ResponseEntity.status(response.isSuccess() ? 200 : 401).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(@RequestHeader("Authorization") String token) {
        String accessToken = token.replace("Bearer ", "");
        BaseResponse<String> response = authService.logout(accessToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        // In a production system, you would return the actual JWKS
        Map<String, Object> jwks = new HashMap<>();
        return ResponseEntity.ok(jwks);
    }
}

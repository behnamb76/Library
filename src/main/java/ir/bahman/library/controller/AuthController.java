package ir.bahman.library.controller;

import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.LogoutRequest;
import ir.bahman.library.dto.RefreshRequest;
import ir.bahman.library.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestBody(required = false) LogoutRequest req) {
        String refreshToken = req != null ? req.getRefreshToken() : null;
        return authService.logout(refreshToken);
    }
}

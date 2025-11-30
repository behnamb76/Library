package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.exception.AccessDeniedException;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.enums.AccountStatus;
import ir.bahman.library.security.CustomUserDetailsService;
import ir.bahman.library.security.JwtService;
import ir.bahman.library.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AccountRepository accountRepository;

    public AuthServiceImpl(AuthenticationManager authManager,
                           JwtService jwtService,
                           CustomUserDetailsService userDetailsService, AccountRepository accountRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.accountRepository = accountRepository;
    }


    @Override
    public Map<String, String> login(LoginRequest dto) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        UserDetails user = userDetailsService.loadUserByUsername(dto.getUsername());
        Account account = accountRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found!"));

        if (account.getStatus().equals(AccountStatus.ACTIVE)) {
            account.setAuthId(UUID.randomUUID());
            accountRepository.save(account);

            String accessToken = jwtService.generateAccessToken(account.getAuthId());
            String refreshToken = jwtService.generateRefreshToken(account.getAuthId());

            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            );
        }
        throw new AccessDeniedException("Access denied. Your account is not active.");
    }

    @Override
    public Map<String, String> refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken, "refresh")) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String authId = jwtService.extractAuthId(refreshToken);
        Account account = accountRepository.findByAuthId(UUID.fromString(authId))
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        UUID uuid = UUID.randomUUID();
        account.setAuthId(uuid);
        accountRepository.save(account);

        String newAccess = jwtService.generateAccessToken(uuid);
        String newRefresh = jwtService.generateRefreshToken(uuid);

        return Map.of(
                "accessToken", newAccess,
                "refreshToken", newRefresh
        );
    }

    @Override
    public Map<String, String> logout(String refreshToken) {
        if (refreshToken != null && jwtService.isTokenValid(refreshToken, "refresh")) {
            String authId = jwtService.extractAuthId(refreshToken);
            Account account = accountRepository.findByAuthId(UUID.fromString(authId))
                    .orElseThrow(() -> new EntityNotFoundException("Account not found"));
            account.setAuthId(null);
            accountRepository.save(account);
            return Map.of("message", "User " + account.getUsername() + " logged out successfully");
        }
        return Map.of("message", "Logged out");
    }
}

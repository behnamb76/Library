package ir.bahman.library.security;

import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.model.Account;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AccountRepository accountRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, AccountRepository accountRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isTokenValid(token, "access")) {
                String authId = jwtService.extractAuthId(token);

                Account account = accountRepository.findByAuthId(UUID.fromString(authId))
                        .orElseThrow(() -> new EntityNotFoundException("Account not found"));

                UserDetails user = userDetailsService.loadUserByUsername(account.getUsername());
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }
}

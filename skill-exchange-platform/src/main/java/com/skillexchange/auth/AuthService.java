package com.skillexchange.auth;

import com.skillexchange.exception.DuplicateResourceException;
import com.skillexchange.exception.UnauthorizedException;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registering user {}", request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .active(true)
                .build();
        userRepository.save(user);
        return tokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        log.debug("Authenticating user {}", request.getEmail());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            User user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
            return tokens(user);
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    public AuthResponse refresh(String refreshToken) {
        log.debug("Refreshing JWT");
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!jwtService.validateToken(refreshToken, user)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        return AuthResponse.builder().accessToken(jwtService.generateToken(user)).refreshToken(refreshToken).build();
    }

    private AuthResponse tokens(User user) {
        return AuthResponse.builder().accessToken(jwtService.generateToken(user)).refreshToken(jwtService.generateRefreshToken(user)).build();
    }
}

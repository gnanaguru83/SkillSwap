package com.skillexchange.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillexchange.exception.DuplicateResourceException;
import com.skillexchange.exception.GlobalExceptionHandler;
import com.skillexchange.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    AuthService authService;
    @MockBean
    JwtService jwtService;
    @MockBean
    UserDetailsService userDetailsService;

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = register();
        when(authService.register(any())).thenReturn(tokens());
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt"));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        when(authService.register(any())).thenThrow(new DuplicateResourceException("Email already registered"));
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(register())))
                .andExpect(status().isConflict());
    }

    @Test
    void testLoginSuccess() throws Exception {
        when(authService.login(any())).thenReturn(tokens());
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login("correct-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt"));
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        when(authService.login(any())).thenThrow(new UnauthorizedException("Invalid credentials"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login("wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginNonExistentUser() throws Exception {
        when(authService.login(any())).thenThrow(new UnauthorizedException("Invalid credentials"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login("whatever-password"))))
                .andExpect(status().isUnauthorized());
    }

    private RegisterRequest register() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("ada@example.com");
        request.setPassword("correct-password");
        request.setFullName("Ada Lovelace");
        return request;
    }

    private LoginRequest login(String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail("ada@example.com");
        request.setPassword(password);
        return request;
    }

    private AuthResponse tokens() {
        return AuthResponse.builder().accessToken("jwt").refreshToken("refresh").build();
    }
}


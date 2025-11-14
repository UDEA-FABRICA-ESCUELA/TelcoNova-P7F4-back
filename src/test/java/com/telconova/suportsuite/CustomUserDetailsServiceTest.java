package com.telconova.suportsuite;

import com.telconova.suportsuite.entity.User;
import com.telconova.suportsuite.repository.UserRepository;
import com.telconova.suportsuite.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CustomUserDetailsService customUserDetailsService;

    public CustomUserDetailsServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe cargar un usuario existente correctamente (AAA)")
    void testLoadUserByUsername_UserExists() {
        // ARRANGE
        User user = new User();
        user.setUsername("jonatan");
        user.setPasswordHash("1234"); // ⚠ usa setPasswordHash como dijiste
        user.setRoles("ADMIN");

        when(userRepository.findByUsername("jonatan"))
                .thenReturn(Optional.of(user));

        // ACT
        UserDetails result = customUserDetailsService.loadUserByUsername("jonatan");

        // ASSERT
        assertNotNull(result);
        assertEquals("jonatan", result.getUsername());
        assertEquals("1234", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe (AAA)")
    void testLoadUserByUsername_UserNotFound() {
        // ARRANGE
        when(userRepository.findByUsername("noexiste"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("noexiste")
        );
    }
}

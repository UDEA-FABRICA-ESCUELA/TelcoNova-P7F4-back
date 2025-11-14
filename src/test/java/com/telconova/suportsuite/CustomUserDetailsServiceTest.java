package com.telconova.suportsuite;

import com.telconova.suportsuite.entity.User;
import com.telconova.suportsuite.repository.UserRepository;
import com.telconova.suportsuite.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("noexiste"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("noexiste")
        );

        verify(userRepository, times(1)).findByUsername("noexiste");
    }
    @Test
    void testLoadUserByUsername_UserExists() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("jonatan");
        mockUser.setPasswordHash("123456");          // debe ser NO nulo
        mockUser.setRoles("ADMIN");              // también requerido por tu método

        when(userRepository.findByUsername("jonatan"))
                .thenReturn(Optional.of(mockUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("jonatan");

        // Assert
        assertNotNull(result);
        assertEquals("jonatan", result.getUsername());
        assertEquals("123456", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }

}

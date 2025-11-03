package com.telconova.suportsuite.service;
import com.telconova.suportsuite.entity.User;
import com.telconova.suportsuite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación de UserDetailsService para cargar los detalles del usuario
 * desde la base de datos (tn_user) para Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Cargar el usuario desde la base de datos
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Mapear el rol (String simple) a una lista de GrantedAuthority
        Collection<? extends GrantedAuthority> authorities = mapRoleToAuthority(user.getRoles());

        // 3. Devolver una instancia de UserDetails (usando la clase User de Spring Security)
        // Spring Security usará el metodo getPassword() que devuelve passwordHash de tu entidad
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    /**
     * Convierte el String de rol de la entidad (ej. "ADMIN_ALERTS")
     * a una colección de GrantedAuthority.
     * @param role El string del rol.
     * @return Colección con una SimpleGrantedAuthority.
     */
    private Collection<? extends GrantedAuthority> mapRoleToAuthority(String role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.toUpperCase());
        return Collections.singletonList(authority);
    }
}

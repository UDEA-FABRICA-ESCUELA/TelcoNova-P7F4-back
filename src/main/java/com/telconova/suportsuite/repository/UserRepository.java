package com.telconova.suportsuite.repository;

import com.telconova.suportsuite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Importar la anotación Query
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Necesario para buscar al usuario por su nombre de usuario en el login
    Optional<User> findByUsername(String username);

    // Nuevo metodo para verificar la conexión a la base de datos
    // Ejecuta una consulta trivial (SELECT 1) para confirmar que el pool
    // de conexiones puede entregar una conexión válida.
    @Query(value = "SELECT 1", nativeQuery = true)
    Integer checkDatabaseConnection();
}
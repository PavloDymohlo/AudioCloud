package ua.dymohlo.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.auth_service.entiti.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserEmail(String userEmail);
}

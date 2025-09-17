package ua.dymohlo.user_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.user_service.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByUserEmail(String userEmail);

    Optional<Page<User>> findUserBySubscription(String subscription, Pageable pageable);
}

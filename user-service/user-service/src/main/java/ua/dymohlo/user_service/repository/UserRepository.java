package ua.dymohlo.user_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.dymohlo.user_service.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByUserEmail(String userEmail);
    Optional<Page<User>> findUserBySubscription(String subscription, Pageable pageable);
    @Query("SELECT u FROM User u WHERE u.subscriptionExpiresAt < :time " +
            "AND u.userRole NOT IN :excludedRoles " +
            "AND u.subscription NOT IN :excludedSubscriptions")
    List<User> findExpiredUsers(
            @Param("time") LocalDateTime time,
            @Param("excludedRoles") List<String> excludedRoles,
            @Param("excludedSubscriptions") List<String> excludedSubscriptions
    );
}

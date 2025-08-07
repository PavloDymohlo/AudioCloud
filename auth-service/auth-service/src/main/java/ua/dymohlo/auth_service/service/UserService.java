package ua.dymohlo.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.auth_service.entiti.User;
import ua.dymohlo.auth_service.exception.UserNotFoundException;
import ua.dymohlo.auth_service.repository.UserRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void deleteUser(String email){
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
        userRepository.delete(user);
    }
}

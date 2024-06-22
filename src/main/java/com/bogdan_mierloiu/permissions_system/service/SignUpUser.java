package com.bogdan_mierloiu.permissions_system.service;

import com.bogdan_mierloiu.permissions_system.entity.User;
import com.bogdan_mierloiu.permissions_system.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignUpUser {

    private final UserRepo userRepo;
    private final UserService userService;

    public User verifyExistOrSave(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isPresent()) {
            log.info("User found in database");
            return optionalUser.get();
        }
        User userToSave = User.builder()
                .name(jwt.getClaimAsString("name"))
                .surname(jwt.getClaimAsString("surname"))
                .email(email)
                .groups(new HashSet<>())
                .build();
        log.info("User not found in database, saving user");
        return userService.save(userToSave, "MEMBER");
    }

}

package com.tafakkoor.e_learn.services;

import com.tafakkoor.e_learn.domain.AuthUser;
import com.tafakkoor.e_learn.dto.UserRegisterDTO;
import com.tafakkoor.e_learn.enums.Levels;
import com.tafakkoor.e_learn.repository.AuthUserRepository;
import com.tafakkoor.e_learn.repository.TokenRepository;
import com.tafakkoor.e_learn.utils.Util;
import com.tafakkoor.e_learn.utils.mail.EmailService;
import lombok.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {
    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(AuthUserRepository userRepository, PasswordEncoder passwordEncoder, TokenRepository tokenRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        this.tokenService = tokenService;
    }

    public List<Levels> getLevels(@NonNull Levels level) {
        List<Levels> levelsList = new ArrayList<>();
        if (level.equals(Levels.DEFAULT)) {
            return levelsList;
        }
        Levels[] levels = Levels.values();
        for (int i = 0; i < levels.length; i++) {
            if (!levels[i].equals(level)) {
                levelsList.add(levels[i]);
            } else {
                levelsList.add(levels[i]);
                break;
            }
        }
        return levelsList;
    }

    public AuthUser getUser(Long id) {
        return userRepository.findById(id);
    }

    public void saveUserAndSendEmail(UserRegisterDTO dto) {
        AuthUser user = AuthUser.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .email(dto.email())
                .build();
        sendActivationEmail(user);
        userRepository.save(user);
    }

    public void sendActivationEmail(AuthUser authUser) {
        Util util = Util.getInstance();
        String token = tokenService.generateToken();  // TODO: 3/12/23 encrypt token
        String email = authUser.getEmail();
        String body = util.generateBody(authUser.getUsername(), token);
        tokenService.save(util.buildToken(token, authUser));
        CompletableFuture.runAsync(() -> EmailService.getInstance().sendActivationToken(email, body, "Activate Email"));
    }
}
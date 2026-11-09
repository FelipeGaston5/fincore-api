package com.fincore.fincore_api.modules.user.service;

import com.fincore.fincore_api.modules.user.dto.CreateUserRequest;
import com.fincore.fincore_api.modules.user.dto.UserResponse;
import com.fincore.fincore_api.modules.user.entity.User;
import com.fincore.fincore_api.modules.user.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request){
        String normalizedEmail = request.email().toLowerCase(Locale.ROOT);

        if(userRepository.existsByEmailIgnoreCase(normalizedEmail)){
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Este E-mail já foi cadastrado."
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
            request.name().trim(),
            normalizedEmail,
            passwordHash
        );

        try{
            User savedUser = userRepository.save(user);
            return toResponse(savedUser);

        } catch (DataIntegrityViolationException exception){
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Não foi possível cadastrar. Verifique se o e-mail já existe."
            );
        }
    }

    private UserResponse toResponse(User user){
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.isEnabled(),
            user.getCreatedAt()
        );
        
    }
}

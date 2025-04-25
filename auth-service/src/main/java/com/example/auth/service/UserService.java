package com.example.auth.service;

import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final CryptoService cryptoService;
    private final UserRepository userRepository;

    public User createUser(User user) {
        // Criptografa a senha antes de salvar
        user.setPassword(cryptoService.encryptPassword(user.getPassword()));
        
        // Criptografa outros dados sensíveis se necessário
        if (user.getDocumentNumber() != null) {
            user.setDocumentNumber(cryptoService.encryptSensitiveData(user.getDocumentNumber()));
        }
        
        return userRepository.save(user);
    }

    public boolean validatePassword(String rawPassword, User user) {
        return cryptoService.matchesPassword(rawPassword, user.getPassword());
    }
} 
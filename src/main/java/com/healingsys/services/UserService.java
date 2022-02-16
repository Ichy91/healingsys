package com.healingsys.services;

import com.healingsys.entities.User;
import com.healingsys.exception.ApiNoSuchElementException;
import com.healingsys.repositories.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Data
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    public User getById(UUID userId) throws ApiNoSuchElementException {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty())
            throw new ApiNoSuchElementException(String.format("User not found with id: %s!", userId));

        return user.get();
    }
}

package com.bmtechnology.bmcrm.identity.application.usecase;

import com.bmtechnology.bmcrm.identity.application.command.CreateUserCommand;
import com.bmtechnology.bmcrm.identity.application.exception.EmailAlreadyRegisteredException;
import com.bmtechnology.bmcrm.identity.application.port.in.CreateUserUseCase;
import com.bmtechnology.bmcrm.identity.application.port.out.PasswordHasher;
import com.bmtechnology.bmcrm.identity.application.port.out.UserRepository;
import com.bmtechnology.bmcrm.identity.domain.entity.UserEntity;
import com.bmtechnology.bmcrm.identity.domain.valueobjects.Email;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUserUseCaseImpl(
            UserRepository userRepository,
            PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UUID execute(CreateUserCommand command) {

        Email email = Email.of(command.email());

        if (userRepository
                .findByEmail(email)
                .isPresent()) {

            throw new EmailAlreadyRegisteredException();
        }

        String passwordHash =
                passwordHasher.hash(command.password());

        UserEntity user = UserEntity.create(
                command.name(),
                email,
                passwordHash
        );

        UserEntity savedUser = userRepository.save(user);

        return savedUser.getId();

    }
}
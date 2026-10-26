package com.bmtechnology.bmcrm.identity.infrastructure.security;

import com.bmtechnology.bmcrm.identity.application.port.out.UserRepository;
import com.bmtechnology.bmcrm.identity.domain.entity.UserEntity;
import com.bmtechnology.bmcrm.identity.domain.valueobjects.Email;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {

        UserEntity user = userRepository
                .findByEmail(Email.of(email))
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado"
                        )
                );

        return new UserDetailsImpl(user);
    }
}
package com.dev.user_manage.config;

import com.dev.user_manage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements CommandLineRunner {

    private final UserRepository userRepository;
}

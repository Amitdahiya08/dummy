package com.hashedin.huspark.config;

import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@huspark.com";
        String adminPass = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123");

        if (userRepo.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .fullName("System Admin")
                    .email(adminEmail)
                    .password(encoder.encode(adminPass))
                    .role(Role.ADMIN)
                    .build();
            userRepo.save(admin);
            System.out.println("✅ Default admin created: " + adminEmail + " / " + adminPass);
        } else {
            System.out.println("ℹ️ Admin already exists, skipping creation.");
        }
    }
}

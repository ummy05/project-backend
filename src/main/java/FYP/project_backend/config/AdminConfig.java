package FYP.project_backend.config;
import FYP.project_backend.enums.Role;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initializeAdmin() {

        return args -> {

            if (!userRepository.existsByEmail("admin@coastal.com")) {

                User admin = User.builder()
                        .fullName("System Administrator")
                        .email("admin@coastal.com")
                        .phoneNumber("255700000000")
                        .password(passwordEncoder.encode("admin123"))
                        .age(30)
                        .gender("Male")
                        .address("Zanzibar")
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build();

                userRepository.save(admin);

                System.out.println("Default Admin Created Successfully");
            }

        };
    }

}

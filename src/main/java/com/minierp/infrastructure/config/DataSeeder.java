package com.minierp.infrastructure.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.minierp.domain.model.user.Role;
import com.minierp.domain.model.user.User;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.out.UserRepositoryPort;
import com.minierp.domain.port.out.WarehouseRepositoryPort;

/**
 * Development seed data: default users per role and one warehouse.
 * Only runs when {@code minierp.seed.enabled=true}.
 */
@Configuration
public class DataSeeder {

    @Bean
    ApplicationRunner seedData(UserRepositoryPort userRepository, WarehouseRepositoryPort warehouseRepository,
            PasswordEncoder passwordEncoder, MinierpProperties properties) {
        return args -> {
            if (!properties.seed().enabled()) {
                return;
            }
            seedUser(userRepository, passwordEncoder, "admin", "Admin123!", "Administrator", Role.ADMIN);
            seedUser(userRepository, passwordEncoder, "manager", "Manager123!", "Manager", Role.MANAGER);
            seedUser(userRepository, passwordEncoder, "staff", "Staff123!", "Staff Operasional", Role.STAFF);
            if (warehouseRepository.findByCode("MAIN").isEmpty()) {
                warehouseRepository.save(Warehouse.create("MAIN", "Gudang Utama", "Jakarta"));
            }
        };
    }

    private void seedUser(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder,
            String username, String rawPassword, String fullName, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        userRepository.save(User.create(username, passwordEncoder.encode(rawPassword), fullName, role));
    }
}

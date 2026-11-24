package com.minierp.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.minierp.user.Role;
import com.minierp.user.User;
import com.minierp.user.UserRepository;
import com.minierp.warehouse.Warehouse;
import com.minierp.warehouse.WarehouseRepository;

/**
 * Development seed data: default users per role and one warehouse.
 * Only runs when {@code minierp.seed.enabled=true}.
 */
@Configuration
public class DataSeeder {

    @Bean
    ApplicationRunner seedData(UserRepository userRepository, WarehouseRepository warehouseRepository,
            PasswordEncoder passwordEncoder, MinierpProperties properties) {
        return args -> {
            if (!properties.seed().enabled()) {
                return;
            }
            seedUser(userRepository, passwordEncoder, "admin", "Admin123!", "Administrator", Role.ADMIN);
            seedUser(userRepository, passwordEncoder, "manager", "Manager123!", "Manager", Role.MANAGER);
            seedUser(userRepository, passwordEncoder, "staff", "Staff123!", "Staff Operasional", Role.STAFF);
            if (warehouseRepository.findByCode("MAIN").isEmpty()) {
                Warehouse warehouse = new Warehouse();
                warehouse.setCode("MAIN");
                warehouse.setName("Gudang Utama");
                warehouse.setLocation("Jakarta");
                warehouseRepository.save(warehouse);
            }
        };
    }

    private void seedUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
            String username, String rawPassword, String fullName, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        userRepository.save(user);
    }
}

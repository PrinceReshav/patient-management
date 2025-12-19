package com.pm.authservice.repository;
// Repository is interface as we create JPARepository we extend JPARepository to ass our own methods

import com.pm.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}

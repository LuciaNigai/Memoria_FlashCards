package com.lucia.memoria.repository;

import com.lucia.memoria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUserId(UUID uuid);
}

package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByResetCode(String resetCode);
    Optional<Users> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
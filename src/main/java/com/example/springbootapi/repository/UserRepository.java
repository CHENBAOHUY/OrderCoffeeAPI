package com.example.springbootapi.repository;

import com.example.springbootapi.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByResetCode(String resetCode);
    Optional<Users> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<Users> findByEmailAndResetCode(String email, String resetCode); // Thêm phương thức này
    @Query("SELECT u FROM Users u WHERE u.isDeleted = false")
    List<Users> findAllActiveUsers();
    @Query("SELECT u FROM Users u WHERE u.id = :id AND u.isDeleted = false")
    Optional<Users> findActiveById(@Param("id") Integer id);
    @Query("SELECT u FROM Users u WHERE u.phone = :phone AND u.isDeleted = true")
    Optional<Users> findDeletedByPhone(@Param("phone") String phone);
    @Query("SELECT u FROM Users u WHERE u.email = :email AND u.isDeleted = true")
    Optional<Users> findDeletedByEmail(@Param("email") String email);
}
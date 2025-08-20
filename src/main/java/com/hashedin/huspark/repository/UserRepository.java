package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findByRole(Role role, Pageable pageable);
    boolean existsByEmail(String email);
}

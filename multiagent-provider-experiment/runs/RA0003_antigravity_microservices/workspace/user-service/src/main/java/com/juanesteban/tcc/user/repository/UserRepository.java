package com.juanesteban.tcc.user.repository;

import com.juanesteban.tcc.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByUsername(String username);
}

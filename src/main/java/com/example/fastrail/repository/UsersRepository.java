package com.example.fastrail.repository;

import com.example.fastrail.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    boolean existsByEmail(String email);
    boolean existsByTwId(String twId);

    Optional<Users> findByEmail(String email);
}

package com.thanhtam.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thanhtam.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Page<User> findAll(Pageable pageable);

    Page<User> findAllByDeleted(boolean deleted, Pageable pageable);

    Page<User> findAllByDeletedAndUsernameContains(boolean deleted, String username, Pageable pageable);

    // ❌ Đã xóa: findAllByUsernameContainsOrEmailContains

    List<User> findAllByDeleted(boolean statusDeleted);

    List<User> findAllByIntakeId(Long id);

    List<User> findByDeletedIsFalseOrderByCreatedDateDesc();
}

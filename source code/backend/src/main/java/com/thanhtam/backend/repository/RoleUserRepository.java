// RoleUserRepository.java
package com.thanhtam.backend.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.thanhtam.backend.entity.RoleUser;

@Repository
public interface RoleUserRepository extends JpaRepository<RoleUser, Long> {
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM role_user WHERE user_id IN (?1)", nativeQuery = true)
    void deleteAllByUserIds(List<Long> userIds);
}

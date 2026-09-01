package com.project.BookCarOnline.identity.repository;

import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;
import com.project.BookCarOnline.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,String> {
    Optional<Role> findByRoleId(String roleId);
    Optional<Role> findByRoleName(PredefinedRole roleName);
    Boolean existsByRoleName(PredefinedRole roleName);
}

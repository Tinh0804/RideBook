package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,String> {
    Optional<Role> findByRoleId(String roleId);
    Optional<Role> findByRoleName(PredefinedRole roleName);
    Boolean existsByRoleName(PredefinedRole roleName);
}

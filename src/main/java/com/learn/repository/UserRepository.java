package com.learn.repository;

import com.learn.entity.User;
import com.learn.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByUserRole(UserRole userRole);

    Page<User> findByUserRole(
            UserRole userRole,
            Pageable pageable
    );

    List<User> findByUserRoleAndCompany_CompanyId(
            UserRole userRole,
            Long companyId
    );

    Page<User> findByUserRoleAndCompany_CompanyId(
            UserRole userRole,
            Long companyId,
            Pageable pageable
    );
}
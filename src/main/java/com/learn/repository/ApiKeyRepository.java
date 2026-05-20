package com.learn.repository;

import com.learn.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    @Query("SELECT k FROM ApiKey k JOIN FETCH k.company WHERE k.keyHash = :hash AND k.active = true")
    Optional<ApiKey> findByKeyHashAndActiveTrue(@Param("hash") String keyHash);

    @Query("SELECT k FROM ApiKey k JOIN FETCH k.company WHERE k.company.companyId = :companyId")
    List<ApiKey> findByCompany_CompanyId(@Param("companyId") Long companyId);

    @Query("SELECT k FROM ApiKey k JOIN FETCH k.company WHERE k.keyId = :keyId")
    Optional<ApiKey> findByIdWithCompany(@Param("keyId") Long keyId);
}

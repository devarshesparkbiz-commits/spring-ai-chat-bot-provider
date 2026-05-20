package com.learn.repository;

import com.learn.entity.Chatbot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotRepository extends JpaRepository<Chatbot, Long> {

    Optional<Chatbot> findByCompany_CompanyId(Long companyId);

    boolean existsByCompany_CompanyId(Long companyId);
}

package com.learn.repository;

import com.learn.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByCompany_CompanyId(Long companyId);

    Page<Faq> findByCompany_CompanyId(Long companyId, Pageable pageable);
}

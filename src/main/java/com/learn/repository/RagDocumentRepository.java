package com.learn.repository;

import com.learn.entity.RagDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {

    List<RagDocument> findByCompany_CompanyIdAndActiveTrue(Long companyId);

    Page<RagDocument> findByCompany_CompanyId(Long companyId, Pageable pageable);
}

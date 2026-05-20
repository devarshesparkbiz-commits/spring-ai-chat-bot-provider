package com.learn.repository;

import com.learn.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Page<ChatSession> findByUser_UserIdAndActiveTrue(Long userId, Pageable pageable);

    Optional<ChatSession> findBySessionIdAndUser_UserId(Long sessionId, Long userId);

    Optional<ChatSession> findByExternalSessionTokenAndActiveTrue(String externalSessionToken);
}

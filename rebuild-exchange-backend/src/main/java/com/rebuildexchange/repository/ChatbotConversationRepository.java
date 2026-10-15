package com.rebuildexchange.repository;

import com.rebuildexchange.entity.ChatbotConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {
    Optional<ChatbotConversation> findBySessionId(String sessionId);
}

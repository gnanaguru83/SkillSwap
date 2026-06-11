package com.skillexchange.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    @EntityGraph(attributePaths = {"sender", "receiver", "session"})
    @Query("select m from Message m where (m.sender.id = :a and m.receiver.id = :b) or (m.sender.id = :b and m.receiver.id = :a) order by m.sentAt asc")
    Page<Message> history(@Param("a") UUID a, @Param("b") UUID b, Pageable pageable);

    @Query("select distinct case when m.sender.id = :userId then m.receiver.id else m.sender.id end from Message m where m.sender.id = :userId or m.receiver.id = :userId")
    List<UUID> conversationUserIds(@Param("userId") UUID userId);

    @Query("select distinct case when m.sender.id = :userId then m.receiver.id else m.sender.id end from Message m where m.sender.id = :userId or m.receiver.id = :userId")
    List<UUID> findDistinctPartnerIds(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"sender", "receiver", "session"})
    @Query("select m from Message m where (m.sender.id = :userId and m.receiver.id = :partnerId) or (m.sender.id = :partnerId and m.receiver.id = :userId) order by m.sentAt desc")
    List<Message> findTopConversationMessage(@Param("userId") UUID userId, @Param("partnerId") UUID partnerId, Pageable pageable);

    @Query("select count(m) from Message m where m.sender.id = :senderId and m.receiver.id = :receiverId and m.read = false")
    long countUnreadFrom(@Param("senderId") UUID senderId, @Param("receiverId") UUID receiverId);

    @Modifying
    @Query("update Message m set m.read = true where m.sender.id = :senderId and m.receiver.id = :receiverId and m.read = false")
    int markAsRead(@Param("senderId") UUID senderId, @Param("receiverId") UUID receiverId);
}
package com.gigu.chatnotification.infrastructure.persistence.repository;
import com.gigu.chatnotification.infrastructure.persistence.entity.NotificationEntity;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    @Query("select n from NotificationEntity n where n.recipientId=:userId and (:unreadOnly is null or :unreadOnly=false or n.read=false) order by n.createdAt desc")
    Page<NotificationEntity> findUserNotifications(UUID userId, Boolean unreadOnly, Pageable pageable);
    @Modifying @Query("update NotificationEntity n set n.read=true, n.readAt=:readAt where n.recipientId=:userId and n.read=false")
    int markAllRead(UUID userId, java.time.Instant readAt);
}

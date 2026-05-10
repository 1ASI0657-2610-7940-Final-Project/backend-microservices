package com.gigu.chatnotification.infrastructure.persistence.repository;
import com.gigu.chatnotification.infrastructure.persistence.entity.UserReportEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserReportJpaRepository extends JpaRepository<UserReportEntity, UUID> {}

package com.example.repo;

import com.example.domain.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    @Query("select s from SessionEntity s where s.id = :id and s.expiresAt > :now")
    Optional<SessionEntity> findActiveById(@Param("id") UUID id, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("delete from SessionEntity s where s.expiresAt <= :now")
    int deleteExpired(@Param("now") OffsetDateTime now);
}

package com.example.repo;

import com.example.domain.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    List<LocationEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("select l from LocationEntity l where l.id = :id and l.user.id = :userId")
    Optional<LocationEntity> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}

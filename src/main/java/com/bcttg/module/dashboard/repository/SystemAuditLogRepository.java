package com.bcttg.module.dashboard.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.bcttg.module.dashboard.entity.SystemAuditLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long> {
    long countByDeletedAtIsNullAndActionTypeAndCreatedAtBetween(String actionType, Instant from, Instant to);

    long countByDeletedAtIsNullAndActionTypeInAndCreatedAtBetween(Collection<String> actionTypes, Instant from, Instant to);

    List<SystemAuditLog> findTop10ByDeletedAtIsNullOrderByCreatedAtDesc();

    @Query(value = "SELECT DATE(created_at) AS d, COUNT(*) AS c " +
        "FROM system_audit_logs " +
        "WHERE deleted_at IS NULL AND action_type = :actionType " +
        "AND created_at >= :fromTime AND created_at < :toTime " +
        "GROUP BY DATE(created_at)", nativeQuery = true)
    List<Object[]> countGroupedByDate(
        @Param("actionType") String actionType,
        @Param("fromTime") Instant fromTime,
        @Param("toTime") Instant toTime
    );
}

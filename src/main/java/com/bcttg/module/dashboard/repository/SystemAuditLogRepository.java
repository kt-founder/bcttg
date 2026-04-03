package com.bcttg.module.dashboard.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.bcttg.module.dashboard.entity.SystemAuditLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long>, JpaSpecificationExecutor<SystemAuditLog> {
    long countByDeletedAtIsNullAndActionTypeAndCreatedAtBetween(String actionType, Instant from, Instant to);

    long countByDeletedAtIsNullAndActionTypeInAndCreatedAtBetween(Collection<String> actionTypes, Instant from, Instant to);

    List<SystemAuditLog> findTop10ByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<SystemAuditLog> findTopByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<SystemAuditLog> findTopByDeletedAtIsNullAndActionTypeOrderByCreatedAtDesc(String actionType);

    @Query("select count(distinct u.id) " +
        "from SystemAuditLog l join l.actorUser u " +
        "where l.deletedAt is null and u.deletedAt is null and l.createdAt >= :fromTime and upper(l.status) <> 'FAILED'")
    long countDistinctSuccessfulActorsSince(@Param("fromTime") Instant fromTime);

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

    @Query(value = "SELECT DATE(created_at) AS d, COUNT(*) AS c " +
        "FROM system_audit_logs " +
        "WHERE deleted_at IS NULL AND action_type = 'LOGIN' " +
        "AND UPPER(status) <> 'FAILED' " +
        "AND created_at >= :fromTime AND created_at < :toTime " +
        "GROUP BY DATE(created_at)", nativeQuery = true)
    List<Object[]> countSuccessfulLoginsGroupedByDate(
        @Param("fromTime") Instant fromTime,
        @Param("toTime") Instant toTime
    );

    @Query("select u.role, count(distinct u.id) " +
        "from SystemAuditLog l join l.actorUser u " +
        "where l.deletedAt is null and l.createdAt >= :fromTime and l.createdAt < :toTime " +
        "group by u.role")
    List<Object[]> countDistinctActorsByRole(@Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime);
}

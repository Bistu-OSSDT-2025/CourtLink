package com.courtlink.issuetracker.repository;

import com.courtlink.issuetracker.entity.Issue;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    @Query("""
        SELECT i FROM Issue i
        WHERE (:status IS NULL OR i.status = :status)
        AND (:priority IS NULL OR i.priority = :priority)
        AND (:module IS NULL OR i.module = :module)
    """)
    Page<Issue> findByFilters(
        IssueStatus status,
        IssuePriority priority,
        IssueModule module,
        Pageable pageable
    );
} 
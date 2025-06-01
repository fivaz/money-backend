package com.example.money.repository;

import com.example.money.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SourceRepository extends JpaRepository<Source, Long> {
    List<Source> findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(String userId);
}
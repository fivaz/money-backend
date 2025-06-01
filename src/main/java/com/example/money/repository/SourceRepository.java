package com.example.money.repository;

import com.example.money.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SourceRepository extends JpaRepository<Source, Long> {
    List<Source> findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(String userId);

    List<Source> findByUserIdAndIsDeletedFalse(String userId);
}
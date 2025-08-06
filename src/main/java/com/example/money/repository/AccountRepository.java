package com.example.money.repository;

import com.example.money.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(String userId);

    Optional<Account> findByIdAndUserIdAndIsDeletedFalse(Long id, String userId);
}

package com.example.money.controller;

import com.example.money.entity.Source;
import com.example.money.entity.Transaction;
import com.example.money.repository.SourceRepository;
import com.example.money.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceRepository sourceRepository;

    private final TransactionRepository transactionRepository;

    @GetMapping
    public ResponseEntity<List<Source>> getAll(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");
        List<Source> sources = sourceRepository.findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(userId);
        return ResponseEntity.ok(sources);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Source source, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        try {
            source.setId(null);
            source.setUserId(userId);
            Source saved = sourceRepository.save(source);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create source: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Source updated, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Source> optional = sourceRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Source not found.");
        }

        Source source = optional.get();
        if (!source.getUserId().equals(userId) || source.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized or deleted source.");
        }

        source.setName(updated.getName());
        source.setSortOrder(updated.getSortOrder());
        source.setIcon(updated.getIcon());
        source.setBalance(updated.getBalance());

        Source saved = sourceRepository.save(source);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/reorder")
    public ResponseEntity<?> reorder(@RequestBody List<Source> reorderedSources, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        List<Source> toSave = new ArrayList<>();

        for (int i = 0; i < reorderedSources.size(); i++) {
            Source incoming = reorderedSources.get(i);

            Optional<Source> optional = sourceRepository.findById(incoming.getId());
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Source not found: ID " + incoming.getId());
            }

            Source source = optional.get();

            if (!source.getUserId().equals(userId) || source.isDeleted()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to reorder source: ID " + source.getId());
            }

            // Update sort order to match new position
            source.setSortOrder(i);
            toSave.add(source);
        }

        sourceRepository.saveAll(toSave);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Source> optional = sourceRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Source not found.");
        }

        Source source = optional.get();
        if (!source.getUserId().equals(userId) || source.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized or already deleted.");
        }

        source.setDeleted(true);
        sourceRepository.save(source);

        return ResponseEntity.noContent().build();
    }
}
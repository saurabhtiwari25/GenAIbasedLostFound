package com.my.lostfound.repository;

import com.my.lostfound.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @NonNull
    @EntityGraph(attributePaths = {"reporter"})
    Page<Item> findAll(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"reporter"})
    Page<Item> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter"})
    Page<Item> findByLocationAndFound(String location, boolean found, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter"})
    Page<Item> findByReporterId(Long reporterId, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter"})
    List<Item> findByFoundTrue();

    @EntityGraph(attributePaths = {"reporter"})
    List<Item> findByFoundFalse();

    @NonNull
    @EntityGraph(attributePaths = {"reporter"})
    Optional<Item> findById(@NonNull Long id);
}
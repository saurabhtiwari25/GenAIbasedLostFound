package com.my.lostfound.repository;

import com.my.lostfound.entity.Item;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;


public interface ItemRepository extends JpaRepository<Item, Long> {

    @NonNull
    @EntityGraph(attributePaths = {"reporter"})
    List<Item> findAll();

    @EntityGraph(attributePaths = {"reporter"})
    List<Item> findByTitleContainingIgnoreCase(String keyword);

    @EntityGraph(attributePaths = {"reporter"})
    List<Item> findByLocationAndFound(String location, boolean found);

    @EntityGraph(attributePaths = {"reporter"})
    List<Item> findByReporterId(Long reporterId);

    @EntityGraph(attributePaths = {"reporter"})
    List<Item> findByFoundTrue();

    @EntityGraph(attributePaths = {"reporter"})
    List<Item> findByFoundFalse();

    @NonNull
    @EntityGraph(attributePaths = {"reporter"})
    Optional<Item> findById(@NonNull Long id);
}
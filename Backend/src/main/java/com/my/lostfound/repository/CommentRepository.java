package com.my.lostfound.repository;

import com.my.lostfound.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemIdOrderByCreatedAtAsc(Long itemId);
}

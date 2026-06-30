package com.my.lostfound.repository;

import com.my.lostfound.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {


    List<Message> findBySenderIdOrReceiverIdOrderByCreatedAtAsc(Long senderId, Long receiverId);
}

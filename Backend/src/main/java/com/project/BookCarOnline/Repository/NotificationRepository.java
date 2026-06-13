package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByAccountNo_UserNameOrderBySentAtDesc(String username);
}

package com.pardos.pos.repository;

import com.pardos.pos.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findTop5ByOrderByCreatedAtDesc();
}

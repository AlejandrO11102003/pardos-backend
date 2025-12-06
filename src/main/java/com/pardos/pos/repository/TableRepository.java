package com.pardos.pos.repository;

import com.pardos.pos.model.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long> {
	Optional<TableEntity> findByNumber(Integer number);
	Optional<TableEntity> findByCurrentOrderId(String currentOrderId);
}

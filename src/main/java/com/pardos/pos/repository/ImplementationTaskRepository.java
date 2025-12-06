package com.pardos.pos.repository;

import com.pardos.pos.model.ImplementationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImplementationTaskRepository extends JpaRepository<ImplementationTask, Long> {
    List<ImplementationTask> findAllByOrderByOrderAsc();
}

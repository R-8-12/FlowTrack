package com.example.IMS.repository;

import com.example.IMS.model.DashboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IDashboardSnapshotRepository extends JpaRepository<DashboardSnapshot, Long> {
    List<DashboardSnapshot> findTop20ByOrderByTimestampDesc();
    List<DashboardSnapshot> findByTimestampAfterOrderByTimestampAsc(LocalDateTime timestamp);
}

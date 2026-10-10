package com.horsemanagement.repository;

import com.horsemanagement.entity.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentReportRepository extends JpaRepository<IncidentReport, Integer> {
}

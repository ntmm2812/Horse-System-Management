package com.horsemanagement.repository;

import com.horsemanagement.entity.PrescriptionItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Integer> {
    @EntityGraph(attributePaths = "supply")
    List<PrescriptionItem> findByTreatmentPlan_TreatmentIdOrderByPrescriptionItemIdAsc(
        Integer treatmentId);
}

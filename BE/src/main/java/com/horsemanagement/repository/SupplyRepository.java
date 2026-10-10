package com.horsemanagement.repository;

import com.horsemanagement.entity.Supply;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyRepository extends JpaRepository<Supply, Integer> {
    @EntityGraph(attributePaths = "category")
    Optional<Supply> findBySupplyId(Integer supplyId);
}

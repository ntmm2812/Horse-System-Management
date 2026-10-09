package com.horsemanagement.repository;

import com.horsemanagement.entity.Horse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorseRepository extends JpaRepository<Horse, Integer> {
}

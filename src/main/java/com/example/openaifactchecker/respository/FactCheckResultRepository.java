package com.example.openaifactchecker.respository;

import com.example.openaifactchecker.entity.FactCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactCheckResultRepository extends JpaRepository<FactCheckResult, Integer> {
}

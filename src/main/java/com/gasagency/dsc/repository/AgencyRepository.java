package com.gasagency.dsc.repository;

import com.gasagency.dsc.entity.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    Optional<Agency> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByActiveTrue();

    @Modifying
    @Query("UPDATE Agency a SET a.callsUsedThisMonth = 0")
    int resetAllMonthlyUsage();
}

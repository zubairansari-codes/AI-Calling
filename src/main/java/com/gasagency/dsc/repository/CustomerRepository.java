package com.gasagency.dsc.repository;

import com.gasagency.dsc.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByAgencyId(Long agencyId, Pageable pageable);

    List<Customer> findByAgencyId(Long agencyId);

    Optional<Customer> findByIdAndAgencyId(Long id, Long agencyId);

    Optional<Customer> findByPhoneAndAgencyId(String phone, Long agencyId);
}

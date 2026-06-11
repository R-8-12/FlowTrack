package com.example.IMS.repository;

import com.example.IMS.model.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IVendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    Optional<VendorProfile> findByUserId(Long userId);
}
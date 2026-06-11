package com.example.IMS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.IMS.model.VendorProduct;

@Repository
public interface VendorProductRepository extends JpaRepository<VendorProduct, Long> {
    List<VendorProduct> findByVendorProfileIdAndActiveTrue(Long vendorProfileId);
    long countByVendorProfileIdAndActiveTrue(Long vendorProfileId);
}

package com.example.IMS.repository;

import com.example.IMS.model.RetailerVendorConnection;
import com.example.IMS.model.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerVendorConnectionRepository extends JpaRepository<RetailerVendorConnection, Long> {

    /** Find a connection between a specific retailer and vendor profile. */
    @Query("SELECT c FROM RetailerVendorConnection c WHERE c.retailer.id = :retailerId AND c.vendorProfile.id = :vendorProfileId")
    Optional<RetailerVendorConnection> findByRetailerIdAndVendorProfileId(
            @Param("retailerId") Long retailerId,
            @Param("vendorProfileId") Long vendorProfileId);

    /** All connections initiated by a retailer, newest first. */
    @Query("SELECT c FROM RetailerVendorConnection c WHERE c.retailer.id = :retailerId ORDER BY c.createdAt DESC")
    List<RetailerVendorConnection> findByRetailerId(@Param("retailerId") Long retailerId);

    /** All connections for a vendor profile, newest first. */
    @Query("SELECT c FROM RetailerVendorConnection c WHERE c.vendorProfile.id = :vendorProfileId ORDER BY c.createdAt DESC")
    List<RetailerVendorConnection> findByVendorProfileId(@Param("vendorProfileId") Long vendorProfileId);

    /** Pending requests for a vendor profile (for approval queue). */
    @Query("SELECT c FROM RetailerVendorConnection c WHERE c.vendorProfile.id = :vendorProfileId AND c.status = 'REQUESTED' ORDER BY c.createdAt ASC")
    List<RetailerVendorConnection> findPendingByVendorProfileId(@Param("vendorProfileId") Long vendorProfileId);

    /** Active (CONNECTED) connections for a retailer. */
    @Query("SELECT c FROM RetailerVendorConnection c WHERE c.retailer.id = :retailerId AND c.status = 'CONNECTED' ORDER BY c.updatedAt DESC")
    List<RetailerVendorConnection> findConnectedByRetailerId(@Param("retailerId") Long retailerId);

    /** Active (CONNECTED) connections for a vendor profile. */
    @Query("SELECT c FROM RetailerVendorConnection c WHERE c.vendorProfile.id = :vendorProfileId AND c.status = 'CONNECTED' ORDER BY c.updatedAt DESC")
    List<RetailerVendorConnection> findConnectedByVendorProfileId(@Param("vendorProfileId") Long vendorProfileId);

    /** Count of CONNECTED retailers for a vendor profile (for dashboard stats). */
    @Query("SELECT COUNT(c) FROM RetailerVendorConnection c WHERE c.vendorProfile.id = :vendorProfileId AND c.status = 'CONNECTED'")
    long countConnectedRetailersByVendorProfileId(@Param("vendorProfileId") Long vendorProfileId);

    /** Count of CONNECTED vendors for a retailer (for dashboard stats). */
    @Query("SELECT COUNT(c) FROM RetailerVendorConnection c WHERE c.retailer.id = :retailerId AND c.status = 'CONNECTED'")
    long countConnectedVendorsByRetailerId(@Param("retailerId") Long retailerId);

    /** Check if a connection exists in any status. */
    @Query("SELECT COUNT(c) > 0 FROM RetailerVendorConnection c WHERE c.retailer.id = :retailerId AND c.vendorProfile.id = :vendorProfileId")
    boolean existsByRetailerIdAndVendorProfileId(
            @Param("retailerId") Long retailerId,
            @Param("vendorProfileId") Long vendorProfileId);
}

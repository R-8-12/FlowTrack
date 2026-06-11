package com.example.IMS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.ProcurementOrderStatus;
import com.example.IMS.model.User;
import com.example.IMS.model.Vendor;

@Repository
public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrder, Long> {
    List<ProcurementOrder> findByRetailerOrderByCreatedAtDesc(User retailer);
    List<ProcurementOrder> findByVendorOrderByCreatedAtDesc(Vendor vendor);

    /** Count all orders placed by a retailer (for analytics). */
    @Query("SELECT COUNT(o) FROM ProcurementOrder o WHERE o.retailer.id = :retailerId")
    long countByRetailerId(@Param("retailerId") Long retailerId);

    /** All orders for a vendor's BusinessProfile, newest first. */
    @Query("SELECT o FROM ProcurementOrder o WHERE o.vendorProfile.id = :vendorProfileId ORDER BY o.createdAt DESC")
    List<ProcurementOrder> findByVendorProfileId(@Param("vendorProfileId") Long vendorProfileId);

    /** Count orders for a vendor profile filtered by status (for dashboard stats). */
    @Query("SELECT COUNT(o) FROM ProcurementOrder o WHERE o.vendorProfile.id = :vendorProfileId AND o.status = :status")
    long countByVendorProfileIdAndStatus(@Param("vendorProfileId") Long vendorProfileId,
                                         @Param("status") ProcurementOrderStatus status);
}

package com.example.IMS.repository;

import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.User;
import com.example.IMS.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrder, Long> {
    List<ProcurementOrder> findByRetailerOrderByCreatedAtDesc(User retailer);
    List<ProcurementOrder> findByVendorOrderByCreatedAtDesc(Vendor vendor);

    /** Count all orders placed by a retailer (for analytics). */
    @Query("SELECT COUNT(o) FROM ProcurementOrder o WHERE o.retailer.id = :retailerId")
    long countByRetailerId(@Param("retailerId") Long retailerId);
}

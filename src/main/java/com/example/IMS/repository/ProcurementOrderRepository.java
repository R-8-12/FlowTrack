package com.example.IMS.repository;

import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.User;
import com.example.IMS.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrder, Long> {
    List<ProcurementOrder> findByRetailerOrderByCreatedAtDesc(User retailer);
    List<ProcurementOrder> findByVendorOrderByCreatedAtDesc(Vendor vendor);
}

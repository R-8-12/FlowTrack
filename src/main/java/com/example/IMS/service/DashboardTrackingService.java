package com.example.IMS.service;

import com.example.IMS.model.DashboardSnapshot;
import com.example.IMS.model.Item;
import com.example.IMS.model.Loan;
import com.example.IMS.repository.IDashboardSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardTrackingService {
    
    @Autowired
    private IDashboardSnapshotRepository snapshotRepository;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private ItemIssuanceService itemIssuanceService;
    
    public void captureSnapshot(String eventType) {
        List<Item> allItems = itemService.getAllItems();
        List<Loan> allLoans = itemIssuanceService.getAllIssuedItems();
        
        long itemsBorrowed = allLoans.stream()
                .filter(loan -> loan.getReturnDate() == null || loan.getReturnDate().isEmpty())
                .count();
        
        long itemsReturned = allLoans.stream()
                .filter(loan -> loan.getReturnDate() != null && !loan.getReturnDate().isEmpty())
                .count();
        
        int inventoryRemaining = allItems.stream()
                .mapToInt(Item::getQuantity)
                .sum();
        
        long itemsIssued = allLoans.size();
        
        DashboardSnapshot snapshot = new DashboardSnapshot(
            LocalDateTime.now(),
            itemsBorrowed,
            itemsReturned,
            inventoryRemaining,
            itemsIssued,
            eventType
        );
        
        snapshotRepository.save(snapshot);
    }
    
    public List<DashboardSnapshot> getRecentSnapshots() {
        return snapshotRepository.findTop20ByOrderByTimestampDesc();
    }
}

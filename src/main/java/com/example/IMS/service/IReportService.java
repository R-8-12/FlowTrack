package com.example.IMS.service;

import com.example.IMS.model.Item;
import com.example.IMS.model.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IReportService {
    // Item Stock Report
    List<Item> getItemStockReport();
    long getTotalItemsCount();
    long getLowStockItemsCount();
    
    // Borrow History Report
    List<Loan> getBorrowHistoryReport(LocalDate startDate, LocalDate endDate);
    long getTotalLoansCount();
    
    // Currently Borrowed Items Report
    List<Map<String, Object>> getCurrentlyBorrowedItems();
    long getCurrentlyBorrowedCount();
    
    // Issued Items Report
    List<Map<String, Object>> getIssuedItemsReport(LocalDate startDate, LocalDate endDate);
    long getTotalIssuedCount(LocalDate startDate, LocalDate endDate);
}

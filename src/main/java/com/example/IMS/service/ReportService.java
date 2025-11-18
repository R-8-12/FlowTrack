package com.example.IMS.service;

import com.example.IMS.model.Item;
import com.example.IMS.model.Loan;
import com.example.IMS.repository.IItemRepository;
import com.example.IMS.repository.ILoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService implements IReportService {

    @Autowired
    private IItemRepository itemRepository;

    @Autowired
    private ILoanRepository loanRepository;

    @Override
    public List<Item> getItemStockReport() {
        return itemRepository.findAll();
    }

    @Override
    public long getTotalItemsCount() {
        return itemRepository.count();
    }

    @Override
    public long getLowStockItemsCount() {
        return itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() < 10)
                .count();
    }

    @Override
    public List<Loan> getBorrowHistoryReport(LocalDate startDate, LocalDate endDate) {
        // Get all loans - dates are stored as strings in the model
        return loanRepository.findAll().stream()
                .filter(loan -> loan.getIssueDate() != null && !loan.getIssueDate().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalLoansCount() {
        return loanRepository.count();
    }

    @Override
    public List<Map<String, Object>> getCurrentlyBorrowedItems() {
        List<Map<String, Object>> borrowedItems = new ArrayList<>();
        
        List<Loan> activeLoans = loanRepository.findAll().stream()
                .filter(loan -> loan.getReturnDate() == null || loan.getReturnDate().isEmpty())
                .collect(Collectors.toList());
        
        for (Loan loan : activeLoans) {
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("loanId", loan.getId());
            itemInfo.put("itemName", loan.getItem().getName());
            itemInfo.put("borrowerName", loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName());
            itemInfo.put("borrowerEmail", loan.getBorrower().getEmail());
            itemInfo.put("issueDate", loan.getIssueDate());
            itemInfo.put("dueDate", "N/A"); // Calculate based on loan duration if needed
            itemInfo.put("fine", loan.getTotalFine());
            borrowedItems.add(itemInfo);
        }
        
        return borrowedItems;
    }

    @Override
    public long getCurrentlyBorrowedCount() {
        return loanRepository.findAll().stream()
                .filter(loan -> loan.getReturnDate() == null || loan.getReturnDate().isEmpty())
                .count();
    }

    @Override
    public List<Map<String, Object>> getIssuedItemsReport(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> issuedItems = new ArrayList<>();
        
        List<Loan> loans = loanRepository.findAll();
        
        for (Loan loan : loans) {
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("loanId", loan.getId());
            itemInfo.put("itemName", loan.getItem().getName());
            itemInfo.put("borrowerName", loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName());
            itemInfo.put("issueDate", loan.getIssueDate());
            itemInfo.put("returnDate", loan.getReturnDate() != null && !loan.getReturnDate().isEmpty() ? loan.getReturnDate() : null);
            itemInfo.put("status", loan.getReturnDate() == null || loan.getReturnDate().isEmpty() ? "Borrowed" : "Returned");
            itemInfo.put("fine", loan.getTotalFine());
            issuedItems.add(itemInfo);
        }
        
        return issuedItems;
    }

    @Override
    public long getTotalIssuedCount(LocalDate startDate, LocalDate endDate) {
        return loanRepository.count();
    }
}

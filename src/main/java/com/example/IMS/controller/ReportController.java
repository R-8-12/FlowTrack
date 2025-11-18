package com.example.IMS.controller;

import com.example.IMS.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private IReportService reportService;

    // Main reports page
    @GetMapping
    public String reportsHome() {
        return "reports/index";
    }

    // Item Stock Report
    @GetMapping("/stock")
    public String itemStockReport(Model model) {
        model.addAttribute("items", reportService.getItemStockReport());
        model.addAttribute("totalItems", reportService.getTotalItemsCount());
        model.addAttribute("lowStockItems", reportService.getLowStockItemsCount());
        return "reports/stock-report";
    }

    // Borrow History Report
    @GetMapping("/borrow-history")
    public String borrowHistoryReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        model.addAttribute("loans", reportService.getBorrowHistoryReport(start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("totalLoans", reportService.getTotalLoansCount());
        return "reports/borrow-history";
    }

    // Currently Borrowed Items Report
    @GetMapping("/borrowed-items")
    public String borrowedItemsReport(Model model) {
        model.addAttribute("borrowedItems", reportService.getCurrentlyBorrowedItems());
        model.addAttribute("totalBorrowed", reportService.getCurrentlyBorrowedCount());
        return "reports/borrowed-items";
    }

    // Issued Items Report
    @GetMapping("/issued-items")
    public String issuedItemsReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        model.addAttribute("issuedItems", reportService.getIssuedItemsReport(start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("totalIssued", reportService.getTotalIssuedCount(start, end));
        return "reports/issued-items";
    }
}

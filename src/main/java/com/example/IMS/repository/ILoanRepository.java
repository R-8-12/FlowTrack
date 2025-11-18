package com.example.IMS.repository;

import com.example.IMS.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ILoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrowerId(Long borrowerId);
    List<Loan> findByItemId(Long itemId);
}

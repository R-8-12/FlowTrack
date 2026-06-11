package com.example.IMS.repository;

import com.example.IMS.model.RetailerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRetailerProfileRepository extends JpaRepository<RetailerProfile, Long> {
    Optional<RetailerProfile> findByUserId(Long userId);
}
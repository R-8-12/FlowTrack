package com.example.IMS.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.IMS.model.Vendor;

@Repository
public interface IVendorRepository extends JpaRepository<Vendor, Long> {

	Optional<Vendor> findByEmailIgnoreCase(String email);

	Optional<Vendor> findByNameIgnoreCase(String name);

}

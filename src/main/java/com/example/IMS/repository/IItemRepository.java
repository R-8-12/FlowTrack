package com.example.IMS.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.IMS.model.Item;

@Repository
public interface IItemRepository extends JpaRepository<Item, Long> {

	Optional<Item> findByNameIgnoreCaseAndItemType_TypeNameIgnoreCase(String name, String itemTypeName);

}

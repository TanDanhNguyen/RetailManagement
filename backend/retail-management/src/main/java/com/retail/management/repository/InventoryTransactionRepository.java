
package com.retail.management.repository;

import com.retail.management.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findAllByOrderByCreatedAtDesc();

    List<InventoryTransaction> findByProduct_IdOrderByCreatedAtDesc(
            Long productId
    );
}

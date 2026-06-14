package com.thorben.janssen.spring.ai.rag.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPositionRepository extends JpaRepository<OrderPosition,Long> {
}

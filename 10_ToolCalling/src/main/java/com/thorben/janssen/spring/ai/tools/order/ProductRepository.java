package com.thorben.janssen.spring.ai.tools.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {

    Product findByName(String name);
}

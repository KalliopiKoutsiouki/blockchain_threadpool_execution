package com.warehouse.repository;

import com.warehouse.model.ProductBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<ProductBlock, Long> {

    ProductBlock save(ProductBlock entity);

    List<ProductBlock> findAll();

    ProductBlock findTopByOrderByIdDesc();
    @Query("select p from ProductBlock p where p.blockJson LIKE %:param% order by p.id desc" )
    List<ProductBlock> findProductContainingParam(@Param("param") String param);

    ProductBlock findFirstByOrderByIdDesc();

}

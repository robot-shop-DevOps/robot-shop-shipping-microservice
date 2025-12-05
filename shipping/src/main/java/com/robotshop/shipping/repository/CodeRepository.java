package com.robotshop.shipping.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import com.robotshop.shipping.model.Code; 

public interface CodeRepository extends PagingAndSortingRepository<Code, Long> {

    Iterable<Code> findAll();

    Code findById(long id);
}

package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.OrderSummary;

@Mapper
public interface OrderSummaryMapper {

	void addCart(OrderSummary orderSummary);
	
	int getTotalItemsForUser(@Param("userId") int userId);
	
	List<OrderSummary> cartCheck(@Param("userId") int userId);
	
	void updateCart(OrderSummary orderSummary);
	
	void cartUpsert(@Param("list") List<OrderSummary> orderSummaries);

}

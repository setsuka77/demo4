package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.entity.Categories;

@Mapper
public interface CategoriesMapper {
	
	List<Categories> findCategory();
}

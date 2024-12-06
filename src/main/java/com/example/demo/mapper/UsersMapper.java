package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.entity.Users;

@Mapper
public interface UsersMapper {
	
	Users findUser(Integer id);
	
	void insert(Users user);

}

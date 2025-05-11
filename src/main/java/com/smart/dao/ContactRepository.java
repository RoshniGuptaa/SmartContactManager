package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
   //Pagination
	
	//getting contacts by userId
	
	@Query("from Contact c where c.user.id=:userId")
	public Page<Contact> findContactsByUserId(@Param("userId")int userId,Pageable pageable);
	
	//search
	public List<Contact> findByNameContainingAndUser(String name,User user);
	}



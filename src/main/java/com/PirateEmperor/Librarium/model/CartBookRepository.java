package com.PirateEmperor.Librarium.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartBookRepository extends CrudRepository<CartBook, Long> {
	Optional<CartBook> findById(CartBookKey cartBookId);
	
	List<CartBook> findByCart(Cart cart);
	
	long deleteByCart(Cart cart);
	
	long deleteById(CartBookKey cartBookId);
}
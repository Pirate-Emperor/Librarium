package com.PirateEmperor.Librarium.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
	@SuppressWarnings("null")
	Optional<Order> findById(Long orderid);
	
	@Query(value = "SELECT o.* FROM orders AS o JOIN cart AS ca ON (ca.cartid = o.cartid) WHERE userid = ?1", nativeQuery = true)
	List<Order> findByUserid(Long userId);
}

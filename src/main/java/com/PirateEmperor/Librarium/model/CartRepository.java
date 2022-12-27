package com.PirateEmperor.Librarium.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.PirateEmperor.Librarium.sqlHandlers.QuantityOfCart;
import com.PirateEmperor.Librarium.sqlHandlers.TotalOfCart;

@Repository
public interface CartRepository extends CrudRepository<Cart, Long> {
	@SuppressWarnings("null")
	Optional<Cart> findById(Long cartid);

	@Query(value = "SELECT cart.* FROM cart WHERE current AND userid = ?1", nativeQuery = true)
	List<Cart> findCurrentByUserid(Long userId);

	@Query(value = "SELECT cartid FROM cart WHERE NOT current AND userid =?1", nativeQuery = true)
	List<Long> findNotCurrentByUserid(Long userId);

	@Query(value = "SELECT ca.cartid AS cartid, SUM(quantity * price) AS total FROM cart AS ca JOIN cart_book AS cb ON (cb.cartid = ca.cartid) JOIN book AS bo ON (bo.id = cb.bookid) WHERE current AND userid=?1 GROUP BY ca.cartid", nativeQuery = true)
	TotalOfCart findTotalOfCurrentCart(Long userId);

	@Query(value = "SELECT ca.cartid AS cartid, SUM(quantity * price) AS total FROM cart AS ca JOIN orders AS o ON (o.cartid = ca.cartid) JOIN cart_book AS cb ON (cb.cartid = ca.cartid) JOIN book AS bo ON (bo.id = cb.bookid) WHERE orderid=?1 GROUP BY ca.cartid", nativeQuery = true)
	TotalOfCart findTotalOfOrder(Long orderid);

	@Query(value = "SELECT ca.cartid AS cartid, SUM(quantity * price) AS total FROM cart AS ca JOIN cart_book AS cb ON (cb.cartid = ca.cartid) JOIN book AS bo ON (bo.id = cb.bookid) WHERE ca.cartid=?1 GROUP BY ca.cartid", nativeQuery = true)
	TotalOfCart findTotalOfCart(Long cartid);

	@Query(value = "SELECT ca.cartid, SUM(quantity) AS items FROM cart AS ca JOIN cart_book AS cb ON (cb.cartid = ca.cartid) WHERE current AND userid =?1 GROUP BY ca.cartid", nativeQuery = true)
	QuantityOfCart findQuantityInCurrent(Long userId);
}

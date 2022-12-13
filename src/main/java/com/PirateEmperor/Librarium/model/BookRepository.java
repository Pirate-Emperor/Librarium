package com.PirateEmperor.Librarium.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.PirateEmperor.Librarium.sqlHandlers.BookInCurrentCart;
import com.PirateEmperor.Librarium.sqlHandlers.RawBookInfo;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {
	@Query(value = "SELECT bo.id AS bookid, ca.cartid, title, author, isbn, book_year, price, url, ca.name AS category, cc.quantity FROM book AS bo JOIN category AS ca ON (ca.categoryid = bo.categoryid) JOIN cart_book AS cc ON (cc.bookid = bo.id) JOIN cart AS ca ON (ca.cartid = cc.cartid) JOIN users AS u ON (u.id = ca.userid) WHERE u.id = ?1 AND current", nativeQuery = true)
	List<BookInCurrentCart> findBooksInCurrentCartByUserid(Long userId);

	@Query(value = "SELECT bo.id AS bookid FROM book AS bo JOIN cart_book AS cc ON (cc.bookid = bo.id) JOIN cart AS ca ON (ca.cartid = cc.cartid) JOIN users AS u ON (u.id = ca.userid) WHERE u.id = ?1 AND current", nativeQuery = true)
	List<Long> findIdsOfBooksInCurrentCart(Long userId);

	@Query(value = "SELECT bo.id AS bookid FROM book AS bo JOIN cart_book AS cc ON (cc.bookid = bo.id) JOIN cart AS ca ON (ca.cartid = cc.cartid) WHERE ca.cartid = ?1", nativeQuery = true)
	List<Long> findIdsOfBooksByCartid(Long cartId);

	@Query(value = "SELECT bo.id AS bookid, ca.cartid, title, author, isbn, book_year, price, url, ca.name AS category, cc.quantity FROM book AS bo JOIN category AS ca ON (ca.categoryid = bo.categoryid) JOIN cart_book AS cc ON (cc.bookid = bo.id) JOIN cart AS ca ON (ca.cartid = cc.cartid) JOIN orders as o ON (o.cartid = ca.cartid) WHERE orderid = ?1", nativeQuery = true)
	List<BookInCurrentCart> findBooksInOrder(Long orderid);

	@Query(value = "SELECT bo.id AS bookid, ca.cartid, title, author, isbn, book_year, price, url, ca.name AS category, cc.quantity FROM book AS bo JOIN category AS ca ON (ca.categoryid = bo.categoryid) JOIN cart_book AS cc ON (cc.bookid = bo.id) JOIN cart AS ca ON (ca.cartid = cc.cartid) WHERE ca.cartid = ?1", nativeQuery = true)
	List<BookInCurrentCart> findBooksInCart(Long cartid);

	@Query(value = "SELECT bo.id AS bookid, title, author, isbn, book_year, price, url FROM book AS bo JOIN cart_book AS cc ON (cc.bookid = bo.id) JOIN cart AS ca ON (ca.cartid = cc.cartid) WHERE NOT current GROUP BY bo.id ORDER BY SUM(quantity) DESC LIMIT 10", nativeQuery = true)
	List<RawBookInfo> findTopSales();

	List<Book> findByCategory(Category category);

	Optional<Book> findByIsbn(String isbn);
}
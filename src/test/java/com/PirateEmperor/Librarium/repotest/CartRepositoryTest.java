package com.PirateEmperor.Librarium.repotest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.PirateEmperor.Librarium.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.PirateEmperor.Librarium.model.Cart;
import com.PirateEmperor.Librarium.sqlHandlers.QuantityOfCart;
import com.PirateEmperor.Librarium.sqlHandlers.TotalOfCart;

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class CartRepositoryTest {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private CartBookRepository cartBookRepository;

	@Autowired
	private BookRepository bookrepository;

	@Autowired
	private CategoryRepository crepository;

	@Autowired
	private OrderRepository orepository;

	@Autowired
	private CartRepository cartrepository;

	@BeforeAll
	public void resetUserRepo() {
		urepository.deleteAll();
		cartBookRepository.deleteAll();
		bookrepository.deleteAll();
		crepository.deleteAll();
		orepository.deleteAll();
		cartrepository.deleteAll();
	}

	// CRUD tests for the cart repository
	// Create functionality
	@Test
	@Rollback
	public void testCreateCartWithUser() {
		Cart newCart1 = this.createCartWithUser(true, "user1");
		assertThat(newCart1.getCartid()).isNotNull();

		this.createCartWithUser(true, "user2");
		List<Cart> carts = (List<Cart>) cartrepository.findAll();
		assertThat(carts).hasSize(2);
	}

	@Test
	@Rollback
	public void testCreateCartNoUser() {
		Cart newCart1 = this.createCartNoUser(true);
		assertThat(newCart1.getCartid()).isNotNull();

		this.createCartNoUser(true);
		List<Cart> carts = (List<Cart>) cartrepository.findAll();
		assertThat(carts).hasSize(2);
	}

	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Cart> carts = (List<Cart>) cartrepository.findAll();
		assertThat(carts).isEmpty();

		Optional<Cart> optionalCart = cartrepository.findById(Long.valueOf(2));
		assertThat(optionalCart).isNotPresent();

		Cart cart = this.createCartNoUser(true);
		Long cartid = cart.getCartid();

		optionalCart = cartrepository.findById(cartid);
		assertThat(optionalCart).isPresent();

		this.createCartNoUser(true);

		carts = (List<Cart>) cartrepository.findAll();
		assertThat(carts).hasSize(2);
	}

	@Test
	@Rollback
	public void testFindQuantityInCurrent() {
		QuantityOfCart quantityOfCart = cartrepository.findQuantityInCurrent(Long.valueOf(2));
		assertThat(quantityOfCart).isNull();

		String username = "user1";
		User user = this.createUser(username);
		Long userId = user.getId();

		Cart cart = this.createCartWithUser(true, username);
		Book book1 = this.createBook("Little Women", "Other", 10.2);
		this.createCartBookCustomQuantity(2, book1, cart);

		quantityOfCart = cartrepository.findQuantityInCurrent(userId);
		assertThat(quantityOfCart).isNotNull();
		assertThat(quantityOfCart.getItems()).isEqualTo(2);

		Book book2 = this.createBook("Little Women 2", "Other", 10.2);
		this.createCartBookCustomQuantity(3, book2, cart);
		quantityOfCart = cartrepository.findQuantityInCurrent(userId);
		assertThat(quantityOfCart.getItems()).isEqualTo(5);
	}

	@Test
	@Rollback
	public void testfindTotalOfCart() {
		TotalOfCart totalOfCart = cartrepository.findTotalOfCart(Long.valueOf(2));
		assertThat(totalOfCart).isNull();

		String username = "user1";
		Cart cart = this.createCartWithUser(true, username);
		Long cartid = cart.getCartid();

		double price1 = 10.2;
		Book book1 = this.createBook("Little Women", "Other", price1);
		this.createCartBookCustomQuantity(2, book1, cart);

		totalOfCart = cartrepository.findTotalOfCart(cartid);
		assertThat(totalOfCart).isNotNull();
		assertThat(totalOfCart.getTotal()).isEqualTo(price1 * 2);

		double price2 = 8.2;
		Book book2 = this.createBook("Little Women 2", "Other", price2);
		this.createCartBookCustomQuantity(3, book2, cart);
		totalOfCart = cartrepository.findTotalOfCart(cartid);
		assertThat(totalOfCart).isNotNull();
		assertThat(totalOfCart.getTotal()).isEqualTo(price1 * 2 + price2 * 3);

		Cart cartNoUser = this.createCartNoUser(true);
		Long cart2Id = cartNoUser.getCartid();

		totalOfCart = cartrepository.findTotalOfCart(cart2Id);
		assertThat(totalOfCart).isNull();

		this.createCartBookCustomQuantity(1, book2, cartNoUser);
		totalOfCart = cartrepository.findTotalOfCart(cart2Id);
		assertThat(totalOfCart).isNotNull();
		assertThat(totalOfCart.getTotal()).isEqualTo(price2);
	}

	@Test
	@Rollback
	public void testFindTotalOfOrder() {
		TotalOfCart totalOfCart = cartrepository.findTotalOfOrder(Long.valueOf(2));
		assertThat(totalOfCart).isNull();

		int quantity = 2;
		double priceBook1 = 11.2;
		double priceBook2 = 4.6;

		List<Book> booksInOrder1 = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", priceBook1);
		Book book2 = this.createBook("Fight Club", "Thriller", priceBook2);
		booksInOrder1.add(book1);
		booksInOrder1.add(book2);

		Order order1 = this.createSale(quantity, booksInOrder1);
		Long order1Id = order1.getOrderid();

		totalOfCart = cartrepository.findTotalOfOrder(order1Id);
		assertThat(totalOfCart).isNotNull();
		assertThat(totalOfCart.getTotal()).isCloseTo(quantity * (priceBook1 + priceBook2), offset(0.01));
	}

	@Test
	@Rollback
	public void testFindTotalOfCurrentCart() {
		TotalOfCart totalOfCurrent = cartrepository.findTotalOfCurrentCart(Long.valueOf(2));
		assertThat(totalOfCurrent).isNull();

		String username = "user1";
		Cart cart = this.createCartWithUser(true, username);

		double price1 = 10.2;
		Book book1 = this.createBook("Little Women", "Other", price1);
		this.createCartBookCustomQuantity(2, book1, cart);

		Long user1Id = urepository.findByUsername(username).get().getId();

		totalOfCurrent = cartrepository.findTotalOfCurrentCart(user1Id);
		assertThat(totalOfCurrent).isNotNull();
		assertThat(totalOfCurrent.getTotal()).isEqualTo(price1 * 2);

		double price2 = 8.2;
		Book book2 = this.createBook("Little Women 2", "Other", price2);
		this.createCartBookCustomQuantity(3, book2, cart);
		totalOfCurrent = cartrepository.findTotalOfCurrentCart(user1Id);
		assertThat(totalOfCurrent).isNotNull();
		assertThat(totalOfCurrent.getTotal()).isEqualTo(price1 * 2 + price2 * 3);
	}

	@Test
	@Rollback
	public void testFindNotCurrentByUserid() {
		List<Long> idsOfNotCurrentCarts = cartrepository.findNotCurrentByUserid(Long.valueOf(2));
		assertThat(idsOfNotCurrentCarts).isEmpty();

		String username = "user1";
		Cart cart = this.createCartWithUser(false, username);
		this.createCartWithUser(true, username);
		Long cartid = cart.getCartid();

		Long user1Id = urepository.findByUsername(username).get().getId();

		idsOfNotCurrentCarts = cartrepository.findNotCurrentByUserid(user1Id);
		assertThat(idsOfNotCurrentCarts).hasSize(1);
		assertThat(idsOfNotCurrentCarts.get(0)).isEqualTo(cartid);
	}

	@Test
	@Rollback
	public void testFindCurrentByUserid() {
		List<Cart> currentCarts = cartrepository.findCurrentByUserid(Long.valueOf(2));
		assertThat(currentCarts).isEmpty();

		String username = "user1";
		this.createCartWithUser(false, username);
		this.createCartWithUser(true, username);

		Long user1Id = urepository.findByUsername(username).get().getId();

		currentCarts = cartrepository.findCurrentByUserid(user1Id);
		assertThat(currentCarts).hasSize(1);
	}

	@Test
	@Rollback
	public void testUpdateCart() {
		boolean current = true;
		Cart newCart = this.createCartNoUser(current);

		String updatedPwd = "newHash";

		newCart.setCurrent(!current);
		newCart.setPasswordHash(updatedPwd);
		cartrepository.save(newCart);

		assertThat(newCart.isCurrent()).isEqualTo(!current);
		assertThat(newCart.getPasswordHash()).isEqualTo(updatedPwd);
	}

	@Test
	@Rollback
	public void testDeleteCart() {
		Cart cartToDelete = this.createCartNoUser(false);
		Long cartid = cartToDelete.getCartid();
		cartrepository.deleteById(cartid);

		Optional<Cart> optionalCart = cartrepository.findById(cartid);
		assertThat(optionalCart).isNotPresent();

		this.createCartNoUser(false);
		this.createCartNoUser(false);
		cartrepository.deleteAll();

		List<Cart> carts = (List<Cart>) cartrepository.findAll();
		assertThat(carts).isEmpty();
	}

	private Order createSale(int quantity, List<Book> books) {
		Cart cart = this.createCartNoUser(false);

		for (Book book : books) {
			this.createCartBookCustomQuantity(quantity, book, cart);
		}

		String stringField = "field";

		Order newOrder = new Order(stringField, stringField, stringField, stringField, stringField, stringField,
				stringField, cart, stringField, stringField);
		orepository.save(newOrder);

		return newOrder;
	}

	private Cart createCartWithUser(boolean current, String username) {
		User user = this.createUser(username);

		List<Cart> currentCarts = cartrepository.findCurrentByUserid(user.getId());
		if (currentCarts.size() != 0)
			return currentCarts.get(0);

		Cart newCart = new Cart(current, user);
		cartrepository.save(newCart);

		return newCart;
	}

	private Cart createCartNoUser(boolean current) {
		Cart newCart = new Cart(current);
		cartrepository.save(newCart);

		return newCart;
	}

	private User createUser(String username) {
		Optional<User> optionalUser = urepository.findByUsername(username);

		if (optionalUser.isPresent())
			return optionalUser.get();

		User user = new User("Firstname", "Lastname", username, "hash_pwd", "USER", username + "@mail.com", false);
		urepository.save(user);

		return user;
	}

	private CartBook createCartBookCustomQuantity(int quantity, Book book, Cart cart) {
		CartBook newCartBook = new CartBook(quantity, cart, book);
		cartBookRepository.save(newCartBook);

		return newCartBook;
	}

	private Book createBook(String title, String categoryName, double price) {
		Category category = this.createCategory(categoryName);
		Book newBook = new Book(title, "Chuck Palahniuk", title + "isbn", 1998, price, category, "someurlToPicture");
		bookrepository.save(newBook);

		return newBook;
	}

	private Category createCategory(String categoryName) {
		Optional<Category> optionalCategory = crepository.findByName(categoryName);
		if (optionalCategory.isPresent())
			return optionalCategory.get();

		Category category = new Category(categoryName);
		crepository.save(category);

		return category;
	}

}

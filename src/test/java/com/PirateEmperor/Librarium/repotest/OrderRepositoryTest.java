package com.PirateEmperor.Librarium.repotest;

import static org.assertj.core.api.Assertions.assertThat;

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

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class OrderRepositoryTest {
	@Autowired
	private CategoryRepository crepository;

	@Autowired
	private UserRepository urepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private CartBookRepository cartBookRepository;

	@Autowired
	private OrderRepository orepository;

	@BeforeAll
	public void resetRepos() {
		crepository.deleteAll();
		urepository.deleteAll();
		cartRepository.deleteAll();
		bookRepository.deleteAll();
		cartBookRepository.deleteAll();
		orepository.deleteAll();
	}

	@Test
	@Rollback
	public void testCreateOrderWithStatusNoUser() {
		List<Book> booksInOrder1 = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", 11.2);
		Book book2 = this.createBook("Fight Club", "Thriller", 11.2);
		booksInOrder1.add(book1);
		booksInOrder1.add(book2);
		Order order1 = this.createOrderWithStatusNoNoteNoUser(2, booksInOrder1, "In process");
		assertThat(order1.getOrderid()).isNotNull();

		this.createOrderWithStatusNoNoteNoUser(3, booksInOrder1, "In process");

		List<Order> orders = (List<Order>) orepository.findAll();
		assertThat(orders).hasSize(2);
	}

	@Test
	@Rollback
	public void testCreateOrderDefaultStatusNoUser() {
		List<Book> booksInOrder1 = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", 11.2);
		Book book2 = this.createBook("Fight Club", "Thriller", 11.2);
		booksInOrder1.add(book1);
		booksInOrder1.add(book2);
		Order order1 = this.createOrderWithDefaultStatusNoUser(2, booksInOrder1);
		assertThat(order1.getOrderid()).isNotNull();

		this.createOrderWithDefaultStatusNoUser(3, booksInOrder1);

		List<Order> orders = (List<Order>) orepository.findAll();
		assertThat(orders).hasSize(2);
	}

	@Test
	@Rollback
	public void testCreateOrderWithStatusAndUser() {
		List<Book> booksInOrder1 = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", 11.2);
		Book book2 = this.createBook("Fight Club", "Thriller", 11.2);
		booksInOrder1.add(book1);
		booksInOrder1.add(book2);
		Order order1 = this.createOrderWithStatusNoNoteWithUser(2, booksInOrder1, "In process", "user1");
		assertThat(order1.getOrderid()).isNotNull();

		this.createOrderWithStatusNoNoteWithUser(3, booksInOrder1, "Closed", "user1");

		List<Order> orders = (List<Order>) orepository.findAll();
		assertThat(orders).hasSize(2);
	}

	@Test
	@Rollback
	public void testCreateOrderDefaultStatusWithUser() {
		List<Book> booksInOrder1 = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", 11.2);
		Book book2 = this.createBook("Fight Club", "Thriller", 11.2);
		booksInOrder1.add(book1);
		booksInOrder1.add(book2);
		Order order1 = this.createOrderWithDefaultStatusWithUser(2, booksInOrder1, "user1");
		assertThat(order1.getOrderid()).isNotNull();

		this.createOrderWithDefaultStatusWithUser(3, booksInOrder1, "user1");

		List<Order> orders = (List<Order>) orepository.findAll();
		assertThat(orders).hasSize(2);
	}

	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Order> orders = (List<Order>) orepository.findAll();
		assertThat(orders).isEmpty();

		Optional<Order> optionalOrder = orepository.findById(Long.valueOf(2));
		assertThat(optionalOrder).isNotPresent();

		List<Book> booksInOrder1 = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", 11.2);
		Book book2 = this.createBook("Fight Club", "Thriller", 11.2);
		booksInOrder1.add(book1);
		booksInOrder1.add(book2);
		Order order1 = this.createOrderWithDefaultStatusWithUser(2, booksInOrder1, "user1");
		Long order1Id = order1.getOrderid();

		optionalOrder = orepository.findById(order1Id);
		assertThat(optionalOrder).isPresent();

		this.createOrderWithDefaultStatusWithUser(3, booksInOrder1, "user1");
		orders = (List<Order>) orepository.findAll();
		assertThat(orders).hasSize(2);
	}

	@Test
	@Rollback
	public void testFindByUserid() {
		List<Order> ordersOfUser1 = orepository.findByUserid(Long.valueOf(2));
		assertThat(ordersOfUser1).isEmpty();

		User user1 = this.createUser("user1");
		Long user1Id = user1.getId();

		ordersOfUser1 = orepository.findByUserid(user1Id);
		assertThat(ordersOfUser1).isEmpty();

		List<Book> booksInOrder = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", 11.2);
		Book book2 = this.createBook("Fight Club", "Thriller", 11.2);
		booksInOrder.add(book1);
		booksInOrder.add(book2);

		this.createOrderWithDefaultStatusNoUser(3, booksInOrder);

		ordersOfUser1 = orepository.findByUserid(user1Id);
		assertThat(ordersOfUser1).isEmpty();

		this.createOrderWithDefaultStatusWithUser(3, booksInOrder, "user1");
		ordersOfUser1 = orepository.findByUserid(user1Id);
		assertThat(ordersOfUser1).hasSize(1);

		this.createOrderWithDefaultStatusWithUser(3, booksInOrder, "user1");
		ordersOfUser1 = orepository.findByUserid(user1Id);
		assertThat(ordersOfUser1).hasSize(2);
	}

	@Test
	@Rollback
	public void testUpdateOrder() {
		List<Book> booksInOrder = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", 11.2);
		Book book2 = this.createBook("Fight Club", "Thriller", 11.2);
		booksInOrder.add(book1);
		booksInOrder.add(book2);

		Order order = this.createOrderWithDefaultStatusNoUser(3, booksInOrder);
		String firstname = "Alex";
		String lastname = "Doe";
		String country = "Finland";
		String city = "Helsinki";
		String street = "Juustenintie 3J 110";
		String postcode = "00410";
		String status = "In delivery";
		String email = "contact@mail.com";
		String note = "Quick delivery";
		String password = "Some hash pwd";

		order.setFirstname(firstname);
		order.setLastname(lastname);
		order.setCountry(country);
		order.setCity(city);
		order.setStreet(street);
		order.setPostcode(postcode);
		order.setStatus(status);
		order.setEmail(email);
		order.setNote(note);
		order.setPassword(password);
		orepository.save(order);

		assertThat(order.getFirstname()).isEqualTo(firstname);
		assertThat(order.getLastname()).isEqualTo(lastname);
		assertThat(order.getCountry()).isEqualTo(country);
		assertThat(order.getCity()).isEqualTo(city);
		assertThat(order.getStreet()).isEqualTo(street);
		assertThat(order.getPostcode()).isEqualTo(postcode);
		assertThat(order.getStatus()).isEqualTo(status);
		assertThat(order.getEmail()).isEqualTo(email);
		assertThat(order.getNote()).isEqualTo(note);
		assertThat(order.getPassword()).isEqualTo(password);
	}
	
	@Test
	@Rollback
	public void testDeleteOrder() {
		List<Book> booksInOrder = new ArrayList<Book>();
		Book book1 = this.createBook("Little Women", "Other", 11.2);
		Book book2 = this.createBook("Fight Club", "Thriller", 11.2);
		booksInOrder.add(book1);
		booksInOrder.add(book2);

		Order order = this.createOrderWithDefaultStatusNoUser(3, booksInOrder);
		Long orderId = order.getOrderid();
		orepository.deleteById(orderId);
		
		Optional<Order> optionalOrder = orepository.findById(orderId);
		assertThat(optionalOrder).isNotPresent();
		
		this.createOrderWithDefaultStatusNoUser(3, booksInOrder);
		this.createOrderWithDefaultStatusNoUser(3, booksInOrder);
		orepository.deleteAll();
		
		List<Order> orders = (List<Order>) orepository.findAll();
		assertThat(orders).isEmpty();
	}

	private Order createOrderWithStatusNoNoteNoUser(int quantity, List<Book> books, String status) {
		Cart cart = this.createCartNoUser(false);

		for (Book book : books) {
			this.createCartBookCustomQuantity(quantity, book, cart);
		}

		String stringField = "field";

		Order newOrder = new Order(stringField, stringField, stringField, stringField, stringField, stringField, status,
				stringField, cart, stringField);
		orepository.save(newOrder);

		return newOrder;
	}

	private Order createOrderWithStatusNoNoteWithUser(int quantity, List<Book> books, String status, String username) {
		Cart cart = this.createCartWithUser(false, username);

		for (Book book : books) {
			this.createCartBookCustomQuantity(quantity, book, cart);
		}

		String stringField = "field";

		Order newOrder = new Order(stringField, stringField, stringField, stringField, stringField, stringField, status,
				stringField, cart, stringField);
		orepository.save(newOrder);

		return newOrder;
	}

	private Order createOrderWithDefaultStatusNoUser(int quantity, List<Book> books) {
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

	private Order createOrderWithDefaultStatusWithUser(int quantity, List<Book> books, String username) {
		Cart cart = this.createCartWithUser(false, username);

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

		List<Cart> currentCarts = cartRepository.findCurrentByUserid(user.getId());
		if (currentCarts.size() != 0 && current)
			return currentCarts.get(0);

		Cart newCart = new Cart(current, user);
		cartRepository.save(newCart);

		return newCart;
	}

	private Cart createCartNoUser(boolean current) {
		Cart newCart = new Cart(current);
		cartRepository.save(newCart);

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
		bookRepository.save(newBook);

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

package com.PirateEmperor.Librarium.repotest;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.PirateEmperor.Librarium.sqlHandlers.BookInCurrentCart;
import com.PirateEmperor.Librarium.sqlHandlers.RawBookInfo;

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class BookRepositoryTest {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private CategoryRepository crepository;

	@Autowired
	private CartRepository cartrepository;

	@Autowired
	private CartBookRepository cartBookRepository;

	@Autowired
	private OrderRepository orepository;

	@Autowired
	private BookRepository bookrepository;

	@BeforeAll
	public void resetUserRepo() {
		urepository.deleteAll();
		crepository.deleteAll();
		cartrepository.deleteAll();
		cartBookRepository.deleteAll();
		orepository.deleteAll();
		bookrepository.deleteAll();
	}

	// CRUD tests for the book repository
	// Create functionality
	@Test
	@Rollback
	public void testCreateBook() {
		Book newBook = this.createBook("Little Women", "Romance");
		assertThat(newBook.getId()).isNotNull();

		this.createBook("Little Women 2", "Thriller");
		List<Book> users = (List<Book>) bookrepository.findAll();
		assertThat(users).hasSize(2);
	}

	// Read functionalities tests
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Book> books = (List<Book>) bookrepository.findAll();
		assertThat(books).isEmpty();

		Optional<Book> optionalBook = bookrepository.findById(Long.valueOf(2));
		assertThat(optionalBook).isNotPresent();

		Book newBook1 = this.createBook("Little Women", "Romance");
		Long newBook1Id = newBook1.getId();
		this.createBook("Little Women 2", "Thriller");

		books = (List<Book>) bookrepository.findAll();
		assertThat(books).hasSize(2);

		optionalBook = bookrepository.findById(newBook1Id);
		assertThat(optionalBook).isPresent();
	}

	@Test
	@Rollback
	public void testFindByCategory() {
		Category categoryOther = this.createCategory("Other");
		List<Book> books = bookrepository.findByCategory(categoryOther);
		assertThat(books).isEmpty();

		this.createBook("Little Women", "Other");
		this.createBook("Little Women 2", "Other");

		books = bookrepository.findByCategory(categoryOther);
		assertThat(books).hasSize(2);
	}

	@Test
	@Rollback
	public void testFindByIsbn() {
		String title = "Little Women";
		String isbn = title + "isbn";
		Optional<Book> optionalBook = bookrepository.findByIsbn(isbn);
		assertThat(optionalBook).isNotPresent();

		this.createBook("Little Women", "Other");

		optionalBook = bookrepository.findByIsbn(isbn);
		assertThat(optionalBook).isPresent();
	}

	@Test
	@Rollback
	public void testFindTopSales() {
		List<RawBookInfo> topSalesBooks = bookrepository.findTopSales();
		assertThat(topSalesBooks).isEmpty();

		Book book1 = this.createBook("Little Women", "Other");
		this.createSale(2, book1);

		topSalesBooks = bookrepository.findTopSales();
		assertThat(topSalesBooks).hasSize(1);

		String topSaleBookTitle = "Little Women2";
		Book book2 = this.createBook(topSaleBookTitle, "Other");
		this.createSale(4, book2);

		topSalesBooks = bookrepository.findTopSales();
		assertThat(topSalesBooks).hasSize(2);
		assertThat(topSalesBooks.get(0).getTitle()).isEqualTo(topSaleBookTitle);
	}

	@Test
	@Rollback
	public void testfindBooksInCart() {
		List<BookInCurrentCart> booksInCart = bookrepository.findBooksInCart(Long.valueOf(1));
		assertThat(booksInCart).isEmpty();

		Cart cart = this.createCartNoUser(true);
		Long cartid = cart.getCartid();
		Book book1 = this.createBook("Little Women", "Other");
		this.createCartBookCustomQuantity(2, book1, cart);

		booksInCart = bookrepository.findBooksInCart(cartid);
		assertThat(booksInCart).hasSize(1);

		Book book2 = this.createBook("Little Women 2", "Other");
		this.createCartBookCustomQuantity(2, book2, cart);

		booksInCart = bookrepository.findBooksInCart(cartid);
		assertThat(booksInCart).hasSize(2);
	}

	@Test
	@Rollback
	public void testFindBooksInOrder() {
		List<BookInCurrentCart> booksInOrder = bookrepository.findBooksInOrder(Long.valueOf(1));
		assertThat(booksInOrder).isEmpty();

		String bookTitle = "Little Women";
		Book book1 = this.createBook(bookTitle, "Other");
		Order newOrder = this.createSale(2, book1);
		Long orderId = newOrder.getOrderid();

		booksInOrder = bookrepository.findBooksInOrder(orderId);
		assertThat(booksInOrder).hasSize(1);
		assertThat(booksInOrder.get(0).getTitle()).isEqualTo(bookTitle);
	}

	@Test
	@Rollback
	public void testfindIdsOfBooksByCartid() {
		List<Long> idsOfBooks = bookrepository.findIdsOfBooksByCartid(Long.valueOf(2));
		assertThat(idsOfBooks).isEmpty();

		Cart cart = this.createCartNoUser(true);
		Long cartid = cart.getCartid();
		Book book1 = this.createBook("Little Women", "Other");
		this.createCartBookCustomQuantity(2, book1, cart);

		idsOfBooks = bookrepository.findIdsOfBooksByCartid(cartid);
		assertThat(idsOfBooks).hasSize(1);

		Book book2 = this.createBook("Little Women 2", "Other");
		this.createCartBookCustomQuantity(2, book2, cart);

		idsOfBooks = bookrepository.findIdsOfBooksByCartid(cartid);
		assertThat(idsOfBooks).hasSize(2);
	}

	@Test
	@Rollback
	public void testfindBooksInCurrentCartByUserid() {
		List<BookInCurrentCart> booksInCart = bookrepository.findBooksInCurrentCartByUserid(Long.valueOf(2));
		assertThat(booksInCart).isEmpty();

		String username = "user1";
		User user = this.createVerifiedUser(username, username + "@gmail.com");
		Long userId = user.getId();

		Cart cart = this.createCurrentCartWithUser(user);
		Book book1 = this.createBook("Little Women", "Other");
		this.createCartBookCustomQuantity(2, book1, cart);

		booksInCart = bookrepository.findBooksInCurrentCartByUserid(userId);
		assertThat(booksInCart).hasSize(1);

		Book book2 = this.createBook("Little Women 2", "Other");
		this.createCartBookCustomQuantity(2, book2, cart);

		booksInCart = bookrepository.findBooksInCurrentCartByUserid(userId);
		assertThat(booksInCart).hasSize(2);
	}

	@Test
	@Rollback
	public void testfindIdsOfBooksInCurrentCart() {
		List<Long> idsOfBooks = bookrepository.findIdsOfBooksInCurrentCart(Long.valueOf(2));
		assertThat(idsOfBooks).isEmpty();

		String username = "user1";
		User user = this.createVerifiedUser(username, username + "@gmail.com");
		Long userId = user.getId();

		Cart cart = this.createCurrentCartWithUser(user);
		Book book1 = this.createBook("Little Women", "Other");
		this.createCartBookCustomQuantity(2, book1, cart);

		idsOfBooks = bookrepository.findIdsOfBooksInCurrentCart(userId);
		assertThat(idsOfBooks).hasSize(1);

		Book book2 = this.createBook("Little Women 2", "Other");
		this.createCartBookCustomQuantity(2, book2, cart);

		idsOfBooks = bookrepository.findIdsOfBooksInCurrentCart(userId);
		assertThat(idsOfBooks).hasSize(2);
	}

	@Test
	@Rollback
	public void testUpdateBook() {
		Book book1 = this.createBook("Little Women", "Other");

		Category updatedCategory = this.createCategory("Romance");
		String updatedTitle = "The oldman and the sea";
		String updatedAuthor = "Ernest Hemingway";
		int updatedBookYear = 2001;
		double updatedPrice = 11.24;
		String updatedUrl = "url.com";
		String updatedIsbn = "isbn222222";

		book1.setAuthor(updatedAuthor);
		book1.setBookYear(updatedBookYear);
		book1.setCategory(updatedCategory);
		book1.setIsbn(updatedIsbn);
		book1.setTitle(updatedTitle);
		book1.setUrl(updatedUrl);
		book1.setPrice(updatedPrice);
		bookrepository.save(book1);

		List<Book> romances = bookrepository.findByCategory(updatedCategory);
		assertThat(romances).hasSize(1);

		Book updatedBook = romances.get(0);
		assertThat(updatedBook.getAuthor()).isEqualTo(updatedAuthor);
		assertThat(updatedBook.getBookYear()).isEqualTo(updatedBookYear);
		assertThat(updatedBook.getCategory()).isEqualTo(updatedCategory);
		assertThat(updatedBook.getIsbn()).isEqualTo(updatedIsbn);
		assertThat(updatedBook.getTitle()).isEqualTo(updatedTitle);
		assertThat(updatedBook.getPrice()).isEqualTo(updatedPrice);
		assertThat(updatedBook.getUrl()).isEqualTo(updatedUrl);
	}

	@Test
	@Rollback
	public void testDeleteBook() {
		Book book1 = this.createBook("Little Women", "Other");
		Long book1Id = book1.getId();
		bookrepository.deleteById(book1Id);

		List<Book> books = (List<Book>) bookrepository.findAll();
		assertThat(books).isEmpty();

		this.createBook("Little Women", "Other");
		this.createBook("Little Women 2", "Other");

		bookrepository.deleteAll();
		books = (List<Book>) bookrepository.findAll();
		assertThat(books).isEmpty();
	}

	private Book createBook(String title, String categoryName) {
		Category category = this.createCategory(categoryName);
		Book newBook = new Book(title, "Chuck Palahniuk", title + "isbn", 1998, 10.2, category, "someurlToPicture");
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

	private Order createSale(int quantity, Book book) {
		Cart cart = this.createCartNoUser(false);
		this.createCartBookCustomQuantity(quantity, book, cart);

		String stringField = "field";

		Order newOrder = new Order(stringField, stringField, stringField, stringField, stringField, stringField,
				stringField, cart, stringField, stringField);
		orepository.save(newOrder);

		return newOrder;
	}

	private CartBook createCartBookCustomQuantity(int quantity, Book book, Cart cart) {
		CartBook newCartBook = new CartBook(quantity, cart, book);
		cartBookRepository.save(newCartBook);

		return newCartBook;
	}

	private Cart createCurrentCartWithUser(User user) {
		List<Cart> currentCarts = cartrepository.findCurrentByUserid(user.getId());
		if (currentCarts.size() != 0)
			return currentCarts.get(0);

		Cart newCart = new Cart(true, user);
		cartrepository.save(newCart);

		return newCart;
	}

	private Cart createCartNoUser(boolean isCurrent) {
		Cart newCart = new Cart(isCurrent);
		cartrepository.save(newCart);

		return newCart;
	}

	private User createVerifiedUser(String username, String email) {
		Optional<User> optionalUser = urepository.findByUsername(username);
		if (optionalUser.isPresent())
			return optionalUser.get();

		User newUser = new User("Test", "test", username, "Some_Pwd_Hash", "USER", email, true);
		urepository.save(newUser);

		return newUser;
	}
}

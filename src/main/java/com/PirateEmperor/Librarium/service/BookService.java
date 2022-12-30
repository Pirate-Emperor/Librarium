package com.PirateEmperor.Librarium.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.PirateEmperor.Librarium.httpHandlers.CartInfo;
import com.PirateEmperor.Librarium.httpHandlers.BookUpdate;
import com.PirateEmperor.Librarium.model.Book;
import com.PirateEmperor.Librarium.model.BookRepository;
import com.PirateEmperor.Librarium.model.Category;
import com.PirateEmperor.Librarium.model.CategoryRepository;
import com.PirateEmperor.Librarium.model.User;
import com.PirateEmperor.Librarium.sqlHandlers.BookInCurrentCart;
import com.PirateEmperor.Librarium.sqlHandlers.RawBookInfo;

@Service
public class BookService {
	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CommonService commonService;

	// Method to get books
	public List<Book> getBooks() {
		List<Book> books = (List<Book>) bookRepository.findAll();
		return books;
	}

	// Method to get a book by id
	public Optional<Book> getBookById(Long bookId) {
		Optional<Book> optionalBook = bookRepository.findById(bookId);
		return optionalBook;
	}

	// Method to get list of books by category:
	public List<Book> getBooksByCategory(Category category) {
		List<Book> booksInCategory = bookRepository.findByCategory(category);
		return booksInCategory;
	}

	// Method to get list of books that are top saled:
	public List<RawBookInfo> getTopSales() {
		List<RawBookInfo> booksTopSaled = bookRepository.findTopSales();
		return booksTopSaled;
	}

	// Method to get list of Ids of books by cart id
	public List<Long> getIdsOfBooksBycartid(Long cartid) {
		commonService.findCartAndCheckIsPrivate(cartid);

		List<Long> idsOfBooksInCart = bookRepository.findIdsOfBooksByCartid(cartid);
		return idsOfBooksInCart;
	}

	// Method to get the list of IDs of the books in the current cart of the user
	// by user authentication:
	public List<Long> getIdsOfBooksInCurrentCart(Authentication authentication) {
		User user = commonService.checkAuthentication(authentication);
		Long userId = user.getId();
		commonService.findCurrentCartOfUser(user);

		List<Long> idsOfBooksInCurrentCart = bookRepository.findIdsOfBooksInCurrentCart(userId);
		return idsOfBooksInCurrentCart;
	}

	// Method to get list of Books in Cart by cartid and cart password:
	public List<BookInCurrentCart> getBooksInCartByIdAndPassword(CartInfo cartInfo) {
		Long cartid = cartInfo.getId();
		String password = cartInfo.getPassword();

		commonService.findCartAndCheckIsPrivateAndCheckPassword(cartid, password);

		List<BookInCurrentCart> booksInCart = bookRepository.findBooksInCart(cartid);
		return booksInCart;
	}

	// Method to get the list of books in current cart of the user by user id and
	// authentication
	public List<BookInCurrentCart> getCurrentCartByUserId(Long userId, Authentication authentication) {
		User user = commonService.checkAuthenticationAndAuthorize(authentication, userId);
		commonService.findCurrentCartOfUser(user);

		List<BookInCurrentCart> booksInCurrentCartOfUser = bookRepository.findBooksInCurrentCartByUserid(userId);
		return booksInCurrentCartOfUser;
	}

	// Method to get list of Books in order by orderId:
	public List<BookInCurrentCart> getBooksByOrderId(Long orderId) {
		commonService.findOrder(orderId);

		List<BookInCurrentCart> booksInOrder = bookRepository.findBooksInOrder(orderId);
		return booksInOrder;
	}

	// Method to update book:
	public ResponseEntity<?> updateBook(Long bookId, BookUpdate updatedBook) {
		Book book = commonService.findBook(bookId);
		String isbn = updatedBook.getIsbn();
		this.checkIsbn(isbn, book);
		this.updateBook(book, updatedBook);

		return new ResponseEntity<>("The book was updated successfully", HttpStatus.OK);
	}

	private void checkIsbn(String isbn, Book bookToUpdate) {
		Optional<Book> optionalBook = bookRepository.findByIsbn(isbn);
		if (optionalBook.isPresent()) {
			Book bookInDb = optionalBook.get();
			if (bookInDb.getId() != bookToUpdate.getId())
				throw new ResponseStatusException(HttpStatus.CONFLICT, "The duplicate ISBN value is not allowed");
		}

	}

	private void updateBook(Book bookToUpdate, BookUpdate updatedBook) {
		bookToUpdate.setTitle(updatedBook.getTitle());
		bookToUpdate.setAuthor(updatedBook.getAuthor());
		bookToUpdate.setIsbn(updatedBook.getIsbn());
		bookToUpdate.setBookYear(updatedBook.getBookYear());
		bookToUpdate.setPrice(updatedBook.getPrice());
		bookToUpdate.setUrl(updatedBook.getUrl());

		Category updatedCategory = this.findCategory(updatedBook.getCategoryId());
		bookToUpdate.setCategory(updatedCategory);
		bookRepository.save(bookToUpdate);
	}

	private Category findCategory(Long categoryId) {
		Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

		if (!optionalCategory.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The category wasn't found");

		Category category = optionalCategory.get();
		return category;
	}
}

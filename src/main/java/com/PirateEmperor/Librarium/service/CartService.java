package com.PirateEmperor.Librarium.service;

import java.util.Optional;

import com.PirateEmperor.Librarium.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.PirateEmperor.Librarium.httpHandlers.CartInfo;
import com.PirateEmperor.Librarium.httpHandlers.BookQuantityInfo;
import com.PirateEmperor.Librarium.httpHandlers.QuantityInfo;
import com.PirateEmperor.Librarium.model.Cart;
import com.PirateEmperor.Librarium.sqlHandlers.QuantityOfCart;
import com.PirateEmperor.Librarium.sqlHandlers.TotalOfCart;

@Service
public class CartService {
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartBookRepository cartBookRepository;

	@Autowired
	private CommonService commonService;

	// Method to get the total price of the cart by cartid and cart password:
	public TotalOfCart getTotalBycartid(CartInfo cartInfo) {
		Long cartid = cartInfo.getId();
		String password = cartInfo.getPassword();
		commonService.findCartAndCheckIsPrivateAndCheckPassword(cartid, password);

		TotalOfCart totalOfCart = cartRepository.findTotalOfCart(cartid);
		return totalOfCart;
	}

	// Method to get the total of current cart of user by authentication:
	public TotalOfCart getCurrentCartTotal(Authentication authentication) {
		User user = commonService.checkAuthentication(authentication);
		Long userId = user.getId();
		commonService.findCurrentCartOfUser(user);

		TotalOfCart totalOfCurrentCart = cartRepository.findTotalOfCurrentCart(userId);
		return totalOfCurrentCart;
	}

	// Method to get the total amount of books in the current cart of the user
	// (returns interface with cartid and items fields):
	public QuantityOfCart getCurrentCartQuantity(Authentication authentication) {
		User user = commonService.checkAuthentication(authentication);
		Long userId = user.getId();
		commonService.findCurrentCartOfUser(user);

		QuantityOfCart quantityOfCurrentCart = cartRepository.findQuantityInCurrent(userId);
		return quantityOfCurrentCart;
	}

	// Method to create Cart with password and no user. The method returns the
	// cart Id and its password
	public CartInfo createCartNoAuthentication() {
		String password = RandomStringUtils.randomAlphanumeric(15);
		String hashedPassword = commonService.encodePassword(password);
		Long cartid = this.createCart(hashedPassword);

		CartInfo createdCartInfo = new CartInfo(cartid, password);
		return createdCartInfo;
	}

	private Long createCart(String hashedPassword) {
		Cart cart = new Cart(hashedPassword);
		cartRepository.save(cart);
		Long cartid = cart.getCartid();

		return cartid;
	}

	// Method to add the certain quantity of the book to the cart by cartid and
	// cart password:
	public ResponseEntity<?> addBookToCartNoAuthentication(Long cartid,
			BookQuantityInfo bookQuantityAndCartPassword) {
		Long bookId = bookQuantityAndCartPassword.getBookid();
		int additionalQuantity = bookQuantityAndCartPassword.getQuantity();
		String password = bookQuantityAndCartPassword.getPassword();

		Cart cart = commonService.findCartAndCheckIsPrivateAndCheckPasswordAndCheckIsCurrent(cartid, password);

		return this.addQuantityOfBookToTheCart(cart, bookId, additionalQuantity);
	}

	// Method to add the certain quantity of the book to the current cart of the
	// user:
	public ResponseEntity<?> addBookToCurrentCart(Long bookId, QuantityInfo quantityInfo,
			Authentication authentication) {
		int additionalQuantity = quantityInfo.getQuantity();

		User user = commonService.checkAuthentication(authentication);
		Cart currentCart = commonService.findCurrentCartOfUser(user);

		return this.addQuantityOfBookToTheCart(currentCart, bookId, additionalQuantity);
	}

	private ResponseEntity<?> addQuantityOfBookToTheCart(Cart cart, Long bookId, int additionalQuantity) {
		Long cartid = cart.getCartid();
		Book book = commonService.findBook(bookId);
		Optional<CartBook> optionalCartBook = this.getOptionalCartBook(cartid, bookId);

		if (optionalCartBook.isPresent()) {
			CartBook cartBook = optionalCartBook.get();
			this.addQuantityToCartBook(cartBook, additionalQuantity);
		} else {
			this.createCartBook(additionalQuantity, cart, book);
		}

		return new ResponseEntity<>("Book was added to cart successfully", HttpStatus.OK);
	}

	private void addQuantityToCartBook(CartBook cartBook, int additionalQuantity) {
		int currentQuantity = cartBook.getQuantity();
		int newQuantity = currentQuantity + additionalQuantity;
		this.setBookQuantityInCart(newQuantity, cartBook);
	}

	private void createCartBook(int quantity, Cart cart, Book book) {
		CartBook cartBook = new CartBook(quantity, cart, book);
		cartBookRepository.save(cartBook);
	}

	// Method to reduce the amount of book by bookid and cartInfo
	public ResponseEntity<?> reduceBookNoAuthentication(Long bookId, CartInfo cartInfo) {
		Long cartid = cartInfo.getId();
		String password = cartInfo.getPassword();

		Cart cart = commonService.findCartAndCheckIsPrivateAndCheckPasswordAndCheckIsCurrent(cartid, password);

		return this.reduceQuantityOfBookInCart(cart, bookId);
	}

	// Method to reduce the quantity of the book in the current cart of the
	// authenticated user:
	public ResponseEntity<?> reduceBookAuthenticated(Long bookId, Authentication authentication) {
		User user = commonService.checkAuthentication(authentication);
		Cart currentCart = commonService.findCurrentCartOfUser(user);

		return this.reduceQuantityOfBookInCart(currentCart, bookId);
	}

	private ResponseEntity<?> reduceQuantityOfBookInCart(Cart cart, Long bookId) {
		Long cartid = cart.getCartid();
		commonService.findBook(bookId);

		CartBook cartBook = this.findCartBook(bookId, cartid);
		int quantity = cartBook.getQuantity();
		quantity = quantity - 1;

		return this.reduceQuantityOfBookInCart(quantity, cartBook);
	}

	private ResponseEntity<?> reduceQuantityOfBookInCart(int quantity, CartBook cartBook) {
		if (quantity > 0) {
			this.setBookQuantityInCart(quantity, cartBook);
			return new ResponseEntity<>("The quantity of the book in the cart was reduced by one", HttpStatus.OK);
		} else {
			return this.deleteBookFromCart(cartBook);
		}
	}

	// Method to delete book from cart By bookid and cartInfo
	public ResponseEntity<?> deleteBookNoAuthentication(Long bookId, CartInfo cartInfo) {
		Long cartid = cartInfo.getId();
		String password = cartInfo.getPassword();

		Cart cart = commonService.findCartAndCheckIsPrivateAndCheckPasswordAndCheckIsCurrent(cartid, password);

		return this.deleteBookFromCart(cart, bookId);
	}

	// Method to delete the book from the current cart of the authenticated user:
	public ResponseEntity<?> deleteBookFromCurrentCart(Long bookId, Authentication authentication) {
		User user = commonService.checkAuthentication(authentication);
		Cart currentCart = commonService.findCurrentCartOfUser(user);

		return this.deleteBookFromCart(currentCart, bookId);
	}

	private ResponseEntity<?> deleteBookFromCart(Cart cart, Long bookId) {
		Long cartid = cart.getCartid();
		commonService.findBook(bookId);
		CartBook cartBook = this.findCartBook(bookId, cartid);

		return this.deleteBookFromCart(cartBook);
	}

	// Method to clear current cart of the authenticated user:
	public ResponseEntity<?> clearCurrentCart(Long userId, Authentication authentication) {
		User user = commonService.checkAuthenticationAndAuthorize(authentication, userId);
		Cart currentCart = commonService.findCurrentCartOfUser(user);

		long deleted = cartBookRepository.deleteByCart(currentCart);
		return new ResponseEntity<>(deleted + " records were deleted from current cart", HttpStatus.OK);
	}

	// Method to find CartBook:
	private CartBook findCartBook(Long bookId, Long cartid) {
		Optional<CartBook> optionalCartBook = this.getOptionalCartBook(cartid, bookId);
		if (!optionalCartBook.isPresent())
			throw new ResponseStatusException(HttpStatus.CONFLICT, "The book is not in the cart");

		CartBook cartBook = optionalCartBook.get();
		return cartBook;
	}

	// Method to find optional cart book by cartid and bookId:
	private Optional<CartBook> getOptionalCartBook(Long cartid, Long bookId) {
		CartBookKey cartBookKey = new CartBookKey(cartid, bookId);

		Optional<CartBook> optionalCartBook = cartBookRepository.findById(cartBookKey);
		return optionalCartBook;
	}

	// Method to set new quantity for the book in the cart
	private void setBookQuantityInCart(int quantity, CartBook cartBook) {
		cartBook.setQuantity(quantity);
		cartBookRepository.save(cartBook);
	}

	// Method to delete the book from the cart by deleting cartBook record:
	private ResponseEntity<?> deleteBookFromCart(CartBook cartBook) {
		cartBookRepository.delete(cartBook);
		return new ResponseEntity<>("The book was deleted from the cart", HttpStatus.OK);
	}
}

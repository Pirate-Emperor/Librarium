package com.PirateEmperor.Librarium.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.PirateEmperor.Librarium.UserProfile;
import com.PirateEmperor.Librarium.model.Cart;
import com.PirateEmperor.Librarium.model.CartRepository;
import com.PirateEmperor.Librarium.model.Book;
import com.PirateEmperor.Librarium.model.BookRepository;
import com.PirateEmperor.Librarium.model.Order;
import com.PirateEmperor.Librarium.model.OrderRepository;
import com.PirateEmperor.Librarium.model.User;
import com.PirateEmperor.Librarium.model.UserRepository;

@Service
public class CommonService {
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookRepository bookRepository;

	// Method to find the cart and check if it's private:
	public Cart findCartAndCheckIsPrivate(Long cartid) {
		Cart cart = this.findCart(cartid);
		this.checkIfCartIsPrivate(cart);
		return cart;
	}

	// Method to find cart, check if it is private and check the provided
	// password:
	public Cart findCartAndCheckIsPrivateAndCheckPassword(Long cartid, String password) {
		Cart cart = this.findCartAndCheckIsPrivate(cartid);
		this.checkPassword(password, cart.getPasswordHash());
		return cart;
	}

	// Method to find cart, check if it's private, it's password and check if it's
	// current
	public Cart findCartAndCheckIsPrivateAndCheckPasswordAndCheckIsCurrent(Long cartid, String password) {
		Cart cart = this.findCartAndCheckIsPrivateAndCheckPassword(cartid, password);
		this.checkIfCartIsCurrent(cart);
		return cart;
	}

	// The method to find out if the user has exactly one current cart:
	public Cart findCurrentCartOfUser(User user) {
		Long userId = user.getId();
		List<Cart> currentCartsOfUser = cartRepository.findCurrentByUserid(userId);

		if (currentCartsOfUser.size() != 1) {
			return this.handleUserHasMoreOrLessThanOneCurrentCartCase(currentCartsOfUser, user);
		}

		Cart currentCart = currentCartsOfUser.get(0);
		return currentCart;
	}

	private Cart handleUserHasMoreOrLessThanOneCurrentCartCase(List<Cart> currentCartsOfUser, User user) {
		for (Cart currentCartOfUser : currentCartsOfUser) {
			cartRepository.delete(currentCartOfUser);
		}
		Cart newCurrentCartForUser = new Cart(true, user);
		cartRepository.save(newCurrentCartForUser);
		return newCurrentCartForUser;
	}

	// Method to add new current Cart for the user
	public void addCurrentCartForUser(User user) {
		Long userId = user.getId();
		this.checkCurrentCartsOfUser(userId);
		Cart newCurrentCartForUser = new Cart(true, user);
		cartRepository.save(newCurrentCartForUser);
	}

	private void checkCurrentCartsOfUser(Long userId) {
		List<Cart> currentCarts = cartRepository.findCurrentByUserid(userId);

		if (currentCarts.size() > 0) {
			for (Cart currentCartOfUser : currentCarts) {
				cartRepository.delete(currentCartOfUser);
			}
		}
	}

	private Cart findCart(Long cartid) {
		Optional<Cart> optionalCart = cartRepository.findById(cartid);

		if (!optionalCart.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The cart wasn't found by id");

		Cart cart = optionalCart.get();
		return cart;
	}

	private void checkIfCartIsPrivate(Cart cart) {
		User cartOwner = cart.getUser();

		if (cartOwner != null)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The cart is private");
	}

	private void checkIfCartIsCurrent(Cart cart) {
		if (!cart.isCurrent())
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You can't change not current cart");
	}

	// Method to encode password:
	public String encodePassword(String password) {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(password);

		return hashPwd;
	}

	// Method to compare password and hashedPassword with BCryp encoder
	public void checkPassword(String password, String passwordHash) {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		if (!bc.matches(password, passwordHash))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is wrong");
	}

	// Method to check if the order is in the db by orderId:
	public Order findOrder(Long orderId) {
		Optional<Order> optionalOrder = orderRepository.findById(orderId);
		if (!optionalOrder.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The order wasn't found by id");

		Order order = optionalOrder.get();
		return order;
	}

	// Method to find order and check its password
	public Order findOrderAndCheckPassword(Long orderId, String password) {
		Order order = this.findOrder(orderId);
		this.checkPassword(password, order.getPassword());

		return order;
	}

	// Method to check provided authentication and find user by auth
	public User checkAuthentication(Authentication authentication) {
		String username = this.getAuthenticatedUsername(authentication);
		User user = this.findUserByUsername(username);

		return user;
	}

	// Method to check authentication and then check if the authenticated user is
	// the target user by comparing IDs
	public User checkAuthenticationAndAuthorize(Authentication authentication, Long userId) {
		User user = this.checkAuthentication(authentication);
		this.checkAuthorization(user, userId);
		return user;
	}

	// Method to check if the user has rights by user instance and userId by
	// comparing IDs:
	public void checkAuthorization(User user, Long userId) {
		if (user.getId() != userId)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"You are not allowed to get someone else's info");
	}

	private String getAuthenticatedUsername(Authentication authentication) {
		if (!(authentication.getPrincipal() instanceof UserProfile))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");

		UserProfile UserProfile = (UserProfile) authentication.getPrincipal();
		String username = UserProfile.getUsername();

		return username;
	}

	// The method to find the user by username:
	public User findUserByUsername(String username) {
		Optional<User> optionalUser = userRepository.findByUsername(username);

		if (!optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this username doesn't exist");

		User user = optionalUser.get();

		return user;
	}

	// Method to find the book by id:
	public Book findBook(Long bookId) {
		Optional<Book> optionalBook = bookRepository.findById(bookId);
		if (!optionalBook.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The book wasn't found by id");

		Book book = optionalBook.get();
		return book;
	}
}

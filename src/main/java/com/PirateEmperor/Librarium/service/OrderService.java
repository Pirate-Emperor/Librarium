package com.PirateEmperor.Librarium.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.PirateEmperor.Librarium.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.PirateEmperor.Librarium.httpHandlers.AddressInfo;
import com.PirateEmperor.Librarium.httpHandlers.AddressInfoNoAuthentication;
import com.PirateEmperor.Librarium.httpHandlers.OrderInfo;
import com.PirateEmperor.Librarium.httpHandlers.OrderPasswordInfo;
import com.PirateEmperor.Librarium.model.Cart;
import com.PirateEmperor.Librarium.sqlHandlers.TotalOfCart;

import jakarta.mail.MessagingException;

@Service
public class OrderService {
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartBookRepository cartBookRepository;

	@Autowired
	private CommonService commonService;

	@Autowired
	private MailService mailService;

	// Method to get the list of all the orders:
	public List<Order> getOrders() {
		List<Order> orders = (List<Order>) orderRepository.findAll();
		return orders;
	}

	// Method to get the order by order id:
	public Order getOrderById(Long orderId) {
		Order order = commonService.findOrder(orderId);
		return order;
	}

	// Method to get the list of orders of the user
	public List<Order> getOrdersByUserId(Long userId, Authentication authentication) {
		commonService.checkAuthenticationAndAuthorize(authentication, userId);

		List<Order> ordersOfUser = orderRepository.findByUserid(userId);
		return ordersOfUser;
	}

	// Method to get order by it's id and password
	public Order getOrderByIdAndPassword(OrderPasswordInfo orderInfo) {
		Long orderId = orderInfo.getOrderid();
		String password = orderInfo.getPassword();

		Order order = commonService.findOrderAndCheckPassword(orderId, password);
		return order;
	}

	// Method to get total price of order by order Id:
	public TotalOfCart getTotalOfOrderByOrderId(Long orderId) {
		commonService.findOrder(orderId);

		TotalOfCart totalOfOrder = cartRepository.findTotalOfOrder(orderId);
		return totalOfOrder;
	}

	// Method to check provided order id and order password:
	public ResponseEntity<?> checkOrderNumber(OrderPasswordInfo orderInfo) {
		Long orderId = orderInfo.getOrderid();
		String password = orderInfo.getPassword();

		commonService.findOrderAndCheckPassword(orderId, password);

		return new ResponseEntity<>("The order number and password are correct", HttpStatus.OK);
	}

	// Method to create order out of cart by cart id and cart password:
	public OrderPasswordInfo makeSaleNoAuthentication(AddressInfoNoAuthentication addressInfo)
			throws MessagingException, UnsupportedEncodingException {
		Long cartid = addressInfo.getCartid();
		String cartPassword = addressInfo.getPassword();

		Cart cart = commonService.findCartAndCheckIsPrivateAndCheckPasswordAndCheckIsCurrent(cartid,
				cartPassword);
		String passwordRandom = this.checkIfCartIsEmptyAndSetCartNotCurrentAndGeneratePassword(cart);
		String hashedPassword = commonService.encodePassword(passwordRandom);

		Long orderId = this.createOrderByAddressInfoNoAuthentication(addressInfo, cart, hashedPassword);
		OrderPasswordInfo orderPassword = new OrderPasswordInfo(orderId, passwordRandom);

		this.tryToSendOrderInfoEmail(addressInfo.getFirstname(), addressInfo.getEmail(), orderId, passwordRandom);

		return orderPassword;
	}

	private Long createOrderByAddressInfoNoAuthentication(AddressInfoNoAuthentication addressInfo, Cart cart,
			String hashedPassword) {
		Order order = new Order(addressInfo.getFirstname(), addressInfo.getLastname(), addressInfo.getCountry(),
				addressInfo.getCity(), addressInfo.getStreet(), addressInfo.getPostcode(), addressInfo.getEmail(),
				cart, addressInfo.getNote(), hashedPassword);
		orderRepository.save(order);

		Long orderId = order.getOrderid();
		return orderId;
	}

	// Method to create order out of authenticated user's current cart:
	public OrderPasswordInfo makeSaleByUserId(Long userId, AddressInfo addressInfo, Authentication authentication)
			throws MessagingException, UnsupportedEncodingException {
		User user = commonService.checkAuthenticationAndAuthorize(authentication, userId);
		Cart currentCart = commonService.findCurrentCartOfUser(user);

		String passwordRandom = this.checkIfCartIsEmptyAndSetCartNotCurrentAndGeneratePassword(currentCart);
		String hashedPassword = commonService.encodePassword(passwordRandom);

		Long orderId = this.createOrderByAddressInfo(addressInfo, currentCart, hashedPassword);
		OrderPasswordInfo orderPassword = new OrderPasswordInfo(orderId, passwordRandom);

		this.tryToSendOrderInfoEmail(user.getUsername(), user.getEmail(), orderId, passwordRandom);

		if (!addressInfo.getEmail().equals(user.getEmail())) {
			this.tryToSendOrderInfoEmail(user.getUsername(), addressInfo.getEmail(), orderId, passwordRandom);
		}

		commonService.addCurrentCartForUser(user);

		return orderPassword;
	}

	private Long createOrderByAddressInfo(AddressInfo addressInfo, Cart cart, String hashedPassword) {
		Order order = new Order(addressInfo.getFirstname(), addressInfo.getLastname(), addressInfo.getCountry(),
				addressInfo.getCity(), addressInfo.getStreet(), addressInfo.getPostcode(), addressInfo.getEmail(),
				cart, addressInfo.getNote(), hashedPassword);
		orderRepository.save(order);

		Long orderId = order.getOrderid();
		return orderId;
	}

	private String checkIfCartIsEmptyAndSetCartNotCurrentAndGeneratePassword(Cart cart) {
		this.checkIfCartIsEmpty(cart);
		this.setCartNotCurrent(cart);

		String passwordRandom = RandomStringUtils.randomAlphanumeric(15);
		return passwordRandom;
	}

	private void checkIfCartIsEmpty(Cart cart) {
		List<CartBook> cartBooksInCart = cartBookRepository.findByCart(cart);
		if (cartBooksInCart.size() == 0)
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The cart is empty");
	}

	private void setCartNotCurrent(Cart cart) {
		cart.setCurrent(false);
		cartRepository.save(cart);
	}

	private void tryToSendOrderInfoEmail(String firstnameOrUsername, String email, Long orderId, String password)
			throws MessagingException, UnsupportedEncodingException {
		try {
			mailService.sendOrderInfoEmail(firstnameOrUsername, email, orderId, password);
		} catch (MailAuthenticationException e) {
		}
	}

	// Method to update order's info by orderId:
	public ResponseEntity<?> updateOrder(Long orderId, OrderInfo orderInfo)
			throws MessagingException, UnsupportedEncodingException {
		Order order = commonService.findOrder(orderId);
		this.updateOrder(order, orderInfo, orderId);
		return new ResponseEntity<>("Order Info was updated successfully", HttpStatus.OK);
	}

	private void updateOrder(Order order, OrderInfo orderInfo, Long orderId)
			throws MessagingException, UnsupportedEncodingException {
		this.updateOrderFieldsExceptEmailAndStatus(order, orderInfo);
		order = this.handleEmailChangedCase(order, orderInfo, orderId);
		order = this.handleStatusChangedCase(order, orderInfo, orderId);

		orderRepository.save(order);
	}

	private Order updateOrderFieldsExceptEmailAndStatus(Order order, OrderInfo orderInfo) {
		order.setFirstname(orderInfo.getFirstname());
		order.setLastname(orderInfo.getLastname());
		order.setCountry(orderInfo.getCountry());
		order.setCity(orderInfo.getCity());
		order.setStreet(orderInfo.getStreet());
		order.setPostcode(orderInfo.getPostcode());

		return order;
	}

	private Order handleEmailChangedCase(Order order, OrderInfo orderInfo, Long orderId)
			throws MessagingException, UnsupportedEncodingException {
		if (!order.getEmail().equals(orderInfo.getEmail())) {
			this.tryToSendOrderEmailChanged(orderInfo, orderId);
			order.setEmail(orderInfo.getEmail());
		}

		return order;
	}

	private Order handleStatusChangedCase(Order order, OrderInfo orderInfo, Long orderId)
			throws MessagingException, UnsupportedEncodingException {
		if (!order.getStatus().equals(orderInfo.getStatus())) {
			Cart cart = order.getCart();
			if (cart.getUser() != null) {
				this.tryToSendStatusChangeEmail(orderInfo, cart.getUser().getEmail(), orderId);
			}
			this.tryToSendStatusChangeEmail(orderInfo, orderInfo.getEmail(), orderId);
			order.setStatus(orderInfo.getStatus());
		}
		return order;
	}

	private void tryToSendOrderEmailChanged(OrderInfo orderInfo, Long orderId)
			throws MessagingException, UnsupportedEncodingException {
		try {
			mailService.sendOrderEmailChanged(orderInfo.getFirstname(), orderInfo.getEmail(), orderId);
		} catch (MailAuthenticationException e) {
		}
	}

	private void tryToSendStatusChangeEmail(OrderInfo orderInfo, String email, Long orderId)
			throws MessagingException, UnsupportedEncodingException {
		try {
			mailService.sendStatusChangeEmail(orderInfo.getFirstname(), email, orderId, orderInfo.getStatus());
		} catch (MailAuthenticationException e) {
		}
	}
}

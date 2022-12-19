package com.PirateEmperor.Librarium.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.PirateEmperor.Librarium.httpHandlers.BookUpdate;
import com.PirateEmperor.Librarium.httpHandlers.OrderInfo;
import com.PirateEmperor.Librarium.httpHandlers.RoleVerificationInfo;
import com.PirateEmperor.Librarium.model.Order;
import com.PirateEmperor.Librarium.model.User;
import com.PirateEmperor.Librarium.service.BookService;
import com.PirateEmperor.Librarium.service.OrderService;
import com.PirateEmperor.Librarium.service.UserService;

import jakarta.mail.MessagingException;

@CrossOrigin(origins = "*")
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
public class RestAdminController {
	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private BookService bookService;

	@GetMapping("/users")
	public @ResponseBody List<User> getUsers() {

		return userService.getUsers();

	}

	@GetMapping("/orders")
	public @ResponseBody List<Order> getOrders() {

		return orderService.getOrders();

	}

	@GetMapping("/orders/{orderid}")
	public @ResponseBody Order getOrderById(@PathVariable("orderid") Long orderId) {

		return orderService.getOrderById(orderId);

	}

	@PutMapping("books/{bookid}")
	public ResponseEntity<?> updateBook(@PathVariable("bookid") Long bookId, @RequestBody BookUpdate book) {

		return bookService.updateBook(bookId, book);

	}

	@PutMapping("/updateorder/{orderid}")
	public ResponseEntity<?> updateOrder(@PathVariable("orderid") Long orderId, @RequestBody OrderInfo orderInfo)
			throws MessagingException, UnsupportedEncodingException {

		return orderService.updateOrder(orderId, orderInfo);

	}

	@PutMapping("/verifyandchangerole/{userid}")
	public ResponseEntity<?> changeUserRoleAndVerification(@PathVariable("userid") Long userId,
			@RequestBody RoleVerificationInfo roleVerificationInfo,
			Authentication authentication) {

		return userService.changeUserRoleAndVerification(userId, roleVerificationInfo, authentication);

	}
}

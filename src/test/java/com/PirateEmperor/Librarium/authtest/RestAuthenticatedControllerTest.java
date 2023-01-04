package com.PirateEmperor.Librarium.authtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.PirateEmperor.Librarium.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.PirateEmperor.Librarium.httpHandlers.AccountCredentials;
import com.PirateEmperor.Librarium.httpHandlers.AddressInfo;
import com.PirateEmperor.Librarium.httpHandlers.PasswordInfo;
import com.PirateEmperor.Librarium.httpHandlers.QuantityInfo;
import com.PirateEmperor.Librarium.model.Cart;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class RestAuthenticatedControllerTest {
	private static final String BOOK_TITLE = "Little Women";
	private static final String OTHER_CATEGORY = "Other";
	private static final String ROMANCE_CATEGORY = "Romance";
	private static final Double DEFAULT_PRICE = 10.5;

	private static final String FIRSTNAME = "John";
	private static final String LASTNAME = "Doe";
	private static final String COUNTRY = "Finland";
	private static final String CITY = "Helsinki";
	private static final String STREET = "Kitarakuja 3B";
	private static final String POSTCODE = "00410";
	private static final String NOTE = "Complete my order quickly";

	private static final String USERNAME = "user1";
	private static final String EMAIL = "user1@mail.com";

	private static final String DEFAULT_PASSWORD = "test";
	private static final String WRONG_PWD = "wrong_pwd";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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

	private String jwt;
	private Long authenticatedUserId;
	private User authenticatedUser;
	private Cart authenticatedUserCurrentCart;

	@BeforeAll
	public void setUp() throws Exception {
		this.resetRepos();
		this.getToken();
	}

	@Nested
	class testGetOrdersByUserId {
		@Test
		@Rollback
		public void testGetOrdersByUserIdIdMissmatchCase() throws Exception {
			String requestURI = this.getRequestURI(Long.valueOf(200));

			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isUnauthorized());
		}

		@Test
		@Rollback
		public void testGetOrdersByUserIdGoodCases() throws Exception {
			String requestURI = this.getRequestURI(authenticatedUserId);

			// No orders case
			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(0));

			List<Book> booksInOrder = new ArrayList<Book>();
			Book book1 = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Book book2 = createBook(BOOK_TITLE + " 2", OTHER_CATEGORY, DEFAULT_PRICE);
			booksInOrder.add(book1);
			booksInOrder.add(book2);
			createOrderOutOfCurrentCartOfAuthenticatedUser(2, booksInOrder);

			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(1));
		}

		private String getRequestURI(Long userId) {
			return "/users/" + userId + "/orders";
		}
	}

	@Nested
	class testGetUserById {
		@Test
		@Rollback
		public void testGetUserByIdIdMissmatchCase() throws Exception {
			String requestURI = "/users/";

			String requestURIIdMissmatch = requestURI + Long.valueOf(22);

			mockMvc.perform(get(requestURIIdMissmatch).header("Authorization", jwt))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@Rollback
		public void testGetUserByIdIdGoodCase() throws Exception {
			String requestURI = "/users/";

			String requestURIGood = requestURI + authenticatedUserId;

			mockMvc.perform(get(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.firstname").value(FIRSTNAME));
		}
	}

	@Nested
	class testAddBookToCurrentCart {
		@Test
		@Rollback
		public void testAddBookToCurrentCartBookNotFoundCase() throws Exception {
			String requestURIBookNotFound = "/additem/11";

			QuantityInfo quantityInfo = new QuantityInfo(3);
			String requestBody = objectMapper.writeValueAsString(quantityInfo);

			mockMvc.perform(post(requestURIBookNotFound).header("Authorization", jwt)
					.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isNotFound());
		}

		@Test
		@Rollback
		public void testAddBookToCurrentCartGoodCases() throws Exception {
			String requestURI = "/additem/";

			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();
			String requestURIGood = requestURI + bookId;
			QuantityInfo quantityInfo = new QuantityInfo(1);
			String requestBody = objectMapper.writeValueAsString(quantityInfo);

			// Book is not in the cart yet case
			mockMvc.perform(post(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isOk());

			List<CartBook> cartBooks = (List<CartBook>) cartBookRepository.findAll();
			assertThat(cartBooks).hasSize(1);
			assertThat(cartBooks.get(0).getQuantity()).isEqualTo(1);

			// Book is already in the cart case:
			mockMvc.perform(post(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isOk());

			cartBooks = (List<CartBook>) cartBookRepository.findAll();
			assertThat(cartBooks).hasSize(1);
			assertThat(cartBooks.get(0).getQuantity()).isEqualTo(2);
		}

		@Test
		@Rollback
		public void testAddBookToCurrentCartNoCurrentCartsCase() throws Exception {
			String requestURI = "/additem/";

			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();
			String requestURIGood = requestURI + bookId;
			QuantityInfo quantityInfo = new QuantityInfo(1);
			String requestBody = objectMapper.writeValueAsString(quantityInfo);
			cartRepository.deleteAll();

			mockMvc.perform(post(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isOk());

			List<CartBook> cartBooks = (List<CartBook>) cartBookRepository.findAll();
			assertThat(cartBooks).hasSize(1);
			assertThat(cartBooks.get(0).getQuantity()).isEqualTo(1);
		}

		@Test
		@Rollback
		public void testAddBookToCurrentCartMoreThan1CurrentCartsCase() throws Exception {
			String requestURI = "/additem/";

			createSecondCurrentCartForAuthenticatedUser();

			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();
			String requestURIGood = requestURI + bookId;
			QuantityInfo quantityInfo = new QuantityInfo(1);
			String requestBody = objectMapper.writeValueAsString(quantityInfo);

			mockMvc.perform(post(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isOk());

			List<CartBook> cartBooks = (List<CartBook>) cartBookRepository.findAll();
			assertThat(cartBooks).hasSize(1);
			assertThat(cartBooks.get(0).getQuantity()).isEqualTo(1);
		}
	}

	@Nested
	class testGetIdsOfBooksInCurrentCart {
		@Test
		@Rollback
		public void testGetIdsOfBooksInCurrentCartNoCurrentCartsCase() throws Exception {
			String requestURI = "/booksids";

			cartRepository.deleteAll();

			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(0));
		}

		@Test
		@Rollback
		public void testGetIdsOfBooksInCurrentCartMoreThan1CurrentCartsCase() throws Exception {
			String requestURI = "/booksids";

			createSecondCurrentCartForAuthenticatedUser();

			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(0));
		}

		@Test
		@Rollback
		public void testGetIdsOfBooksInCurrentCartGoodCases() throws Exception {
			String requestURI = "/booksids";

			// Empty cart case:
			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(0));

			addTwoBooksToAuthenticatedUserCurrentCart();

			// Two books in the cart case:
			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(2));
		}
	}

	@Nested
	class testGetCurrentCartByUserId {
		@Test
		@Rollback
		public void testGetCurrentCartByUserIdIdMissmatchCase() throws Exception {
			String requestURIWrongId = "/showcart/22";

			mockMvc.perform(get(requestURIWrongId).header("Authorization", jwt)).andExpect(status().isUnauthorized());
		}

		@Test
		@Rollback
		public void testGetCurrentCartByUserIdNotOneCurrentCartCase() throws Exception {
			String requestURI = "/showcart/";
			String requestURIGood = requestURI + authenticatedUserId;

			// No current cart case:
			cartRepository.deleteAll();

			mockMvc.perform(get(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(0));

			// More than one current Cart case:
			createCartWithUser(true, USERNAME, EMAIL);
			createSecondCurrentCartForAuthenticatedUser();
			mockMvc.perform(get(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(0));
		}

		@Test
		@Rollback
		public void testGetCurrentCartByUserIdGoodCases() throws Exception {
			String requestURI = "/showcart/";
			String requestURIGood = requestURI + authenticatedUserId;

			// No books in current cart case:
			mockMvc.perform(get(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(0));

			// Books are in the current Cart case:
			addTwoBooksToAuthenticatedUserCurrentCart();
			mockMvc.perform(get(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()").value(2));
		}
	}

	@Nested
	class testGetCurrentCartTotal {
		@Test
		@Rollback
		public void testGetCurrentCartTotalNotOneCurrentCartCase() throws Exception {
			String requestURI = "/getcurrenttotal";

			// No current cart case:
			cartRepository.deleteAll();

			MvcResult result = mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andReturn();
			assertThat(result.getResponse().getContentAsString()).isEqualTo("");

			// More than one current Cart case:
			createCartWithUser(true, USERNAME, EMAIL);
			createSecondCurrentCartForAuthenticatedUser();
			result = mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andReturn();
			assertThat(result.getResponse().getContentAsString()).isEqualTo("");
		}

		@Test
		@Rollback
		public void testGetCurrentCartTotalGoodCases() throws Exception {
			String requestURI = "/getcurrenttotal";

			// Empty current cart case:
			MvcResult result = mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andReturn();
			assertThat(result.getResponse().getContentAsString()).isEqualTo("");

			// Current Cart is not empty case:
			addTwoBooksToAuthenticatedUserCurrentCart();
			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.total").value(DEFAULT_PRICE * 4));

		}
	}

	@Nested
	class testClearCurrentCart {
		@Test
		@Rollback
		public void testClearCurrentcartidMissmatchCase() throws Exception {
			String requestURIIdMissmatch = "/clearcart/22";

			mockMvc.perform(delete(requestURIIdMissmatch).header("Authorization", jwt))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@Rollback
		public void testClearCurrentCartNotOneCurrentCartCase() throws Exception {
			String requestURI = "/clearcart/";
			String requestURIGood = requestURI + authenticatedUserId;

			// No current cart case:
			cartRepository.deleteAll();

			mockMvc.perform(delete(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk());

			// More than one current Cart case:
			createCartWithUser(true, USERNAME, EMAIL);
			createSecondCurrentCartForAuthenticatedUser();
			mockMvc.perform(delete(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk());
		}

		@Test
		@Rollback
		public void testClearCurrentCartGoodCases() throws Exception {
			String requestURI = "/clearcart/";
			String requestURIGood = requestURI + authenticatedUserId;

			// Empty current cart case:
			mockMvc.perform(delete(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(content().string("0 records were deleted from current cart"));

			// Current Cart is not empty case:
			addTwoBooksToAuthenticatedUserCurrentCart();
			mockMvc.perform(delete(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(content().string("2 records were deleted from current cart"));
		}
	}

	@Nested
	class testReduceBookAuthenticated {
		@Test
		@Rollback
		public void testReduceBookAuthenticatedBookNotFoundCase() throws Exception {
			String requestURIBookNotFound = "/reduceitem/24";

			mockMvc.perform(put(requestURIBookNotFound).header("Authorization", jwt)).andExpect(status().isNotFound());
		}

		@Test
		@Rollback
		public void testReduceBookAuthenticatedBookIsNotInCartCase() throws Exception {
			String requestURI = "/reduceitem/";

			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();

			String requestURIBookNotInCart = requestURI + bookId;

			mockMvc.perform(put(requestURIBookNotInCart).header("Authorization", jwt))
					.andExpect(status().isConflict());
		}

		@Test
		@Rollback
		public void testReduceBookAuthenticatedNotOneCurrentCartCase() throws Exception {
			String requestURI = "/reduceitem/";
			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();
			String requestURIBookNotInCart = requestURI + bookId;

			// No current cart case:
			cartRepository.deleteAll();

			mockMvc.perform(put(requestURIBookNotInCart).header("Authorization", jwt))
					.andExpect(status().isConflict());

			// More than one current Cart case:
			createCartWithUser(true, USERNAME, EMAIL);
			createSecondCurrentCartForAuthenticatedUser();
			mockMvc.perform(put(requestURIBookNotInCart).header("Authorization", jwt))
					.andExpect(status().isConflict());
		}

		@Test
		@Rollback
		public void testReduceBookAuthenticatedGoodCases() throws Exception {
			String requestURI = "/reduceitem/";

			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();

			createCartBookCustomQuantity(2, book, authenticatedUserCurrentCart);

			String requestURIGood = requestURI + bookId;

			// Book quantity is reduced by one case:
			mockMvc.perform(put(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(content().string("The quantity of the book in the cart was reduced by one"));

			List<CartBook> cartBooks = (List<CartBook>) cartBookRepository.findAll();
			assertThat(cartBooks).hasSize(1);
			assertThat(cartBooks.get(0).getQuantity()).isEqualTo(1);

			// Book is reduced from the cart case:
			mockMvc.perform(put(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(content().string("The book was deleted from the cart"));

			cartBooks = (List<CartBook>) cartBookRepository.findAll();
			assertThat(cartBooks).hasSize(0);
		}
	}

	@Nested
	class testDeleteBookFromCurrentCart {
		@Test
		@Rollback
		public void testDeleteBookFromCurrentCartBookNotFoundCase() throws Exception {
			String requestURIBookNotFound = "/deleteitem/24";

			mockMvc.perform(delete(requestURIBookNotFound).header("Authorization", jwt))
					.andExpect(status().isNotFound());
		}

		@Test
		@Rollback
		public void testDeleteBookFromCurrentCartBookIsNotInCartCase() throws Exception {
			String requestURI = "/deleteitem/";

			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();

			String requestURIBookNotInCart = requestURI + bookId;

			mockMvc.perform(delete(requestURIBookNotInCart).header("Authorization", jwt))
					.andExpect(status().isConflict());
		}

		@Test
		@Rollback
		// In this case the book is not in cart
		public void testDeleteBookFromCurrentCartNotOneCurrentCartCase() throws Exception {
			String requestURI = "/deleteitem/";

			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();

			String requestURIGood = requestURI + bookId;

			// No current cart case:
			cartRepository.deleteAll();

			mockMvc.perform(delete(requestURIGood).header("Authorization", jwt)).andExpect(status().isConflict());

			// More than one current Cart case:
			createCartWithUser(true, USERNAME, EMAIL);
			createSecondCurrentCartForAuthenticatedUser();
			mockMvc.perform(delete(requestURIGood).header("Authorization", jwt)).andExpect(status().isConflict());
		}

		@Test
		@Rollback
		public void testDeleteBookFromCurrentCartGoodCase() throws Exception {
			String requestURI = "/deleteitem/";

			Book book = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Long bookId = book.getId();

			createCartBookCustomQuantity(2, book, authenticatedUserCurrentCart);

			String requestURIGood = requestURI + bookId;

			mockMvc.perform(delete(requestURIGood).header("Authorization", jwt)).andExpect(status().isOk());

			List<CartBook> cartBooks = (List<CartBook>) cartBookRepository.findAll();
			assertThat(cartBooks).hasSize(0);
		}
	}

	@Nested
	class testGetCurrentCartQuantity {
		@Test
		@Rollback
		public void testGetCurrentCartQuantityEmptyCartGoodCase() throws Exception {
			String requestURI = "/currentcartquantity";

			MvcResult result = mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andReturn();
			assertThat(result.getResponse().getContentAsString()).isEqualTo("");
		}

		@Test
		@Rollback
		public void testGetCurrentCartQuantityNotOneCurrentCartCase() throws Exception {
			String requestURI = "/currentcartquantity";

			// No current cart case:
			cartRepository.deleteAll();

			MvcResult result = mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andReturn();
			assertThat(result.getResponse().getContentAsString()).isEqualTo("");

			// More than one current Cart case:
			createCartWithUser(true, USERNAME, EMAIL);
			createSecondCurrentCartForAuthenticatedUser();
			result = mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andReturn();
			assertThat(result.getResponse().getContentAsString()).isEqualTo("");
		}

		@Test
		@Rollback
		public void testGetCurrentCartQuantityNotEmptyCartGoodCase() throws Exception {
			String requestURI = "/currentcartquantity";

			Book book1 = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
			Book book2 = createBook(BOOK_TITLE + " 2", OTHER_CATEGORY, DEFAULT_PRICE);
			createCartBookCustomQuantity(3, book1, authenticatedUserCurrentCart);
			createCartBookCustomQuantity(2, book2, authenticatedUserCurrentCart);

			mockMvc.perform(get(requestURI).header("Authorization", jwt)).andExpect(status().isOk())
					.andExpect(jsonPath("$.items").value(5));
		}
	}

	@Nested
	class testMakeSaleByUserId {
		@Test
		@Rollback
		public void testMakeSaleByUserIdIdMissmatchCase() throws Exception {
			String requestURIIdMissmatchCase = "/makesale/22";

			AddressInfo addressInfoDefaultEmail = createAddressInfo(EMAIL);
			String requestBody = objectMapper.writeValueAsString(addressInfoDefaultEmail);

			mockMvc.perform(post(requestURIIdMissmatchCase).header("Authorization", jwt)
					.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isUnauthorized());
		}

		@Test
		@Rollback
		public void testMakeSaleByUserIdNotOneCurrentCartCase() throws Exception {
			String requestURI = "/makesale/";

			String requestURIGood = requestURI + authenticatedUserId;
			AddressInfo addressInfoDefaultEmail = createAddressInfo(EMAIL);
			String requestBody = objectMapper.writeValueAsString(addressInfoDefaultEmail);

			// No current cart case:
			cartRepository.deleteAll();

			mockMvc.perform(post(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isNotAcceptable());

			// More than one current Cart case:
			createCartWithUser(true, USERNAME, EMAIL);
			createSecondCurrentCartForAuthenticatedUser();

			mockMvc.perform(post(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isNotAcceptable());
		}

		@Test
		@Rollback
		public void testMakeSaleByUserIdCartIsEmptyCase() throws Exception {
			String requestURI = "/makesale/";

			String requestURIGood = requestURI + authenticatedUserId;
			AddressInfo addressInfoDefaultEmail = createAddressInfo(EMAIL);
			String requestBody = objectMapper.writeValueAsString(addressInfoDefaultEmail);

			mockMvc.perform(post(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isNotAcceptable());
		}

		@Test
		@Rollback
		public void testMakeSaleByUserIdGoodCase() throws Exception {
			String requestURI = "/makesale/";

			addTwoBooksToAuthenticatedUserCurrentCart();

			String requestURIGood = requestURI + authenticatedUserId;
			AddressInfo addressInfoDefaultEmail = createAddressInfo(EMAIL);
			String requestBody = objectMapper.writeValueAsString(addressInfoDefaultEmail);

			mockMvc.perform(post(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isOk()).andExpect(jsonPath("$.orderid").exists())
					.andExpect(jsonPath("$.password").exists());

			List<Long> idsOfNotCurrentCartsOfUser = cartRepository.findNotCurrentByUserid(authenticatedUserId);
			assertThat(idsOfNotCurrentCartsOfUser).hasSize(1);
			List<Cart> carts = (List<Cart>) cartRepository.findAll();
			assertThat(carts).hasSize(2);
		}
	}

	@Nested
	class testUpdateUser {
		@Test
		@Rollback
		public void testUpdateUserIdInPathMissmatchCase() throws Exception {
			String requestURIIdInPathMissmatch = "/updateuser/24";

			String requestBody = objectMapper.writeValueAsString(authenticatedUser);

			mockMvc.perform(put(requestURIIdInPathMissmatch).header("Authorization", jwt)
					.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isUnauthorized());
		}

		@Test
		@Rollback
		public void testUpdateUserIdInBodyMissmatchCase() throws Exception {
			String requestURI = "/updateuser/";
			String requestURIGood = requestURI + authenticatedUserId;

			User user = createUser(USERNAME + "2", "2" + EMAIL);
			String requestBodyWrongUser = objectMapper.writeValueAsString(user);

			mockMvc.perform(put(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBodyWrongUser)).andExpect(status().isUnauthorized());
		}

		@Test
		@Rollback
		public void testUpdateUserGoodCase() throws Exception {
			String requestURI = "/updateuser/";
			String requestURIGood = requestURI + authenticatedUserId;

			String updatedFirstname = "Paulo";
			String updatedLastname = "Polo";
			String updatedCountry = "Italy";
			String updatedCity = "Milan";
			String updatedStreet = "Centrale 3A";
			String updatedPostcode = "11000";

			authenticatedUser.setFirstname(updatedFirstname);
			authenticatedUser.setLastname(updatedLastname);
			authenticatedUser.setCountry(updatedCountry);
			authenticatedUser.setCity(updatedCity);
			authenticatedUser.setStreet(updatedStreet);
			authenticatedUser.setPostcode(updatedPostcode);

			String requestBody = objectMapper.writeValueAsString(authenticatedUser);

			mockMvc.perform(put(requestURIGood).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isOk());

			User updatedUser = urepository.findById(authenticatedUserId).get();
			assertThat(updatedUser.getFirstname()).isEqualTo(updatedFirstname);
			assertThat(updatedUser.getLastname()).isEqualTo(updatedLastname);
			assertThat(updatedUser.getCountry()).isEqualTo(updatedCountry);
			assertThat(updatedUser.getCity()).isEqualTo(updatedCity);
			assertThat(updatedUser.getStreet()).isEqualTo(updatedStreet);
			assertThat(updatedUser.getPostcode()).isEqualTo(updatedPostcode);
		}
	}

	@Nested
	class testChangePassword {
		@Test
		@Rollback
		public void testChangePasswordIdMissmatchCase() throws Exception {
			String requestURI = "/changepassword";

			PasswordInfo passwordInfoWrongId = new PasswordInfo(Long.valueOf(200), DEFAULT_PASSWORD, DEFAULT_PASSWORD);
			String requestBodyWrongId = objectMapper.writeValueAsString(passwordInfoWrongId);

			mockMvc.perform(put(requestURI).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBodyWrongId)).andExpect(status().isUnauthorized());
		}

		@Test
		@Rollback
		public void testChangePasswordOldPasswordIsIncorrectCase() throws Exception {
			String requestURI = "/changepassword";

			PasswordInfo passwordInfoWrongOldPassword = new PasswordInfo(authenticatedUserId, WRONG_PWD,
					DEFAULT_PASSWORD);
			String requestBodyWrongOldPassword = objectMapper.writeValueAsString(passwordInfoWrongOldPassword);

			mockMvc.perform(put(requestURI).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBodyWrongOldPassword)).andExpect(status().isBadRequest());
		}

		@Test
		@Rollback
		public void testChangePasswordGoodCase() throws Exception {
			String requestURI = "/changepassword";

			String newPassword = "newPwd";
			PasswordInfo passwordInfo = new PasswordInfo(authenticatedUserId, DEFAULT_PASSWORD, newPassword);
			String requestBody = objectMapper.writeValueAsString(passwordInfo);

			mockMvc.perform(put(requestURI).header("Authorization", jwt).contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)).andExpect(status().isOk());

			User updatedUser = urepository.findById(authenticatedUserId).get();
			BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
			assertThat(bc.matches(newPassword, updatedUser.getPassword())).isTrue();
		}
	}

	private AddressInfo createAddressInfo(String email) {
		AddressInfo newAddressInfo = new AddressInfo(FIRSTNAME, LASTNAME, COUNTRY, CITY, STREET, POSTCODE, email, NOTE);

		return newAddressInfo;
	}

	private Order createOrderOutOfCurrentCartOfAuthenticatedUser(int quantity, List<Book> books) {
		List<Cart> cartsCurrentOfUser = cartRepository.findCurrentByUserid(authenticatedUserId);

		Cart cart = cartsCurrentOfUser.get(0);

		for (Book book : books) {
			this.createCartBookCustomQuantity(quantity, book, cart);
		}

		User user = urepository.findById(authenticatedUserId).get();

		Order newOrder = new Order(user.getFirstname(), user.getLastname(), user.getCountry(), user.getCity(),
				user.getStreet(), user.getPostcode(), user.getEmail(), cart, NOTE, DEFAULT_PASSWORD);
		orepository.save(newOrder);

		return newOrder;
	}

	private Cart createCartWithUser(boolean current, String username, String email) {
		User user = this.createUser(username, email);

		List<Cart> currentCarts = cartRepository.findCurrentByUserid(user.getId());
		if (currentCarts.size() != 0 && current)
			return currentCarts.get(0);

		Cart newCart = new Cart(current, user);
		cartRepository.save(newCart);

		return newCart;
	}

	private User createUser(String username, String email) {
		Optional<User> optionalUser = urepository.findByUsername(username);

		if (optionalUser.isPresent())
			return optionalUser.get();

		optionalUser = urepository.findByEmail(email);

		if (optionalUser.isPresent())
			return optionalUser.get();

		String hashPwd = this.encodePassword(DEFAULT_PASSWORD);
		User user = new User(FIRSTNAME, LASTNAME, username, hashPwd, "USER", email, false);
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

	private String encodePassword(String password) {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(password);

		return hashPwd;
	}

	private void resetRepos() {
		crepository.deleteAll();
		urepository.deleteAll();
		cartRepository.deleteAll();
		bookRepository.deleteAll();
		cartBookRepository.deleteAll();
		orepository.deleteAll();
	}

	private void getToken() throws Exception {
		String requestURI = "/login";

		// Adding verified user to the database
		User user = this.createUser(USERNAME, EMAIL);
		user.setAccountVerified(true);
		user.setVerificationCode(null);
		urepository.save(user);
		Cart currentCart = this.createCartWithUser(true, USERNAME, EMAIL);

		AccountCredentials creds = new AccountCredentials(USERNAME, DEFAULT_PASSWORD);
		String requestBody = objectMapper.writeValueAsString(creds);
		MvcResult result = mockMvc
				.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();

		jwt = result.getResponse().getHeader("Authorization");
		authenticatedUserId = Long.valueOf(result.getResponse().getHeader("Host"));
		authenticatedUser = user;
		authenticatedUserCurrentCart = currentCart;
	}

	private void createSecondCurrentCartForAuthenticatedUser() {
		Cart newCurrentCart = new Cart(true, authenticatedUser);
		cartRepository.save(newCurrentCart);
	}

	private void addTwoBooksToAuthenticatedUserCurrentCart() {
		Book book1 = createBook(BOOK_TITLE, OTHER_CATEGORY, DEFAULT_PRICE);
		Book book2 = createBook(BOOK_TITLE + " 2", ROMANCE_CATEGORY, DEFAULT_PRICE);
		createCartBookCustomQuantity(2, book1, authenticatedUserCurrentCart);
		createCartBookCustomQuantity(2, book2, authenticatedUserCurrentCart);
	}
}

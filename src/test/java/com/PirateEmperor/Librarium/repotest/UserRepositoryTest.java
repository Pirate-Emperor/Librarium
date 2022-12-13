package com.PirateEmperor.Librarium.repotest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.PirateEmperor.Librarium.model.User;
import com.PirateEmperor.Librarium.model.UserRepository;

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class UserRepositoryTest {
	@Autowired
	private UserRepository urepository;

	@BeforeAll
	public void resetUserRepo() {
		urepository.deleteAll();
	}

	// CRUD tests for the user repository
	// Create functionality
	@Test
	@Rollback
	public void testCreateVerifiedUser() {
		User newUser1 = this.createVerifiedUser("user1", "user1@mail.com");
		assertThat(newUser1.getId()).isNotNull();

		this.createVerifiedUser("user2", "user2@mail.com");
		List<User> users = (List<User>) urepository.findAll();
		assertThat(users).hasSize(2);
	}

	@Test
	@Rollback
	public void testCreateUnverifiedUser() {
		String verificationCode = RandomStringUtils.random(64);

		User newUser1 = this.createUnverifiedUser("user1", "user1@mail.com", verificationCode);
		assertThat(newUser1.getId()).isNotNull();

		this.createUnverifiedUser("user2", "user2@mail.com", verificationCode);
		List<User> users = (List<User>) urepository.findAll();
		assertThat(users).hasSize(2);
	}

	// Read functionalities tests
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<User> users = (List<User>) urepository.findAll();
		assertThat(users).isEmpty();

		Optional<User> optionalUser = urepository.findById(Long.valueOf(2));
		assertThat(optionalUser).isNotPresent();

		User newUser1 = this.createVerifiedUser("user1", "user1@mail.com");
		Long newUser1Id = newUser1.getId();
		this.createVerifiedUser("user2", "user2@mail.com");

		users = (List<User>) urepository.findAll();
		assertThat(users).hasSize(2);

		optionalUser = urepository.findById(newUser1Id);
		assertThat(optionalUser).isPresent();
	}

	@Test
	@Rollback
	public void testFindByUsername() {
		String username = "user";
		String email = "mail@mail.com";
		Optional<User> optionalUser = urepository.findByUsername(username);
		assertThat(optionalUser).isNotPresent();

		this.createVerifiedUser(username, email);
		optionalUser = urepository.findByUsername(username);
		assertThat(optionalUser).isPresent();
	}

	@Test
	@Rollback
	public void testFindByEmail() {
		String username = "user";
		String email = "mail@mail.com";
		Optional<User> optionalUser = urepository.findByEmail(email);
		assertThat(optionalUser).isNotPresent();

		this.createVerifiedUser(username, email);
		optionalUser = urepository.findByEmail(email);
		assertThat(optionalUser).isPresent();
	}

	@Test
	@Rollback
	public void testByVerificationCode() {
		String username = "user";
		String email = "mail@mail.com";
		String verificationCode = RandomStringUtils.random(64);
		Optional<User> optionalUser = urepository.findByVerificationCode(verificationCode);
		assertThat(optionalUser).isNotPresent();

		this.createUnverifiedUser(username, email, verificationCode);
		optionalUser = urepository.findByVerificationCode(verificationCode);
		assertThat(optionalUser).isPresent();

		User user = optionalUser.get();
		assertThat(user.getUsername()).isEqualTo(username);
	}

	@Test
	@Rollback
	public void testUpdateUser() {
		User user = this.createVerifiedUser("userDefautl", "default@mail.com");

		String updatedUsername = "user";
		String updatedEmail = "mail@mail.com";
		String updatedFirstname = "Alex";
		String updatedLastname = "Birch";
		String updatedCountry = "Finland";
		String updatedCity = "Helsinki";
		String updatedStreet = "Juustenintie 3J";
		String updatedPostcode = "00410";

		user.setEmail(updatedEmail);
		user.setUsername(updatedUsername);
		user.setCity(updatedCity);
		user.setCountry(updatedCountry);
		user.setFirstname(updatedFirstname);
		user.setLastname(updatedLastname);
		user.setStreet(updatedStreet);
		user.setPostcode(updatedPostcode);

		urepository.save(user);
		Optional<User> optionalUser = urepository.findByUsername(updatedUsername);
		assertThat(optionalUser).isPresent();

		User updatedUser = optionalUser.get();
		assertThat(updatedUser.getEmail()).isEqualTo(updatedEmail);
		assertThat(updatedUser.getUsername()).isEqualTo(updatedUsername);
		assertThat(updatedUser.getCountry()).isEqualTo(updatedCountry);
		assertThat(updatedUser.getCity()).isEqualTo(updatedCity);
		assertThat(updatedUser.getStreet()).isEqualTo(updatedStreet);
		assertThat(updatedUser.getPostcode()).isEqualTo(updatedPostcode);
		assertThat(updatedUser.getFirstname()).isEqualTo(updatedFirstname);
		assertThat(updatedUser.getLastname()).isEqualTo(updatedLastname);
	}

	@Test
	@Rollback
	public void testVerifyUser() {
		String username = "user";
		String email = "mail@mail.com";
		String verificationCode = RandomStringUtils.random(64);
		this.createUnverifiedUser(username, email, verificationCode);

		this.verifyUser(verificationCode);

		Optional<User> optionalUser = urepository.findByVerificationCode(verificationCode);
		assertThat(optionalUser).isNotPresent();

		optionalUser = urepository.findByUsername(username);
		User user = optionalUser.get();
		assertThat(user.isAccountVerified()).isTrue();
	}

	// Testing delete functionalities:
	@Test
	@Rollback
	public void testDeleteUser() {
		User user1 = this.createVerifiedUser("user1", "user1@mail.com");
		Long user1Id = user1.getId();

		urepository.deleteById(user1Id);
		List<User> users = (List<User>) urepository.findAll();
		assertThat(users).hasSize(0);

		this.createVerifiedUser("user1", "user1@mail.com");
		this.createVerifiedUser("user2", "user2@mail.com");

		urepository.deleteAll();
		users = (List<User>) urepository.findAll();
		assertThat(users).hasSize(0);
	}

	private User createVerifiedUser(String username, String email) {
		User newUser = new User("Test", "test", username, "Some_Pwd_Hash", "USER", email, true);
		urepository.save(newUser);

		return newUser;
	}

	private User createUnverifiedUser(String username, String email, String verificationCode) {
		User newUser = new User("Test", "test", username, "Some_Pwd_Hash", "USER", email, verificationCode, false);
		urepository.save(newUser);

		return newUser;
	}

	private void verifyUser(String code) {
		Optional<User> optionalUser = urepository.findByVerificationCode(code);
		assertThat(optionalUser).isPresent();

		User user = optionalUser.get();
		user.setAccountVerified(true);
		user.setVerificationCode(null);
		urepository.save(user);
	}
}

package com.PirateEmperor.Librarium.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.PirateEmperor.Librarium.httpHandlers.AccountCredentials;
import com.PirateEmperor.Librarium.httpHandlers.EmailInfo;
import com.PirateEmperor.Librarium.httpHandlers.PasswordInfo;
import com.PirateEmperor.Librarium.httpHandlers.RoleVerificationInfo;
import com.PirateEmperor.Librarium.httpHandlers.SignupCredentials;
import com.PirateEmperor.Librarium.httpHandlers.TokenInfo;
import com.PirateEmperor.Librarium.model.User;
import com.PirateEmperor.Librarium.model.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CommonService commonService;

	@Autowired
	private AuthenticationService jwtService;

	@Autowired
	private MailService mailService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Value("${spring.mail.username}")
	private String springMailUsername;

	// Login method
	public ResponseEntity<?> getToken(AccountCredentials credentials) {
		String emailOrUsername = credentials.getUsername();
		User user = this.findUserByEmailOrUsername(emailOrUsername);
		this.handleAccountUnverifiedCase(user);

		String username = user.getUsername();
		String password = credentials.getPassword();
		User authenticatedUser = this.authenticateUser(username, password);

		return returnAuthenticationInfo(authenticatedUser);
	}

	private User findUserByEmailOrUsername(String emailOrUsername) {
		Optional<User> optionalUser = userRepository.findByEmail(emailOrUsername);

		if (!optionalUser.isPresent()) {
			optionalUser = userRepository.findByUsername(emailOrUsername);
			if (!optionalUser.isPresent())
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wrong username/email");
		}

		User user = optionalUser.get();
		return user;
	}

	private User authenticateUser(String username, String password) {
		String authenticatedUsername = this.getAuthenticatedUsername(username, password);

		User authenticatedUser = commonService.findUserByUsername(authenticatedUsername);
		return authenticatedUser;
	}

	private String getAuthenticatedUsername(String username, String password) {
		UsernamePasswordAuthenticationToken authenticationCredentials = new UsernamePasswordAuthenticationToken(
				username, password);
		Authentication authenticationInstance = authenticationManager.authenticate(authenticationCredentials);

		String authenticatedUsername = authenticationInstance.getName();
		return authenticatedUsername;
	}

	private ResponseEntity<?> returnAuthenticationInfo(User authenticatedUser) {
		String authenticatedUsername = authenticatedUser.getUsername();

		String jwts = jwtService.getToken(authenticatedUsername);
		String role = authenticatedUser.getRole();
		String id = authenticatedUser.getId().toString();

		return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + jwts).header(HttpHeaders.ALLOW, role)
				.header(HttpHeaders.HOST, id)
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Allow", "Host").build();
	}

	// Method to get the list of all users:
	public List<User> getUsers() {
		List<User> users = (List<User>) userRepository.findAll();
		return users;
	}

	// Method to get signle user info:
	public User getUserById(Long userId, Authentication authentication) {
		User user = commonService.checkAuthenticationAndAuthorize(authentication, userId);
		return user;
	}

	// Signup method
	public ResponseEntity<?> signUp(SignupCredentials credentials)
			throws MessagingException, UnsupportedEncodingException {
		String username = credentials.getUsername();
		String email = credentials.getEmail();
		this.checkUsernameOrEmailInUse(username, email);

		User newUser = this.createUnverifiedUserBySignupCredentials(credentials);

		try {
			mailService.sendVerificationEmail(newUser);
			return new ResponseEntity<>("We sent verification link to your email address :)", HttpStatus.OK);
		} catch (MailAuthenticationException e) {
			this.verifyUserAndCreateCurrentCart(newUser);
			return new ResponseEntity<>("Registration went well, you can login now", HttpStatus.ACCEPTED);
		}
	}

	private void checkUsernameOrEmailInUse(String username, String email) {
		this.checkEmailInUse(email);
		this.checkUsernameInUse(username);
	}

	private void checkEmailInUse(String email) {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Email is already in use");
	}

	private void checkUsernameInUse(String username) {
		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
	}

	private User createUnverifiedUserBySignupCredentials(SignupCredentials credentials) {
		String randomCode = RandomStringUtils.randomAlphanumeric(64);
		String rawPassword = credentials.getPassword();
		String hashPwd = commonService.encodePassword(rawPassword);

		User newUser = new User(credentials.getFirstname(), credentials.getLastname(), credentials.getUsername(),
				hashPwd, "USER", credentials.getEmail(), randomCode, false);
		userRepository.save(newUser);

		return newUser;
	}

	// Verification method
	public ResponseEntity<?> verifyUser(TokenInfo tokenInfo) {
		String token = tokenInfo.getToken();
		User user = this.findUserByVerificationCode(token);

		if (user.isAccountVerified())
			return new ResponseEntity<>("User is already verified", HttpStatus.CONFLICT);

		this.verifyUserAndCreateCurrentCart(user);
		return new ResponseEntity<>("Verification went well", HttpStatus.OK);
	}

	private User findUserByVerificationCode(String verificationCode) {
		Optional<User> optionalUser = userRepository.findByVerificationCode(verificationCode);

		if (!optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification code is incorrect");

		User user = optionalUser.get();

		return user;
	}

	private void verifyUserAndCreateCurrentCart(User user) {
		this.verifyUser(user);
		commonService.addCurrentCartForUser(user);
	}

	private void verifyUser(User user) {
		user.setAccountVerified(true);
		user.setVerificationCode(null);
		userRepository.save(user);
	}

	// Reset password by email method:
	public ResponseEntity<?> resetPassword(EmailInfo emailInfo)
			throws MessagingException, UnsupportedEncodingException {
		String email = emailInfo.getEmail();
		User user = this.findUserByEmail(email);
		this.handleAccountUnverifiedCase(user);

		String password = RandomStringUtils.randomAlphanumeric(15);

		try {
			mailService.sendPasswordEmail(user, password);
			this.setNewPassword(user, password);
			return new ResponseEntity<>("A temporary password was sent to your email address", HttpStatus.OK);
		} catch (MailAuthenticationException e) {
			return new ResponseEntity<>("The email service is not in use now", HttpStatus.NOT_IMPLEMENTED);
		}
	}

	private User findUserByEmail(String email) {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (!optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"User with this email (" + email + ") doesn't exist");

		User user = optionalUser.get();
		return user;
	}

	private void setNewPassword(User user, String password) {
		String hashedPassword = commonService.encodePassword(password);
		user.setPassword(hashedPassword);
		userRepository.save(user);
	}

	// Method to update the user's own info:
	public ResponseEntity<?> updateUser(Long userId, User user, Authentication authentication) {
		User userToUpdate = commonService.checkAuthenticationAndAuthorize(authentication, userId);

		commonService.checkAuthorization(user, userId);

		this.updateUser(userToUpdate, user);
		return new ResponseEntity<>("User info was updated successfully", HttpStatus.OK);
	}

	private void updateUser(User userToUpdate, User updatedUser) {
		userToUpdate.setLastname(updatedUser.getLastname());
		userToUpdate.setFirstname(updatedUser.getFirstname());
		userToUpdate.setCountry(updatedUser.getCountry());
		userToUpdate.setCity(updatedUser.getCity());
		userToUpdate.setStreet(updatedUser.getStreet());
		userToUpdate.setPostcode(updatedUser.getPostcode());
		userRepository.save(userToUpdate);
	}

	// The method to change user's own password:
	public ResponseEntity<?> changePassword(PasswordInfo passwordInfo, Authentication authentication) {
		Long userId = passwordInfo.getUserId();
		String oldPassword = passwordInfo.getOldPassword();
		String newPassword = passwordInfo.getNewPassword();

		User user = commonService.checkAuthenticationAndAuthorize(authentication, userId);
		commonService.checkPassword(oldPassword, user.getPassword());

		this.setNewPassword(user, newPassword);
		return new ResponseEntity<>("The password was changed", HttpStatus.OK);
	}

	// Method to change user's role and verification:
	public ResponseEntity<?> changeUserRoleAndVerification(Long userId, RoleVerificationInfo roleVerificationInfo,
			Authentication authentication) {
		User admin = commonService.checkAuthentication(authentication);
		Long adminId = admin.getId();
		User userToChange = this.findUserByUserId(userId);

		if (adminId == userId)
			return new ResponseEntity<>("You can't change your own role/verification", HttpStatus.NOT_ACCEPTABLE);

		String role = roleVerificationInfo.getRole();
		boolean isAccountVerified = roleVerificationInfo.isAccountVerified();
		userToChange.setRole(role);
		this.updateVerification(isAccountVerified, userToChange);
		userRepository.save(userToChange);

		return new ResponseEntity<>("Role of the user was successfully changed", HttpStatus.OK);
	}

	private User findUserByUserId(Long userId) {
		Optional<User> optionalUser = userRepository.findById(userId);

		if (!optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User wasn't found by id");

		User user = optionalUser.get();
		return user;
	}

	private void updateVerification(boolean isAccountVerified, User user) {
		if (isAccountVerified != user.isAccountVerified() && !user.isAccountVerified()) {
			user.setAccountVerified(isAccountVerified);
			user.setVerificationCode(null);
		}
	}

	// Handling user is not verified case: if smtp service is working (then
	// springMailUsername shouldn't equal to 'default_value') then the exception is
	// thrown. Otherwise user is verified automatically
	private void handleAccountUnverifiedCase(User user) {
		if (!user.isAccountVerified()) {
			if (!this.springMailUsername.equals("default_value"))
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is not verified");

			this.verifyUserAndCreateCurrentCart(user);
		}
	}
}

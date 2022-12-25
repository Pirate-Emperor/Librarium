package com.PirateEmperor.Librarium.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Cart {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "cartid", nullable = false, updatable = false)
	private Long cartid;
	
	// The value indicates whether the cart is current or closed
	@Column(name = "current", nullable = false)
	private boolean current;
	
	@Column(name = "expiry_date")
	private String expiryDate;
	
	@Column(name = "password")
	private String passwordHash;
	
	@ManyToOne
	@JoinColumn(name="userid")
	private User user;
	
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "cart")
	private List<CartBook> cartbooks;
	
	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "cart")
	private Order order;
	
	public Cart() {}
	
	public Cart(boolean current, User user) {
		this.current = current;
		this.user = user;
		this.expiryDate = null;
		this.passwordHash = null;
	}
	
	public Cart(boolean current) {
		this.current = current;
		this.user = null;
		this.expiryDate = LocalDate.now().plusDays(1).toString();
		
		String password = "test";
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(password);
		this.passwordHash = hashPwd;
	}
	
	public Cart(String passwordHash) {
		this.current = true;
		this.user = null;
		this.expiryDate = LocalDate.now().plusDays(1).toString();
		
		this.passwordHash = passwordHash;
	}

	public Long getCartid() {
		return cartid;
	}

	public void setCartid(Long cartid) {
		this.cartid = cartid;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<CartBook> getCartbooks() {
		return cartbooks;
	}

	public void setCartbooks(List<CartBook> cartbooks) {
		this.cartbooks = cartbooks;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
}
package com.PirateEmperor.Librarium.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_book")
public class CartBook {

	@EmbeddedId
	private CartBookKey id;

	@Column(name = "quantity")
	private int quantity;

	@ManyToOne
	@MapsId("cartid")
	@JoinColumn(name = "cartid", nullable = false)
	private Cart cart;

	@ManyToOne
	@MapsId("bookid")
	@JoinColumn(name = "bookid", nullable = false)
	private Book book;

	public CartBook() {}

	public CartBook(int quantity, Cart cart, Book book) {
		super();
		CartBookKey cartBookKey = new CartBookKey(cart.getCartid(), book.getId());
		this.id = cartBookKey;
		this.quantity = quantity;
		this.cart = cart;
		this.book = book;
	}

	public CartBook(Cart cart, Book book) {
		super();
		CartBookKey cartBookKey = new CartBookKey(cart.getCartid(), book.getId());
		this.id = cartBookKey;
		this.quantity = 1;
		this.cart = cart;
		this.book = book;
	}

	public CartBookKey getId() {
		return id;
	}

	public void setId(CartBookKey id) {
		this.id = id;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Cart getCart() {
		return cart;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

}
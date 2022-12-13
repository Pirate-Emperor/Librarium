package com.PirateEmperor.Librarium.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CartBookKey implements Serializable {
	private static final long serialVersionUID = -6860012402807015054L;

	@Column(name = "cartid")
	Long cartid;
	
	@Column(name = "bookid")
	Long bookid;
	
	public CartBookKey() {}

	public CartBookKey(Long cartid, Long bookid) {
		super();
		this.cartid = cartid;
		this.bookid = bookid;
	}

	public Long getCartid() {
		return cartid;
	}

	public void setCartid(Long cartid) {
		this.cartid = cartid;
	}

	public Long getBookid() {
		return bookid;
	}

	public void setBookid(Long bookid) {
		this.bookid = bookid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cartid, bookid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CartBookKey other = (CartBookKey) obj;
		return Objects.equals(cartid, other.cartid) && Objects.equals(bookid, other.bookid);
	}
}
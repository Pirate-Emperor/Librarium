package com.PirateEmperor.Librarium.httpHandlers;

public class BookQuantityInfo {
	private int quantity;
	private Long bookid;
	private String password;
	
	public BookQuantityInfo() {}
	
	public BookQuantityInfo(int quantity, Long bookid, String password) {
		super();
		this.quantity = quantity;
		this.bookid = bookid;
		this.password = password;
	}

	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public Long getBookid() {
		return bookid;
	}
	public void setBookid(Long bookid) {
		this.bookid = bookid;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}

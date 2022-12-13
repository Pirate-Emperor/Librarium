package com.PirateEmperor.Librarium.httpHandlers;

public class BookUpdate {
	private String title, author, url, isbn;
	private int bookYear;
	private double price;
	private Long categoryId;

	public BookUpdate() {
	}

	public BookUpdate(String title, String author, String url, String isbn, int bookYear, double price,
			Long categoryId) {
		this.title = title;
		this.author = author;
		this.url = url;
		this.isbn = isbn;
		this.bookYear = bookYear;
		this.price = price;
		this.categoryId = categoryId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public int getBookYear() {
		return bookYear;
	}

	public void setBookYear(int bookYear) {
		this.bookYear = bookYear;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
}

package com.PirateEmperor.Librarium.model;

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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String title, author;
	private int bookYear;
	private double price;
	private String url;
	
	@Column(unique = true)
	private String isbn;

	@ManyToOne
	@JoinColumn(name = "categoryid")
	private Category category;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "book")
	List<CartBook> cartbooks;

	public Book() {}

	public Book(String title, String author, String isbn, int bookYear, double price, Category category, String url) {
		super();
		this.title = title;
		this.author = author;
		this.isbn = isbn;
		this.bookYear = bookYear;
		this.price = price;
		this.category = category;
		this.url = url;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<CartBook> getCartbooks() {
		return cartbooks;
	}

	public void setCarttbooks(List<CartBook> cartbooks) {
		this.cartbooks = cartbooks;
	}

}

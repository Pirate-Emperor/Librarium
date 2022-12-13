package com.PirateEmperor.Librarium.httpHandlers;

public class OrderInfo {
	private String status;
	private String firstname;
	private String lastname;
	private String country;
	private String city;
	private String street;
	private String postcode;
	private String email;

	public OrderInfo() {
	}

	public OrderInfo(String status, String firstname, String lastname, String country, String city, String street,
			String postcode, String email) {
		this.status = status;
		this.firstname = firstname;
		this.lastname = lastname;
		this.country = country;
		this.city = city;
		this.street = street;
		this.postcode = postcode;
		this.email = email;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}

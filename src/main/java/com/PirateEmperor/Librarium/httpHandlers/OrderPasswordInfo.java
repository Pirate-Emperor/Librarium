package com.PirateEmperor.Librarium.httpHandlers;

public class OrderPasswordInfo {
	private Long orderid;
	private String password;
	
	public OrderPasswordInfo() {}
	
	public OrderPasswordInfo(Long orderid, String password) {
		this.orderid = orderid;
		this.password = password;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}

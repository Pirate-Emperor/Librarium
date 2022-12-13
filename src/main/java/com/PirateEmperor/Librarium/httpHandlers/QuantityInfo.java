package com.PirateEmperor.Librarium.httpHandlers;

public class QuantityInfo {
	private int quantity;
	
	public QuantityInfo() {}
	
	public QuantityInfo(int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

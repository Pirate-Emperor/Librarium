package com.PirateEmperor.Librarium.httpHandlers;

public class RoleVerificationInfo {
	private String role;
	private boolean isAccountVerified;

	public RoleVerificationInfo() {
	}

	public RoleVerificationInfo(String role, boolean isAccountVerified) {
		this.role = role;
		this.isAccountVerified = isAccountVerified;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isAccountVerified() {
		return isAccountVerified;
	}

	public void setAccountVerified(boolean isAccountVerified) {
		this.isAccountVerified = isAccountVerified;
	}
}

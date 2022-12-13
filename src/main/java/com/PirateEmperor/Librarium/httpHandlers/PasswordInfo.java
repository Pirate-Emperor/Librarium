package com.PirateEmperor.Librarium.httpHandlers;

public class PasswordInfo {
	private Long userId;
	private String oldPassword;
	private String newPassword;
	
	public PasswordInfo() {}
	
	public PasswordInfo(Long userId, String oldPassword, String newPassword) {
		this.userId = userId;
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}

	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}

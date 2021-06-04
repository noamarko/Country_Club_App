package twins.boundaries;

import twins.additionalClasses.UserId;
import twins.data.UserRole;


public class UserBoundary {
	private UserId userId;
	private UserRole role;
	private String username;
	private String avatar;

	
	public UserBoundary() {
		this.userId = new UserId();
	}

	public UserBoundary(String space, String email, UserRole role, String username, String avatar) {
		this();
		this.userId.setSpace(space);
		this.userId.setEmail(email);
		this.role = role;
		this.username = username;
		this.avatar = avatar;
	}
	
	public UserId getUserId() {
		return userId;
	}

	public void setUserId(UserId userId) {
		this.userId = userId;
	}
	public void setUserId(String userSpace, String userEmail) {
		this.userId.setSpace(userSpace);
		this.userId.setEmail(userEmail);
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(String role) {
		if (role.equals("PLAYER"))
			this.role = UserRole.PLAYER;
		if (role.equals("MANAGER"))
			this.role = UserRole.MANAGER;
		if (role.equals("ADMIN"))
			this.role = UserRole.ADMIN;		
	}
	public void setRole(UserRole role) {
		this.role = role;		
	}

	public String getUsername() {
		return username;
	}

	public void setUserName(String username) {
		this.username = username;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
	@Override
	public String toString() {
		return "Member's Details:\n [userId : [" + userId.getSpace() + " , "+ userId.getEmail() + "], role : " + role + ", username : " + username
				+ ", avatar : " + avatar + "]";
	}

}

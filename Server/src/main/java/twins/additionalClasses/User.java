package twins.additionalClasses;

public class User {
	
	private UserId userId;
	public User() {
		this.userId = new UserId(); //
	}
	public User(String space, String email) {
		this();
		this.userId.setSpace(space);
		this.userId.setEmail(email);
	}
	
	public void setSpace(String space) {
		userId.setSpace(space);
	}
	
	public void setEmail(String email) {
		userId.setEmail(email);
	}
	public UserId getUserId() {
		return userId;
	}
	public void setUserId(UserId userId) {
		this.userId = userId;
	}

}

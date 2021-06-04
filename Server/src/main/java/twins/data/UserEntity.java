package twins.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.NamedQuery;

import twins.additionalClasses.UserId;

/*
USERS_TABLE

      
USERID <PK>  | ROLE          | USERNAME        | AVATAR         
===========================================================
VARCHAR(255) | VARCHAR(255)  | VARCHAR(255)    |  VARCHAR(255)     
*/

@Entity
@Table(name="USERS_TABLE")
//@NamedQuery(name = "UserEntity.getAllUsers",
//query = "select u from UserEntity u")
public class UserEntity {
	
	private String userId;
	private String role;
	private String username;
	private String avatar;
	
	
	public UserEntity() {
		
	}
		
	@Id
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	@Transient
	public void setUserID(UserId userId) {
		this.userId = userId.getSpace()+ "|" +userId.getEmail();
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getAvatar() {
		return avatar;
	}
	
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
}

package twins;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import twins.additionalClasses.NewUserDetails;
import twins.additionalClasses.UserId;
import twins.boundaries.UserBoundary;
import twins.logic.UserServiceImplementation;
import twins.data.UserEntity;
import twins.data.UserRole;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserTests {

	
	/*
	 * 
	 *  This class is for User URL HTML test 
	 * 
	 */
	
	private int port;
	private String url; // http://localhost:port/twins/users
	private RestTemplate restTemplate;
	
	@LocalServerPort // inject port number to test case
	public void setPort(int port) {
		this.port = port;
	}
	
	@PostConstruct
	public void init() {
		this.url = "http://localhost:" + this.port + "/twins/users";
		this.restTemplate = new RestTemplate();
	}
	
	@AfterEach
	public void tearDown() {
		/// clean database
		//TODO fix to the right url
		//this.restTemplate
		//	.delete(this.url); 
	}
	
	@Test
	public void testCreateUser() throws Exception{
		NewUserDetails userDetails = new NewUserDetails();
		userDetails.setEmail("gunman@gmail.com");
		userDetails.setRole(UserRole.MANAGER);
		userDetails.setUsername("cowboy77");
		userDetails.setAvatar("cb");
		UserBoundary response = this.restTemplate
				.postForObject(this.url, userDetails, UserBoundary.class);
		
		assertThat(response.getAvatar())
			.isEqualTo("cb");
	}
	
	@Test 
	public void testUpdateUserAndValidateTheDatabaseIsUpdated() {
		// Given the database contains a user
		NewUserDetails userDetails = new NewUserDetails();
		userDetails.setEmail("cowboy4life@gmail.com");
		userDetails.setRole(UserRole.PLAYER);
		userDetails.setUsername("cowboy11");
		userDetails.setAvatar("cb");
		
		UserBoundary response = this.restTemplate
			.postForObject(this.url, userDetails, UserBoundary.class);
		
		// When I invoke PUT /twins/users/{userSpace}/{userEmail} and {"avatar":"cbn"}
		UserBoundary update = new UserBoundary();
		update.setUserId(new UserId("2021b.Daniel.Aizenband","cowboy4life@gmail.com"));
		update.setRole("PLAYER");
		update.setUserName("cowboy11");
		update.setAvatar("cbn");
		
		this.restTemplate
			.put(this.url + "/{userSpace}/{userEmail}", update, response.getUserId().getSpace(), response.getUserId().getEmail());
		
		// Assert that the database user is updated
		assertThat(this.restTemplate
			.getForObject(this.url + "/login/{userSpace}/{userEmail}", UserBoundary.class, response.getUserId().getSpace(), response.getUserId().getEmail())
			.getAvatar())
		
			.isEqualTo(update.getAvatar())
			.isNotEqualTo(response.getAvatar());
	}
	
	@Test 
	public void testDeleteAllUsers() {
		// Given the database contains 2 users
		NewUserDetails userDetails = new NewUserDetails();
		userDetails.setEmail("cowboy4life@gmail.com");
		userDetails.setRole(UserRole.PLAYER);
		userDetails.setUsername("cowboy11");
		userDetails.setAvatar("cb");
		UserBoundary response = this.restTemplate
			.postForObject(this.url, userDetails, UserBoundary.class);
		
		userDetails.setEmail("indian4life@gmail.com");
		userDetails.setRole(UserRole.ADMIN);
		userDetails.setUsername("indian11");
		userDetails.setAvatar("indi");		
		response = this.restTemplate
			.postForObject(this.url, userDetails, UserBoundary.class);
		
		// Assert that the array contains users and isn't empty.		
		UserBoundary[] array = this.restTemplate
				.getForObject("http://localhost:" + this.port + "/twins/admin/users/{userSpace}/{userEmail}", UserBoundary[].class, response.getUserId().getSpace(), response.getUserId().getEmail());
		assertThat(array.length).isNotEqualTo(0);
		
		// Delete all users.
		this.restTemplate
			.delete("http://localhost:" + this.port + "/twins/admin/users/{userSpace}/{userEmail}", UserBoundary.class, response.getUserId().getSpace(), response.getUserId().getEmail());
		
		// Assert that the array is empty after delete.
		array = this.restTemplate
				.getForObject("http://localhost:" + this.port + "/twins/admin/users/{userSpace}/{userEmail}", UserBoundary[].class, response.getUserId().getSpace(), response.getUserId().getEmail());
		assertThat(array.length).isEqualTo(0);
		
	}
	@Test
	public void testExportUsers() {
		// Given the database contains 2 users
		NewUserDetails userDetails = new NewUserDetails();
		userDetails.setEmail("cowboy4life@gmail.com");
		userDetails.setRole(UserRole.PLAYER);
		userDetails.setUsername("cowboy11");
		userDetails.setAvatar("cb");
		UserBoundary response = this.restTemplate
			.postForObject(this.url, userDetails, UserBoundary.class);
		
		userDetails.setEmail("indian4life@gmail.com");
		userDetails.setRole(UserRole.ADMIN);
		userDetails.setUsername("indian11");
		userDetails.setAvatar("indi");		
		response = this.restTemplate
			.postForObject(this.url, userDetails, UserBoundary.class);

		// Assert that the array isn't empty.
		UserBoundary[] array = this.restTemplate
			.getForObject("http://localhost:" + this.port + "/twins/admin/users/{userSpace}/{userEmail}", UserBoundary[].class, response.getUserId().getSpace(), response.getUserId().getEmail());
		assertThat(array.length).isNotEqualTo(0);
		for (UserBoundary userBoundary : array) {
			System.err.println(userBoundary);
		}
	}
}

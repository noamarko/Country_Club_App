package twins;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import twins.additionalClasses.ItemId;
import twins.additionalClasses.Location;
import twins.additionalClasses.NewUserDetails;
import twins.additionalClasses.UserId;
import twins.boundaries.ItemBoundary;
import twins.boundaries.UserBoundary;
import twins.data.UserRole;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ItemTests {

	/*
	 * 
	 * This class test Item URL HTTP request
	 * 
	 */

	private int port;
	private String url; // http://localhost:port/messages
	private RestTemplate restTemplate;

	@LocalServerPort // inject port number to test case
	public void setPort(int port) {
		this.port = port;
	}

	@PostConstruct
	public void init() {
		this.url = "http://localhost:" + this.port + "/twins";
		this.restTemplate = new RestTemplate();
	}

	@AfterEach
	public void tearDown() {
		/// clean database
//		this.restTemplate.delete(this.url);
//			.delete(this.url + "/{space}/{email}", "myspace", "admin@afeka.ac.il");
	}

	@Test
	public void testCreateItem() {

		// Setting new user with Manager role in the DB
		NewUserDetails newUserDetails = new NewUserDetails("ben@gmail.com", UserRole.MANAGER, "ben", "wolf");
		UserBoundary userResponse = this.restTemplate.postForObject(url + "/users", newUserDetails, UserBoundary.class);
		assertThat(userResponse.getRole().toString()).isEqualTo("MANAGER");

		// Setting up and new item and saving it to the DB
		ItemBoundary item = new ItemBoundary("towel", "ben", true,
				new UserId("2021b.Daniel.Aizenband", "ben@gmail.com"), new Location(1.0, 1.0));
		ItemBoundary reponse = this.restTemplate.postForObject(this.url + "/items/2021b.Daniel.Aizenband/ben@gmail.com",
				item, ItemBoundary.class);

		// Asserting that what we saved matches what set up
		assertThat(reponse.getName()).isEqualTo("ben");
	}

	@Test
	public void testUpdateItem() {
		// Setting new user with Manager role in the DB
		NewUserDetails newUserDetails = new NewUserDetails("ben@gmail.com", UserRole.MANAGER, "ben", "wolf");
		UserBoundary userResponse = this.restTemplate.postForObject(url + "/users", newUserDetails, UserBoundary.class);
		assertThat(userResponse.getRole().toString()).isEqualTo("MANAGER");

		// creating Item boundary
		ItemBoundary itemToBeReplaced = new ItemBoundary("towel", "ben", true,
				new UserId("2021b.Daniel.Aizenband", "ben@gmail.com"), new Location(1.0, 1.0));

		// this post method - ensure's the creator user is a manager
		ItemBoundary itemReponse = this.restTemplate.postForObject(
				this.url + "/items/2021b.Daniel.Aizenband/ben@gmail.com", itemToBeReplaced, ItemBoundary.class);

		ItemBoundary itemToBePlaced = new ItemBoundary("pool", "moshe", true,
				new UserId("2021b.Daniel.Aizenband", "ben123123@gmail.com"), new Location(2.0, 2.0));

		// update item
		/// twins/items/{userSpace}/{userEmail}/{itemSpace}/{itemID}
		this.restTemplate.put(
				this.url + "/items/" + userResponse.getUserId().getSpace() + "/" + userResponse.getUserId().getEmail()
						+ "/" + itemReponse.getItemID().getSpace() + "/" + itemReponse.getItemID().getID(),
				itemToBePlaced);

		// get updated Item from DB
		/// twins/items/{userSpace}/{userEmail}/{itemSpace}/{itemID}
		ItemBoundary finalUpdatedItem = this.restTemplate.getForObject(
				this.url + "/items/" + userResponse.getUserId().getSpace() + "/" + userResponse.getUserId().getEmail()
						+ "/" + itemReponse.getItemID().getSpace() + "/" + itemReponse.getItemID().getID(),
				ItemBoundary.class);

		/*
		 * Asserting that the item type was changed from "towel" to "pool" and item name
		 * change from "ben" to "moshe"
		 */
		assertThat(finalUpdatedItem.getType()).isEqualTo("pool");
		assertThat(finalUpdatedItem.getName()).isEqualTo("moshe");
	}

	@Test
	public void testRetrieveItem() {
		// this was tested on the test "testUpdateItem()"
	}

	@Test
	public void testRetrieveAllItem() {

		/*
		 * This is a paging test we make 7 items - page size is 5 page 0 should have 5
		 * items page 1 should have 2 items
		 */
		
		// creating user with Role = MANAGER
		NewUserDetails newUserDetails = new NewUserDetails("ben@gmail.com", UserRole.MANAGER, "ben", "wolf");
		UserBoundary userResponse = this.restTemplate.postForObject(url + "/users", newUserDetails, UserBoundary.class);
		assertThat(userResponse.getRole().toString()).isEqualTo("MANAGER");

		// creating 7 item with the MANAGER user
		createItem("towel1", "ben");
		createItem("swimming glasses", "dodo");
		createItem("keys", "popo");
		createItem("golf cart", "momo");
		createItem("towel3", "bobo");
		createItem("towel2", "gogo");
		createItem("towel4", "bebe");

		// Using the getAll rest GET method to bring pages of items
		ItemBoundary itemsPage0[] = this.restTemplate.getForObject(
				url + "/items/2021b.Daniel.Aizenband/ben123123@gmail.com?size=5&page=0", ItemBoundary[].class);
		ItemBoundary itemsPage1[] = this.restTemplate.getForObject(
				url + "/items/2021b.Daniel.Aizenband/ben123123@gmail.com?size=5&page=1", ItemBoundary[].class);

		assertThat(itemsPage0.length).isEqualTo(5);
		assertThat(itemsPage1.length).isEqualTo(2);

	}

	@Test
	public void testAdminDeleteItems() {
		
		// creating user with Role = ADMIN
		NewUserDetails newAdminUserDetails = new NewUserDetails("admin@gmail.com", UserRole.ADMIN, "admin", "wolf");
		UserBoundary adminUserResponse = this.restTemplate.postForObject(url + "/users", newAdminUserDetails,
				UserBoundary.class);
		assertThat(adminUserResponse.getRole().toString()).isEqualTo("ADMIN");

		// ADMIN deletes all items that may have left from other tests !!
		// "/twins/admin/items/{userSpace}/{userEmail}"
		this.restTemplate.delete(this.url + "/admin/items/2021b.Daniel.Aizenband/admin@gmail.com");
		
		// creating user with Role = MANAGER
		NewUserDetails newManagerUserDetails = new NewUserDetails("ben@gmail.com", UserRole.MANAGER, "ben", "wolf");
		UserBoundary userResponse = this.restTemplate.postForObject(url + "/users", newManagerUserDetails,
				UserBoundary.class);
		assertThat(userResponse.getRole().toString()).isEqualTo("MANAGER");

		// creating 7 item with the MANAGER user
		createItem("towel1", "ben");
		createItem("swimming glasses", "dodo");
		createItem("keys", "popo");
		createItem("golf cart", "momo");
		createItem("towel3", "bobo");
		createItem("towel2", "gogo");
		createItem("towel4", "bebe");

		// Using the getAll rest GET method to bring pages of items
		ItemBoundary itemsPage0[] = this.restTemplate
				.getForObject(url + "/items/2021b.Daniel.Aizenband/ben@gmail.com?size=5&page=0", ItemBoundary[].class);
		ItemBoundary itemsPage1[] = this.restTemplate
				.getForObject(url + "/items/2021b.Daniel.Aizenband/ben@gmail.com?size=5&page=1", ItemBoundary[].class);

		assertThat(itemsPage0.length).isEqualTo(5);
		assertThat(itemsPage1.length).isEqualTo(2);


		// ADMIN deletes all the new items that were made just now
		// "/twins/admin/items/{userSpace}/{userEmail}"
		this.restTemplate.delete(this.url + "/admin/items/2021b.Daniel.Aizenband/admin@gmail.com");

		// asserting that all items were deleted with user MANAGER
		// Using the getAll rest GET method to bring pages of items
		ItemBoundary itemsPageNum0[] = this.restTemplate
				.getForObject(url + "/items/2021b.Daniel.Aizenband/ben@gmail.com?size=5&page=0", ItemBoundary[].class);
		ItemBoundary itemsPageNum1[] = this.restTemplate
				.getForObject(url + "/items/2021b.Daniel.Aizenband/ben@gmail.com?size=5&page=1", ItemBoundary[].class);
		
		// Asserting that the item were indeed deleted
		assertThat(itemsPageNum0.length).isEqualTo(0);
		assertThat(itemsPageNum1.length).isEqualTo(0);
	}

	public void createItem(String type, String name) {
		// creating Item boundary
		ItemBoundary itemBoundary = new ItemBoundary(type, name, true,
				new UserId("2021b.Daniel.Aizenband", "ben@gmail.com"), new Location(1.0, 1.0));

		// this post method - ensure the creator user is a manager
		this.restTemplate.postForObject(this.url + "/items/2021b.Daniel.Aizenband/ben@gmail.com", itemBoundary,
				ItemBoundary.class);
	}

}

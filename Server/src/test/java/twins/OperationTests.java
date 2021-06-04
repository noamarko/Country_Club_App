package twins;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import twins.additionalClasses.Item;
import twins.additionalClasses.Location;
import twins.additionalClasses.NewUserDetails;
import twins.additionalClasses.OperationId;
import twins.additionalClasses.User;
import twins.additionalClasses.UserId;
import twins.boundaries.ItemBoundary;
import twins.boundaries.OperationBoundary;
import twins.boundaries.UserBoundary;
import twins.data.UserRole;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class OperationTests {

	
	/*
	 * 
	 *  This class is for Operation URL HTML test 
	 * 
	 */
	
	private int port;
	private String url; // http://localhost:port/twins/operations
	private String adminUrl;
	private RestTemplate restTemplate;
	
	@LocalServerPort // inject port number to test case
	public void setPort(int port) {
		this.port = port;
	}
	
	@PostConstruct
	public void init() {
		this.url = "http://localhost:" + this.port + "/twins/operations";
		this.adminUrl = "http://localhost:" + this.port +"/twins/admin/operations/{userSpace}/{userEmail}"; 
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
	public void TestInvokeOperatiton() throws Exception{
		// create user with PLAYER permission to invoke operation
		String urlCreateUser = "http://localhost:" + this.port + "/twins/users";
		NewUserDetails nud = new NewUserDetails("demo@gmail.com" , UserRole.PLAYER, "dani", "abc");
		UserBoundary userRes =this.restTemplate.postForObject(urlCreateUser, nud, UserBoundary.class);
		
		//create user with MANAGER permission to create new item
		NewUserDetails nud1 = new NewUserDetails("demo2@gmail.com" , UserRole.MANAGER, "daniel", "abcd");
		UserBoundary userRes1 =this.restTemplate.postForObject(urlCreateUser, nud1, UserBoundary.class);
		
		//create item using the user with MANAGER permissions
//		ItemBoundary ib = new ItemBoundary("space","124","Pool","blabla",true, new UserId(userRes1.getUserId().getSpace(),userRes1.getUserId().getEmail()), new Location(0, 0));
		ItemBoundary ib = new ItemBoundary("2021b.Daniel.Aizenband","daniel", true, new UserId(userRes1.getUserId().getSpace(),userRes1.getUserId().getEmail()), new Location(0, 0));
		String urlCreateItem = "http://localhost:" + this.port +"/twins/items/"+ib.getCreatedBy().getSpace()+"/"+ib.getCreatedBy().getEmail();
		ItemBoundary itemRes = this.restTemplate.postForObject(urlCreateItem, ib, ItemBoundary.class,userRes1.getUserId().getSpace(),userRes1.getUserId().getEmail());
		
		OperationBoundary ob = new OperationBoundary();
		ob.setType("check");
		ob.setItem(new Item(itemRes.getItemID().getSpace(), itemRes.getItemID().getID()));
		ob.setOperationId(new OperationId("Sector 12", "de938525-f579-4e33-ae40-1def64cb4bdb"));
		ob.setInvokedBy(new User(userRes.getUserId().getSpace(),userRes.getUserId().getEmail()));
		OperationBoundary response = this.restTemplate
				.postForObject(this.url, ob, OperationBoundary.class);
		assertThat(response.getType())
			.isEqualTo("check");
	}
	
	@Test
	public void TestInvokeAsynchorniousOperation() throws Exception{
				
		String urlCreateUser = "http://localhost:" + this.port + "/twins/users";
		//User1 a player who invoked the operations 
		NewUserDetails nud1 = new NewUserDetails("demo@gmail.com" , UserRole.PLAYER, "dani", "abc");
		UserBoundary player =this.restTemplate.postForObject(urlCreateUser, nud1, UserBoundary.class);
		
		//User2 an admin to check that the operations exists
		NewUserDetails nud2 = new NewUserDetails("demo2@gmail.com" , UserRole.ADMIN, "Noam", "bla");
		UserBoundary admin =this.restTemplate.postForObject(urlCreateUser, nud2, UserBoundary.class);
		
		//User3 a manager in order to create item
		NewUserDetails nud3 = new NewUserDetails("demo3@gmail.com" , UserRole.MANAGER, "Noama", "blab");
		UserBoundary manager =this.restTemplate.postForObject(urlCreateUser, nud3, UserBoundary.class);
		
		ItemBoundary ib = new ItemBoundary("2021b.Daniel.Aizenband","daniel", true, new UserId(manager.getUserId().getSpace(),manager.getUserId().getEmail()), new Location(0, 0));
		String urlCreateItem = "http://localhost:" + this.port +"/twins/items/"+ib.getCreatedBy().getSpace()+"/"+ib.getCreatedBy().getEmail();
		ItemBoundary itemRes = this.restTemplate.postForObject(urlCreateItem, ib, ItemBoundary.class);
		System.err.println("creation went ok\n");
		OperationBoundary ob = new OperationBoundary();
		ob.setType("check");
		ob.setItem(new Item(itemRes.getItemID().getSpace(), itemRes.getItemID().getID()));
		ob.setOperationId(new OperationId("Sector 12", "de938525-f579-4e33-ae40-1def64cb4bdb"));
		ob.setInvokedBy(new User(player.getUserId().getSpace(),player.getUserId().getEmail()));
		System.err.println("itemId ="+itemRes.getItemID().getSpace()+","+ itemRes.getItemID().getID());
		OperationBoundary immediateResponse = this.restTemplate
				.postForObject(this.url + "/async", ob, OperationBoundary.class);
		
		assertThat(immediateResponse.getType()).isEqualTo(ob.getType());
		
		String urlForExport = "http://localhost:" + this.port +"/twins/admin/operations/{userSpace}/{userEmail}";
		boolean flag = false;
		while(!flag) {
			try {
				System.err.println("before getforobject\n");
				OperationBoundary boundaryFromDB = this.restTemplate.
						getForObject(urlForExport,
									OperationBoundary.class,
									admin.getUserId().getSpace(),admin.getUserId().getEmail());
				System.err.println("boundaries after get\n");
				flag = true;
			} catch (Exception e) {
				try {
					Thread.sleep(2000);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				flag = false;
			}
		}
		System.err.println("*** DONE! ***\n");
	}
	
	@Test
	public void TestGetAllOperations() throws Exception{
		String urlCreateUser = "http://localhost:" + this.port + "/twins/users";
		NewUserDetails nud1 = new NewUserDetails("demo@gmail.com" , UserRole.PLAYER, "dani", "abc");
		UserBoundary player =this.restTemplate.postForObject(urlCreateUser, nud1, UserBoundary.class);
		
		//User2 an admin to check that the operations exists
		NewUserDetails nud2 = new NewUserDetails("demo2@gmail.com" , UserRole.ADMIN, "Noam", "bla");
		UserBoundary admin =this.restTemplate.postForObject(urlCreateUser, nud2, UserBoundary.class);
		
		//User3 a manager in order to create item
		NewUserDetails nud3 = new NewUserDetails("demo3@gmail.com" , UserRole.MANAGER, "Noama", "blab");
		UserBoundary manager =this.restTemplate.postForObject(urlCreateUser, nud3, UserBoundary.class);
		
		ItemBoundary ib = new ItemBoundary("2021b.Daniel.Aizenband","daniel", true, new UserId(manager.getUserId().getSpace(),manager.getUserId().getEmail()), new Location(0, 0));
		String urlCreateItem = "http://localhost:" + this.port +"/twins/items/"+ib.getCreatedBy().getSpace()+"/"+ib.getCreatedBy().getEmail();
		ItemBoundary itemRes = this.restTemplate.postForObject(urlCreateItem, ib, ItemBoundary.class);
		
		OperationBoundary ob1 = new OperationBoundary();
		ob1.setType("check");
		ob1.setItem(new Item(itemRes.getItemID().getSpace(), itemRes.getItemID().getID()));
		ob1.setOperationId(new OperationId("Sector 12", "de938525-f579-4e33-ae40-1def64cb4bdb"));
		ob1.setInvokedBy(new User(player.getUserId().getSpace(),player.getUserId().getEmail()));
		OperationBoundary ob1Res = this.restTemplate.postForObject(this.url, ob1,
				OperationBoundary.class);
		
		OperationBoundary ob2 = new OperationBoundary();
		ob2.setType("check");
		ob2.setItem(new Item(itemRes.getItemID().getSpace(), itemRes.getItemID().getID()));
		ob2.setOperationId(new OperationId("Sector 12", "de938525-f579-4e33-ae40-1def64cb4bdb"));
		ob2.setInvokedBy(new User(player.getUserId().getSpace(),player.getUserId().getEmail()));
		OperationBoundary ob2Res = this.restTemplate
				.postForObject(this.url, ob2,
								OperationBoundary.class);
		OperationBoundary[] array = this.restTemplate
				.getForObject(this.adminUrl, OperationBoundary[].class, 
						admin.getUserId().getSpace(),admin.getUserId().getEmail());
		assertThat(array.length).isNotEqualTo(0);
	}
	
	@Test
	public void TestDeleteAllOperations() throws Exception{
		String urlCreateUser = "http://localhost:" + this.port + "/twins/users";
		NewUserDetails nud1 = new NewUserDetails("demo@gmail.com" , UserRole.PLAYER, "dani", "abc");
		UserBoundary player =this.restTemplate.postForObject(urlCreateUser, nud1, UserBoundary.class);
		
		//User2 an admin to check that the operations exists
		NewUserDetails nud2 = new NewUserDetails("demo2@gmail.com" , UserRole.ADMIN, "Noam", "bla");
		UserBoundary admin =this.restTemplate.postForObject(urlCreateUser, nud2, UserBoundary.class);
		
		//User3 a manager in order to create item
		NewUserDetails nud3 = new NewUserDetails("demo3@gmail.com" , UserRole.MANAGER, "Noama", "blab");
		UserBoundary manager =this.restTemplate.postForObject(urlCreateUser, nud3, UserBoundary.class);
		
		ItemBoundary ib = new ItemBoundary("2021b.Daniel.Aizenband","daniel", true, new UserId(manager.getUserId().getSpace(),manager.getUserId().getEmail()), new Location(0, 0));
		String urlCreateItem = "http://localhost:" + this.port +"/twins/items/"+ib.getCreatedBy().getSpace()+"/"+ib.getCreatedBy().getEmail();
		ItemBoundary itemRes = this.restTemplate.postForObject(urlCreateItem, ib, ItemBoundary.class);
		
		
		OperationBoundary ob1 = new OperationBoundary();
		ob1.setType("check");
		ob1.setItem(new Item(itemRes.getItemID().getSpace(), itemRes.getItemID().getID()));
		ob1.setOperationId(new OperationId("Sector 12", "de938525-f579-4e33-ae40-1def64cb4bdb"));
		ob1.setInvokedBy(new User(player.getUserId().getSpace(),player.getUserId().getEmail()));
		OperationBoundary ob1Res = this.restTemplate.postForObject(this.url, ob1,
				OperationBoundary.class);
		
		OperationBoundary ob2 = new OperationBoundary();
		ob2.setType("check");
		ob2.setItem(new Item(itemRes.getItemID().getSpace(), itemRes.getItemID().getID()));
		ob2.setOperationId(new OperationId("Sector 12", "de938525-f579-4e33-ae40-1def64cb4bdb"));
		ob2.setInvokedBy(new User(player.getUserId().getSpace(),player.getUserId().getEmail()));
		OperationBoundary ob2Res = this.restTemplate
				.postForObject(this.url, ob2,
								OperationBoundary.class);
		
		OperationBoundary[] array = this.restTemplate
				.getForObject(this.adminUrl, OperationBoundary[].class, 
						admin.getUserId().getSpace(), admin.getUserId().getEmail());
		assertThat(array.length).isNotEqualTo(0);
		
		this.restTemplate.delete(this.adminUrl,
								admin.getUserId().getSpace(),admin.getUserId().getEmail());
		array = this.restTemplate
				.getForObject(this.adminUrl, OperationBoundary[].class, 
						admin.getUserId().getSpace(),admin.getUserId().getEmail());
		assertThat(array.length).isEqualTo(0);
	}
}

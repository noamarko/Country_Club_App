package twins.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import twins.additionalClasses.Location;
import twins.additionalClasses.UserId;
import twins.boundaries.ItemBoundary;
import twins.logic.ExtendedItemsService;
import twins.logic.ItemsService;

// Save this file again
/* LAST UPDATE: 22/4/21 19:00 p.m
- Added a Constructor for the services.  
- Added class OperationTest.
- Added one test invokedOperation(doesn't work at the moment).
- 
*/

@CrossOrigin(origins = "*")
@RestController
public class ItemController {
	private ExtendedItemsService itemsService;
	
	
	@Autowired
	public ItemController(ExtendedItemsService itemsService) {
		this.itemsService = itemsService;
	}	
	
	/* Digital Items related API */
	@RequestMapping(
			path="/twins/items/{userSpace}/{userEmail}",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary createItem (@RequestBody ItemBoundary IDlessItem,
			@PathVariable("userSpace") String userSpace,
			@PathVariable("userEmail") String userEmail){
		
		ItemBoundary itemboundary = this.itemsService.createItem(userSpace, userEmail, IDlessItem,0);
		return itemboundary;
	}
	
	@RequestMapping(
			path="/twins/items/{userSpace}/{userEmail}/{itemSpace}/{itemID}",
			method = RequestMethod.PUT,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateItem (@RequestBody ItemBoundary itemBoundary,
			@PathVariable("userSpace") String userSpace,
			@PathVariable("userEmail") String userEmail,
			@PathVariable("itemSpace") String itemSpace,
			@PathVariable("itemID") String itemID){
		this.itemsService.updateItem(userSpace, userEmail, itemSpace, itemID, itemBoundary);
	}
	
	@RequestMapping(
			path="/twins/items/{userSpace}/{userEmail}/{itemSpace}/{itemID}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary retrieveItem (
			@PathVariable("userSpace") String userSpace,
			@PathVariable("userEmail") String userEmail,
			@PathVariable("itemSpace") String itemSpace,
			@PathVariable("itemID") String itemID){
		return this.itemsService.getSpecificItem(userSpace, userEmail, itemSpace, itemID);
	}
	
	@RequestMapping(
			path="/twins/items/{userSpace}/{userEmail}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBoundary[] retrieveAllItem (
			@PathVariable("userSpace") String userSpace,
			@PathVariable("userEmail") String userEmail,
	
			@RequestParam(name="size", required = false, defaultValue = "20") int  size,
			@RequestParam(name="page", required = false, defaultValue = "0") int page){
		List<ItemBoundary> allItems = this.
				itemsService.getAllItems(userSpace, userEmail,page,size);
		return allItems.toArray(new ItemBoundary[0]);
	}
	
	/* Admin API */
	
	@RequestMapping(
			path="/twins/admin/items/{userSpace}/{userEmail}",
			method = RequestMethod.DELETE)
	public void deleteItems (
			@PathVariable("userSpace") String userSpace,
			@PathVariable("userEmail") String userEmail){
		this.itemsService.deleteAllItems(userSpace, userEmail);
	}
}
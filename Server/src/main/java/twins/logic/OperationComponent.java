package twins.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import twins.additionalClasses.ItemId;
import twins.boundaries.OperationBoundary;
import twins.dal.ItemHandler;
import twins.dal.OperationHandler;
import twins.dal.UserHandler;
import twins.data.ItemEntity;
import twins.data.UserEntity;

@Component
public class OperationComponent {
	private ItemHandler itemHandler;
	private UserHandler userHandler;
	private ObjectMapper jackson;

	@Autowired
	public OperationComponent(OperationHandler operationHandler) {
		super();
		this.jackson = new ObjectMapper();
	}	

	@Autowired
	public void setItemHandler(ItemHandler itemHandler) {
		this.itemHandler = itemHandler;
	}

	@Autowired
	public void setUserHandler(UserHandler userHandler) {
		this.userHandler = userHandler;
	}

	public Object switchCase(OperationBoundary operationBoundary) {

		Optional<ItemEntity> itemOptional = this.itemHandler
				.findById(this.marshall(new ItemId(operationBoundary.getItem().getItemId().getSpace(),
						operationBoundary.getItem().getItemId().getID())));
		Optional<UserEntity> userOptional = this.userHandler
				.findById(operationBoundary.getInvokedBy().getUserId().getSpace() + "|"
						+ operationBoundary.getInvokedBy().getUserId().getEmail());
		if (itemOptional.isPresent() && userOptional.isPresent()) {
			ItemEntity itemEntity = itemOptional.get();
			UserEntity userEntity = userOptional.get();
			switch (itemEntity.getType()) {

			case "pool":
				return managePoolOperations(operationBoundary, itemEntity, userEntity);

			case "sportsField":
				return manageFieldsOperations(operationBoundary, itemEntity, userEntity);

			case "gym":
				return manageGymOperations(operationBoundary, itemEntity, userEntity);

			case "sauna":
				return manageSaunaOperations(operationBoundary, itemEntity, userEntity);

			default:
				break;
			}
			

		} else
			throw new RuntimeException("");

		return operationBoundary;
	}

	private Object manageSaunaOperations(OperationBoundary operationBoundary, ItemEntity itemEntity,
			UserEntity userEntity) {
		return itemAddRemoveUsers(operationBoundary, itemEntity, userEntity);
	}
	
	private Object managePoolOperations(OperationBoundary operationBoundary, ItemEntity itemEntity,
			UserEntity userEntity) {
		return itemAddRemoveUsers(operationBoundary, itemEntity, userEntity);

	}

	private Object manageGymOperations(OperationBoundary operationBoundary, ItemEntity itemEntity,
			UserEntity userEntity) {
		
		Map<String, Object> operationAttributes = operationBoundary.getOperationAttributes();
		Map<String, Object> itemAttributes = unmarshall(itemEntity.getItemAttributes(), HashMap.class);
		int currAmount = 0;
		int maxAmount = 0;
		if (operationAttributes == null)
			throw new RuntimeException("operation attributes were not Initialized");
		
		if (itemAttributes == null)
			throw new RuntimeException("Item attributes were not Initialized");
		if (!itemAttributes.containsKey("Max Users Amount"))
			throw new RuntimeException("Item Max Users Amount was not Initialized or can not be zero");
		else
			maxAmount = Integer.parseInt(itemAttributes.get("Max Users Amount").toString());
		currAmount =  Integer.parseInt(itemAttributes.get("Current Users Amount").toString());
		switch (operationBoundary.getType()) {

		case "addUser":
			/*
			 * adding user to item attributes +1 to the item capacity counter and adding the
			 * user from the operation attributes to the item array of users. 
			 */
			if (currAmount < maxAmount) {

				// adding users to the gym and saving the new users list
								
				itemAttributes.put("Current Users Amount", currAmount + 1);
				itemAttributes.put("member: "+ operationBoundary.getInvokedBy().getUserId().getEmail(), userEntity.getUserId());
				
				itemEntity.setItemAttributes(this.marshall(itemAttributes));
				this.itemHandler.save(itemEntity);

				operationAttributes.put("Last Operation", "New user add to " + itemEntity.getType());

			} else {
				operationAttributes.put("Last Operation", itemEntity.getType() + " has reached it's full capacity");
			}
			break;
		
		case "removeUser":
			
			if (currAmount > 0 && itemAttributes.containsValue(userEntity.getUserId())) {
				// saving the updated users list after the user remove
			
				itemAttributes.remove("member: "+operationBoundary.getInvokedBy().getUserId().getEmail());
				itemAttributes.put("Current Users Amount", currAmount - 1);
				itemEntity.setItemAttributes(this.marshall(itemAttributes));
				this.itemHandler.save(itemEntity);
				operationAttributes.put("Last Operation", "User was removed from " + itemEntity.getType());

			} else
				operationAttributes.put("Last Operation", "User could not be removed");
			break;
		default:
			break;
		}
		return operationBoundary;

	}

	private Object manageFieldsOperations(OperationBoundary operationBoundary, ItemEntity itemEntity,
			UserEntity userEntity) {
		Map<String, Object> itemResevationsList;
		int maxAmount;
		boolean flag = false;
		Map<String, Object> operationAttributes = operationBoundary.getOperationAttributes();
		Map<String, Object> itemAttributes = unmarshall(itemEntity.getItemAttributes(), HashMap.class);
	
		if (operationAttributes == null)
			throw new RuntimeException("operation attributes were not Initialized");
		
		if (itemAttributes == null)
			throw new RuntimeException("Item attributes were not Initialized");
		
		if(!itemAttributes.containsKey("usersReservations"))
			itemResevationsList = new HashMap<>();
		else
			itemResevationsList = (Map<String, Object>) itemAttributes.get("usersReservations");
		
		if (!itemAttributes.containsKey("Max Users Amount"))
			throw new RuntimeException("Item Max Users Amount was not Initialized or can not be zero");
		else
			maxAmount = Integer.parseInt(itemAttributes.get("Max Users Amount").toString());

		if(itemAttributes.containsKey("Reserved Fully"))
			flag = (boolean) itemAttributes.get("Reserved Fully");
		switch (operationBoundary.getType()) {

		case "reserveField":
			if(!flag)
				addUserReservation(operationBoundary, itemEntity, userEntity, operationAttributes, itemAttributes,
					itemResevationsList, maxAmount);
			else
				operationAttributes.put("Last Operation", "Reservation failed, field is fully reserved");
			break;

		case "cancelReservation":
			
			cancelUserReservation(itemEntity, userEntity, operationAttributes, itemAttributes, itemResevationsList);

			break;
		case "reserveCourt":
			if(!flag) {
				flag= true;
				itemAttributes.put("Reserved Fully", true);
				addUserReservation(operationBoundary, itemEntity, userEntity, operationAttributes, itemAttributes, itemResevationsList, maxAmount);				
			}
			else
				operationAttributes.put("Last Operation", "Reservation failed, field is fully reserved");
			break;
		case "cancelCourtReservation":
			itemAttributes.put("Reserved Fully", false);
			cancelUserReservation(itemEntity, userEntity, operationAttributes, itemAttributes, itemResevationsList);
			flag = false;
		}
		return operationBoundary;

	}

	public void cancelUserReservation(ItemEntity itemEntity, UserEntity userEntity,
			Map<String, Object> operationAttributes, Map<String, Object> itemAttributes,
			Map<String, Object> itemResevationsList) {

		int playersAmount,currAmount;
		if (!operationAttributes.containsKey("playersAmount"))
			throw new RuntimeException("The operation Players Amount was not Initialized or can not be zero");
		else
			playersAmount = Integer.parseInt(operationAttributes.get("playersAmount").toString());
		currAmount = Integer.parseInt(itemAttributes.get("Current Users Amount").toString());
		// checking if the reservation actually exists
		if (itemResevationsList.containsKey(userEntity.getUserId())) {
			itemResevationsList.remove(userEntity.getUserId());
			itemAttributes.put("usersReservations", itemResevationsList);
			itemAttributes.put("Current Users Amount", currAmount - playersAmount);
			itemEntity.setItemAttributes(this.marshall(itemAttributes));
			this.itemHandler.save(itemEntity);
			operationAttributes.put("Last Operation", userEntity.getUsername() + " reservation was canceled");

		} else
			operationAttributes.put("Last Operation", "Reservation does not exists");
	}

	public void addUserReservation(OperationBoundary operationBoundary, ItemEntity itemEntity, UserEntity userEntity,
			Map<String, Object> operationAttributes, Map<String, Object> itemAttributes,
			Map<String, Object> itemResevationsList, int maxAmount) {
		
		int playersAmount,currAmount; 
		if (!operationAttributes.containsKey("playersAmount"))
			throw new RuntimeException("The operation Players Amount was not Initialized or can not be zero");
		else
			playersAmount =  Integer.parseInt(operationAttributes.get("playersAmount").toString());
		
		currAmount =  Integer.parseInt(itemAttributes.get("Current Users Amount").toString());
		if (currAmount+playersAmount <= maxAmount) {

			/*
			 * saving array of two objects in to the map of reservations object 1 = the
			 * player id object 2 = a map of the reservation amount of people
			 */

			// object 2
			Map<String, Object> reservePlayersAmount = new HashMap<>();
			reservePlayersAmount.put("playersAmount", playersAmount);
			itemAttributes.put("Current Users Amount", currAmount + playersAmount);
			// array of object 1 and 2
			Object[] reservationDetails = { operationBoundary.getInvokedBy().getUserId(), reservePlayersAmount };

			itemResevationsList.put(userEntity.getUserId(), reservationDetails);
			itemAttributes.put("usersReservations", itemResevationsList);
			
			// saving the new item attributes
			itemEntity.setItemAttributes(this.marshall(itemAttributes));
			this.itemHandler.save(itemEntity);

			operationAttributes.put("Last Operation", userEntity.getUsername() + " added a field reservation");

		} else
			operationAttributes.put("Last Operation", "Reservation failed, too many players for this field");
	}
	
	public Object itemAddRemoveUsers(OperationBoundary operationBoundary, ItemEntity itemEntity,
			UserEntity userEntity) {
		Map<String, Object> itemUsersList;
		int maxAmount = 0;
		int currAmount = 0;
		
		Map<String, Object> operationAttributes = operationBoundary.getOperationAttributes();
		Map<String, Object> itemAttributes = unmarshall(itemEntity.getItemAttributes(), HashMap.class);
		
		if (operationAttributes == null)
			throw new RuntimeException("operation attributes were not Initialized");
		
		if (itemAttributes == null)
			throw new RuntimeException("Item attributes were not Initialized");

		// checking the item has users that are using it
		if(itemAttributes.containsKey("GetUsers"))
			itemUsersList = (Map<String, Object>) itemAttributes.get("GetUsers");
		else
			itemUsersList = new HashMap<>();

		
		if (!itemAttributes.containsKey("Max Users Amount"))
			throw new RuntimeException("Item Max Users Amount was not Initialized or can not be zero");
		else
			maxAmount =  Integer.parseInt(itemAttributes.get("Max Users Amount").toString());

		if (!itemAttributes.containsKey("Current Users Amount"))
			throw new RuntimeException("Item Current Users Amount was not Initialized");
		else
			currAmount =  Integer.parseInt(itemAttributes.get("Current Users Amount").toString());

		switch (operationBoundary.getType()) {

		case "addUser":
			addUserToItemAttributes(operationBoundary, itemEntity, userEntity, operationAttributes, itemAttributes,
					itemUsersList, currAmount, maxAmount);
			break;

		case "removeUser":
			removeUserFromItemAttributes(itemEntity, userEntity, operationAttributes, itemAttributes, itemUsersList,
					currAmount);
			break;

		default:
			break;
		}

		return operationBoundary;
	}


	private void removeUserFromItemAttributes(ItemEntity itemEntity, UserEntity userEntity,
			Map<String, Object> operationAttributes, Map<String, Object> itemAttributes,
			Map<String, Object> itemUsersList, int currAmount) {
		/*
		 * removing user from item attributes -1 to the item capacity counter and
		 * removing the user from the item array of users.
		 */

		if (itemUsersList == null)
			itemUsersList = new HashMap<>();
		if (currAmount > 0 && itemUsersList.containsKey(userEntity.getUserId())) {
			// saving the updated users list after the user remove
			itemUsersList.remove(userEntity.getUserId());
			itemAttributes.put("GetUsers", itemUsersList);
			itemAttributes.put("Current Users Amount", currAmount - 1);
			itemEntity.setItemAttributes(this.marshall(itemAttributes));
			this.itemHandler.save(itemEntity);
			operationAttributes.put("Last Operation", "User was removed from " + itemEntity.getType());

		} else
			operationAttributes.put("Last Operation", "User could not be removed");
	}

	private void addUserToItemAttributes(OperationBoundary operationBoundary, ItemEntity itemEntity,
			UserEntity userEntity, Map<String, Object> operationAttributes, Map<String, Object> itemAttributes,
			Map<String, Object> itemUsersList, int currAmount, int maxAmount) {
		
		// checking the item has not reached it's full capacity
		if (currAmount < maxAmount) {

			// adding user to the sauna and saving the new users list
			itemUsersList.put(userEntity.getUserId(), operationBoundary.getInvokedBy().getUserId());
			itemAttributes.put("GetUsers", itemUsersList);
			itemAttributes.put("Current Users Amount", currAmount + 1);
			itemEntity.setItemAttributes(this.marshall(itemAttributes));
			this.itemHandler.save(itemEntity);

			operationAttributes.put("Last Operation", "New user add to " + itemEntity.getType());

		} else {
			operationAttributes.put("Last Operation", itemEntity.getType() + " has reached it's full capacity");
		}
	}

	private String marshall(Object value) {
		try {
			return this.jackson.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private <T> T unmarshall(String json, Class<T> requiredType) {
		try {
			return this.jackson.readValue(json, requiredType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

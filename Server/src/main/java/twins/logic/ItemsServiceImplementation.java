package twins.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import twins.additionalClasses.ItemId;
import twins.additionalClasses.Location;
import twins.additionalClasses.UserId;
import twins.boundaries.ItemBoundary;
import twins.dal.ItemHandler;
import twins.dal.UserHandler;
import twins.data.ItemEntity;
import twins.data.UserEntity;

@Service
public class ItemsServiceImplementation implements ExtendedItemsService {

	private ItemHandler itemHandler;
	private UserHandler userHandler;
	private ObjectMapper jackson;
	private String space;

	@Autowired
	public ItemsServiceImplementation(ItemHandler itemHandler) {
		super();
		this.itemHandler = itemHandler;
		this.jackson = new ObjectMapper();
	}

	@Autowired
	public void setUserHandler(UserHandler userHandler) {
		this.userHandler = userHandler;
	}

	// have spring initialize the dummy value using property:
	// spring.application.name
	// or generate default value if property does not exist: "dummyValue"
	@Value("${spring.application.name:countryClubValue}")
	public void setDummy(String space) {
		this.space = space;
	}

	// have spring invoke this operation after initializing Spring bean
	@PostConstruct
	public void init() {
		System.err.println("space: " + this.space);
	}

	@Override
	@Deprecated
	public ItemBoundary createItem(String userSpace, String userEmail, ItemBoundary item) {
		throw new RuntimeException("depracted method\n");
	}
	@Override
	public ItemBoundary createItem(String userSpace, String userEmail, ItemBoundary item,int a) {
		
		// 1. validate input - make sure Item is not null
		if (item == null || item.getType() == null)
			throw new RuntimeException("Item and item type must not be null");

		if (item.getName() == null || item.getName().equals(""))
			throw new RuntimeException("Item name must not be null or empty");

		if (!checkUserRole(new UserId(userSpace, userEmail), "MANAGER"))
			throw new RuntimeException("User is not Manager");
		item.setCreatedBy(new UserId(userSpace, userEmail));
		item.setItemID(new ItemId(this.space, UUID.randomUUID().toString()));
		item.setCreatedTimestamp(new Date());
		if(item.getType().equals("gym"))
			item.getItemAttributes().put("instructor", item.getCreatedBy());

		// 2. boundary -> entity
		ItemEntity entity = this.convertToEntity(item);

		// 3. INSERT to database
		entity = this.itemHandler.save(entity);

		// 4. entity -> boundary
		return this.convertToBoundary(entity);
	}
	@Override
	public ItemBoundary updateItem(String userSpace, String userEmail, String itemSpace, String itemId,
			ItemBoundary newItemBoundary) {

		Optional<ItemEntity> existing = this.itemHandler.findById(this.marshall(new ItemId(itemSpace, itemId)));
		if (checkItemExisting(existing.isPresent()) && checkUserRole(new UserId(userSpace, userEmail), "MANAGER")) {

			newItemBoundary.setItemID(this.unmarshall(existing.get().getItemId(), ItemId.class));
			newItemBoundary.setCreatedBy(this.unmarshall(existing.get().getCreatedBy(), UserId.class));
			newItemBoundary.setCreatedTimestamp(existing.get().getCreatedTimestamp());
			ItemEntity updatedEntity = this.convertToEntity(newItemBoundary);

			// UPDATE
			this.itemHandler.save(updatedEntity);
		}

		return newItemBoundary;
	}

	private boolean checkUserRole(UserId userId, String role) {
		if (userId == null)
			return false;
		Optional<UserEntity> existing = this.userHandler.findById(userId.getSpace() + "|" + userId.getEmail());

		if (checkItemExisting(existing.isPresent()) && existing.get().getRole().toString().equals(role))
			return true;

		return false;

	}

	private boolean checkItemExisting(boolean present) {
		if (!present)
			throw new RuntimeException("this object could not be found");
		return true;
	}

	@Override
	@Transactional(readOnly = true) // handle race condition
	public List<ItemBoundary> getAllItems(String userSpace, String userEmail, int page, int size) {
		// BEGIN new tx (transaction)

		Page<ItemEntity> entitiesPage = this.itemHandler.findAll(PageRequest.of(page, size, Direction.DESC, "name"));

		List<ItemEntity> pagedEntities = entitiesPage.getContent();
		List<ItemBoundary> rv = new ArrayList<>();

		for (ItemEntity entity : pagedEntities) {
			ItemBoundary boundary = this.convertToBoundary(entity);
			if (boundary.isActive() || checkUserRole(new UserId(userSpace, userEmail), "MANAGER"))
				rv.add(boundary);
		}
		return rv;
	}

	@Deprecated
	@Override
	@Transactional(readOnly = true) // handle race condition
	public List<ItemBoundary> getAllItems(String userSpace, String userEmail) {
		
		throw new RuntimeException("depracted method\n");
	}

	@Override
	public ItemBoundary getSpecificItem(String userSpace, String userEmail, String itemSpace, String itemId) {
		Optional<ItemEntity> existing = this.itemHandler.findById(this.marshall(new ItemId(itemSpace, itemId)));
		if (existing.isPresent()) {
			ItemEntity itemEntity = existing.get();
			if (checkUserRole(new UserId(userSpace, userEmail), "MANAGER") || itemEntity.isActive())
				return this.convertToBoundary(itemEntity);
			else
				throw new RuntimeException("Item is not active");
		} else
			throw new RuntimeException("Item could not be found");
	}

	@Override
	public void deleteAllItems(String adminSpace, String adminEmail) {
		if (checkUserRole(new UserId(adminSpace, adminEmail), "ADMIN")) {
			this.itemHandler.deleteAll();
			System.err.println("All items from space " + adminSpace + " have been deleted by " + adminEmail);
		}
		else
			System.err.println("User from space " + adminSpace + " and email " + adminEmail+" tried to delete all item but is not an ADMIN");
	}

	private ItemBoundary convertToBoundary(ItemEntity entity) {
		ItemBoundary boundary = new ItemBoundary();
		boundary.setItemID(this.unmarshall(entity.getItemId(), ItemId.class));
		boundary.setType(entity.getType());
		boundary.setName(entity.getName());
		boundary.setActive(entity.isActive());
		boundary.setCreatedBy(this.unmarshall(entity.getCreatedBy(), UserId.class));
		boundary.setLocation(this.unmarshall(entity.getLocation(), Location.class));

		boundary.setCreatedTimestamp(entity.getCreatedTimestamp());
		String details = entity.getItemAttributes();
		// use jackson for unmarshalling JSON --> Map
		Map<String, Object> moreDetailsMap = this.unmarshall(details, Map.class);
		boundary.setItemAttributes(moreDetailsMap);
		return boundary;
	}

	private ItemEntity convertToEntity(ItemBoundary boundary) {
		ItemEntity entity = new ItemEntity();
		entity.setItemId(this.marshall(boundary.getItemID()));
		entity.setType(boundary.getType());
		entity.setName(boundary.getName());
		entity.setActive(boundary.isActive());
		entity.setCreatedTimestamp(boundary.getCreatedTimestamp());
		entity.setCreatedBy(this.marshall(boundary.getCreatedBy()));
		entity.setLocation(this.marshall(boundary.getLocation()));
		// marshalling of Map to JSON (returned as String)
		entity.setItemAttributes(this.marshall(boundary.getItemAttributes()));
		return entity;
	}

	private <T> T unmarshall(String json, Class<T> requiredType) {
		try {
			return this.jackson.readValue(json, requiredType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String marshall(Object value) {
		try {
			return this.jackson.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

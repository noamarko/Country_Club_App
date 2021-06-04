package twins.boundaries;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//Save this file again

import twins.additionalClasses.ItemId;
import twins.additionalClasses.Location;
import twins.additionalClasses.UserId;


public class ItemBoundary {
	private ItemId itemID;			
	private String type;
	private String name;
	private boolean active;
	private Date createdTimestamp;
	private UserId createdBy;
	private Location location;
	private Map<String, Object> itemAttributes;
	
	public ItemBoundary() {
		this.createdTimestamp = new Date();
		this.itemAttributes = new HashMap<>();
	}
	
	public ItemBoundary(String space, String id, String type, String name, boolean active, UserId createdBy, Location location) {
		this();
		this.itemID = new ItemId(space, id);
		this.type = type;
		this.name = name;
		this.active = active;
		this.createdBy = createdBy;
		this.location = new Location(location.getLat(),location.getLng());
	}
	
	public ItemBoundary(String type, String name, boolean active, UserId createdBy, Location location) {
		this();
		this.itemID = new ItemId("NaN", "NaN");
		this.type = type;
		this.name = name;
		this.active = active;
		this.createdBy = createdBy;
		this.location = new Location(location.getLat(),location.getLng());
	}
	
	public ItemId getItemID() {
		return itemID;
	}

	public void setItemID(ItemId itemID) {
		this.itemID = itemID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public UserId getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UserId createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}



	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Map<String, Object> getItemAttributes() {
		return itemAttributes;
	}

	public void setItemAttributes(Map<String, Object> moreDetailsMap) {
		this.itemAttributes = moreDetailsMap;
	}

	

}

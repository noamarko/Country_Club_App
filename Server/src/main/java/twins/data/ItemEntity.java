package twins.data;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/*
ITEMS_TABLE

DATE <PK>      | NAME      | TYPE         |  ACTIVE				(date was chosen for its unique number) 
=====================================================
DATE       | VARCHAR(255)  | VARCHAR(255) | BOOLEAN              //TODO add more attributes if needed
*/

@Entity
@Table(name = "ITEMS_TABLE")
public class ItemEntity {

	
	private String itemId;
	private String type;
	private String name;
	private boolean active;
	private Date createdTimestamp;
	private String createdBy;
	private String location;
	private String itemAttributes;

	
	public ItemEntity() {

	}

	@Id
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="MESSAGE_TIMESTAMP") //  set column name: MESSAGE_TIMESTAMP
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}


	public String getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	
	public String getLocation() {
		return location;
	}

	
	public void setLocation(String location) {
		this.location = location;
	}

	@Lob
	public String getItemAttributes() {
		return itemAttributes;
	}

	
	public void setItemAttributes(String itemAttributes) {
		this.itemAttributes = itemAttributes;
	}
}

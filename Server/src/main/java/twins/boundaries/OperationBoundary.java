package twins.boundaries;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import twins.additionalClasses.Item;
import twins.additionalClasses.OperationId;
import twins.additionalClasses.User;

public class OperationBoundary {
	private OperationId operationId;
	private String type;
	private Item item;
	private Date createdTimestamp;
	private User invokedBy;
	private Map<String, Object> operationAttributes;

	public OperationBoundary() {
		this.createdTimestamp = new Date();
		this.operationAttributes = new HashMap<>();
	}

	public OperationBoundary(String space, String id, String type, Item item, User invokedBy) {
		this();
		this.operationId = new OperationId(space, id);
		this.type = type;
		this.item = item;
		this.invokedBy = invokedBy;
	}

	public OperationBoundary(String space, String id, OperationBoundary ob) {
		this();
		this.operationId = new OperationId(space, id);
		this.type = ob.getType();
		this.item = ob.getItem();
		this.invokedBy = ob.getInvokedBy();
		this.createdTimestamp = ob.getCreatedTimestamp();
		this.operationAttributes.putAll(ob.getOperationAttributes());
	}
	
	public OperationId getOperationId() {
		return operationId;
	}

	public void setOperationId(OperationId operationId) {
		this.operationId = operationId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public User getInvokedBy() {
		return invokedBy;
	}

	public void setInvokedBy(User invokedBy) {
		this.invokedBy = invokedBy;
	}

	public Map<String, Object> getOperationAttributes() {
		return operationAttributes;
	}

	public void setOperationAttributes(Map<String, Object> operationAttributes) {
		this.operationAttributes = operationAttributes;
	}

	@Override
	public String toString() {
		return "OperationBoundary [operationId=" + operationId + ", type=" + type + ", item=" + item
				+ ", createdTimestamp=" + createdTimestamp + ", invokedBy=" + invokedBy + ", operationAttributes="
				+ operationAttributes + "]";
	}

}

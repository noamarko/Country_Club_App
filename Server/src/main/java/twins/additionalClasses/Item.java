package twins.additionalClasses;

public class Item {
	private ItemId itemId;

	public Item() {
		this.itemId = new ItemId();
	}
	
	public Item(String space, String id) {
		this();
		this.itemId.setSpace(space);
		this.itemId.setID(id);
	}

	public ItemId getItemId() {
		return itemId;
	}

	public void setItemId(ItemId itemId) {
		this.itemId = itemId;
	}
	@Override
	public String toString() {
		return "ItemID [itemId=" + itemId + "]";
	}
}

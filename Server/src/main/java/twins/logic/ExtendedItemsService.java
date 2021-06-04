package twins.logic;

import java.util.List;

import twins.boundaries.ItemBoundary;

public interface ExtendedItemsService extends ItemsService {

	public List<ItemBoundary> getAllItems(String userSpace, String userEmail, int page, int size);
	public ItemBoundary createItem(String userSpace, String userEmail, ItemBoundary item, int a);
}

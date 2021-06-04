package twins.dal;

//import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import twins.data.ItemEntity;

public interface ItemHandler extends PagingAndSortingRepository<ItemEntity, String> {
	// C - Create
	
	// R - Read
	
	// U - Update
	
	// D - Delete

}
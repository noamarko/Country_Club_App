package twins.logic;

import java.util.List;

import twins.boundaries.OperationBoundary;

public interface ExtendedOperationsService extends OperationsService {

	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail, int page, int size);
}

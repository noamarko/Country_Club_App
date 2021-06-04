package twins.logic;

import java.util.List;

import twins.boundaries.OperationBoundary;

public interface OperationsService {
	Object invokeOperation(OperationBoundary operation);
	OperationBoundary invokeAsynchronousOperation(OperationBoundary operation);
	@Deprecated List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail);
	void deleteAllOperations(String adminSpace, String adminEmail);
}

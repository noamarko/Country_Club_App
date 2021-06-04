package twins.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import twins.boundaries.OperationBoundary;
import twins.logic.ExtendedOperationsService;
//import twins.logic.OperationsService;


@CrossOrigin(origins = "*")
@RestController
public class OperationController {
	private ExtendedOperationsService operationsService;
	
	
	@Autowired
	public OperationController(ExtendedOperationsService operationsService) {
		this.operationsService = operationsService;
	}
		
	/* Operations related API*/
	@RequestMapping(
			path="/twins/operations",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Object invokeOperation (@RequestBody OperationBoundary operation){
		return this.operationsService.invokeOperation(operation);
		
	}
	
	@RequestMapping(
			path="/twins/operations/async",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public OperationBoundary syncOperation (@RequestBody OperationBoundary operation){
		return this.operationsService.invokeAsynchronousOperation(operation);
	}
	
	
	/* Admin API */
	
	@RequestMapping(
			path="/twins/admin/operations/{userSpace}/{userEmail}",
			method = RequestMethod.DELETE)
	public void deleteOperations (
			@PathVariable("userSpace") String userSpace,
			@PathVariable("userEmail") String userEmail){
		this.operationsService.deleteAllOperations(userSpace, userEmail);
	}
	
	@RequestMapping(
			path="/twins/admin/operations/{userSpace}/{userEmail}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public OperationBoundary[] exportOperations (
			@PathVariable("userSpace") String userSpace,
			@PathVariable("userEmail") String userEmail,
			@RequestParam(name="size", required = false, defaultValue = "20") int  size,
			@RequestParam(name="page", required = false, defaultValue = "0") int page){
		List<OperationBoundary> allOp = this.operationsService
											.getAllOperations(userSpace, userEmail,page,size);
		return allOp.toArray(new OperationBoundary[0]);
	}
}
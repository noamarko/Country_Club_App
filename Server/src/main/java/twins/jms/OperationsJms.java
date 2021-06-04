package twins.jms;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import twins.boundaries.OperationBoundary;
import twins.logic.ExtendedOperationsService;

@Component
public class OperationsJms {

	private ObjectMapper jackson;
	private ExtendedOperationsService operationLogic;
	
	@Autowired
	public OperationsJms(ExtendedOperationsService operationLogic) {
		super();
		this.operationLogic = operationLogic;
	}
	
	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}
	
	@JmsListener(destination = "OperationsDestination")
	public void handleOperations(String json) {
		
		try {
			OperationBoundary ob = this.jackson.readValue(json, OperationBoundary.class);
			this.operationLogic.invokeOperation(ob);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}

package twins.logic;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import twins.additionalClasses.OperationId;
import twins.additionalClasses.User;
import twins.boundaries.OperationBoundary;
import twins.dal.ItemHandler;
import twins.dal.OperationHandler;
import twins.dal.UserHandler;
import twins.data.ItemEntity;
import twins.data.OperationEntity;
import twins.data.UserEntity;

import twins.additionalClasses.Item;
import twins.additionalClasses.ItemId;

@Service
public class OperationServiceImplementation implements ExtendedOperationsService {

	private OperationHandler operationHandler;
	private ItemHandler itemHandler;
	private UserHandler userHandler;
	private ObjectMapper jackson;
	private String space;
	private JmsTemplate jmsTemplate;
	private OperationComponent operationComponent;

	
	@Autowired
	public OperationServiceImplementation(OperationHandler operationHandler) {
		super();
		this.operationHandler = operationHandler;
		this.jackson = new ObjectMapper();
	}
	
	@Autowired
	public void setOperationComponent(OperationComponent operationComponent) {
		this.operationComponent = operationComponent;
	}

	@Autowired
	public void setItemHandler(ItemHandler itemHandler) {
		this.itemHandler = itemHandler;
	}
	
	@Autowired
	public void setUserHandler(UserHandler userHandler) {
		this.userHandler = userHandler;
	}
	
	@Autowired
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	// have spring initialize the groupName value using property: spring.application.name 
	// or generate default value if property does not exist: "2021b.integ"
	@Value("${spring.application.name:2021b.integ}")
	public void Space(String space) {
		this.space = space;
	}

	// have spring invoke this operation after initializing Spring bean
	@PostConstruct
	public void init() {
	
	}

	//return JSON obj, after creating new id and updating time stamp
	@Override					
	@Transactional

	public Object invokeOperation(OperationBoundary operation) {

		if(operation.getType() == null)
			throw new RuntimeException("operation Type must not be null");
		if(operation.getItem() == null)
			throw new RuntimeException("operation Item must not be null");
		if(operation.getItem().getItemId() == null)
			throw new RuntimeException("oper ItemId must not be null");
		if(operation.getInvokedBy() == null)
			throw new RuntimeException("operation InvokedBy not be null");

		operation.setCreatedTimestamp(new Date());
		operation.setOperationId(new OperationId(this.space,UUID.randomUUID().toString()));
		Optional<ItemEntity> itemOptional = this.itemHandler.findById(this.marshall(new ItemId(operation.getItem().getItemId().getSpace(),operation.getItem().getItemId().getID())));
		if(itemOptional.isPresent()) {
			ItemEntity ie = itemOptional.get();
			Optional<UserEntity> userOptinoal = this.userHandler.findById(operation.getInvokedBy().getUserId().getSpace() + "|" + operation.getInvokedBy().getUserId().getEmail());
		
			if( userOptinoal.isPresent()) { 
				UserEntity ue = userOptinoal.get();
				if(ie.isActive() == true && ue.getRole().equals("PLAYER")) {
					OperationBoundary updatedOperation= (OperationBoundary) this.operationComponent.switchCase(operation);
					OperationEntity oe = this.operationHandler.save(this.convertToEntity(updatedOperation));
					return this.convertToBoundary(oe);
					
				}
					
				else 
					throw new RuntimeException("Item must be active and UserRole must be Player\n");
			}
			else 
				throw new RuntimeException("User must not be null\n");
			
		}
		else 
			throw new RuntimeException("item must not be null\n");
	}




	//return JSON obj, after creating new id and updating time stamp
	@Override
	@Transactional
	public OperationBoundary invokeAsynchronousOperation(OperationBoundary operation) { 
		if(operation.getType() == null)
			throw new RuntimeException("operation Type must not be null\n");
		if(operation.getItem() == null)
			throw new RuntimeException("operation Item must not be null\n");
		if(operation.getItem().getItemId() == null)
			throw new RuntimeException("oper ItemId must not be null\n");
		if(operation.getInvokedBy() == null)
			throw new RuntimeException("operation Item must not be null\n");

		try {
			operation.setCreatedTimestamp(new Date());
			operation.setOperationId(new OperationId(this.space,UUID.randomUUID().toString()));
			operation.getOperationAttributes().put("Last Operation", "preforming async operation");
			OperationEntity oe = this.convertToEntity(operation);
			Optional<ItemEntity> itemOptional = this.itemHandler.findById(this.marshall(new ItemId(operation.getItem().getItemId().getSpace(),operation.getItem().getItemId().getID())));
			Optional<UserEntity> userOptional = this.userHandler.findById(operation.getInvokedBy().getUserId().getSpace() + "|" + operation.getInvokedBy().getUserId().getEmail());
			if(itemOptional.isPresent() && userOptional.isPresent()) {
				ItemEntity ie = itemOptional.get();
				UserEntity ue = userOptional.get();
				if(ie.isActive() == true && ue.getRole().equals("PLAYER")) { 
					String json = this.jackson.writeValueAsString(operation);
					this.jmsTemplate.send("OperationsDestination", session -> session.createTextMessage(json));
				}
				else 
					throw new RuntimeException("Item must be active and UserRole must be Player\n");
			}
			else {
				throw new RuntimeException("Item must not be null and User must not be null\n");
			}
			return this.convertToBoundary(oe);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//Create a list of all operations entities and convert them to boundaries
	@Override
	@Transactional (readOnly = true)
	@Deprecated
	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail) {
		throw new RuntimeException("depracted method\n");
	}
	@Override
	@Transactional (readOnly = true)
	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail,
			int page, int size){
		Page<OperationEntity> entitiesPage = this.operationHandler.findAll(PageRequest.of(page, size, Direction.DESC, "createdTimestamp"));
		Optional<UserEntity> userOptional = this.userHandler.findById(adminSpace+ "|" +adminEmail);
		List<OperationBoundary> rv = new ArrayList<>();
		if(userOptional.isPresent()) {
			UserEntity ue = userOptional.get();
			if(ue.getRole().equals("ADMIN")) {
				List<OperationEntity> pagedEntities = entitiesPage.getContent();
				
				for (OperationEntity operation : pagedEntities) {
					OperationBoundary ob = this.convertToBoundary(operation);
					if(ob !=null)
						rv.add(ob);
				}	
			}	
		}
		else {
			throw new RuntimeException("User must not be null\n");
		}
		return rv;
	}
	


	@Override
	@Transactional
	public void deleteAllOperations(String adminSpace, String adminEmail) {
		Optional<UserEntity> userOptional = this.userHandler.findById(adminSpace+ "|" +adminEmail);
		if(userOptional.isPresent()) {
			UserEntity ue = userOptional.get();
			if(ue.getRole().equals("ADMIN")) 
				this.operationHandler.deleteAll();
			else
				throw new RuntimeException("User must be admin in order to delete operations\n");
		}			
	}
	
	//convert entity-> boundary
	private OperationBoundary convertToBoundary(OperationEntity oe) {
		OperationBoundary ob = new OperationBoundary();
		String [] tokens = getTokens(oe.getOperationId());
		ob.setOperationId(new OperationId(tokens[0],tokens[1]));
		ob.setType(oe.getType());
		ob.setCreatedTimestamp(oe.getCreatedTimestamp());
		ob.setItem(this.unmarshall(oe.getItem(), Item.class));
		String details = oe.getOperationAttributes();
		// use jackson for unmarshalling JSON --> Map
		Map<String, Object> operationAttributesMap = this.unmarshall(details, Map.class);
		ob.setOperationAttributes(operationAttributesMap);
		ob.setInvokedBy(this.unmarshall(oe.getInvokedBy(), User.class));
		return ob;
	}

	//convert boundary -> entity
	private OperationEntity convertToEntity(OperationBoundary operation) {
		OperationEntity entity = new OperationEntity();
		entity.setOperationId(operation.getOperationId());
		entity.setType(operation.getType());
		entity.setItem(this.marshall(operation.getItem()));
		entity.setCreatedTimestamp(operation.getCreatedTimestamp());
		entity.setOperationAttributes(this.marshall(operation.getOperationAttributes()));

		if (operation.getInvokedBy()!= null) {
			entity.setInvokedBy(this.marshall(operation.getInvokedBy()));
		}
		return entity;
	}

	private String marshall(Object value) {
		try {
			return this.jackson
					.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private <T> T unmarshall(String json, Class<T> requiredType) {
		try {
			return this.jackson.readValue(json, requiredType);
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String[] getTokens(String userID) {
		String[] tokens = new String[2];
		tokens = userID.split("\\|");
		return tokens;
	}

}

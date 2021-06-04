package twins.data;

import java.util.Date;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import twins.additionalClasses.OperationId;

/*
OPERATION_TABLE

OPERATION_ID <PK>      | NAME      	   | TYPE         				(date was chosen for its unique number) 
======================================================
OPERATION_ID	       | VARCHAR(255)  | VARCHAR(255)              //TODO add more attributes if needed
*/



@Entity
@Table(name = "OPERATION_TABLE")
public class OperationEntity {
	private String operationId;
	//private String operationSpace;
	private String type;
	private String item;
	private Date createdTimestamp;
	private String invokedBy;
	private String operationAttributes;

	
	
	
	public OperationEntity() {
		this.createdTimestamp = new Date();
	}
	
	public OperationEntity(String operationId, String type, String item, Date createdTimestamp, String invokedBy,
			String operationAttributes) {
		this();
		this.operationId = operationId;
		this.type = type;
		this.item = item;
		this.createdTimestamp = createdTimestamp;
		this.invokedBy = invokedBy;
		this.operationAttributes = operationAttributes;
	}

	@Id
	public String getOperationId() {
		return this.operationId;
	}
	
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}
	@Transient
	public void setOperationId(OperationId operationId) {
		this.operationId = operationId.getSpace()+ "|" + operationId.getId();;
	}
	/*public String getOperationSpace() {
		return this.operationSpace;
	}
	
	public void setOperationSpace(String operationSpace) {
		this.operationSpace = operationSpace;
	}*/
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getItem() {
		return item;
	}
	
	public void setItem(String item) {
		this.item = item;
	}
	
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="MESSAGE_TIMESTAMP")
	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	
	public String getInvokedBy() {
		return invokedBy;
	}
	
	public void setInvokedBy(String invokedBy) {
		this.invokedBy = invokedBy;
	}
	
	@Lob
	public String getOperationAttributes() {
		return operationAttributes;
	}
	
	public void setOperationAttributes(String operationAttributes) {
		this.operationAttributes = operationAttributes;
	}
	

}

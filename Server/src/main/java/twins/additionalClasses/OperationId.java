package twins.additionalClasses;

public class OperationId {
	private String space;
	private String id;

	public OperationId() {

	}

	public OperationId(String space, String id) {
		super();
		this.space = space;
		this.id = id;
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


}

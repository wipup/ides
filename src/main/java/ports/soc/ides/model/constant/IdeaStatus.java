package ports.soc.ides.model.constant;


public enum IdeaStatus {

	Approved("A", "Approved"), Withdrawn("W", "Withdrawn"), Provisional("P", "Provisional"), Allocated("S", "Allocated"), 
	Trashed("D", "Trashed");
	
	private final String value;
	private String label;

	private IdeaStatus(String value, String label) {
		this.value = value;
		this.label = label;
	}
	
	public String getValue() {
		return value;
	}
	
	public static final IdeaStatus getIdeaStatus(String value) {
		if (value != null) {
			switch (value.toUpperCase()) {
			case "S":
				return Allocated;
			case "A":
				return Approved;
			case "W":
				return Withdrawn;
			case "P":
				return Provisional;
			case "D":
				return Trashed;
			}
		}
		return Withdrawn;
	}

	public String getLabel() {
		return label;
	}
}

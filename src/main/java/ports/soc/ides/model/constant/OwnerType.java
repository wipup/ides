package ports.soc.ides.model.constant;

public enum OwnerType {
	EMAIL("E"),
	DOMAIN("D");
	
	private String value;
	
	private OwnerType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static final OwnerType getOwnerType(String value) {
		if (value != null) {
			switch (value.toUpperCase()) {
			case "D":
				return DOMAIN;
			case "E":
				return EMAIL;
			}
		}
		return null;
	}
}

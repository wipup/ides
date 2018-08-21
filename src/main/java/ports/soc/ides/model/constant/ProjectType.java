package ports.soc.ides.model.constant;

public enum ProjectType {

	Study("S", "Study Project"), Engineering("E", "Engineering Project"), NotSpecific("N", "Not Specific");

	private String value;
	private String label;

	private ProjectType(String v, String label) {
		this.value = v;
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public static final ProjectType getProjectType(String value) {
		if (value != null) {
			switch (value.toUpperCase()) {
			case "S":
				return Study;
			case "E":
				return Engineering;
			case "N":
				return NotSpecific;
			}
		}
		return NotSpecific;
	}
}

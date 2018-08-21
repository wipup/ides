package ports.soc.ides.controller.helper;

public enum CreateIdeaWizardStep {
	CREATE_ORGANISATION, CREATE_IDEA, REVIEW;

	public int getOrdinal() {
		return ordinal();
	}

	public static final CreateIdeaWizardStep getWizardStep(String toString) {
		if (CREATE_ORGANISATION.toString().equalsIgnoreCase(toString)) {
			return CREATE_ORGANISATION;
		} else if (CREATE_IDEA.toString().equalsIgnoreCase(toString)) {
			return CREATE_IDEA;
		} else if (REVIEW.toString().equalsIgnoreCase(toString)) {
			return REVIEW;
		}
		return null;
	}
}
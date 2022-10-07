package be.vlaanderen.informatievlaanderen.ldes.processors.valueobjects;

public class MemberInfo {
	private final String id;
	private final String observedAt;

	public MemberInfo(String id, String observedAt) {
		this.id = id;
		this.observedAt = observedAt;
	}

	public String getId() {
		return id;
	}

	public String getObservedAt() {
		return observedAt;
	}
}

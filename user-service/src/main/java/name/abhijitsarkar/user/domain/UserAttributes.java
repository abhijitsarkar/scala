package name.abhijitsarkar.user.domain;

public enum UserAttributes {
	FIRST_NAME("firstName"), LAST_NAME("lastName"), EMAIL("email"), PHONE_NUM("phoneNum"), ACTIVE("active");

	UserAttributes(String value) {
		this.value = value;
	}

	private String value;

	public String toString() {
		return this.value;
	}
}

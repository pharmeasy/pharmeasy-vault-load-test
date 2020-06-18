package in.pharmeasy.outward.api.helpers;

import com.auito.core.api.entry.RequestSpecification;

import in.pharmeasy.outward.api.constants.Constants;

public class UtilsMethods {

	public static RequestSpecification setNoAuthtHeader(RequestSpecification requestSpecification) {

		requestSpecification.addHeader("Content-Type", "application/json");
		requestSpecification.addHeader("Accept", "application/json");
		return requestSpecification;
	}

	public static RequestSpecification setDefaultHeader(RequestSpecification requestSpecification) {

		requestSpecification.addHeader("Authorization", Constants.AUTH_TOKEN);
		requestSpecification.addHeader("Content-Type", "application/json");
		requestSpecification.addHeader("Accept", "application/json");
		return requestSpecification;
	}

	public static RequestSpecification setDefaultHeader(RequestSpecification requestSpecification, String authToken) {

		requestSpecification.addHeader("Authorization", authToken);
		requestSpecification.addHeader("Content-Type", "application/json");
		requestSpecification.addHeader("Accept", "application/json");
		return requestSpecification;
	}
}

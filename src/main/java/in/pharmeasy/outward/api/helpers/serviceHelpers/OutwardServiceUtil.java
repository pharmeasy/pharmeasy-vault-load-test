package in.pharmeasy.outward.api.helpers.serviceHelpers;

import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.testng.Assert;

import com.auito.core.api.IServiceHelper;
import com.auito.core.api.entry.HttpResponse;
import com.auito.core.api.entry.RequestSpecification;

import in.pharmeasy.outward.api.constants.Constants;
import in.pharmeasy.outward.api.helpers.DBHelper;
import in.pharmeasy.outward.api.helpers.TokenGeneration;
import in.pharmeasy.outward.api.helpers.UtilsMethods;
import in.pharmeasy.utils.entry.JWTAuthHeaderPayload;

public class OutwardServiceUtil implements IServiceHelper {
	UtilsMethods util = new UtilsMethods();

	public HashMap<String, Object> login() {
		HashMap<String, Object> pickerData = new HashMap<>();
		DBHelper dbHelper = new DBHelper();
		// Picker App token
		JWTAuthHeaderPayload pickerAppToken_PAYLOAD = TokenGeneration.getPickerDefaultAuthPayload();
		String pickerToken = TokenGeneration.generateToken(pickerAppToken_PAYLOAD);
		String pid = pickerAppToken_PAYLOAD.getUid();

		pickerData.put("pickerAppToken", pickerToken);
		pickerData.put("pickerId", pid);

		dbHelper.insertPickerUser((String) pickerData.get("pickerId"));
		return pickerData;
	}

	public HttpResponse deleteAssignedPickerTask(String authToken) {
		RequestSpecification requestSpecification = new RequestSpecification(Constants.ASSIGN_URL);
		requestSpecification = util.setDefaultHeader(requestSpecification, authToken);
		HttpResponse pickerTaskResponse = getRestClient().createRequest(requestSpecification).delete();
		Assert.assertEquals(pickerTaskResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "http status : 200");
		return pickerTaskResponse;
	}
}

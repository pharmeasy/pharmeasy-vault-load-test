package in.pharmeasy.outward.apiTest.Tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.auito.core.utils.CSVUtils;

import in.pharmeasy.outward.api.constants.Constants;
import in.pharmeasy.outward.api.helpers.DBHelper;
import in.pharmeasy.outward.api.helpers.serviceHelpers.OutwardServiceUtil;

public class TestOutward {
	DBHelper dbHelper = new DBHelper();
	OutwardServiceUtil util = new OutwardServiceUtil();

	@Test
	public void getUcodes() throws IOException {
		List<Map<String, Object>> data = dbHelper.getLoadProductDetails();
		List<String[]> uploadLines = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			String newLine[] = new String[Constants.ALL_UCODES_HEADERS.length];
			int count = 0;
			Map<String, Object> map = data.get(i);
			System.out.println(map.get("name") + "," + map.get("code"));
			newLine[count++] = map.get("name").toString();
			newLine[count++] = map.get("code").toString();
			uploadLines.add(newLine);
		}
		CSVUtils.writeAll(Constants.ALL_UCODES_FILE_NAME, uploadLines);
	}

	@Test
	public void unAssign() throws IOException {
		List<HashMap<String, Object>> pickerDataList = new ArrayList<HashMap<String, Object>>();
		List<String[]> uploadLines = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			HashMap<String, Object> pickerData = util.login();
			util.deleteAssignedPickerTask((String) pickerData.get("pickerAppToken"));
			pickerDataList.add(pickerData);
		}
		for (int i = 0; i < pickerDataList.size(); i++) {
			Map<String, Object> map = pickerDataList.get(i);
			String newLine[] = new String[Constants.ALL_UCODES_HEADERS.length];
			int count = 0;
			System.out.println(map.get("pickerAppToken") + "," + map.get("pickerId"));
			newLine[count++] = map.get("pickerAppToken").toString();
			newLine[count++] = map.get("pickerId").toString();
			uploadLines.add(newLine);
		}
		CSVUtils.writeAll(Constants.ALL_PICKER__FILE_NAME, uploadLines);

	}
}

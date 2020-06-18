package in.pharmeasy.outward.api.constants;

import com.auito.core.ConfigManager;

public interface Constants {
	String STORE_ID = ConfigManager.getInstance().getString(() -> "storeId");
	String AUTH_TOKEN = "";
	String[] ALL_UCODES_HEADERS = new String[] { "medicine_name", "ucode" };
	String[] ALL_PICKER_HEADERS = new String[] { "token", "pickerId" };
	String TEST_DATA_DIR = "src/test/resources/";
	String ALL_UCODES_FILE_NAME = TEST_DATA_DIR + "b2c_meds.csv";
	String ALL_PICKER__FILE_NAME = TEST_DATA_DIR + "pickerUsers.csv";
	String ASSIGN_URL = ConfigManager.getInstance().getString(() -> "outward.assign");
	String PRODUCT_DETAIL_UCB_V1_LOAD = "SELECT distinct p.name, p.code FROM product_inventory pi, product p WHERE pi.product_id = p.id and p.refrigerated = 0 and p.banned = 0 and pi.available > 100 and expiry > SYSDATE() + INTERVAL 90 DAY and issue_bin = 0 and p.code in (select ucode from `ucode_batch_bin`)";
	String INSERT_PICKER_USER = "INSERT INTO `picker_user` (`registered_user_id`, `user_id`, `status`, `created_on`, `updated_on`) VALUES(?, ?, 'Available', sysdate(), sysdate())";
}
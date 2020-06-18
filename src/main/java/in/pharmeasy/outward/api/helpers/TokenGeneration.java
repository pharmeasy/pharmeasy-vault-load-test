package in.pharmeasy.outward.api.helpers;

import java.util.Arrays;
import java.util.UUID;

import com.auito.core.ConfigManager;
import com.auito.core.api.utils.JWTAlgorithm;
import com.auito.core.api.utils.JavaWTGenerator;

import in.pharmeasy.outward.api.constants.Constants;
import in.pharmeasy.utils.entry.JWTAuthHeaderPayload;

public class TokenGeneration {

	private static JavaWTGenerator javaWTGenerator = JavaWTGenerator.getInstance();
	private static String HEADER = "{\"typ\":\"JWT\",\"alg\":\"HS512\"}";
	private static String SECRET = ConfigManager.getInstance().getString(() -> "token.secret");
	private static String THEA = ConfigManager.getInstance().getString(WareHouseConfig.WAREHOSUE);

	private TokenGeneration() {
	}

	private static TokenGeneration INSTANCE = null;

	public static TokenGeneration getInstance() {
		if (INSTANCE == null) {
			synchronized (JavaWTGenerator.class) {
				if (INSTANCE == null) {
					INSTANCE = new TokenGeneration();
				}
			}
		}
		return INSTANCE;
	}

	public static String generateToken(JWTAuthHeaderPayload payload) {
		String token = javaWTGenerator.encode(JWTAlgorithm.HMAC512, HEADER, payload, SECRET);
		return token;
	}

	public static String getDefaultToken() {
		String token = javaWTGenerator.encode(JWTAlgorithm.HMAC512, HEADER, getMercuryDefaultAuthPayload(), SECRET);
		return token;
	}

	public static JWTAuthHeaderPayload getMercuryDefaultAuthPayload() {
		JWTAuthHeaderPayload payload = new JWTAuthHeaderPayload();
		payload.setApp("nebula");
		payload.setAudience("mercury");
		payload.setUid(UUID.randomUUID().toString());
		payload.setIssuer("PharmEasy.in");
		payload.setName("Automation Nebula User");
		payload.setStore(Constants.STORE_ID);
		payload.setScopes(Arrays.asList("wh-inventory-rectifier", "wh-super-admin"));
		payload.setUser("Automation.User@gmail.com");
		payload.setTenant(THEA);
		return payload;
	}

	public static JWTAuthHeaderPayload getPickerDefaultAuthPayload() {
		JWTAuthHeaderPayload payload = new JWTAuthHeaderPayload();
		payload.setApp("picker");
		payload.setAudience("mercury");
		payload.setUid(UUID.randomUUID().toString());
		payload.setIssuer("PharmEasy.in");
		payload.setName("Automation Picker User");
		payload.setStore("");
		payload.setScopes(Arrays.asList("wh-picker"));
		payload.setUser("Automation.User@gmail.com");
		payload.setTenant(THEA);
		return payload;
	}

	public static JWTAuthHeaderPayload getRackerDefaultAuthPayload() {
		JWTAuthHeaderPayload payload = new JWTAuthHeaderPayload();
		payload.setApp("fetcher");
		payload.setAudience("mercury");
		payload.setUid(UUID.randomUUID().toString());
		payload.setIssuer("PharmEasy.in");
		payload.setName("Automation Racker User");
		payload.setStore("");
		payload.setScopes(Arrays.asList("wh-racker"));
		payload.setUser("Automation.User@gmail.com");
		payload.setTenant(THEA);
		return payload;
	}

	public static JWTAuthHeaderPayload getPharmacistDefaultAuthPayload() {
		JWTAuthHeaderPayload payload = new JWTAuthHeaderPayload();
		payload.setApp("pharmacist");
		payload.setAudience("mercury");
		payload.setUid(UUID.randomUUID().toString());
		payload.setIssuer("PharmEasy.in");
		payload.setName("Automation Racker User");
		payload.setStore("");
		payload.setScopes(Arrays.asList("store-billing-user", "store-pharmacist", "wh-billing-user",
				"wh-gate-pass-user", "wh-signatory", "wh-super-admin"));
		payload.setUser("Automation.User@gmail.com");
		payload.setTenant(THEA);
		return payload;
	}

}

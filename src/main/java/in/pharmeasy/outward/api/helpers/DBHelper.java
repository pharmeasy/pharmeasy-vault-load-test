package in.pharmeasy.outward.api.helpers;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testng.Assert;

import com.auito.core.db.AbstractSQLDataSource;

import in.pharmeasy.outward.api.constants.Constants;

public class DBHelper extends AbstractSQLDataSource {
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public DBHelper() {
		this.namedParameterJdbcTemplate = getNamedParameterJdbcTemplate("database");
		this.jdbcTemplate = getJdbcTemplate("database");
	}

	public void insertPickerUser(String registeredUserId) {

		String userId = "picker user";
		String query = Constants.INSERT_PICKER_USER;
		try {
			int isSuccess = jdbcTemplate.update(query, registeredUserId, userId);
			LOG.info(registeredUserId + " user has been inserted to database with status " + isSuccess);
			Assert.assertTrue(isSuccess >= 0);
		} catch (EmptyResultDataAccessException e) {
			LOG.error("Error in insering user to database");
		}

	}

	public List<Map<String, Object>> getLoadProductDetails() {
		String query = Constants.PRODUCT_DETAIL_UCB_V1_LOAD;
		List<Map<String, Object>> productDetails = jdbcTemplate.queryForList(query);
		return productDetails;
	}
}

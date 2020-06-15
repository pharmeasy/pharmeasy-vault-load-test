package in.pharmeasy.outward.api.helpers;

import com.auito.core.ConfigManager;
import com.auito.core.IConfigKey;
import com.auito.core.entry.CommonConfigKey;

public enum WareHouseConfig implements IConfigKey {


    WAREHOSUE("database.db");

    private String name;

    WareHouseConfig(String name) {
        String env = ConfigManager.getInstance().getString(CommonConfigKey.ENVIRONMENT);
        name = env+"."+name;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}

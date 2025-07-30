package getversion.model;

import getversion.model.CustomerInfoDatabase;

public interface CustomerInfoDatabase {
    CustomerInfo get(String customerID);
}

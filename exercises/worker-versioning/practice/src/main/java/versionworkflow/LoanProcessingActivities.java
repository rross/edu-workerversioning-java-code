package getversion;

import io.temporal.activity.ActivityInterface;
import getversion.model.ChargeInput;
import getversion.model.CustomerInfo;

@ActivityInterface
public interface LoanProcessingActivities {

  public String chargeCustomer(ChargeInput input);

  public String sendThankYouToCustomer(CustomerInfo input);

}

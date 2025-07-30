package getversion;

import org.slf4j.Logger;

import getversion.model.CustomerInfo;
import getversion.model.ChargeInput;

import io.temporal.common.SearchAttributeKey;
import io.temporal.workflow.Workflow;
import io.temporal.activity.ActivityOptions;
import java.util.Arrays;
import java.util.List;
import java.time.Duration;

public class LoanProcessingWorkflowImpl implements LoanProcessingWorkflow {

  public static final Logger logger = Workflow.getLogger(LoanProcessingWorkflowImpl.class);

  public static final SearchAttributeKey<List<String>> TEMPORAL_CHANGE_VERSION = SearchAttributeKey.forKeywordList("TemporalChangeVersion");

  ActivityOptions options =
      ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(5)).build();

  private final LoanProcessingActivities activities =
      Workflow.newActivityStub(LoanProcessingActivities.class, options);

  public String loanProcessingWorkflow(CustomerInfo info) {

    String customerId = info.getCustomerID();
    int amount = info.getAmount();
    int numberOfPeriods = info.getNumberOfPeriods();

    int totalPaid = 0;

    // or workflow executions started before the change, send thank you before the loop
    // TODO B: Comment this out and uncomment the identical block below the loop
    // String confirmation = activities.sendThankYouToCustomer(info);

    for (int period = 1; period <= numberOfPeriods; period++) {

      ChargeInput chargeInput = new ChargeInput(customerId, amount, period, numberOfPeriods);

      activities.chargeCustomer(chargeInput);

      totalPaid += chargeInput.getAmount();
      logger.info("Payment complete for period: {} Total Paid: {}", period, totalPaid);

      Workflow.sleep(Duration.ofSeconds(60));
    }

    // for workflow executions started after the change, send thank you after the loop
    // TODO B: Uncomment this and comment out the identical block above the loop
    String confirmation = activities.sendThankYouToCustomer(info);

    return String.format("Loan for customer %s has been fully paid (total=%d)", customerId,
        totalPaid);

  }
}

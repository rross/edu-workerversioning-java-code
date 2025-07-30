package getversion;

import io.temporal.workflow.Workflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.common.SearchAttributes;
import getversion.model.CustomerInfo;
import getversion.model.SimpleCustomerMap;

public class Starter {

  public static void main(String[] args) {

    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    WorkflowClient client = WorkflowClient.newInstance(service);

    SimpleCustomerMap customers = new SimpleCustomerMap();

    String customerId = args[0];

    CustomerInfo info = customers.get(customerId);

    WorkflowOptions options = WorkflowOptions.newBuilder()
        .setWorkflowId("loan-processing-workflow-customer-" + info.getCustomerID())
        .setTaskQueue(Constants.taskQueueName)
        .build();

    LoanProcessingWorkflow workflow = client.newWorkflowStub(LoanProcessingWorkflow.class, options);

    String result = workflow.loanProcessingWorkflow(info);

    System.out.printf("Workflow result: %s\n", result);
    System.exit(0);
  }
}

package getversion;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerOptions;
import io.temporal.common.VersioningBehavior;
import io.temporal.common.WorkerDeploymentVersion;
import io.temporal.worker.WorkerDeploymentOptions;
import io.temporal.worker.WorkerFactory;

public class LoanProcessingWorker {
  public static void main(String[] args) {

    WorkerOptions options = WorkerOptions.newBuilder()
        .setDeploymentOptions(
            WorkerDeploymentOptions.newBuilder()
                .setVersion(new WorkerDeploymentVersion("worker_versioning_demo", "2.0"))
                .setUseVersioning(true)
                .setDefaultVersioningBehavior(VersioningBehavior.PINNED)
                .build())
        .build();

    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkerFactory factory = WorkerFactory.newInstance(client);

    Worker worker = factory.newWorker(Constants.taskQueueName, options);

    worker.registerWorkflowImplementationTypes(LoanProcessingWorkflowImpl.class);

    worker.registerActivitiesImplementations(new LoanProcessingActivitiesImpl());

    factory.start();
  }
}

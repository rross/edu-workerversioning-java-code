# Exercise 1: Worker Versioning

During this exercise, you will

- Configure a Worker for Versioning
- Mark a Workflow as Pinned
- Move a Pinned Workflow to a new Version
- Cut over your running Workers
- Sunset an old deployment Version

Make your changes to the code in the `practice` subdirectory (look for
`TODO` comments that will guide you to where you should make changes to
the code). If you need a hint or want to verify your changes, look at
the complete version in the `solution` subdirectory.

## Part A: Configure a Worker for Versioning and Mark a Workflow as Pinned

1. Edit the `LoanProcessingWorker.java` file. This file contains a Temporal Worker that
   looks and behaves the same as a Worker without Worker Versioning enabled,
   with the exception of some additional parameters provided to
   `WorkerOptions.newBuilder()`. Of these, `.setUseVersioning(true)` should always be enabled
   to use Worker Versioning, and `setVersion()`, and
   `.setDefaultVersioningBehavior()` should be configured every time.
2. In Java, `setVersion()` is a composite of two fields. For this Exercise, you do
   not need to edit the deployment name, which has been
   set to `"worker_versioning_demo"`, a descriptive name. You need to assign a
   Build ID, which should be a version string like `'1.0'`. You also need to provide
   `.setDefaultVersioningBehavior()`, which can be
   `VersioningBehavior.PINNED` or `VersioningBehavior.AUTO_UPGRADE`. This will determine whether new
   Workflows handled by this Worker will default to automatically running on
   your newest code revision, or will remain Pinned until being migrated
   manually. For now, set it to `VersioningBehavior.PINNED`.
3. Start your Worker by running `mvn clean compile exec:java -Dexec.mainClass="getversion.LoanProcessingWorker"`.
   You should receive output containing your Build ID:

   ```
   2025/07/22 14:33:23 INFO  Started Worker Namespace default TaskQueue loan-processing-workflow-taskqueue WorkerID 80416@Kavorka.local@ BuildID 1.0
   ```
4. In another terminal, run `temporal worker deployment describe --name="worker_versioning_demo"` to get some metadata returned from your running Worker. This should also return your Build ID:
   
   ```
   Worker Deployment:
     Name        worker_versioning_demo
     CreateTime  17 seconds ago

   Version Summaries:
         DeploymentName      BuildId  DrainageStatus    CreateTime
     worker_versioning_demo  1.0      unspecified     17 seconds ago
   ```
5. Next, run `mvn clean compile exec:java -Dexec.mainClass="getversion.Starter" -Dexec.args="a100"`
   to kick off a Workflow. You can check the Temporal Web UI to ensure that it is running normally.

## Part B: Move a Pinned Workflow to a new Version

1. Now, you'll make some changes to your Workflow code and increment your Worker
   version. Edit the `LoanProcessingWorkflowImpl.java` file. You should find two copies of the
   call to `activities.sendThankYouToCustomer(info)`
   — one before the `for` loop and one after the `for` loop that is commented
   out. Comment out the first instance, and uncomment the second. Normally,
   reordering the Activities run by your Workflow this way would potentially
   cause a non-determinism error by causing your Event History to go out of sync
   on Replay. With Worker Versioning, however, you can avoid this. Save the file.
2. Edit the `LoanProcessingWorker.java` file again, and increment your BuildID to `2.0`.
   Save the file. In another terminal, start another Worker by running `mvn clean compile exec:java -Dexec.mainClass="getversion.LoanProcessingWorker"`.
   You should now have both a "1.0" and a "2.0" Worker running.
   The Workflow you already started will continue to run with the "1.0" Worker,
   and the "2.0" Worker will poll the Temporal Service until it has a "2.0"
   Workflow to run.
3. You can verify that you have two different Workers polling with two different
   versions by checking the "Workers" tab in the Temporal Web UI, or by running
   `temporal worker deployment describe --name="worker_versioning_demo"` in yet
   another terminal:
   
   ```
   Worker Deployment:
     Name        worker_versioning_demo
     CreateTime  7 minutes ago

   Version Summaries:
         DeploymentName      BuildId  DrainageStatus    CreateTime
     worker_versioning_demo  2.0      unspecified     1 minute ago
     worker_versioning_demo  1.0      unspecified     7 minutes ago
   ```
4. Now, if you were to kick off another Workflow, for example by running
   `npm run workflow a101`, that Workflow would again be handled by the
   "1.0" Worker, rather than using your updated code. This is because you
   configured this Worker's default versioning behavior to Pinned rather than
   auto-upgraded. To update the default version that new Workflows will run with
   when using Pinning, you first have to run a
   `temporal worker deployment set-current-version` command, like so:

   ```
   $ temporal worker deployment set-current-version \
       --deployment-name "worker_versioning_demo" \
       --build-id "2.0"
   ```
5. Finally, after running this command, go ahead and run
   `mvn clean compile exec:java -Dexec.mainClass="getversion.Starter" -Dexec.args="a101"`
   to kick off a new Workflow. It will be handled by
   your "2.0" Worker, and reflect your code changes, running the second Activity
   after the `for` loop.

### This is the end of the exercise.

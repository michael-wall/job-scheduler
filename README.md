## Introduction ##
This is a sample Job Scheduler that retrieves the Object Records for a specified Object Definition ERC, iterates them and updates a specified field in each Object record.

## Environment ##
- The module is built for 2025.Q1.0 (Liferay Workspace gradle.properties > liferay.workspace.product = dxp-2025.q1.0-lts)
- JDK 21 is expected for compile time and runtime.

## Configuration ##
- Create an Object Definition with Scope set to Company and set an ERC on the Object Definition e.g. STUDENT.
- Add a Field of Type Text and nore the fieldName e.g. studentName and Publish the Object.
- Create some Object Records.
- Build and deploy the custom module.
- Go to Control Panel > Configuration > Job Scheduler
- Click 'New' and select the custom Executor.
- Populate a name e.g. 'Student Update Scheduler' and the following custom properties (one per line in the format key=value) that are used in the custom Executor and 'Save'.
```
object.definition.erc=STUDENT
object.definition.fieldName=studentName
user.id=99999 [this should be the userId of an Active user in the Virtual Instance]
```
- Select the newly created Job from the Grid screen and switch to the 'Job Scheduler Trigger' tab.
- Set to Active, add a Cron expression for example to run it every 5 minutes use 0 */5 * ? * * and then 'Save'.
- Note the 'Next Run Date' and confirm it runs as expected. Check the Logs tab of the Job to see the Run History.
- The 'Run Now' button can be used to manually trigger the Job.

## Notes ##
- This is a ‘proof of concept’ that is being provided ‘as is’ without any support coverage or warranty.

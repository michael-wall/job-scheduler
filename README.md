## Introduction ##
- This is a sample OSGi module that contains 2 Job Schedulers:
- **UpdateObjectEntriesDispatchTaskExecutor**: Retrieves the Object Records for a specified Object Definition ERC, iterates the records and updates a specified field in each Object record using Object Local Services.
  - Custom properties determine the objectDefinition ERC used as well as the fieldName, as a result the OSGi Component code is portable.
  - The Component works for Company and Site scoped Object Definitions.
- **HeadlessUpdateObjectEntriesDispatchTaskExecutor**: Retrieves the Object Records for Employee Object, iterates the records and updates the uuid field in each Object record using headless REST APIs and OAuth 2.0

## Environment ##
- The module was built and tested with 2025.Q1.0 (Liferay Workspace gradle.properties > liferay.workspace.product = dxp-2025.q1.0-lts)
- JDK 21 is expected for compile time and runtime.

## UpdateObjectEntriesDispatchTaskExecutor Configuration ##
- Create an Object Definition with Scope set to Company and set an ERC on the Object Definition e.g. STUDENT.
- Create an Object Definition with Scope set to Site and set an ERC on the Object Definition e.g. DOCUMENT.
- Add 2 Fields of Type Text to each Object Definition (e.g. name and uuid), note the fieldNames and Publish each Object Definition.
- Create some Object Records for the Company Scoped Object Definition.
- Create some Object Records in a chosen Site for the Site Scoped Object Definition and note the Site Id of the Site.
- Build and deploy the custom OSGi module.
- Go to Control Panel > Configuration > Job Scheduler
- Click 'New' and select this custom Executor i.e. update-object-entries.
- Populate a name e.g. 'Student Update Scheduler [Company Scoped]' and the following custom properties (one per line in the format key=value using the correct values) that are used in the custom Executor and 'Save'.
```
object.definition.erc=STUDENT
object.definition.fieldName=uuid
user.id=99999 [this should be a userId of an Active user in the Virtual Instance]
```
- Select the newly created Job from the Grid screen and switch to the 'Job Scheduler Trigger' tab.
- Set to Active, add a Cron expression for example to run it every 5 minutes use 0 */5 * ? * * and then 'Save'.
- Click 'New' and select this custom Executor i.e. update-object-entries.
- Populate a name e.g. 'Document Update Scheduler [Site Scoped]' and the following custom properties (one per line in the format key=value using the correct values) that are used in the custom Executor and 'Save'.
```
object.definition.erc=DOCUMENT
object.definition.fieldName=uuid
user.id=99999 [this should be a userId of an Active user in the Virtual Instance]
group.id=12345 [this should be the Site ID of the Site the Object records were created in]
```
- Select the newly created Job from the Grid screen and switch to the 'Job Scheduler Trigger' tab.
- Set to Active, add a Cron expression for example to run it every 5 minutes use 0 */5 * ? * * and then 'Save'.
- Note the 'Next Run Date' of each Job and confirm they run as expected. The 'Run Now' button can be used to manually trigger a Job.
- Check the Logs tab of the Job to see the Run History.
- A PortalException is thrown to trigger a Job 'Fail' if any of the custom properties are missing or invalid e.g. 'doExecute user.id user not found...' or 'doExecute objectDefinition with ERC: STUDENT not found...'

## HeadlessUpdateObjectEntriesDispatchTaskExecutor Configuration ##
- Create an Object Definition with Scope set to Company called Employees with at least one field with the fieldName uuid.
- Create some Object Records.
- Create an 'OAuth 2 Administration'
  - Client Authentication Method: Client Secret Basic Or Post
  - Client Profile: Headless Server
  - Allowed Authorization Types: Client Credentials
  - Scopes Tab > Enable all options for C_Employee
- Note the Client ID and Client Secret.
- Update the clientId and clientSecret values in the getAccessToken() method.
- Update the protocol, host and port in the URL strings if needed.
- Build and deploy the custom OSGi module.
- Go to Control Panel > Configuration > Job Scheduler
- Click 'New' and select this custom Executor i.e. headless-update-object-entries.
- Populate a name e.g. 'Employee Update Scheduler [Company Scoped]' and 'Save'.
- Click 'Run Now', check the logs and the Employee records.

## Notes ##
- This is a ‘proof of concept’ that is being provided ‘as is’ without any support coverage or warranty.

## Reference ##
https://learn.liferay.com/w/dxp/liferay-development/core-frameworks/job-scheduler-framework/creating-a-new-job-scheduler-task-executor
https://learn.liferay.com/w/dxp/liferay-development/core-frameworks/job-scheduler-framework/using-job-scheduler#adding-a-new-job-scheduler-task

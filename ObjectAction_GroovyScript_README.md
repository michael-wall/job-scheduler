## Object Action > On After Add and on After Update > Groovy Script Thread samples
- The code in this sample runs in a separate thread meaning the Object Record will be created / committed without needing to wait for the script to complete.
- This sample logs basic information only.
```
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import java.util.concurrent.Callable;

def log = LogFactoryUtil.getLog("MW");

TransactionCommitCallbackUtil.registerCallback(new Callable<Void>() {
  @Override
  Void call() throws Exception {
    Thread.start {
      log.info("Background work started for entry: " + firstName);
      try {
        Thread.sleep(30000);
        log.info("Background work completed for entry: " + firstName);
      }
      catch (Exception e) {
        log.error("Error in background thread", e);
      }
    }
    return null;
  }
})
```
- The code in this sample runs in a separate thread meaning the Object Record will be created / committed without needing to wait for the script to complete.
- This sample logs the Object records to show the newly created record is included in the response while the Object Action is still running.
- Update the hardcoded companyId, Object ERC and Object fieldName before running.
- Object ERC and Object fieldName are for the current Object.
```
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import java.util.concurrent.Callable;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import java.util.List;

def log = LogFactoryUtil.getLog("MW");

TransactionCommitCallbackUtil.registerCallback(new Callable<Void>() {
  @Override
  Void call() throws Exception {
    Thread.start {
      log.info("Background work started for entry: " + firstName);
      try {
		long companyId = 77669965486364;

		ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.fetchObjectDefinitionByExternalReferenceCode("EMPLOYEE", companyId);

		if (objectDefinition != null) {
			List<ObjectEntry> objectEntries = ObjectEntryLocalServiceUtil.getObjectEntries(0, objectDefinition.getObjectDefinitionId(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);
				
			for (ObjectEntry objectEntry: objectEntries) {
				String fieldValue = (String)objectEntry.getValues().get("firstName");
				log.info(fieldValue);
			}
		}		
		
		Thread.sleep(30000);
		
        log.info("Background work completed for entry: " + firstName);
      }
      catch (Exception e) {
        log.error("Error in background thread", e);
      }
    }
    return null;
  }
})

```
- The code in this sample runs in a separate thread meaning the Object Record will be created / committed without needing to wait for the script to complete.
- This sample updates a field value in all records in another Object.
- Update the hardcoded companyId, userId, Object ERC and Object fieldName before running.
- Object ERC and Object fieldName are for another Object.
```
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import java.util.concurrent.Callable;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import java.util.List;

def log = LogFactoryUtil.getLog("MW");

TransactionCommitCallbackUtil.registerCallback(new Callable<Void>() {
  @Override
  Void call() throws Exception {
    Thread.start {
      log.info("Background work started for entry: " + firstName);
      try {
		long companyId = 77669965486364;
		long userId = 20123;
    long updateCount = 0;

		ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.fetchObjectDefinitionByExternalReferenceCode("STUDENT", companyId);

		if (objectDefinition != null) {
			List<ObjectEntry> objectEntries = ObjectEntryLocalServiceUtil.getObjectEntries(0, objectDefinition.getObjectDefinitionId(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);
				
			for (ObjectEntry objectEntry: objectEntries) {
				ObjectEntry latestObjectEntry = ObjectEntryLocalServiceUtil.fetchObjectEntry(objectEntry.getObjectEntryId());

				String fieldValue = (String)latestObjectEntry.getValues().get("studentName");
				
				latestObjectEntry.getValues().put("studentName", fieldValue += "y");
				
				log.info("objectEntryId: " + latestObjectEntry.getObjectEntryId() + ", Current MVCC: " + latestObjectEntry.getMvccVersion() + ", newFieldValue: " + fieldValue);
				
				ObjectEntryLocalServiceUtil.updateObjectEntry(userId, latestObjectEntry.getObjectEntryId(), latestObjectEntry.getValues(), new ServiceContext());

        updateCount ++;
			}
		}		
		
		Thread.sleep(30000);
		
        log.info("Background work completed for entry: " + firstName + ", updateCount: " + updateCount);
      }
      catch (Exception e) {
        log.error("Error in background thread", e);
      }
    }
    return null;
  }
})
```

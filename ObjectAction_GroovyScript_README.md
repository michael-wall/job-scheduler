## Object Action > On After Add and on After Update > Groovy Script Thread samples
- The code in this sample runs in a separate thread meaning the Object Record will be created / committed without needing to wait for the script to complete.
- This sample logs basic information only.
```
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import java.util.concurrent.Callable;

def _log = LogFactoryUtil.getLog("MW");

TransactionCommitCallbackUtil.registerCallback(new Callable<Void>() {
  @Override
  Void call() throws Exception {
    Thread.start {
      _log.info("Background work started ...");
      try {
        Thread.sleep(30000);
        _log.info("Background work completed ...");
      }
      catch (Exception e) {
        _log.error("Error in background thread", e);
      }
    }
    return null;
  }
})
```
- The code in this sample runs in a separate thread meaning the Object Record will be created / committed without needing to wait for the script to complete.
- This sample logs the Object records to show the newly created record is included in the response while the Object Action is still running.
- Update the hardcoded objectDefinitionERC and objectDefinitionFieldName before running.
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
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;

def _log = LogFactoryUtil.getLog("MW");
ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();

String objectDefinitionERC = "EMPLOYEE";
String objectDefinitionFieldName = "firstName"

TransactionCommitCallbackUtil.registerCallback(new Callable<Void>() {
  @Override
  Void call() throws Exception {
    Thread.start {
      _log.info("Background work started ...");
      try {
		ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.fetchObjectDefinitionByExternalReferenceCode(objectDefinitionERC, serviceContext.getCompanyId());

		if (objectDefinition != null) {
			List<ObjectEntry> objectEntries = ObjectEntryLocalServiceUtil.getObjectEntries(0, objectDefinition.getObjectDefinitionId(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);
				
			for (ObjectEntry objectEntry: objectEntries) {
				String fieldValue = (String)objectEntry.getValues().get(objectDefinitionFieldName);
				_log.info(fieldValue);
			}
		}		
		
		Thread.sleep(30000);
		
        _log.info("Background work completed ...");
      }
      catch (Exception e) {
        _log.error("Error in background thread", e);
      }
    }
    return null;
  }
})
```
- The code in this sample runs in a separate thread meaning the Object Record will be created / committed without needing to wait for the script to complete.
- This sample updates a field value in all records in another Object.
- Update the hardcoded objectDefinitionERC and objectDefinitionFieldName before running.
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
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import java.util.List;
import java.util.UUID;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

def _log = LogFactoryUtil.getLog("MW");
ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();

String objectDefinitionERC = "STUDENT";
String objectDefinitionFieldName = "uuid";

TransactionCommitCallbackUtil.registerCallback(new Callable<Void>() {
  @Override
  Void call() throws Exception {
    Thread.start {
      _log.info("Background work started ...");
      try {
    	long updateCount = 0;

		ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.fetchObjectDefinitionByExternalReferenceCode(objectDefinitionERC, serviceContext.getCompanyId());

		if (objectDefinition != null) {
			List<ObjectEntry> objectEntries = ObjectEntryLocalServiceUtil.getObjectEntries(0, objectDefinition.getObjectDefinitionId(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);
				
			for (ObjectEntry objectEntry: objectEntries) {
				ObjectEntry latestObjectEntry = ObjectEntryLocalServiceUtil.fetchObjectEntry(objectEntry.getObjectEntryId());
				
				String oldFieldValue = (String)latestObjectEntry.getValues().get(objectDefinitionFieldName);
				String newFieldValue = UUID.randomUUID().toString();
							
				_log.info("objectEntryId: " + latestObjectEntry.getObjectEntryId() + ", Current MVCC: " + latestObjectEntry.getMvccVersion()  + ", oldFieldValue: " + oldFieldValue + ", newFieldValue: " + newFieldValue);
				
				Map<String, Serializable> updatedValues = new HashMap<String, Serializable>();
				
				updatedValues.put(objectDefinitionFieldName, newFieldValue);
				
				ObjectEntryLocalServiceUtil.updateObjectEntry(serviceContext.getUserId(), latestObjectEntry.getObjectEntryId(), updatedValues, new ServiceContext());
				
				updateCount ++;
			}
		}		
		
		Thread.sleep(30000);
		
        _log.info("Background work completed ..., updateCount: " + updateCount);
      }
      catch (Exception e) {
      	_log.error("Error in background thread", e);
      }
    }
    return null;
  }
})
```

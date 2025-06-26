package com.mw.job.scheduler;

import com.liferay.dispatch.executor.BaseDispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutorOutput;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	property = {
		"dispatch.task.executor.name=mw", "dispatch.task.executor.type=mw"
	},
	service = DispatchTaskExecutor.class
)
public class MWDispatchTaskExecutor extends BaseDispatchTaskExecutor {
	private interface JOB_PROPERTIES {
		static final String OBJECT_DEFINITION_ERC = "object.definition.erc";
		static final String OBJECT_DEFINITION_FIELD_NAME = "object.definition.fieldName";
		static final String USER_ID = "user.id";
	}
	
	@Override
	public String getName() {
		return "mw";
	}	
	
	@Activate
	protected void activate(Map<String, Object> properties)  throws Exception {
		_log.info("activate");
	}

	@Override
	public void doExecute(
			DispatchTrigger dispatchTrigger,
			DispatchTaskExecutorOutput dispatchTaskExecutorOutput)
		throws IOException, PortalException {

		_log.info("doExecute starting");
		
		UnicodeProperties properties = dispatchTrigger.getDispatchTaskSettingsUnicodeProperties();		
		
		// Reading known properties from the Job Details screen properties.
		String objectDefinitionERC = properties.getProperty(JOB_PROPERTIES.OBJECT_DEFINITION_ERC, null);
		String objectDefinitionFieldName = properties.getProperty(JOB_PROPERTIES.OBJECT_DEFINITION_FIELD_NAME, null);
		String userId = properties.getProperty(JOB_PROPERTIES.USER_ID, null);
		
		_log.info("doExecute " + JOB_PROPERTIES.OBJECT_DEFINITION_ERC + ": " + objectDefinitionERC);
		_log.info("doExecute " + JOB_PROPERTIES.OBJECT_DEFINITION_FIELD_NAME + ": " + objectDefinitionFieldName);
		_log.info("doExecute " + JOB_PROPERTIES.USER_ID + ": " + userId);
		
		_log.info("doExecute properties start");
		
		_log.info("doExecute properties start");
		Set<Entry<String, String>> entries = properties.entrySet();
		
		// Log all of the properties...
        for (Map.Entry<String, String> entry : entries) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        _log.info("doExecute properties end");
		
        if (Validator.isNull(objectDefinitionERC)) {
			_log.info("doExecute " + JOB_PROPERTIES.OBJECT_DEFINITION_ERC + " property is null...");
			
			throw new PortalException("doExecute " + JOB_PROPERTIES.OBJECT_DEFINITION_ERC + " property is null...");
		}
        
        if (Validator.isNull(objectDefinitionFieldName)) {
			_log.info("doExecute " + JOB_PROPERTIES.OBJECT_DEFINITION_FIELD_NAME + " property is null...");
			
			throw new PortalException("doExecute " + JOB_PROPERTIES.OBJECT_DEFINITION_FIELD_NAME + " property is null...");
		}
        
        User user = null;
        
        if (Validator.isNull(userId) || !isValidLong(userId)) {
			_log.info("doExecute " + JOB_PROPERTIES.USER_ID + " property is null or invalid...");
			
			throw new PortalException("doExecute " + JOB_PROPERTIES.USER_ID + " property is null or invalid...");
		} else {
			user = userLocalService.fetchUser(Long.parseLong(userId));
			
			if (user == null) {			
				_log.info("doExecute " + JOB_PROPERTIES.USER_ID + " user not found...");
				
				throw new PortalException("doExecute " + JOB_PROPERTIES.USER_ID + " user not found...");
			}
		}
		
		ObjectDefinition objectDefinition = objectDefinitionLocalService.fetchObjectDefinitionByExternalReferenceCode(objectDefinitionERC, dispatchTrigger.getCompanyId());
		
		if (objectDefinition == null) {
			_log.info("doExecute objectDefinition with ERC: " + objectDefinitionERC + " not found...");
			
			throw new PortalException("doExecute objectDefinition with ERC: " + objectDefinitionERC + " not found...");
		}
		
		List<ObjectEntry> objectEntries = objectEntryLocalService.getObjectEntries(0, objectDefinition.getObjectDefinitionId(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);
		
		for (ObjectEntry objectEntry: objectEntries) {
			ObjectEntry latestObjectEntry = objectEntryLocalService.fetchObjectEntry(objectEntry.getObjectEntryId());

			String studentName = (String)latestObjectEntry.getValues().get(objectDefinitionFieldName);
			
			latestObjectEntry.getValues().put(objectDefinitionFieldName, studentName += "x");
			
			_log.info(studentName + ", " + latestObjectEntry.getObjectEntryId());
			
			objectEntryLocalService.updateObjectEntry(user.getUserId(), latestObjectEntry.getObjectEntryId(), latestObjectEntry.getValues(), new ServiceContext());
		}
		
		_log.info("doExecute completed");
	}
	
	private boolean isValidLong(String longString) {
		if (Validator.isName(longString)) return false;
		
	    try {
	        Long.parseLong(longString.trim());
	        
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	
	@Reference(unbind = "-")
	private ObjectEntryLocalService objectEntryLocalService;
	
	@Reference(unbind = "-")
	private ObjectDefinitionLocalService objectDefinitionLocalService;
	
	@Reference(unbind = "-")
	private UserLocalService userLocalService;

	private static final Log _log = LogFactoryUtil.getLog(MWDispatchTaskExecutor.class);
}
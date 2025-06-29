package com.mw.job.scheduler;

import com.liferay.dispatch.executor.BaseDispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutorOutput;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(
	property = {
		"dispatch.task.executor.name=headless-update-object-entries", "dispatch.task.executor.type=headless-update-object-entries"
	},		
	service = DispatchTaskExecutor.class
)
public class HeadlessUpdateObjectEntriesDispatchTaskExecutor extends BaseDispatchTaskExecutor {
	
	private static final String oAuthClientId = "id-d589ec63-720e-0592-e409-97582cb79d";  //TODO Externalize to config, store securely...
	private static final String oAuthClientSecret = "secret-aef95246-77cd-4479-61e4-d441287424a"; //TODO Externalize to config, store securely...	
	private static final String hostname = "http://localhost:8080";   //TODO Externalize to config
	private String objectPath = "employees";
	private String objectFieldName = "uuid";

	@Override
	public String getName() {
		return "headless-update-object-entries";
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

	        _log.info("Starting...");

	        String accessToken = getOAuthAccessToken();
	        
	        if (Validator.isNull(accessToken)) {
	  			_log.info("accessToken null, unable to proceed...");
				
				throw new PortalException("accessToken null, unable to proceed...");
	        }

	        Http.Options options = new Http.Options();
	        options.setLocation(hostname + "/o/c/" + objectPath + "/");
	        
	        Map<String, String> headers = HashMapBuilder.put("Authorization", "Bearer " + accessToken).put("Content-Type", "application/json").put("Accept", "application/json").build();

            options.setHeaders(headers);

	        String jsonResponse = HttpUtil.URLtoString(options);

	        JSONObject responseObject = JSONFactoryUtil.createJSONObject(jsonResponse);
	        JSONArray items = responseObject.getJSONArray("items");

	        for (int i = 0; i < items.length(); i++) {
	            JSONObject objectRecordJsonObject = items.getJSONObject(i);

	            long id = objectRecordJsonObject.getLong("id");
	            _log.info("Object Record ID: " + id);

	            String patchUrl = hostname + "/o/c/" + objectPath + "/" + id;

	            Http.Options patchOptions = new Http.Options();
	            patchOptions.setLocation(patchUrl);
	            patchOptions.setPatch(true);
	            
	            String newUUID = UUID.randomUUID().toString();
	            
	            String patchBody = "{\"" + objectFieldName + "\": \"" + newUUID + "\"}";
	            
	            patchOptions.setBody(patchBody, "application/json", "UTF-8");
	            
	            patchOptions.setHeaders(headers);

	            String patchResponse = HttpUtil.URLtoString(patchOptions);   
	        }

	        _log.info("Finishing...");
	    }

	    private String getOAuthAccessToken() {
	        String tokenEndpoint = hostname + "/o/oauth2/token";

	        Map<String, String> headers = HashMapBuilder.put("Content-Type", "application/x-www-form-urlencoded").build();
	  
	        Http.Options options = new Http.Options();
	        options.setLocation(tokenEndpoint);
	        options.setPost(true);
	        options.setHeaders(headers);
	        options.setBody(
	                "grant_type=client_credentials&client_id=" + oAuthClientId +
	                        "&client_secret=" + oAuthClientSecret,
	                "application/x-www-form-urlencoded", "UTF-8");

	        try {
				String response = HttpUtil.URLtoString(options);
				JSONObject responseObject = JSONFactoryUtil.createJSONObject(response);

				return responseObject.getString("access_token");
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
	        return null;
	    }
		
	    @Reference
	    private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;


	    private static final Log _log = LogFactoryUtil.getLog(
	    		HeadlessUpdateObjectEntriesDispatchTaskExecutor.class);
	}
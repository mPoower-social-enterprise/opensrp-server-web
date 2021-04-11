package org.opensrp.web.rest;

import static java.text.MessageFormat.format;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.web.utils.Utils;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

@Controller
@RequestMapping(value = "/rest/validate/mhealth")
public class MhealthValidateResource {
	
	private static Logger logger = LogManager.getLogger(MhealthValidateResource.class.toString());
	
	private MhealthClientService mhealthClientService;
	
	private MhealthEventService mhealthEventService;
	
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	@Autowired
	public MhealthValidateResource(MhealthClientService mhealthClientService, MhealthEventService mhealthEventService) {
		
		this.mhealthClientService = mhealthClientService;
		this.mhealthEventService = mhealthEventService;
	}
	
	/**
	 * Validate that the client and event ids reference actual documents
	 * 
	 * @param data
	 * @return
	 */
	@RequestMapping(headers = { "Accept=application/json" }, method = POST, value = "/sync")
	public ResponseEntity<String> validateSync(@RequestBody String data, HttpServletRequest request) {
		Map<String, Object> response = new HashMap<String, Object>();
		String district = getStringFilter("district", request);
		String postfix = "_" + district;
		
		try {
			if (StringUtils.isBlank(data)) {
				return new ResponseEntity<>(BAD_REQUEST);
			}
			JSONObject syncData = new JSONObject(data);
			if (!syncData.has("clients") && !syncData.has("events")) {
				return new ResponseEntity<>(BAD_REQUEST);
			}
			
			List<String> missingClientIds = new ArrayList<>();
			if (syncData.has("clients")) {
				List<String> clientIds = gson.fromJson(Utils.getStringFromJSON(syncData, "clients"),
				    new TypeToken<ArrayList<String>>() {}.getType());
				for (String clientId : clientIds) {
					try {
						
						Integer getClientId = mhealthClientService.findClientIdByBaseEntityId(clientId, postfix);
						if (getClientId == null || getClientId == 0) {
							missingClientIds.add(clientId);
						}
					}
					catch (Exception e) {
						logger.error("Client Sync Valiation Failed, BaseEntityId: " + clientId, e);
					}
				}
			}
			
			List<String> missingEventIds = new ArrayList<>();
			if (syncData.has("events")) {
				List<String> eventIds = gson.fromJson(Utils.getStringFromJSON(syncData, "events"),
				    new TypeToken<ArrayList<String>>() {}.getType());
				for (String eventId : eventIds) {
					try {
						Integer getEventId = mhealthEventService.findEventIdByFormSubmissionId(eventId, postfix);
						if (getEventId == null || getEventId == 0) {
							missingEventIds.add(eventId);
						}
						
					}
					catch (Exception e) {
						logger.error("Event Sync Valiation Failed, FormSubmissionId: " + eventId, e);
					}
				}
			}
			
			JsonArray clientsArray = (JsonArray) gson.toJsonTree(missingClientIds,
			    new TypeToken<List<String>>() {}.getType());
			
			JsonArray eventsArray = (JsonArray) gson.toJsonTree(missingEventIds, new TypeToken<List<String>>() {}.getType());
			
			response.put("events", eventsArray);
			response.put("clients", clientsArray);
			
			return new ResponseEntity<>(gson.toJson(response), RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
			
		}
		catch (Exception e) {
			logger.error(format("Validation Sync failed data processing failed with exception {0}.- ", e));
			response.put("msg", "Error occurred");
			return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
	}
}

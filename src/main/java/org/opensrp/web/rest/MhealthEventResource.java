package org.opensrp.web.rest;

import static java.text.MessageFormat.format;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.opensrp.web.rest.RestUtils.getIntegerFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.search.EventSearchBean;
import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.web.bean.EventSyncBean;
import org.opensrp.web.utils.Utils;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

@Controller
@RequestMapping(value = "/rest/event/mhealth")
public class MhealthEventResource {
	
	private static Logger logger = LogManager.getLogger(MhealthEventResource.class.toString());
	
	private MhealthClientService mhealthClientService;
	
	private MhealthEventService mhealthEventService;
	
	Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	@Value("#{opensrp['opensrp.sync.search.missing.client']}")
	private boolean searchMissingClients;
	
	protected ObjectMapper objectMapper;
	
	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	};
	
	@Autowired
	public MhealthEventResource(MhealthClientService mhealthClientService, MhealthEventService mhealthEventService) {
		
		this.mhealthClientService = mhealthClientService;
		this.mhealthEventService = mhealthEventService;
	}
	
	public void setMhealthClientService(MhealthClientService mhealthClientService) {
		this.mhealthClientService = mhealthClientService;
	}
	
	public void setMhealthEventService(MhealthEventService mhealthEventService) {
		this.mhealthEventService = mhealthEventService;
	}
	
	/**
	 * Fetch events ordered by serverVersion ascending order and return the clients associated with
	 * the events
	 *
	 * @param request
	 * @return a map response with events, clients and optionally msg when an error occurs
	 */
	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	protected ResponseEntity<String> sync(HttpServletRequest request) throws JsonProcessingException {
		EventSyncBean response = new EventSyncBean();
		try {
			String providerId = getStringFilter(PROVIDER_ID, request);
			String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
			Integer limit = getIntegerFilter("limit", request);
			String district = getStringFilter("district", request);
			
			String postfix = "_" + district;
			String villageIds = getStringFilter("villageIds", request);
			String isEmptyToAdd = getStringFilter("isEmptyToAdd", request);
			
			if (org.apache.commons.lang3.StringUtils.isBlank(isEmptyToAdd)) {
				isEmptyToAdd = "true";
			}
			List<Long> villageIdsList = new ArrayList<>();
			if (villageIds != null && !org.apache.commons.lang3.StringUtils.isBlank(villageIds)) {
				
				for (String locId : villageIds.split(",")) {
					villageIdsList.add(Long.valueOf(locId));
				}
			}
			
			if (providerId != null || villageIdsList.size() != 0) {
				
				EventSyncBean eventSyncBean = sync(providerId, postfix, serverVersion, villageIdsList, limit, isEmptyToAdd);
				
				HttpHeaders headers = RestUtils.getJSONUTF8Headers();
				return new ResponseEntity<>(objectMapper.writeValueAsString(eventSyncBean), headers, HttpStatus.OK);
				
			} else {
				response.setMsg("specify atleast one filter");
				return new ResponseEntity<>(objectMapper.writeValueAsString(response), BAD_REQUEST);
			}
			
		}
		catch (Exception e) {
			response.setMsg("Error occurred");
			logger.error("", e);
			return new ResponseEntity<>(objectMapper.writeValueAsString(response), INTERNAL_SERVER_ERROR);
		}
	}
	
	public EventSyncBean sync(String providerId, String postfix, String serverVersion, List<Long> villageIdsList,
	                          Integer limit, String isEmptyToAdd) {
		Long lastSyncedServerVersion = null;
		if (serverVersion != null) {
			lastSyncedServerVersion = Long.parseLong(serverVersion) + 1;
		}
		
		EventSearchBean eventSearchBean = new EventSearchBean();
		
		eventSearchBean.setProviderId(providerId);
		
		eventSearchBean.setBaseEntityId("");
		eventSearchBean.setServerVersion(lastSyncedServerVersion);
		return getEventsAndClients(providerId, postfix, villageIdsList, lastSyncedServerVersion,
		    limit == null || limit == 0 ? 25 : limit, isEmptyToAdd);
		
	}
	
	private EventSyncBean getEventsAndClients(String providerId, String postfix, List<Long> villageIdsList,
	                                          Long lastSyncedServerVersion, Integer limit, String isEmptyToAdd) {
		List<Event> events = new ArrayList<Event>();
		List<String> clientIds = new ArrayList<String>();
		List<Client> clients = new ArrayList<Client>();
		long startTime = System.currentTimeMillis();
		if (isEmptyToAdd.equalsIgnoreCase("true")) {
			events = mhealthEventService.findByVillageIds(providerId, villageIdsList, lastSyncedServerVersion, limit,
			    postfix);
		} else {
			events = mhealthEventService.findByProvider(lastSyncedServerVersion, providerId, 0, postfix);
		}
		
		Long totalRecords = 0l;
		logger.info("fetching events took: " + (System.currentTimeMillis() - startTime));
		if (!events.isEmpty()) {
			for (Event event : events) {
				if (org.apache.commons.lang.StringUtils.isNotBlank(event.getBaseEntityId())
				        && !clientIds.contains(event.getBaseEntityId())) {
					clientIds.add(event.getBaseEntityId());
				}
			}
		}
		
		clients = mhealthClientService.findByBaseEntityIds(clientIds, postfix);
		
		EventSyncBean eventSyncBean = new EventSyncBean();
		eventSyncBean.setClients(clients);
		eventSyncBean.setEvents(events);
		eventSyncBean.setNoOfEvents(events.size());
		eventSyncBean.setTotalRecords(totalRecords);
		return eventSyncBean;
	}
	
	@RequestMapping(headers = { "Accept=application/json" }, method = POST, value = "/add")
	public ResponseEntity<String> save(@RequestBody String data, Authentication authentication, HttpServletRequest request)
	    throws JsonProcessingException {
		
		List<String> failedClientsIds = new ArrayList<>();
		List<String> failedEventIds = new ArrayList<>();
		String district = getStringFilter("district", request);
		String division = getStringFilter("division", request);
		String branch = getStringFilter("branch", request);
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			JSONObject syncData = new JSONObject(data);
			if (!syncData.has("clients") && !syncData.has("events")) {
				return new ResponseEntity<>(BAD_REQUEST);
			}
			
			if (syncData.has("clients")) {
				ArrayList<Client> clients = gson.fromJson(Utils.getStringFromJSON(syncData, "clients"),
				    new TypeToken<ArrayList<Client>>() {}.getType());
				for (Client client : clients) {
					try {
						mhealthClientService.addOrUpdate(client, district, division, branch);
					}
					catch (Exception e) {
						logger.error(
						    "Client" + client.getBaseEntityId() == null ? "" : client.getBaseEntityId() + " failed to sync",
						    e);
						failedClientsIds.add(client.getId());
					}
				}
				
			}
			if (syncData.has("events")) {
				ArrayList<Event> events = gson.fromJson(Utils.getStringFromJSON(syncData, "events"),
				    new TypeToken<ArrayList<Event>>() {}.getType());
				for (Event event : events) {
					try {
						mhealthEventService.addorUpdateEvent(event, RestUtils.currentUser(authentication).getUsername(),
						    district, division, branch);
					}
					catch (Exception e) {
						logger.error(
						    "Event of type " + event.getEventType() + " for client " + event.getBaseEntityId() == null ? ""
						            : event.getBaseEntityId() + " failed to sync",
						    e);
						failedEventIds.add(event.getId());
					}
				}
			}
			
		}
		catch (
		
		Exception e) {
			logger.error(format("Sync data processing failed with exception {0}.- ", e));
			return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
		}
		if (failedClientsIds.isEmpty() && failedEventIds.isEmpty()) {
			return new ResponseEntity<>(CREATED);
		} else {
			JsonArray clientsArray = (JsonArray) gson.toJsonTree(failedClientsIds,
			    new TypeToken<List<String>>() {}.getType());
			
			JsonArray eventsArray = (JsonArray) gson.toJsonTree(failedEventIds, new TypeToken<List<String>>() {}.getType());
			
			response.put("failed_events", eventsArray);
			response.put("failed_clients", clientsArray);
			return new ResponseEntity<>(gson.toJson(response), CREATED);
		}
	}
	
}

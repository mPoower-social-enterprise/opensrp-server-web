package org.opensrp.web.rest;

import static java.text.MessageFormat.format;
import static org.opensrp.common.AllConstants.CLIENTS_FETCH_BATCH_SIZE;
import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;
import static org.opensrp.common.AllConstants.BaseEntity.LAST_UPDATE;
import static org.opensrp.common.AllConstants.Event.ENTITY_TYPE;
import static org.opensrp.common.AllConstants.Event.EVENT_DATE;
import static org.opensrp.common.AllConstants.Event.EVENT_TYPE;
import static org.opensrp.common.AllConstants.Event.LOCATION_ID;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.opensrp.common.AllConstants.Event.TEAM;
import static org.opensrp.common.AllConstants.Event.TEAM_ID;
import static org.opensrp.web.Constants.RETURN_COUNT;
import static org.opensrp.web.Constants.TOTAL_RECORDS;
import static org.opensrp.web.rest.RestUtils.getDateRangeFilter;
import static org.opensrp.web.rest.RestUtils.getIntegerFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.text.ParseException;
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
import org.opensrp.api.domain.User;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.search.EventSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.MultimediaService;
import org.opensrp.web.bean.EventSyncBean;
import org.opensrp.web.config.Role;
import org.opensrp.web.utils.MaskingUtils;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

@Controller
@RequestMapping(value = "/rest/event/mhealth")
public class MhealthEventResource extends RestResource<Event> {
	
	private static Logger logger = LogManager.getLogger(MhealthEventResource.class.toString());
	
	private EventService eventService;
	
	private ClientService clientService;
	
	private MultimediaService multimediaService;
	
	Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	@Value("#{opensrp['opensrp.sync.search.missing.client']}")
	private boolean searchMissingClients;
	
	private static final String IS_DELETED = "is_deleted";
	
	private static final String FALSE = "false";
	
	private static final String SAMPLE_CSV_FILE = "/";
	
	@Autowired
	public MhealthEventResource(ClientService clientService, EventService eventService,
	    MultimediaService multimediaService) {
		this.clientService = clientService;
		this.eventService = eventService;
		this.multimediaService = multimediaService;
	}
	
	@Override
	public Event getByUniqueId(String uniqueId) {
		return eventService.find(uniqueId);
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
			String locationId = getStringFilter(LOCATION_ID, request);
			String baseEntityId = getStringFilter(BASE_ENTITY_ID, request);
			String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
			String team = getStringFilter(TEAM, request);
			String teamId = getStringFilter(TEAM_ID, request);
			Integer limit = getIntegerFilter("limit", request);
			boolean returnCount = Boolean.getBoolean(getStringFilter(RETURN_COUNT, request));
			
			if (team != null || providerId != null || locationId != null || baseEntityId != null || teamId != null) {
				
				EventSyncBean eventSyncBean = sync(providerId, locationId, baseEntityId, serverVersion, team, teamId, limit,
				    returnCount);
				
				HttpHeaders headers = RestUtils.getJSONUTF8Headers();
				if (returnCount) {
					headers.add(TOTAL_RECORDS, String.valueOf(eventSyncBean.getTotalRecords()));
				}
				
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
	
	private void searchMissingClients(List<String> clientIds, List<Client> clients, long startTime) {
		if (searchMissingClients) {
			
			List<String> foundClientIds = new ArrayList<>();
			for (Client client : clients) {
				foundClientIds.add(client.getBaseEntityId());
			}
			
			boolean removed = clientIds.removeAll(foundClientIds);
			if (removed) {
				for (String clientId : clientIds) {
					Client client = clientService.getByBaseEntityId(clientId);
					if (client != null) {
						clients.add(client);
					}
				}
			}
			logger.info("fetching missing clients took: " + (System.currentTimeMillis() - startTime));
		}
	}
	
	public EventSyncBean sync(String providerId, String locationId, String baseEntityId, String serverVersion, String team,
	                          String teamId, Integer limit, boolean returnCount) {
		Long lastSyncedServerVersion = null;
		if (serverVersion != null) {
			lastSyncedServerVersion = Long.parseLong(serverVersion) + 1;
		}
		
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setTeam(team);
		eventSearchBean.setTeamId(teamId);
		eventSearchBean.setProviderId(providerId);
		eventSearchBean.setLocationId(locationId);
		eventSearchBean.setBaseEntityId(baseEntityId);
		eventSearchBean.setServerVersion(lastSyncedServerVersion);
		
		return getEventsAndClients(eventSearchBean, limit == null || limit == 0 ? 25 : limit, returnCount);
		
	}
	
	private EventSyncBean getEventsAndClients(EventSearchBean eventSearchBean, Integer limit, boolean returnCount) {
		List<Event> events = new ArrayList<Event>();
		List<String> clientIds = new ArrayList<String>();
		List<Client> clients = new ArrayList<Client>();
		long startTime = System.currentTimeMillis();
		events = eventService.findEvents(eventSearchBean, BaseEntity.SERVER_VERSIOIN, "asc", limit == null ? 25 : limit);
		Long totalRecords = 0l;
		logger.info("fetching events took: " + (System.currentTimeMillis() - startTime));
		if (!events.isEmpty()) {
			for (Event event : events) {
				if (org.apache.commons.lang.StringUtils.isNotBlank(event.getBaseEntityId())
				        && !clientIds.contains(event.getBaseEntityId())) {
					clientIds.add(event.getBaseEntityId());
				}
			}
			for (int i = 0; i < clientIds.size(); i = i + CLIENTS_FETCH_BATCH_SIZE) {
				int end = Math.min(i + CLIENTS_FETCH_BATCH_SIZE, clientIds.size());
				clients.addAll(clientService.findByFieldValue(BASE_ENTITY_ID, clientIds.subList(i, end)));
			}
			logger.info("fetching clients took: " + (System.currentTimeMillis() - startTime));
			
			searchMissingClients(clientIds, clients, startTime);
			
			if (returnCount) {
				totalRecords = eventService.countEvents(eventSearchBean);
			}
			
		}
		
		//PII Data masking 
		//TO DO research on ways to improve this
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = RestUtils.currentUser(authentication);
		
		if (Utils.checkRoleIfRoleExists(user.getRoles(), Role.PII_DATA_MASK)) {
			
			MaskingUtils maskingUtil = new MaskingUtils();
			maskingUtil.processDataMasking(clients);
		}
		
		EventSyncBean eventSyncBean = new EventSyncBean();
		eventSyncBean.setClients(clients);
		eventSyncBean.setEvents(events);
		eventSyncBean.setNoOfEvents(events.size());
		eventSyncBean.setTotalRecords(totalRecords);
		return eventSyncBean;
	}
	
	@RequestMapping(headers = { "Accept=application/json" }, method = POST, value = "/add")
	public ResponseEntity<String> save(@RequestBody String data, Authentication authentication)
	    throws JsonProcessingException {
		
		List<String> failedClientsIds = new ArrayList<>();
		List<String> failedEventIds = new ArrayList<>();
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
						clientService.addorUpdate(client);
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
						event = eventService.processOutOfArea(event);
						eventService.addorUpdateEvent(event, RestUtils.currentUser(authentication).getUsername());
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
	
	@Override
	public Event create(Event o) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return eventService.addEvent(o, RestUtils.currentUser(authentication).getUsername());
	}
	
	@Override
	public List<String> requiredProperties() {
		List<String> p = new ArrayList<>();
		p.add(BASE_ENTITY_ID);
		p.add(EVENT_TYPE);
		p.add(PROVIDER_ID);
		return p;
	}
	
	@Override
	public Event update(Event entity) {
		return eventService.mergeEvent(entity);
	}
	
	@Override
	public List<Event> search(HttpServletRequest request) throws ParseException {
		String clientId = getStringFilter("identifier", request);
		DateTime[] eventDate = getDateRangeFilter(EVENT_DATE, request);
		String eventType = getStringFilter(EVENT_TYPE, request);
		String location = getStringFilter(LOCATION_ID, request);
		String provider = getStringFilter(PROVIDER_ID, request);
		String entityType = getStringFilter(ENTITY_TYPE, request);
		DateTime[] lastEdit = getDateRangeFilter(LAST_UPDATE, request);
		String team = getStringFilter(TEAM, request);
		String teamId = getStringFilter(TEAM_ID, request);
		
		if (!StringUtils.isBlank(clientId)) {
			Client c = clientService.find(clientId);
			if (c == null) {
				return new ArrayList<>();
			}
			
			clientId = c.getBaseEntityId();
		}
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setBaseEntityId(clientId);
		eventSearchBean.setEventDateFrom(eventDate == null ? null : eventDate[0]);
		eventSearchBean.setEventDateTo(eventDate == null ? null : eventDate[1]);
		eventSearchBean.setEventType(eventType);
		eventSearchBean.setEntityType(entityType);
		eventSearchBean.setProviderId(provider);
		eventSearchBean.setLocationId(location);
		eventSearchBean.setLastEditFrom(lastEdit == null ? null : lastEdit[0]);
		eventSearchBean.setLastEditTo(lastEdit == null ? null : lastEdit[1]);
		eventSearchBean.setTeam(team);
		eventSearchBean.setTeamId(teamId);
		
		return eventService.findEventsBy(eventSearchBean);
	}
	
	@Override
	public List<Event> filter(String query) {
		return eventService.findEventsByDynamicQuery(query);
	}
	
}

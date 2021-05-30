package org.opensrp.web.controller;

import static org.opensrp.web.rest.RestUtils.getIntegerFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.postgres.MhealthEventMetadata;
import org.opensrp.domain.postgres.MhealthMigration;
import org.opensrp.domain.postgres.MhealthPractitionerLocation;
import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.service.MhealthMigrationService;
import org.opensrp.service.PractitionerLocationService;
import org.smartregister.domain.Client;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

@Controller
public class MhealthMigrationController {
	
	private MhealthMigrationService mhealthMigrationService;
	
	private MhealthEventService mhealthEventService;
	
	private PractitionerLocationService practitionerLocationService;
	
	private MhealthClientService mhealthClientService;
	
	Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	@Autowired
	public void setWebNotificationService(MhealthMigrationService mhealthMigrationService,
	                                      MhealthEventService mhealthEventService, MhealthClientService mhealthClientService,
	                                      PractitionerLocationService practitionerLocationService) {
		this.mhealthMigrationService = mhealthMigrationService;
		this.mhealthEventService = mhealthEventService;
		this.mhealthClientService = mhealthClientService;
		this.practitionerLocationService = practitionerLocationService;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, method = RequestMethod.POST, value = "/migrate")
	@ResponseBody
	
	public ResponseEntity<String> doMigrate(@RequestBody String data, HttpServletRequest request) throws JSONException {
		String district = getStringFilter("districtId", request);
		String inProvider = request.getRemoteUser();
		String division = getStringFilter("divisionId", request);
		String branch = getStringFilter("branchId", request);
		String type = getStringFilter("type", request);
		JSONObject syncData = new JSONObject(data);
		ArrayList<Client> clients = new ArrayList<Client>();
		if (syncData.has("clients")) {
			clients = (ArrayList<Client>) gson.fromJson(syncData.getString("clients"),
			    new TypeToken<ArrayList<Client>>() {}.getType());
		}
		Client inclient = clients.get(0);
		MhealthPractitionerLocation outUserLocation = new MhealthPractitionerLocation();
		outUserLocation.setBranch(branch);
		outUserLocation.setDistrict(district);
		outUserLocation.setDivision(division);
		String postfix = "";
		if (!StringUtils.isBlank(district)) {
			postfix = "_" + district;
		}
		outUserLocation.setPostFix(postfix);
		outUserLocation.setUsername(inProvider);
		String baseEntityId = inclient.getBaseEntityId();
		MhealthEventMetadata mhealthEventMetadata = mhealthEventService.findFirstEventMetadata(baseEntityId,
		    outUserLocation.getPostFix());
		MhealthMigration existingMigration = mhealthMigrationService.findFirstMigrationBybaseEntityId(baseEntityId);
		String outProvider = "";
		if (existingMigration != null) {
			outProvider = existingMigration.getSKIn();
		} else {
			outProvider = mhealthEventMetadata.getProviderId();
		}
		MhealthPractitionerLocation inUserLocation = practitionerLocationService.generatePostfixAndLocation(outProvider, "",
		    "", "");
		mhealthMigrationService.migrate(inclient, syncData, inUserLocation, outUserLocation, type);
		
		return new ResponseEntity<>("ok", CREATED);
	}
	
	@RequestMapping(headers = {
	        "Accept=application/json;charset=UTF-8" }, method = RequestMethod.POST, value = "/accept-reject-migration")
	public ResponseEntity<String> acceptRejectMigration(HttpServletRequest request) throws JSONException {
		Long id = getIntegerFilter("id", request).longValue();
		String relationalId = getStringFilter("relationalId", request);
		String type = getStringFilter("type", request);
		String status = getStringFilter("status", request);
		mhealthMigrationService.acceptOrRejectMigration(id, relationalId, type, status);
		return new ResponseEntity<>("OK", CREATED);
	}
	
	@RequestMapping(headers = {
	        "Accept=application/json;charset=UTF-8" }, value = "/search-client", method = RequestMethod.GET)
	@ResponseBody
	protected ResponseEntity<String> searchClient(HttpServletRequest request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			Integer villageId = getIntegerFilter("villageId", request);
			String gender = getStringFilter("gender", request);
			Integer startAge = getIntegerFilter("startAge", request);
			Integer endAge = getIntegerFilter("endAge", request);
			String type = getStringFilter("type", request);
			
			List<Client> clients = mhealthClientService.searchClientForMigration(villageId, gender, startAge, endAge, type,
			    type);
			JsonArray clientsArray = (JsonArray) gson.toJsonTree(clients, new TypeToken<List<Client>>() {}.getType());
			response.put("clients", clientsArray);
			return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
		}
		catch (Exception e) {
			response.put("msg", "Error occurred");
			return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		
	}
	
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, value = "/migrated", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getMigratedList(@RequestParam("username") String provider, @RequestParam("type") String type,
	                                    @RequestParam("timestamp") Long timestamp) {
		
		return mhealthMigrationService.getMigratedList(provider, type, timestamp + 1);
	}
	
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, value = "/rejected", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getRejectedList(@RequestParam("username") String provider, @RequestParam("type") String type,
	                                    @RequestParam("timestamp") Long timestamp) {
		
		return mhealthMigrationService.getRejectedList(provider, type, timestamp + 1);
	}
	
}
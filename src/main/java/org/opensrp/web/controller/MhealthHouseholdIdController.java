package org.opensrp.web.controller;

import static org.springframework.http.HttpStatus.OK;

import org.json.JSONArray;
import org.opensrp.service.HouseholdIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MhealthHouseholdIdController {
	
	private HouseholdIdService householdIdService;
	
	@Autowired
	public void setHouseholdIdService(HouseholdIdService householdIdService) {
		this.householdIdService = householdIdService;
	}
	
	@RequestMapping(value = "/household/generated-code", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getHouseholdUniqueId(@RequestParam("username") String username,
	                                                   @RequestParam("villageId") String villageId)
	    throws Exception {
		int[] villageIds = new int[1000];
		String[] ids = villageId.split(",");
		for (int i = 0; i < ids.length; i++) {
			villageIds[i] = Integer.parseInt(ids[i]);
		}
		
		/*if (villageIds[0] == 0) {
			CustomQuery user = householdIdService.getUserId(username);
			List<CustomQuery> locationIds = clientService.getVillageByProviderId(user.getId(), childRoleId, locationTagId);
			int i = 0;
			for (CustomQuery locationId : locationIds) {
				villageIds[i++] = locationId.getId();
			}
		}*/
		JSONArray array = new JSONArray();
		//array = householdIdService.generateHouseholdId(villageIds);
		
		return new ResponseEntity<>(array.toString(), OK);
	}
	
	@RequestMapping(value = "/household/guest/generated-code", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getguestHouseholdUniqueId(@RequestParam("username") String username,
	                                                        @RequestParam("villageId") String villageId)
	    throws Exception {
		int[] villageIds = new int[1000];
		String[] ids = villageId.split(",");
		for (int i = 0; i < ids.length; i++) {
			villageIds[i] = Integer.parseInt(ids[i]);
		}
		/*
		if (villageIds[0] == 0) {
			CustomQuery user = clientService.getUserId(username);
			List<CustomQuery> locationIds = clientService.getVillageByProviderId(user.getId(), childRoleId, locationTagId);
			int i = 0;
			for (CustomQuery locationId : locationIds) {
				villageIds[i++] = locationId.getId();
			}
		}*/
		JSONArray array = new JSONArray();
		//array = eventService.generateGuestHouseholdId(villageIds);
		
		return new ResponseEntity<>(array.toString(), OK);
	}
	
}

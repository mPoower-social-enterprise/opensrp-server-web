package org.opensrp.web.controller;

import static org.springframework.http.HttpStatus.OK;

import org.opensrp.service.HouseholdIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/household/")
@Controller
public class MhealthHouseholdIdController {
	
	private HouseholdIdService householdIdService;
	
	@Autowired
	public void setHouseholdIdService(HouseholdIdService householdIdService) {
		this.householdIdService = householdIdService;
	}
	
	@RequestMapping(value = "/generated-code", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getHouseholdUniqueId(@RequestParam("username") String username,
	                                                   @RequestParam("villageId") String villageId)
	    throws Exception {
		
		return new ResponseEntity<>(householdIdService.generateHouseholdId(username, villageId).toString(), OK);
	}
	
	@RequestMapping(value = "/guest/generated-code", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getGuestHouseholdUniqueId(@RequestParam("username") String username,
	                                                        @RequestParam("villageId") String villageId)
	    throws Exception {
		return new ResponseEntity<>(householdIdService.generateGuestHouseholdId(username, villageId).toString(), OK);
	}
	
}

package org.opensrp.web.controller;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.opensrp.domain.PractitionerLocation;
import org.opensrp.domain.postgres.PractitionerDetails;
import org.opensrp.service.PractionerDetailsService;
import org.opensrp.web.rest.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MhealthUserController {
	
	private PractionerDetailsService practionerDetailsService;
	
	@Value("#{opensrp['child.group.id']}")
	protected int childGroupId;
	
	@Value("#{opensrp['assigned.location.tag.id']}")
	protected int assignedLocationTagId;
	
	@Autowired
	public void setPractionerDetailsService(PractionerDetailsService practionerDetailsService) {
		this.practionerDetailsService = practionerDetailsService;
	}
	
	@RequestMapping(value = "/provider/location-tree", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getPractitionerLocationTree(@RequestParam("username") String username)
	    throws JSONException {
		PractitionerDetails practitionerDetails = practionerDetailsService.findPractitionerDetailsByUsername(username);
		List<PractitionerLocation> practitionerLocations = practionerDetailsService.findPractitionerLocationsByChildGroup(
		    practitionerDetails.getPractitionerId(), childGroupId, assignedLocationTagId);
		String fullName = practitionerDetails.getFirstName() + " " + practitionerDetails.getLastName();
		JSONArray practitionerLocationArray = practionerDetailsService.convertLocationTreeToJSON(practitionerLocations,
		    practitionerDetails.getEnableSimPrint(), fullName);
		return new ResponseEntity<>(practitionerLocationArray.toString(), RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		
	}
}

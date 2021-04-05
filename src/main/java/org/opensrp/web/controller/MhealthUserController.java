package org.opensrp.web.controller;

import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.opensrp.domain.PractitionerLocation;
import org.opensrp.domain.postgres.PractitionerDetails;
import org.opensrp.service.PractionerDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MhealthUserController extends UserController {
	
	@Autowired
	private PractionerDetailsService practionerDetailsService;
	
	@Value("#{opensrp['child.group.id']}")
	protected int childGroupId;
	
	@Value("#{opensrp['assigned.location.tag.id']}")
	protected int assignedLocationTagId;
	
	@RequestMapping(value = "/provider/location-tree", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getLocationTree(@RequestParam("username") String username) throws JSONException {
		PractitionerDetails practitionerDetails = practionerDetailsService.findPractitionerDetailsByUsername(username);
		List<PractitionerLocation> treeDTOS = practionerDetailsService.findPractitionerLocationsByChildGroup(
		    practitionerDetails.getPractitionerId(), childGroupId, assignedLocationTagId);
		JSONArray array = new JSONArray();
		String fullName = practitionerDetails.getFirstName() + " " + practitionerDetails.getLastName();
		try {
			array = practionerDetailsService.convertLocationTreeToJSON(treeDTOS, practitionerDetails.getEnableSimPrint(),
			    fullName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<>(array.toString(), OK);
	}
}

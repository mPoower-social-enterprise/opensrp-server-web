package org.opensrp.web.controller;

import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.opensrp.domain.postgres.MhealthPractitionerLocation;
import org.opensrp.service.PractitionerLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/location/mhealth/")
public class MhealthLocationController {
	
	private PractitionerLocationService practitionerLocationService;
	
	@Autowired
	public void setPractitionerLocationService(PractitionerLocationService practitionerLocationService) {
		this.practitionerLocationService = practitionerLocationService;
	}
	
	@RequestMapping(value = "/district-upazila", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getDistrictAndUpazila(HttpServletRequest request) throws JSONException {
		JSONArray districts = practitionerLocationService.getDistrictAndUpazila(29);
		return new ResponseEntity<>(districts.toString(), OK);
	}
	
	@RequestMapping(headers = {
	        "Accept=application/json;charset=UTF-8" }, value = "/district-list", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<MhealthPractitionerLocation> getDistrictList(HttpServletRequest request) throws JSONException {
		
		return practitionerLocationService.getLocationByTagId(29);
	}
	
	@RequestMapping(headers = {
	        "Accept=application/json;charset=UTF-8" }, value = "/child-location", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<MhealthPractitionerLocation> getChildLocation(@RequestParam("id") Integer id) throws JSONException {
		
		return practitionerLocationService.getLocationByParentId(id);
	}
	
}

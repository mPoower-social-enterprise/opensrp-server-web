package org.opensrp.web.controller;

import java.util.List;

import org.opensrp.domain.postgres.TargetDetails;
import org.opensrp.service.TargetDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TargetDetailsController {
	
	private TargetDetailsService targetDetailsService;
	
	@Autowired
	public void setTargetDetailsService(TargetDetailsService targetDetailsService) {
		this.targetDetailsService = targetDetailsService;
	}
	
	@RequestMapping(value = "/get-target", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<List<TargetDetails>> getTargetDetails(@RequestParam("username") String username,
	                                                            @RequestParam("timestamp") Long timestamp) {
		
		return new ResponseEntity<>(targetDetailsService.getTargetDetailsByUsername(username, timestamp), HttpStatus.OK);
	}
	
}

package org.opensrp.web.controller;

import java.util.List;

import org.opensrp.domain.postgres.WebNotification;
import org.opensrp.service.WebNotificationService;
import org.opensrp.web.rest.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebNotificationController {
	
	private WebNotificationService webNotificationService;
	
	@Autowired
	public void setWebNotificationService(WebNotificationService webNotificationService) {
		this.webNotificationService = webNotificationService;
	}
	
	@RequestMapping(headers = {
	        "Accept=application/json;charset=UTF-8" }, value = "/get_web_notification", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<WebNotification>> getWebNotification(@RequestParam("username") String username,
	                                                                @RequestParam("timestamp") Long timestamp) {
		return new ResponseEntity<>(webNotificationService.getWebNotificationsByUsername(username, timestamp),
		        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		
	}
	
}

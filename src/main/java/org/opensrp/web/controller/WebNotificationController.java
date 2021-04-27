package org.opensrp.web.controller;

import java.util.List;

import org.opensrp.domain.postgres.WebNotification;
import org.opensrp.service.WebNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
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
	public List<WebNotification> getWebNotification(@RequestParam("username") String username,
	                                                @RequestParam("timestamp") Long timestamp) {
		
		return webNotificationService.getWebNotificationsByUsername(username, timestamp);
	}
	
}

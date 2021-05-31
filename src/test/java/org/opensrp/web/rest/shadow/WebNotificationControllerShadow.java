package org.opensrp.web.rest.shadow;

import org.opensrp.service.WebNotificationService;
import org.opensrp.web.controller.WebNotificationController;
import org.springframework.stereotype.Component;

@Component
public class WebNotificationControllerShadow extends WebNotificationController {
	
	@Override
	public void setWebNotificationService(WebNotificationService webNotificationService) {
		super.setWebNotificationService(webNotificationService);
		
	}
}

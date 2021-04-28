package org.opensrp.web.rest.shadow;

import org.opensrp.service.TargetDetailsService;
import org.opensrp.web.controller.TargetDetailsController;
import org.springframework.stereotype.Component;

@Component
public class TargetDetailsControllerShadow extends TargetDetailsController {
	
	@Override
	public void setTargetDetailsService(TargetDetailsService targetDetailsService) {
		super.setTargetDetailsService(targetDetailsService);
	}
	
}

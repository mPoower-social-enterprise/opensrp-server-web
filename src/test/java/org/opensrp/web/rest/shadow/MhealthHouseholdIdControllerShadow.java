package org.opensrp.web.rest.shadow;

import org.opensrp.service.HouseholdIdService;
import org.opensrp.web.controller.MhealthHouseholdIdController;
import org.springframework.stereotype.Component;

@Component
public class MhealthHouseholdIdControllerShadow extends MhealthHouseholdIdController {
	
	@Override
	public void setHouseholdIdService(HouseholdIdService householdIdService) {
		super.setHouseholdIdService(householdIdService);
	}
}

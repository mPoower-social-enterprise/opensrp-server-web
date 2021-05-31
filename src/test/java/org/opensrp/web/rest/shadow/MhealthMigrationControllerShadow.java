package org.opensrp.web.rest.shadow;

import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.service.MhealthMigrationService;
import org.opensrp.service.PractitionerLocationService;
import org.opensrp.web.controller.MhealthMigrationController;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MhealthMigrationControllerShadow extends MhealthMigrationController {
	
	public MhealthMigrationControllerShadow(MhealthMigrationService mhealthMigrationService,
	    MhealthEventService mhealthEventService, MhealthClientService mhealthClientService,
	    PractitionerLocationService practitionerLocationService) {
		super(mhealthMigrationService, mhealthEventService, mhealthClientService, practitionerLocationService);
		
	}
	
	public MhealthMigrationControllerShadow() {
		super(null, null, null, null);
	}
	
	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}
}

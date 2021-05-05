package org.opensrp.web.rest.shadow;

import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.service.PractitionerLocationService;
import org.opensrp.web.rest.MhealthEventResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MhealthEventResourceShadow extends MhealthEventResource {
	
	public MhealthEventResourceShadow(MhealthClientService mhealthClientService, MhealthEventService mhealthEventService,
	    PractitionerLocationService practitionerLocationService) {
		super(mhealthClientService, mhealthEventService, practitionerLocationService);
	}
	
	public MhealthEventResourceShadow() {
		super(null, null, null);
	}
	
	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}
}

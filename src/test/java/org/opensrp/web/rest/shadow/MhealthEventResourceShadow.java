package org.opensrp.web.rest.shadow;

import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.web.rest.MhealthEventResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MhealthEventResourceShadow extends MhealthEventResource {
	
	public MhealthEventResourceShadow(MhealthClientService mhealthClientService, MhealthEventService mhealthEventService) {
		super(mhealthClientService, mhealthEventService);
	}
	
	public MhealthEventResourceShadow() {
		super(null, null);
	}
	
	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}
}

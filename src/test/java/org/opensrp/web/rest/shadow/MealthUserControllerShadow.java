package org.opensrp.web.rest.shadow;

import org.opensrp.service.PractionerDetailsService;
import org.opensrp.web.controller.MhealthUserController;
import org.springframework.stereotype.Component;

@Component
public class MealthUserControllerShadow extends MhealthUserController {
	
	@Override
	public void setPractionerDetailsService(PractionerDetailsService practionerDetailsService) {
		super.setPractionerDetailsService(practionerDetailsService);
	}
}

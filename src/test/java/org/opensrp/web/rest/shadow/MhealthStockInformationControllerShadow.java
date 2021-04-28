package org.opensrp.web.rest.shadow;

import org.opensrp.service.MhealthStockInformationService;
import org.opensrp.web.controller.MhealthStockInformationController;
import org.springframework.stereotype.Component;

@Component
public class MhealthStockInformationControllerShadow extends MhealthStockInformationController {
	
	@Override
	public void setMhealthStockInformationService(MhealthStockInformationService mhealthStockInformationService) {
		super.setMhealthStockInformationService(mhealthStockInformationService);
	}
	
}

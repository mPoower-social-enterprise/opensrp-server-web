package org.opensrp.web.controller;

import java.util.List;

import org.opensrp.domain.postgres.MhealthStockInformation;
import org.opensrp.service.MhealthStockInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MhealthStockInformationController {
	
	private MhealthStockInformationService mhealthStockInformationService;
	
	@Autowired
	public void setMhealthStockInformationService(MhealthStockInformationService mhealthStockInformationService) {
		this.mhealthStockInformationService = mhealthStockInformationService;
	}
	
	@RequestMapping(headers = {
	        "Accept=application/json;charset=UTF-8" }, value = "/get_stock_info", method = RequestMethod.GET)
	@ResponseBody
	public List<MhealthStockInformation> getStockInfo(@RequestParam("username") String username,
	                                                  @RequestParam("timestamp") Long timestamp) {
		
		return mhealthStockInformationService.getStockInformationByUsername(username, timestamp);
	}
	
}

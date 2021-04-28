package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.opensrp.domain.postgres.MhealthStockInformation;
import org.opensrp.service.MhealthStockInformationService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.BaseResourceTest;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class MhealthStockInformationControllerTest extends BaseResourceTest<MhealthStockInformation> {
	
	private final static String BASE_URL = "/get_stock_info/";
	
	protected MockMvc mockMvc;
	
	private MhealthStockInformationService mhealthStockInformationService;
	
	private String expectedString = "[{\"expireyDate\":\"2020-11-01\",\"productId\":1,\"stockId\":1,\"productName\":\"Women package\",\"quantity\":5,\"receiveDate\":\"2020-11-01\",\"year\":2020,\"month\":11,\"timestamp\":1606054400519}]";
	
	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
	
	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity())
		        .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		mhealthStockInformationService = mock(MhealthStockInformationService.class);
		MhealthStockInformationController mhealthStockInformationController = webApplicationContext
		        .getBean(MhealthStockInformationController.class);
		mhealthStockInformationController.setMhealthStockInformationService(mhealthStockInformationService);
	}
	
	@Test
	public void testGetStockInfoWithStatusOK() throws Exception {
		
		when(mhealthStockInformationService.getStockInformationByUsername("p1", 0l)).thenReturn(createStockDetails());
		String actualResult = getResponseAsString(BASE_URL + "/?username=p1&timestamp=0", null,
		    MockMvcResultMatchers.status().isOk());
		verify(mhealthStockInformationService).getStockInformationByUsername(stringArgumentCaptor.capture(),
		    longArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "p1");
		assertEquals(expectedString, actualResult);
	}
	
	@Test
	public void testGetStockInfoWithStatusBadRequest() throws Exception {
		when(mhealthStockInformationService.getStockInformationByUsername("p1", 0l)).thenReturn(createStockDetails());
		getResponseAsString(BASE_URL + "/?usernames=p1&timestamp=0", null, MockMvcResultMatchers.status().isBadRequest());
		
	}
	
	private List<MhealthStockInformation> createStockDetails() {
		MhealthStockInformation mhealthStockInformation = new MhealthStockInformation();
		mhealthStockInformation.setExpireyDate("2020-11-01");
		mhealthStockInformation.setMonth(11);
		mhealthStockInformation.setProductId(1l);
		mhealthStockInformation.setProductName("Women package");
		mhealthStockInformation.setQuantity(5);
		mhealthStockInformation.setReceiveDate("2020-11-01");
		mhealthStockInformation.setStockId(1l);
		mhealthStockInformation.setTimestamp(1606054400519l);
		mhealthStockInformation.setYear(2020);
		
		List<MhealthStockInformation> mhealthStockInformations = new ArrayList<MhealthStockInformation>();
		mhealthStockInformations.add(mhealthStockInformation);
		return mhealthStockInformations;
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<MhealthStockInformation> expectedList,
	                                               List<MhealthStockInformation> actualList) {
		// TODO Auto-generated method stub
		
	}
	
}

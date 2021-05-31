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
import org.opensrp.domain.postgres.TargetDetails;
import org.opensrp.service.TargetDetailsService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.BaseResourceTest;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TargetDetailsControllerTest extends BaseResourceTest<TargetDetails> {
	
	private final static String BASE_URL = "/get-target/";
	
	protected MockMvc mockMvc;
	
	private TargetDetailsService targetDetailsService;
	
	private String expectedString = "[{\"username\":\"p1\",\"targetId\":1,\"targetName\":\"Women package\",\"targetCount\":5,\"year\":\"2021\",\"month\":\"1\",\"day\":\"13\",\"startDate\":\"13-01-2021\",\"endDate\":\"13-01-2021\",\"timestamp\":1610532558497}]";
	
	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
	
	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity())
		        .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		targetDetailsService = mock(TargetDetailsService.class);
		TargetDetailsController targetDetailsController = webApplicationContext.getBean(TargetDetailsController.class);
		targetDetailsController.setTargetDetailsService(targetDetailsService);
	}
	
	@Test
	public void testGetTargetDetailsWithStatusOK() throws Exception {
		
		when(targetDetailsService.getTargetDetailsByUsername("p1", 0l)).thenReturn(createTargetDetails());
		String actualResult = getResponseAsString(BASE_URL + "/?username=p1&timestamp=0", null,
		    MockMvcResultMatchers.status().isOk());
		verify(targetDetailsService).getTargetDetailsByUsername(stringArgumentCaptor.capture(),
		    longArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "p1");
		assertEquals(expectedString, actualResult);
	}
	
	@Test
	public void testGetTargetDetailsWithStatusBadRequest() throws Exception {
		when(targetDetailsService.getTargetDetailsByUsername("p1", 0l)).thenReturn(createTargetDetails());
		getResponseAsString(BASE_URL + "/?usernames=p1&timestamp=0", null, MockMvcResultMatchers.status().isBadRequest());
		
	}
	
	private List<TargetDetails> createTargetDetails() {
		TargetDetails targetDetail = new TargetDetails();
		targetDetail.setDay("13");
		targetDetail.setEndDate("13-01-2021");
		targetDetail.setMonth("1");
		targetDetail.setStartDate("13-01-2021");
		targetDetail.setTargetCount(5);
		targetDetail.setTargetId(1l);
		targetDetail.setTargetName("Women package");
		targetDetail.setTimestamp(1610532558497l);
		targetDetail.setUsername("p1");
		targetDetail.setYear("2021");
		List<TargetDetails> targetDetails = new ArrayList<TargetDetails>();
		targetDetails.add(targetDetail);
		return targetDetails;
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<TargetDetails> expectedList, List<TargetDetails> actualList) {
		
	}
	
}

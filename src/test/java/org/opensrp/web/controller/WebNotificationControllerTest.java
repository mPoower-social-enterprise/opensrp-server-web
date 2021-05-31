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
import org.opensrp.domain.postgres.WebNotification;
import org.opensrp.service.WebNotificationService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.BaseResourceTest;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class WebNotificationControllerTest extends BaseResourceTest<WebNotification> {
	
	private final static String BASE_URL = "/get_web_notification/";
	
	protected MockMvc mockMvc;
	
	private WebNotificationService webNotificationService;
	
	private String expectedString = "[{\"notificationType\":\"general\",\"id\":1,\"title\":\"test\",\"details\":\"test notification\",\"sendDate\":\"2020-11-23\",\"hour\":10,\"minute\":18,\"timestamp\":1606105080000}]";
	
	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
	
	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity())
		        .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		webNotificationService = mock(WebNotificationService.class);
		WebNotificationController webNotificationController = webApplicationContext.getBean(WebNotificationController.class);
		webNotificationController.setWebNotificationService(webNotificationService);
	}
	
	@Test
	public void testGetWebNotificationWithStatusOK() throws Exception {
		
		when(webNotificationService.getWebNotificationsByUsername("p1", 0l)).thenReturn(createWebNotifications());
		String actualResult = getResponseAsString(BASE_URL + "/?username=p1&timestamp=0", null,
		    MockMvcResultMatchers.status().isOk());
		verify(webNotificationService).getWebNotificationsByUsername(stringArgumentCaptor.capture(),
		    longArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "p1");
		assertEquals(expectedString, actualResult);
	}
	
	@Test
	public void testGetWebNotificationWithStatusBadRequest() throws Exception {
		when(webNotificationService.getWebNotificationsByUsername("p1", 0l)).thenReturn(createWebNotifications());
		getResponseAsString(BASE_URL + "/?usernames=p1&timestamp=0", null, MockMvcResultMatchers.status().isBadRequest());
		
	}
	
	private List<WebNotification> createWebNotifications() {
		WebNotification webNotification = new WebNotification();
		webNotification.setDetails("test notification");
		webNotification.setHour(10);
		webNotification.setId(1l);
		webNotification.setMinute(18);
		webNotification.setNotificationType("general");
		webNotification.setSendDate("2020-11-23");
		webNotification.setTimestamp(1606105080000l);
		webNotification.setTitle("test");
		List<WebNotification> webNotifications = new ArrayList<WebNotification>();
		webNotifications.add(webNotification);
		return webNotifications;
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<WebNotification> expectedList, List<WebNotification> actualList) {
		
	}
	
}

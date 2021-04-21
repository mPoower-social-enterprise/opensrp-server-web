package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class MhealthValidateResourceTest {
	
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	private MockMvc mockMvc;
	
	@Mock
	private MhealthClientService mhealthClientService;
	
	@Mock
	private MhealthEventService mhealthEventService;
	
	@InjectMocks
	private MhealthValidateResource mhealthValidateResource;
	
	protected ObjectMapper mapper = new ObjectMapper();
	
	private final String BASE_URL = "/rest/validate/mhealth/";
	
	private String INVALID_JSON = "{\ns" + "  \"client\" : {\n" + "  \"firstName\" : \"Test\" \n" + "  }\n" + "}";
	
	private String SYNC_REQUEST_PAYLOAD = "{\n" + "\t\"clients\": \"[ 1 , 2 ]\",\n" + "\t\"events\": \"[ 1 , 2]\"\n" + "}";
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(mhealthValidateResource)
		        .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
	}
	
	@Test
	public void testValidateSyncWithBlankData() throws Exception {
		when(mhealthClientService.findClientIdByBaseEntityId(any(String.class), anyString())).thenReturn(createClientId());
		when(mhealthEventService.findEventIdByFormSubmissionId(any(String.class), anyString())).thenReturn(createEventId());
		mockMvc.perform(post(BASE_URL + "/sync?district=123").contentType(MediaType.APPLICATION_JSON).content("".getBytes()))
		        .andExpect(status().isBadRequest()).andReturn();
	}
	
	@Test
	public void testValidateSyncWithoutDistrict() throws Exception {
		when(mhealthClientService.findClientIdByBaseEntityId(any(String.class), anyString())).thenReturn(createClientId());
		when(mhealthEventService.findEventIdByFormSubmissionId(any(String.class), anyString())).thenReturn(createEventId());
		mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content("".getBytes()))
		        .andExpect(status().isBadRequest()).andReturn();
	}
	
	@Test
	public void testValidateSync() throws Exception {
		String expected = "{\"clients\":[\"1\",\"2\"],\"events\":[\"1\",\"2\"]}";
		when(mhealthClientService.findClientIdByBaseEntityId(any(String.class), anyString())).thenReturn(null);
		when(mhealthEventService.findEventIdByFormSubmissionId(any(String.class), anyString())).thenReturn(null);
		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync?district=123").contentType(MediaType.APPLICATION_JSON)
		        .content(SYNC_REQUEST_PAYLOAD.getBytes())).andExpect(status().isOk()).andReturn();
		
		assertEquals(result.getResponse().getContentAsString(), expected);
	}
	
	@Test
	public void testValidateSyncWithWrongData() throws Exception {
		when(mhealthClientService.findClientIdByBaseEntityId(any(String.class), anyString())).thenReturn(createClientId());
		when(mhealthEventService.findEventIdByFormSubmissionId(any(String.class), anyString())).thenReturn(createEventId());
		mockMvc.perform(
		    post(BASE_URL + "/sync?district=123").contentType(MediaType.APPLICATION_JSON).content(INVALID_JSON.getBytes()))
		        .andExpect(status().isInternalServerError()).andReturn();
	}
	
	private Long createClientId() {
		return 123l;
	}
	
	private Long createEventId() {
		
		return 345l;
	}
	
}

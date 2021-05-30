package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.opensrp.domain.postgres.MhealthEventMetadata;
import org.opensrp.domain.postgres.MhealthMigration;
import org.opensrp.domain.postgres.MhealthPractitionerLocation;
import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.service.MhealthMigrationService;
import org.opensrp.service.PractitionerLocationService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.BaseSecureResourceTest;
import org.smartregister.domain.Client;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class MhealthMigrationControllerTest extends BaseSecureResourceTest<MhealthMigration> {
	
	private final static String BASE_URL = "/migrate";
	
	protected MockMvc mockMvc;
	
	private MhealthMigrationService mhealthMigrationService;
	
	private MhealthEventService mhealthEventService;
	
	private PractitionerLocationService practitionerLocationService;
	
	private MhealthClientService mhealthClientService;
	
	private final static String memberPayload = "{\"clients\": \"[{\\\"birthdate\\\":\\\"1970-01-01T06:00:00.000Z\\\",\\\"birthdateApprox\\\":false,\\\"deathdateApprox\\\":false,\\\"firstName\\\":\\\"Rasheda\\\",\\\"gender\\\":\\\"M\\\",\\\"baseEntityId\\\":\\\"a6c836ea-800d-49ea-8565-0d7c58fdcb8c\\\",\\\"identifiers\\\":{\\\"opensrp_id\\\":\\\"608844006730700016010001\\\"},\\\"addresses\\\":[{\\\"addressType\\\":\\\"usual_residence\\\",\\\"addressFields\\\":{\\\"address1\\\":\\\"BHOLABAA\\\",\\\"address2\\\":\\\"RUPGANJA\\\",\\\"address3\\\":\\\"NOT POURASABHAA\\\",\\\"address8\\\":\\\"136961\\\"},\\\"countyDistrict\\\":\\\"NARAYANGANJA\\\",\\\"cityVillage\\\":\\\"TORAILA\\\",\\\"stateProvince\\\":\\\"DHAKAA\\\",\\\"country\\\":\\\"BANGLADESHA\\\"}],\\\"attributes\\\":{\\\"age\\\":\\\"35\\\",\\\"DOB_known\\\":\\\"no\\\",\\\"Blood_Group\\\":\\\"A+\\\",\\\"Mobile_Number\\\":\\\"0\\\",\\\"Marital_Status\\\":\\\"Married\\\",\\\"Relation_with_HOH\\\":\\\"Guest\\\",\\\"difficulty_seeing_hearing\\\":\\\"yes_little_difficulties\\\",\\\"difficulty_walking_up_down\\\":\\\"yes_little_difficulties\\\",\\\"trouble_remembering_concentrating\\\":\\\"no_difficulties\\\"},\\\"clientApplicationVersion\\\":31,\\\"clientApplicationVersionName\\\":\\\"1.3.6_DEV\\\",\\\"clientDatabaseVersion\\\":31,\\\"dateCreated\\\":\\\"2021-01-04T16:35:04.023Z\\\",\\\"type\\\":\\\"Client\\\",\\\"relationships\\\":{\\\"mother\\\":[\\\"\\\"],\\\"family\\\":[\\\"05b5acd9-d375-4892-aae3-2cefa17c7538\\\",\\\"05b5acd9-d375-4892-aae3-2cefa17c7538\\\"]}}]\"}";
	
	private final static String familyPayload = "{\"clients\": \"[{\\\"birthdate\\\":\\\"1970-01-01T06:00:00.000Z\\\",\\\"birthdateApprox\\\":false,\\\"deathdateApprox\\\":false,\\\"firstName\\\":\\\"Rashed\\\",\\\"lastName\\\":\\\"Family\\\",\\\"gender\\\":\\\"M\\\",\\\"baseEntityId\\\":\\\"0511acf9-6be8-4a98-b4f9-f8a5cfa704bb\\\",\\\"identifiers\\\":{\\\"opensrp_id\\\":\\\"50884400673070001601\\\"},\\\"addresses\\\":[{\\\"addressType\\\":\\\"usual_residence\\\",\\\"addressFields\\\":{\\\"address1\\\":\\\"BHOLABAQ\\\",\\\"address2\\\":\\\"RUPGANJq\\\",\\\"address3\\\":\\\"NOT POURASABHAq\\\",\\\"address8\\\":\\\"136961\\\"},\\\"countyDistrict\\\":\\\"NARAYANGANJq\\\",\\\"cityVillage\\\":\\\"Migrated Village\\\",\\\"stateProvince\\\":\\\"DHAKAq\\\",\\\"country\\\":\\\"BANGLADESHq\\\"}],\\\"attributes\\\":{\\\"Cluster\\\":\\\"1st_Cluster\\\",\\\"HH_Type\\\":\\\"BRAC VO\\\",\\\"SS_Name\\\":\\\"Forida(SS-1)\\\",\\\"module_id\\\":\\\"TRAINING\\\",\\\"serial_no\\\":\\\"H369\\\",\\\"village_id\\\":\\\"136962\\\",\\\"Has_Latrine\\\":\\\"No\\\",\\\"HOH_Phone_Number\\\":\\\"01471221551\\\",\\\"Number_of_HH_Member\\\":\\\"5\\\"},\\\"clientApplicationVersion\\\":31,\\\"clientApplicationVersionName\\\":\\\"1.3.6_DEV\\\",\\\"clientDatabaseVersion\\\":31,\\\"dateCreated\\\":\\\"2021-01-04T16:35:04.023Z\\\",\\\"type\\\":\\\"Client\\\",\\\"relationships\\\":{\\\"mother\\\":[\\\"\\\"],\\\"family_head\\\":[\\\"0511acf9-6be8-4a98-b4f9-f8a5cfa704bb\\\",\\\"0511acf9-6be8-4a98-b4f9-f8a5cfa704bb\\\"]}}]\"}";
	
	private final static String wrongPayload = "{\"clients\": \"[{\\\"birthdate\\\":\\\"1970-01-01T06:00:00.000Z\\\",\\\"birthdateApprox\\\":false,\\\"deathdateApprox\\\":false,\"firstName\":\\\"Rasheda\\\",\\\"gender\\\":\\\"M\\\",\\\"baseEntityId\\\":\\\"a6c836ea-800d-49ea-8565-0d7c58fdcb8c\\\",\\\"identifiers\\\":{\\\"opensrp_id\\\":\\\"608844006730700016010001\\\"},\\\"addresses\\\":[{\\\"addressType\\\":\\\"usual_residence\\\",\\\"addressFields\\\":{\\\"address1\\\":\\\"BHOLABAA\\\",\\\"address2\\\":\\\"RUPGANJA\\\",\\\"address3\\\":\\\"NOT POURASABHAA\\\",\\\"address8\\\":\\\"136961\\\"},\\\"countyDistrict\\\":\\\"NARAYANGANJA\\\",\\\"cityVillage\\\":\\\"TORAILA\\\",\\\"stateProvince\\\":\\\"DHAKAA\\\",\\\"country\\\":\\\"BANGLADESHA\\\"}],\\\"attributes\\\":{\\\"age\\\":\\\"35\\\",\\\"DOB_known\\\":\\\"no\\\",\\\"Blood_Group\\\":\\\"A+\\\",\\\"Mobile_Number\\\":\\\"0\\\",\\\"Marital_Status\\\":\\\"Married\\\",\\\"Relation_with_HOH\\\":\\\"Guest\\\",\\\"difficulty_seeing_hearing\\\":\\\"yes_little_difficulties\\\",\\\"difficulty_walking_up_down\\\":\\\"yes_little_difficulties\\\",\\\"trouble_remembering_concentrating\\\":\\\"no_difficulties\\\"},\\\"clientApplicationVersion\\\":31,\\\"clientApplicationVersionName\\\":\\\"1.3.6_DEV\\\",\\\"clientDatabaseVersion\\\":31,\\\"dateCreated\\\":\\\"2021-01-04T16:35:04.023Z\\\",\\\"type\\\":\\\"Client\\\",\\\"relationships\\\":{\\\"mother\\\":[\\\"\\\"],\\\"family\\\":[\\\"05b5acd9-d375-4892-aae3-2cefa17c7538\\\",\\\"05b5acd9-d375-4892-aae3-2cefa17c7538\\\"]}}]\"}";
	
	@Captor
	private ArgumentCaptor<Client> clientArgumentCaptor = ArgumentCaptor.forClass(Client.class);
	
	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
	
	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
	
	@Captor
	private ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity())
		        .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		mhealthMigrationService = mock(MhealthMigrationService.class);
		mhealthEventService = mock(MhealthEventService.class);
		practitionerLocationService = mock(PractitionerLocationService.class);
		mhealthClientService = mock(MhealthClientService.class);
		MhealthMigrationController mhealthMigrationController = webApplicationContext
		        .getBean(MhealthMigrationController.class);
		mhealthMigrationController.setMhealthClientService(mhealthClientService);
		mhealthMigrationController.setMhealthEventService(mhealthEventService);
		mhealthMigrationController.setMhealthMigrationService(mhealthMigrationService);
		mhealthMigrationController.setPractitionerLocationService(practitionerLocationService);
		mhealthMigrationController.setObjectMapper(mapper);
	}
	
	@Test
	public void testdoMigrateMemberStatusOK() throws Exception {
		
		doReturn(createMhealthEventMetadata()).when(mhealthEventService).findFirstEventMetadata(anyString(), anyString());
		
		doReturn(createMhealthMigration()).when(mhealthMigrationService).findFirstMigrationBybaseEntityId(anyString());
		
		doReturn(createMhealthPractitionerLocation()).when(practitionerLocationService)
		        .generatePostfixAndLocation(anyString(), anyString(), anyString(), anyString());
		doReturn(true).when(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), anyString());
		
		postRequestWithJsonContent(BASE_URL + "/?username=testsk&districtId=2345&divisionId=234&type=HH&branchId=02",
		    memberPayload, status().isCreated());
		
		verify(mhealthEventService).findFirstEventMetadata(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		
		verify(mhealthMigrationService).findFirstMigrationBybaseEntityId(stringArgumentCaptor.capture());
		
		verify(practitionerLocationService).generatePostfixAndLocation(stringArgumentCaptor.capture(),
		    stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "a6c836ea-800d-49ea-8565-0d7c58fdcb8c");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "_2345");
		assertEquals(stringArgumentCaptor.getAllValues().get(3), "testsk1");
		verify(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), stringArgumentCaptor.capture());
		assertEquals(clientArgumentCaptor.getValue().getFirstName(), "Rasheda");
		assertEquals(clientArgumentCaptor.getValue().getGender(), "M");
		assertEquals(clientArgumentCaptor.getValue().getGender(), "M");
	}
	
	@Test
	public void testdoMigrateFamilyWithNoExistingMigrationStatusOK() throws Exception {
		
		doReturn(createMhealthEventMetadata()).when(mhealthEventService).findFirstEventMetadata(anyString(), anyString());
		
		doReturn(null).when(mhealthMigrationService).findFirstMigrationBybaseEntityId(anyString());
		
		doReturn(createMhealthPractitionerLocation()).when(practitionerLocationService)
		        .generatePostfixAndLocation(anyString(), anyString(), anyString(), anyString());
		doReturn(true).when(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), anyString());
		
		postRequestWithJsonContent(BASE_URL + "/?username=testsk&districtId=2345&divisionId=234&type=HH&branchId=02",
		    familyPayload, status().isCreated());
		
		verify(mhealthEventService).findFirstEventMetadata(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		
		verify(mhealthMigrationService).findFirstMigrationBybaseEntityId(stringArgumentCaptor.capture());
		
		verify(practitionerLocationService).generatePostfixAndLocation(stringArgumentCaptor.capture(),
		    stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "0511acf9-6be8-4a98-b4f9-f8a5cfa704bb");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "_2345");
		
		assertEquals(stringArgumentCaptor.getAllValues().get(3), "testsk");
		verify(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), stringArgumentCaptor.capture());
		assertEquals(clientArgumentCaptor.getValue().getFirstName(), "Rashed");
		assertEquals(clientArgumentCaptor.getValue().getGender(), "M");
		assertEquals(clientArgumentCaptor.getValue().getGender(), "M");
	}
	
	@Test
	public void testdoMigrateMemberStatusBadRequest() throws Exception {
		
		doReturn(createMhealthEventMetadata()).when(mhealthEventService).findFirstEventMetadata(anyString(), anyString());
		
		doReturn(createMhealthMigration()).when(mhealthMigrationService).findFirstMigrationBybaseEntityId(anyString());
		
		doReturn(createMhealthPractitionerLocation()).when(practitionerLocationService)
		        .generatePostfixAndLocation(anyString(), anyString(), anyString(), anyString());
		doReturn(true).when(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), anyString());
		
		postRequestWithJsonContent(BASE_URL + "/?username=testsk&districtId=2345&divisionId=234&type=HH&branchId=02", "{}",
		    status().isBadRequest());
		
	}
	
	@Test
	public void testdoMigrateMemberStatusIntenalServerError() throws Exception {
		
		doReturn(createMhealthEventMetadata()).when(mhealthEventService).findFirstEventMetadata(anyString(), anyString());
		
		doReturn(createMhealthMigration()).when(mhealthMigrationService).findFirstMigrationBybaseEntityId(anyString());
		
		doReturn(createMhealthPractitionerLocation()).when(practitionerLocationService)
		        .generatePostfixAndLocation(anyString(), anyString(), anyString(), anyString());
		doReturn(true).when(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), anyString());
		
		postRequestWithJsonContent(BASE_URL + "/?username=testsk&districtId=2345&divisionId=234&type=HH&branchId=02",
		    wrongPayload, status().isInternalServerError());
		
	}
	
	public MhealthEventMetadata createMhealthEventMetadata() {
		MhealthEventMetadata mhealthEventMetadata = new MhealthEventMetadata();
		mhealthEventMetadata.setBaseEntityId("f67823b0-378e-4a35-93fc-bb00def74e2f");
		mhealthEventMetadata.setProviderId("testsk");
		return mhealthEventMetadata;
	}
	
	public MhealthMigration createMhealthMigration() {
		MhealthMigration mhealthMigration = new MhealthMigration();
		mhealthMigration.setSKIn("testsk1");
		return mhealthMigration;
	}
	
	public MhealthPractitionerLocation createMhealthPractitionerLocation() {
		MhealthPractitionerLocation mhealthPractitionerLocation = new MhealthPractitionerLocation();
		mhealthPractitionerLocation.setUsername("testsk1");
		mhealthPractitionerLocation.setDivision("234");
		mhealthPractitionerLocation.setDistrict("2345");
		mhealthPractitionerLocation.setPostFix("");
		return mhealthPractitionerLocation;
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<MhealthMigration> expectedList, List<MhealthMigration> actualList) {
		
	}
	
}

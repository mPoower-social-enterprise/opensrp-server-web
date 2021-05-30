package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
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
import org.smartregister.domain.Address;
import org.smartregister.domain.Client;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class MhealthMigrationControllerTest extends BaseSecureResourceTest<MhealthMigration> {
	
	private final static String MIGRATE_URL = "/migrate";
	
	private final static String ACCEPT_REJECT_URL = "/accept-reject-migration";
	
	private final static String SEARCH_CLIENT = "/search-client";
	
	private final static String MIGRATED = "/migrated";
	
	private final static String REJECTED = "/rejected";
	
	protected MockMvc mockMvc;
	
	private MhealthMigrationService mhealthMigrationService;
	
	private MhealthEventService mhealthEventService;
	
	private PractitionerLocationService practitionerLocationService;
	
	private MhealthClientService mhealthClientService;
	
	private final static String memberPayload = "{\"clients\": \"[{\\\"birthdate\\\":\\\"1970-01-01T06:00:00.000Z\\\",\\\"birthdateApprox\\\":false,\\\"deathdateApprox\\\":false,\\\"firstName\\\":\\\"Rasheda\\\",\\\"gender\\\":\\\"M\\\",\\\"baseEntityId\\\":\\\"a6c836ea-800d-49ea-8565-0d7c58fdcb8c\\\",\\\"identifiers\\\":{\\\"opensrp_id\\\":\\\"608844006730700016010001\\\"},\\\"addresses\\\":[{\\\"addressType\\\":\\\"usual_residence\\\",\\\"addressFields\\\":{\\\"address1\\\":\\\"BHOLABAA\\\",\\\"address2\\\":\\\"RUPGANJA\\\",\\\"address3\\\":\\\"NOT POURASABHAA\\\",\\\"address8\\\":\\\"136961\\\"},\\\"countyDistrict\\\":\\\"NARAYANGANJA\\\",\\\"cityVillage\\\":\\\"TORAILA\\\",\\\"stateProvince\\\":\\\"DHAKAA\\\",\\\"country\\\":\\\"BANGLADESHA\\\"}],\\\"attributes\\\":{\\\"age\\\":\\\"35\\\",\\\"DOB_known\\\":\\\"no\\\",\\\"Blood_Group\\\":\\\"A+\\\",\\\"Mobile_Number\\\":\\\"0\\\",\\\"Marital_Status\\\":\\\"Married\\\",\\\"Relation_with_HOH\\\":\\\"Guest\\\",\\\"difficulty_seeing_hearing\\\":\\\"yes_little_difficulties\\\",\\\"difficulty_walking_up_down\\\":\\\"yes_little_difficulties\\\",\\\"trouble_remembering_concentrating\\\":\\\"no_difficulties\\\"},\\\"clientApplicationVersion\\\":31,\\\"clientApplicationVersionName\\\":\\\"1.3.6_DEV\\\",\\\"clientDatabaseVersion\\\":31,\\\"dateCreated\\\":\\\"2021-01-04T16:35:04.023Z\\\",\\\"type\\\":\\\"Client\\\",\\\"relationships\\\":{\\\"mother\\\":[\\\"\\\"],\\\"family\\\":[\\\"05b5acd9-d375-4892-aae3-2cefa17c7538\\\",\\\"05b5acd9-d375-4892-aae3-2cefa17c7538\\\"]}}]\"}";
	
	private final static String familyPayload = "{\"clients\": \"[{\\\"birthdate\\\":\\\"1970-01-01T06:00:00.000Z\\\",\\\"birthdateApprox\\\":false,\\\"deathdateApprox\\\":false,\\\"firstName\\\":\\\"Rashed\\\",\\\"lastName\\\":\\\"Family\\\",\\\"gender\\\":\\\"M\\\",\\\"baseEntityId\\\":\\\"0511acf9-6be8-4a98-b4f9-f8a5cfa704bb\\\",\\\"identifiers\\\":{\\\"opensrp_id\\\":\\\"50884400673070001601\\\"},\\\"addresses\\\":[{\\\"addressType\\\":\\\"usual_residence\\\",\\\"addressFields\\\":{\\\"address1\\\":\\\"BHOLABAQ\\\",\\\"address2\\\":\\\"RUPGANJq\\\",\\\"address3\\\":\\\"NOT POURASABHAq\\\",\\\"address8\\\":\\\"136961\\\"},\\\"countyDistrict\\\":\\\"NARAYANGANJq\\\",\\\"cityVillage\\\":\\\"Migrated Village\\\",\\\"stateProvince\\\":\\\"DHAKAq\\\",\\\"country\\\":\\\"BANGLADESHq\\\"}],\\\"attributes\\\":{\\\"Cluster\\\":\\\"1st_Cluster\\\",\\\"HH_Type\\\":\\\"BRAC VO\\\",\\\"SS_Name\\\":\\\"Forida(SS-1)\\\",\\\"module_id\\\":\\\"TRAINING\\\",\\\"serial_no\\\":\\\"H369\\\",\\\"village_id\\\":\\\"136962\\\",\\\"Has_Latrine\\\":\\\"No\\\",\\\"HOH_Phone_Number\\\":\\\"01471221551\\\",\\\"Number_of_HH_Member\\\":\\\"5\\\"},\\\"clientApplicationVersion\\\":31,\\\"clientApplicationVersionName\\\":\\\"1.3.6_DEV\\\",\\\"clientDatabaseVersion\\\":31,\\\"dateCreated\\\":\\\"2021-01-04T16:35:04.023Z\\\",\\\"type\\\":\\\"Client\\\",\\\"relationships\\\":{\\\"mother\\\":[\\\"\\\"],\\\"family_head\\\":[\\\"0511acf9-6be8-4a98-b4f9-f8a5cfa704bb\\\",\\\"0511acf9-6be8-4a98-b4f9-f8a5cfa704bb\\\"]}}]\"}";
	
	private final static String wrongPayload = "{\"clients\": \"[{\\\"birthdate\\\":\\\"1970-01-01T06:00:00.000Z\\\",\\\"birthdateApprox\\\":false,\\\"deathdateApprox\\\":false,\"firstName\":\\\"Rasheda\\\",\\\"gender\\\":\\\"M\\\",\\\"baseEntityId\\\":\\\"a6c836ea-800d-49ea-8565-0d7c58fdcb8c\\\",\\\"identifiers\\\":{\\\"opensrp_id\\\":\\\"608844006730700016010001\\\"},\\\"addresses\\\":[{\\\"addressType\\\":\\\"usual_residence\\\",\\\"addressFields\\\":{\\\"address1\\\":\\\"BHOLABAA\\\",\\\"address2\\\":\\\"RUPGANJA\\\",\\\"address3\\\":\\\"NOT POURASABHAA\\\",\\\"address8\\\":\\\"136961\\\"},\\\"countyDistrict\\\":\\\"NARAYANGANJA\\\",\\\"cityVillage\\\":\\\"TORAILA\\\",\\\"stateProvince\\\":\\\"DHAKAA\\\",\\\"country\\\":\\\"BANGLADESHA\\\"}],\\\"attributes\\\":{\\\"age\\\":\\\"35\\\",\\\"DOB_known\\\":\\\"no\\\",\\\"Blood_Group\\\":\\\"A+\\\",\\\"Mobile_Number\\\":\\\"0\\\",\\\"Marital_Status\\\":\\\"Married\\\",\\\"Relation_with_HOH\\\":\\\"Guest\\\",\\\"difficulty_seeing_hearing\\\":\\\"yes_little_difficulties\\\",\\\"difficulty_walking_up_down\\\":\\\"yes_little_difficulties\\\",\\\"trouble_remembering_concentrating\\\":\\\"no_difficulties\\\"},\\\"clientApplicationVersion\\\":31,\\\"clientApplicationVersionName\\\":\\\"1.3.6_DEV\\\",\\\"clientDatabaseVersion\\\":31,\\\"dateCreated\\\":\\\"2021-01-04T16:35:04.023Z\\\",\\\"type\\\":\\\"Client\\\",\\\"relationships\\\":{\\\"mother\\\":[\\\"\\\"],\\\"family\\\":[\\\"05b5acd9-d375-4892-aae3-2cefa17c7538\\\",\\\"05b5acd9-d375-4892-aae3-2cefa17c7538\\\"]}}]\"}";
	
	private final static String actualSearchString = "{\"clients\":[{\"type\":\"Client\",\"serverVersion\":0,\"baseEntityId\":\"a6c836ea-800d-49ea-8565-0d7c58fdcb8c\",\"identifiers\":{\"opensrp_id\":\"508844006730700016010001\"},\"addresses\":[{\"addressType\":\"usual_residence\",\"addressFields\":{\"address3\":\"NOT POURASABHA\",\"address2\":\"RUPGANJ\",\"address1\":\"BHOLABA\",\"address8\":\"136962\"},\"countyDistrict\":\"NARAYANGANJ\",\"cityVillage\":\"TORAIL\",\"stateProvince\":\"DHAKA\",\"country\":\"BBANGLADESH\"}],\"attributes\":{\"Relation_with_HOH\":\"Son\",\"Blood_Group\":\"A+\",\"difficulty_seeing_hearing\":\"yes_little_difficulties\",\"difficulty_walking_up_down\":\"yes_little_difficulties\",\"Marital_Status\":\"Married\",\"Home_Facility\":\"Linda\",\"age\":\"24\"},\"firstName\":\"Rasheda\",\"lastName\":\"\",\"birthdate\":\"2017-03-31T00:00:00.000+06:00\",\"birthdateApprox\":true,\"gender\":\"Male\",\"relationships\":{\"mother\":[\"0511acf9-6be8-4a98-b4f9-f8a5cfa704bC\"],\"family\":[\"0511acf9-6be8-4a98-b4f9-f8a5cfa704bb\"]}}]}";
	
	private final static String actualMigratedString = "[\"a6c836ea-800d-49ea-8565-0d7c58fdcb8c\",\"a6c836ea-800d-49ea-8565-0d7c58fdcb8b\"]";
	
	@Captor
	private ArgumentCaptor<Client> clientArgumentCaptor = ArgumentCaptor.forClass(Client.class);
	
	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
	
	@Captor
	private ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
	
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
		
		postRequestWithJsonContent(MIGRATE_URL + "/?username=testsk&districtId=2345&divisionId=234&type=HH&branchId=02",
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
		
	}
	
	@Test
	public void testdoMigrateFamilyWithNoExistingMigrationStatusOK() throws Exception {
		
		doReturn(createMhealthEventMetadata()).when(mhealthEventService).findFirstEventMetadata(anyString(), anyString());
		
		doReturn(null).when(mhealthMigrationService).findFirstMigrationBybaseEntityId(anyString());
		
		doReturn(createMhealthPractitionerLocation()).when(practitionerLocationService)
		        .generatePostfixAndLocation(anyString(), anyString(), anyString(), anyString());
		doReturn(true).when(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), anyString());
		
		postRequestWithJsonContent(MIGRATE_URL + "/?username=testsk&districtId=2345&divisionId=234&type=HH&branchId=02",
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
		
	}
	
	@Test
	public void testdoMigrateMemberStatusBadRequest() throws Exception {
		
		doReturn(createMhealthEventMetadata()).when(mhealthEventService).findFirstEventMetadata(anyString(), anyString());
		
		doReturn(createMhealthMigration()).when(mhealthMigrationService).findFirstMigrationBybaseEntityId(anyString());
		
		doReturn(createMhealthPractitionerLocation()).when(practitionerLocationService)
		        .generatePostfixAndLocation(anyString(), anyString(), anyString(), anyString());
		doReturn(true).when(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), anyString());
		
		postRequestWithJsonContent(MIGRATE_URL + "/?username=testsk&districtId=2345&divisionId=234&type=HH&branchId=02",
		    "{}", status().isBadRequest());
		
	}
	
	@Test
	public void testdoMigrateMemberStatusIntenalServerError() throws Exception {
		
		doReturn(createMhealthEventMetadata()).when(mhealthEventService).findFirstEventMetadata(anyString(), anyString());
		
		doReturn(createMhealthMigration()).when(mhealthMigrationService).findFirstMigrationBybaseEntityId(anyString());
		
		doReturn(createMhealthPractitionerLocation()).when(practitionerLocationService)
		        .generatePostfixAndLocation(anyString(), anyString(), anyString(), anyString());
		doReturn(true).when(mhealthMigrationService).migrate(clientArgumentCaptor.capture(), any(JSONObject.class),
		    any(MhealthPractitionerLocation.class), any(MhealthPractitionerLocation.class), anyString());
		postRequestWithJsonContent(MIGRATE_URL + "/?username=testsk&districtId=2345&divisionId=234&type=HH&branchId=02",
		    wrongPayload, status().isInternalServerError());
	}
	
	@Test
	public void testAcceptRejectMigrationStatusOK() throws Exception {
		
		doReturn(true).when(mhealthMigrationService).acceptOrRejectMigration(any(long.class), anyString(), anyString(),
		    anyString());
		
		postRequestWithJsonContent(
		    ACCEPT_REJECT_URL + "/?id=1&relationalId=0511acf9-6be8-4a98-b4f9-f8a5cfa704bb&type=HH&status=REJECT", "",
		    status().isCreated());
		
		verify(mhealthMigrationService).acceptOrRejectMigration(longArgumentCaptor.capture(), stringArgumentCaptor.capture(),
		    stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "0511acf9-6be8-4a98-b4f9-f8a5cfa704bb");
		assertEquals(longArgumentCaptor.getAllValues().get(0) + "", "1");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "HH");
		assertEquals(stringArgumentCaptor.getAllValues().get(2), "REJECT");
		
	}
	
	@Test
	public void testAcceptRejectMigrationStatusBadRequest() throws Exception {
		doReturn(true).when(mhealthMigrationService).acceptOrRejectMigration(any(long.class), anyString(), anyString(),
		    anyString());
		postRequestWithJsonContent(
		    ACCEPT_REJECT_URL + "/?id=0&relationalId=0511acf9-6be8-4a98-b4f9-f8a5cfa704bb&type=HH&status=REJECT", "",
		    status().isBadRequest());
	}
	
	@Test
	public void testAcceptRejectMigrationStatusnternalServerError() throws Exception {
		doReturn(true).when(mhealthMigrationService).acceptOrRejectMigration(any(long.class), anyString(), anyString(),
		    anyString());
		postRequestWithJsonContent(
		    ACCEPT_REJECT_URL + "/?relationalId=0511acf9-6be8-4a98-b4f9-f8a5cfa704bb&type=HH&status=REJECT", "",
		    status().isInternalServerError());
	}
	
	@Test
	public void testSearchClientStatusOK() throws Exception {
		doReturn(createClients()).when(mhealthClientService).searchClientForMigration(any(int.class), anyString(),
		    any(int.class), any(int.class), anyString(), anyString());
		String parameter = "villageId=123&districtId=2345&gender=M&startAge=1&endAge=23&type=HH";
		String response = getResponseAsString(SEARCH_CLIENT, parameter, status().isOk());
		verify(mhealthClientService).searchClientForMigration(integerArgumentCaptor.capture(),
		    stringArgumentCaptor.capture(), integerArgumentCaptor.capture(), integerArgumentCaptor.capture(),
		    stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(actualSearchString, response);
		assertEquals(integerArgumentCaptor.getAllValues().get(0) + "", "123");
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "M");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "HH");
		
	}
	
	@Test
	public void testSearchClientStatusBadRequest() throws Exception {
		doReturn(createClients()).when(mhealthClientService).searchClientForMigration(any(int.class), anyString(),
		    any(int.class), any(int.class), anyString(), anyString());
		String parameter = "villageId=0&districtId=2345&gender=M&startAge=1&endAge=23&type=HH";
		getResponseAsString(SEARCH_CLIENT, parameter, status().isBadRequest());
	}
	
	@Test
	public void testGetMigratedListStatusOK() throws Exception {
		doReturn(getList()).when(mhealthMigrationService).getMigratedList(anyString(), anyString(), any(long.class));
		String parameter = "username=testsk&type=HH&timestamp=0";
		String response = getResponseAsString(MIGRATED, parameter, status().isOk());
		verify(mhealthMigrationService).getMigratedList(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
		    longArgumentCaptor.capture());
		assertEquals(actualMigratedString, response);
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "testsk");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "HH");
		
	}
	
	@Test
	public void testGetEmptyMigratedListStatusOK() throws Exception {
		doReturn(new ArrayList<String>()).when(mhealthMigrationService).getMigratedList(anyString(), anyString(),
		    any(long.class));
		String parameter = "username=testsk&type=HH&timestamp=0";
		String response = getResponseAsString(MIGRATED, parameter, status().isOk());
		verify(mhealthMigrationService).getMigratedList(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
		    longArgumentCaptor.capture());
		assertEquals("[]", response);
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "testsk");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "HH");
		
	}
	
	@Test
	public void testGetRejectedListStatusOK() throws Exception {
		doReturn(getList()).when(mhealthMigrationService).getRejectedList(anyString(), anyString(), any(long.class));
		String parameter = "username=testsk&type=HH&timestamp=0";
		String response = getResponseAsString(REJECTED, parameter, status().isOk());
		verify(mhealthMigrationService).getRejectedList(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
		    longArgumentCaptor.capture());
		assertEquals(actualMigratedString, response);
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "testsk");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "HH");
		
	}
	
	@Test
	public void testGetEmptyRejectedListStatusOK() throws Exception {
		doReturn(new ArrayList<String>()).when(mhealthMigrationService).getRejectedList(anyString(), anyString(),
		    any(long.class));
		String parameter = "username=testsk&type=HH&timestamp=0";
		String response = getResponseAsString(REJECTED, parameter, status().isOk());
		verify(mhealthMigrationService).getRejectedList(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
		    longArgumentCaptor.capture());
		assertEquals("[]", response);
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "testsk");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "HH");
		
	}
	
	public List<String> getList() {
		List<String> list = new ArrayList<String>();
		list.add("a6c836ea-800d-49ea-8565-0d7c58fdcb8c");
		list.add("a6c836ea-800d-49ea-8565-0d7c58fdcb8b");
		return list;
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
	
	public List<Client> createClients() {
		List<Client> clients = new ArrayList<Client>();
		Client client = new Client("a6c836ea-800d-49ea-8565-0d7c58fdcb8c").withBirthdate(new DateTime("2017-03-31"), true)
		        .withGender("Male").withFirstName("Rasheda").withLastName("");
		List<Address> addresses = new ArrayList<Address>();
		Address address = new Address();
		address.setCountry("BBANGLADESH");
		address.setAddressType("usual_residence");
		address.setCityVillage("TORAIL");
		address.setStateProvince("DHAKA");
		address.setCountyDistrict("NARAYANGANJ");
		
		Map<String, String> addressFields = new HashMap<>();
		addressFields.put("address1", "BHOLABA");
		addressFields.put("address2", "RUPGANJ");
		addressFields.put("address3", "NOT POURASABHA");
		addressFields.put("address8", "136962");
		address.setAddressFields(addressFields);
		addresses.add(address);
		Map<String, List<String>> relationships = new HashMap<>();
		List<String> relationalIds = new ArrayList<>();
		relationalIds.add("0511acf9-6be8-4a98-b4f9-f8a5cfa704bb");
		List<String> mother = new ArrayList<>();
		mother.add("0511acf9-6be8-4a98-b4f9-f8a5cfa704bC");
		relationships.put("family", relationalIds);
		relationships.put("mother", mother);
		client.setAddresses(addresses);
		client.setRelationships(relationships);
		client.setServerVersion(0);
		client.withIdentifier("opensrp_id", "508844006730700016010001").withAttribute("Home_Facility", "Linda")
		        .withAttribute("Marital_Status", "Married")
		        .withAttribute("difficulty_seeing_hearing", "yes_little_difficulties")
		        .withAttribute("difficulty_walking_up_down", "yes_little_difficulties").withAttribute("Blood_Group", "A+")
		        .withAttribute("Relation_with_HOH", "Son").withAttribute("age", "24");
		clients.add(client);
		return clients;
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<MhealthMigration> expectedList, List<MhealthMigration> actualList) {
		
	}
	
}

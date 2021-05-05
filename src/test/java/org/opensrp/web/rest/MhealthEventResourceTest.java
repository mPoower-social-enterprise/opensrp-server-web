package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opensrp.common.AllConstants.BaseEntity.SERVER_VERSIOIN;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opensrp.domain.postgres.MhealthPractitionerLocation;
import org.opensrp.search.EventSearchBean;
import org.opensrp.service.MhealthClientService;
import org.opensrp.service.MhealthEventService;
import org.opensrp.service.PractitionerLocationService;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.smartregister.utils.DateTimeTypeConverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MhealthEventResourceTest extends BaseSecureResourceTest<Event> {
	
	private final static String BASE_URL = "/rest/event/mhealth";
	
	private String eventType = "Spray";
	
	private MhealthEventService mhealthEventService;
	
	private MhealthClientService mhealthClientService;
	
	private PractitionerLocationService practitionerLocationService;
	
	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
	
	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
	
	@Captor
	private ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
	
	@Captor
	private ArgumentCaptor<Boolean> booleanArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
	
	@Captor
	private ArgumentCaptor<EventSearchBean> eventSearchBeanArgumentCaptor = ArgumentCaptor.forClass(EventSearchBean.class);
	
	@Captor
	private ArgumentCaptor<Client> clientArgumentCaptor = ArgumentCaptor.forClass(Client.class);
	
	@Captor
	private ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
	
	private MhealthEventResource mhealthEventResource;
	
	private String ADD_REQUEST_PAYLOAD = "{\n"
	        + "\t\"clients\": \"[{\\\"birthdate\\\":\\\"1970-01-01T05:00:00.000Z\\\",\\\"firstName\\\":\\\"Test\\\",\\\"gender\\\":\\\"Male\\\",\\\"lastName\\\":\\\"User\\\" , \\\"baseEntityId\\\":\\\"502f5f2d-5a06-4f71-8f8a-b19a846b9a93\\\"}]\",\n"
	        + "\t\"events\": \"[{\\\"baseEntityId\\\":\\\"502f5f2d-5a06-4f71-8f8a-b19a846b9a93\\\",\\\"eventType\\\":\\\"Family Member Registration\\\",\\\"entityType\\\":\\\"ec_family\\\",\\\"eventDate\\\":\\\"2020-05-02T23:26:21.685Z\\\"}]\"\n"
	        + "}";
	
	private String ADD_REQUEST_EMPTY_PAYLOAD = "{\n" + "\t\"client\": \"[]\",\n" + "\t\"event\": \"[]\"\n" + "}";
	
	public MhealthEventResourceTest() throws IOException {
		super();
	}
	
	@Before
	public void setUp() {
		mhealthEventService = mock(MhealthEventService.class);
		mhealthClientService = mock(MhealthClientService.class);
		practitionerLocationService = mock(PractitionerLocationService.class);
		mhealthEventResource = webApplicationContext.getBean(MhealthEventResource.class);
		mhealthEventResource.setMhealthClientService(mhealthClientService);
		mhealthEventResource.setMhealthEventService(mhealthEventService);
		mhealthEventResource.setPractitionerLocationService(practitionerLocationService);
		mhealthEventResource.setObjectMapper(mapper);
	}
	
	@Test
	public void testSaveWihtStatusOK() throws Exception {
		Client client = createClient();
		Event event = createEvent();
		
		doReturn(client).when(mhealthClientService).addOrUpdate(any(Client.class), any(MhealthPractitionerLocation.class));
		doReturn(event).when(mhealthEventService).addorUpdateEvent(any(Event.class), anyString(),
		    any(MhealthPractitionerLocation.class));
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		
		postRequestWithJsonContent(BASE_URL + "/add?district=12&division=123&branch=2", ADD_REQUEST_PAYLOAD,
		    status().isCreated());
		verify(mhealthClientService).addOrUpdate(clientArgumentCaptor.capture(), any(MhealthPractitionerLocation.class));
		
		assertEquals(clientArgumentCaptor.getValue().getFirstName(), "Test");
		verify(mhealthEventService).addorUpdateEvent(eventArgumentCaptor.capture(), anyString(),
		    any(MhealthPractitionerLocation.class));
		
		assertEquals(eventArgumentCaptor.getValue().getEventType(), "Family Member Registration");
	}
	
	@Test
	public void testSaveWihtStatusBadRequest() throws Exception {
		Client client = createClient();
		Event event = createEvent();
		doReturn(client).when(mhealthClientService).addOrUpdate(any(Client.class), any(MhealthPractitionerLocation.class));
		doReturn(event).when(mhealthEventService).addorUpdateEvent(any(Event.class), anyString(),
		    any(MhealthPractitionerLocation.class));
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		postRequestWithJsonContent(BASE_URL + "/add?district=12&division=123&branch=2", ADD_REQUEST_EMPTY_PAYLOAD,
		    status().isBadRequest());
		
	}
	
	@Test
	public void testSaveWihtStatusBadRequestWithoutBranch() throws Exception {
		Client client = createClient();
		Event event = createEvent();
		doReturn(client).when(mhealthClientService).addOrUpdate(any(Client.class), any(MhealthPractitionerLocation.class));
		doReturn(event).when(mhealthEventService).addorUpdateEvent(any(Event.class), anyString(),
		    any(MhealthPractitionerLocation.class));
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		postRequestWithJsonContent(BASE_URL + "/add?district=12&division=123", ADD_REQUEST_EMPTY_PAYLOAD,
		    status().isBadRequest());
		
	}
	
	@Test
	public void testSaveWihtStatusBadRequestWithoutDivision() throws Exception {
		Client client = createClient();
		Event event = createEvent();
		doReturn(client).when(mhealthClientService).addOrUpdate(any(Client.class), any(MhealthPractitionerLocation.class));
		doReturn(event).when(mhealthEventService).addorUpdateEvent(any(Event.class), anyString(),
		    any(MhealthPractitionerLocation.class));
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		postRequestWithJsonContent(BASE_URL + "/add?district=12&branch=2", ADD_REQUEST_EMPTY_PAYLOAD,
		    status().isBadRequest());
		
	}
	
	@Test
	public void testSaveWihtStatusBadRequestWithoutDistrict() throws Exception {
		Client client = createClient();
		Event event = createEvent();
		doReturn(client).when(mhealthClientService).addOrUpdate(any(Client.class), any(MhealthPractitionerLocation.class));
		doReturn(event).when(mhealthEventService).addorUpdateEvent(any(Event.class), anyString(),
		    any(MhealthPractitionerLocation.class));
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		postRequestWithJsonContent(BASE_URL + "/add?division=123&branch=2", ADD_REQUEST_EMPTY_PAYLOAD,
		    status().isBadRequest());
		
	}
	
	@Test
	public void testSaveThrowsExceptionFromClientService() throws Exception {
		Event event = createEvent();
		doThrow(new IllegalArgumentException()).when(mhealthClientService).addOrUpdate(any(Client.class),
		    any(MhealthPractitionerLocation.class));
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		doReturn(event).when(mhealthEventService).addorUpdateEvent(any(Event.class), anyString(),
		    any(MhealthPractitionerLocation.class));
		postRequestWithJsonContent(BASE_URL + "/add?district=12&division=123&branch=2", ADD_REQUEST_PAYLOAD,
		    status().isCreated());
		verify(mhealthClientService).addOrUpdate(clientArgumentCaptor.capture(), any(MhealthPractitionerLocation.class));
		assertEquals(clientArgumentCaptor.getValue().getFirstName(), "Test");
		verify(mhealthEventService).addorUpdateEvent(eventArgumentCaptor.capture(), anyString(),
		    any(MhealthPractitionerLocation.class));
		assertEquals(eventArgumentCaptor.getValue().getEventType(), "Family Member Registration");
	}
	
	@Test
	public void testSaveThrowsExceptionFromEventService() throws Exception {
		Client client = createClient();
		
		doReturn(client).when(mhealthClientService).addOrUpdate(any(Client.class), any(MhealthPractitionerLocation.class));
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		doThrow(new IllegalArgumentException()).when(mhealthEventService).addorUpdateEvent(any(Event.class), anyString(),
		    any(MhealthPractitionerLocation.class));
		postRequestWithJsonContent(BASE_URL + "/add?district=12&division=123&branch=2", ADD_REQUEST_PAYLOAD,
		    status().isCreated());
		verify(mhealthClientService).addOrUpdate(clientArgumentCaptor.capture(), any(MhealthPractitionerLocation.class));
		assertEquals(clientArgumentCaptor.getValue().getFirstName(), "Test");
		verify(mhealthEventService).addorUpdateEvent(eventArgumentCaptor.capture(), anyString(),
		    any(MhealthPractitionerLocation.class));
		assertEquals(eventArgumentCaptor.getValue().getEventType(), "Family Member Registration");
	}
	
	@Test
	public void testGetSyncByVillageIdsStatusOK() throws Exception {
		List<Event> expectedEvents = new ArrayList<>();
		expectedEvents.add(createEvent());
		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		
		doReturn(expectedEvents).when(mhealthEventService).findByVillageIds(anyString(), anyList(), any(long.class),
		    any(int.class), anyString());
		doReturn(createClient()).when(mhealthClientService).findByBaseEntityId(anyString(), anyString());
		doReturn(expectedClients).when(mhealthClientService).findByBaseEntityIds(anyList(), anyString());
		
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		doReturn(getVillageIds()).when(practitionerLocationService).getPractitionerVillageIds(anyString());
		
		String parameter = PROVIDER_ID + "=providerId&" + SERVER_VERSIOIN
		        + "=15421904649873&isEmptyToAdd=true&villageIds=1,27&district=23";
		String response = getResponseAsString(BASE_URL + "/sync", parameter, status().isOk());
		JsonNode actualObj = mapper.readTree(response);
		verify(mhealthEventService).findByVillageIds(stringArgumentCaptor.capture(), anyList(), longArgumentCaptor.capture(),
		    integerArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(integerArgumentCaptor.getValue(), new Integer(25));
		assertEquals(stringArgumentCaptor.getAllValues().get(0), PROVIDER_ID);
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "_23");
		assertEquals(actualObj.size(), 4);
		assertEquals(actualObj.get("clients").size(), 1);
		assertEquals(actualObj.get("events").size(), 1);
	}
	
	@Test
	public void testGetSyncByWithoutVillageIdsStatusOK() throws Exception {
		List<Event> expectedEvents = new ArrayList<>();
		expectedEvents.add(createEvent());
		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		
		doReturn(expectedEvents).when(mhealthEventService).findByVillageIds(anyString(), anyList(), any(long.class),
		    any(int.class), anyString());
		doReturn(createClient()).when(mhealthClientService).findByBaseEntityId(anyString(), anyString());
		doReturn(expectedClients).when(mhealthClientService).findByBaseEntityIds(anyList(), anyString());
		
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		doReturn(getVillageIds()).when(practitionerLocationService).getPractitionerVillageIds(anyString());
		
		String parameter = PROVIDER_ID + "=providerId&" + SERVER_VERSIOIN + "=15421904649873&isEmptyToAdd=true&district=23";
		String response = getResponseAsString(BASE_URL + "/sync", parameter, status().isOk());
		JsonNode actualObj = mapper.readTree(response);
		verify(mhealthEventService).findByVillageIds(stringArgumentCaptor.capture(), anyList(), longArgumentCaptor.capture(),
		    integerArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(integerArgumentCaptor.getValue(), new Integer(25));
		assertEquals(stringArgumentCaptor.getAllValues().get(0), PROVIDER_ID);
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "_23");
		assertEquals(actualObj.size(), 4);
		assertEquals(actualObj.get("clients").size(), 1);
		assertEquals(actualObj.get("events").size(), 1);
	}
	
	@Test
	public void testGetSyncByProviderStatusOK() throws Exception {
		List<Event> expectedEvents = new ArrayList<>();
		expectedEvents.add(createEvent());
		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		
		doReturn(expectedEvents).when(mhealthEventService).findByProvider(any(long.class), anyString(), any(int.class),
		    anyString());
		doReturn(createClient()).when(mhealthClientService).findByBaseEntityId(anyString(), anyString());
		doReturn(expectedClients).when(mhealthClientService).findByBaseEntityIds(anyList(), anyString());
		
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		doReturn(getVillageIds()).when(practitionerLocationService).getPractitionerVillageIds(anyString());
		
		String parameter = PROVIDER_ID + "=providerId&" + SERVER_VERSIOIN
		        + "=15421904649873&isEmptyToAdd=false&villageIds=1,27&district=23";
		String response = getResponseAsString(BASE_URL + "/sync", parameter, status().isOk());
		JsonNode actualObj = mapper.readTree(response);
		verify(mhealthEventService).findByProvider(longArgumentCaptor.capture(), stringArgumentCaptor.capture(),
		    integerArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), PROVIDER_ID);
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "_23");
		assertEquals(actualObj.size(), 4);
		assertEquals(actualObj.get("clients").size(), 1);
		assertEquals(actualObj.get("events").size(), 1);
	}
	
	@Test
	public void testGetSyncStatusBadRequest() throws Exception {
		List<Event> expectedEvents = new ArrayList<>();
		expectedEvents.add(createEvent());
		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		
		doReturn(expectedEvents).when(mhealthEventService).findByProvider(any(long.class), anyString(), any(int.class),
		    anyString());
		doReturn(createClient()).when(mhealthClientService).findByBaseEntityId(anyString(), anyString());
		doReturn(expectedClients).when(mhealthClientService).findByBaseEntityIds(anyList(), anyString());
		
		doReturn(generatePostfixAndLocation()).when(practitionerLocationService).generatePostfixAndLocation(anyString(),
		    anyString(), anyString(), anyString());
		doReturn(getVillageIds()).when(practitionerLocationService).getPractitionerVillageIds(anyString());
		
		String parameter = SERVER_VERSIOIN + "=15421904649873&isEmptyToAdd=false";
		getResponseAsString(BASE_URL + "/sync", parameter, status().isBadRequest());
		
	}
	
	private Event createEvent() {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
		        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
		String expectedEventString = "{\"identifiers\":{},\"baseEntityId\":\"5dd43b2e-a873-444b-b527-95c4b040a5bb\",\"locationId\":\"fb7ed5db-138d-4e6f-94d8-bc443b58dadb\",\"eventDate\":\"2020-02-14T03:00:00.000+03:00\",\"eventType\":\"Family Member Registration\",\"formSubmissionId\":\"a2fba8d2-42f5-4811-b982-57609f1815fe\",\"providerId\":\"unifiedchwone\",\"duration\":0,\"obs\":[{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"same_as_fam_name\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"same_as_fam_name\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"Baba\"],\"set\":[],\"formSubmissionField\":\"fam_name\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"43\"],\"set\":[],\"formSubmissionField\":\"age_calculated\",\"humanReadableValues\":[]},{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"dob_unknown\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"dob_unknown\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"1\"],\"set\":[],\"formSubmissionField\":\"wra\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"0\"],\"set\":[],\"formSubmissionField\":\"mra\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"160692AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"preg_1yr\",\"humanReadableValues\":[\"No\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"162558AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"disabilities\",\"humanReadableValues\":[\"No\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[null],\"set\":[],\"formSubmissionField\":\"is_primary_caregiver\",\"humanReadableValues\":[\"Yes\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"values\":[\"163096AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"service_provider\",\"humanReadableValues\":[\"Community IMCI\"]},{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"last_interacted_with\",\"parentCode\":\"\",\"values\":[\"1581697252432\"],\"set\":[],\"formSubmissionField\":\"last_interacted_with\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"start\",\"fieldCode\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"2020-02-14 19:19:58\"],\"set\":[],\"formSubmissionField\":\"start\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"end\",\"fieldCode\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"2020-02-14 19:20:52\"],\"set\":[],\"formSubmissionField\":\"end\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"deviceid\",\"fieldCode\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"359050095070928\"],\"set\":[],\"formSubmissionField\":\"deviceid\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"subscriberid\",\"fieldCode\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"639070028267663\"],\"set\":[],\"formSubmissionField\":\"subscriberid\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"simserial\",\"fieldCode\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"89254070000282676636\"],\"set\":[],\"formSubmissionField\":\"simserial\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"162849AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"wra\",\"humanReadableValues\":[]}],\"entityType\":\"ec_family_member\",\"version\":1581697252446,\"teamId\":\"de7d5dbe-6d21-4300-a72e-6eee14712f62\",\"team\":\"Madona\",\"dateCreated\":\"2020-02-14T19:21:21.295+03:00\",\"serverVersion\":1581697281295,\"clientApplicationVersion\":2,\"clientDatabaseVersion\":13,\"type\":\"Event\",\"id\":\"24032715-7d65-434d-b50a-0b96ec63996a\",\"revision\":\"v1\"}";
		return gson.fromJson(expectedEventString, new TypeToken<Event>() {}.getType());
	}
	
	private Client createClient() {
		Client client = new Client("base-entity-id");
		client.setFirstName("Test");
		client.setLastName("User");
		client.setId("1");
		client.setDateCreated(new DateTime());
		client.setDateEdited(new DateTime());
		return client;
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<Event> expectedList, List<Event> actualList) {
		// TODO Auto-generated method stub
		
	}
	
	private MhealthPractitionerLocation generatePostfixAndLocation() {
		MhealthPractitionerLocation location = new MhealthPractitionerLocation();
		location.setBranch("2");
		location.setDistrict("23");
		location.setDivision("123");
		location.setPostFix("_23");
		return location;
	}
	
	private List<Integer> getVillageIds() {
		List<Integer> ids = new ArrayList<>();
		ids.add(1);
		ids.add(27);
		return ids;
	}
}

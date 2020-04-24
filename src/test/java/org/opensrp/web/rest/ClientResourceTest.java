package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.Client;
import org.opensrp.domain.postgres.HouseholdClient;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.web.bean.ClientSyncBean;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class ClientResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private ClientService clientService;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private ClientResource clientResource;

	protected ObjectMapper mapper = new ObjectMapper();

	private final String BASE_URL = "/rest/client";

	private static final String PAGE_SIZE = "pageSize";

	private static final String PAGE_NUMBER = "pageNumber";
	private static final String SEARCHTEXT = "searchText";
	private static final String GENDER = "gender";
	private static final String CLIENTTYPE = "clientType";
	public static final String ALLCLIENTS = "clients";

	private String EXPECTED_CLIENT_SYNC_BEAN_RESPONSE_JSON =  "{\n"
			+ "\t\"clients\": [{\n"
			+ "\t\t\"firstName\": \"Test\",\n"
			+ "\t\t\"lastName\": \"User\"\n"
			+ "\t}],\n"
			+ "\t\"total\": 1\n"
			+ "}";


	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(clientResource)
				.build();
		clientResource.setObjectMapper(objectMapper);
	}

	@Test
	public void testGetByUniqueId() {
		Client expected = createClient();
		when(clientService.find(any(String.class))).thenReturn(expected);
		Client actual = clientResource.getByUniqueId("1");
		assertEquals(actual.getFirstName(),expected.getFirstName());
		assertEquals(actual.getLastName(),expected.getLastName());
		assertEquals(actual.getId(),expected.getId());
		assertEquals(actual.getBaseEntityId(),expected.getBaseEntityId());
	}

	@Test
	public void testCreateClient() {
		Client expected = createClient();
		when(clientService.addClient(any(Client.class))).thenReturn(expected);
		Client obj = new Client("base-entity-id");
		Client actual = clientResource.create(obj);
		assertEquals(actual.getId(),expected.getId());
		assertEquals(actual.getDateCreated(),expected.getDateCreated());
	}

	@Test
	public void testUpdateClient() {
		Client expected = createClient();
		when(clientService.mergeClient(any(Client.class))).thenReturn(expected);
		Client obj = new Client("base-entity-id");
		Client actual = clientResource.update(obj);
		assertEquals(actual.getId(),expected.getId());
		assertEquals(actual.getDateEdited(),expected.getDateEdited());
	}

	@Test
	public void testSearchByCriteria() throws Exception {

		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		HouseholdClient householdClient = new HouseholdClient();
		householdClient.setTotalCount(1);

		when(clientService.findAllClientsByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(expectedClients);
		when(clientService.findTotalCountHouseholdByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(householdClient);
		when(objectMapper.writeValueAsString(any(Object.class))).thenReturn(EXPECTED_CLIENT_SYNC_BEAN_RESPONSE_JSON);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/searchByCriteria").
				param(AllConstants.BaseEntity.BASE_ENTITY_ID, "15421904649873")
				.param(PAGE_NUMBER, "1").param(PAGE_SIZE, "10").param(SEARCHTEXT, "abc").param(GENDER, "male")
				.param(CLIENTTYPE, ALLCLIENTS))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		ClientSyncBean response = mapper.treeToValue(actualObj, ClientSyncBean.class);

		assertEquals(response.getClients().size(),1);
		assertEquals(response.getTotal().intValue(), 1);
		assertEquals(response.getClients().get(0).getFirstName(), "Test");
		assertEquals(response.getClients().get(0).getLastName(), "User");

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


}
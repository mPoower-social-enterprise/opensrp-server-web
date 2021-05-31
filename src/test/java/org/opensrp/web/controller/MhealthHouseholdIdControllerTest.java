package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.List;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.opensrp.domain.postgres.HouseholdId;
import org.opensrp.service.HouseholdIdService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.BaseResourceTest;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class MhealthHouseholdIdControllerTest extends BaseResourceTest<HouseholdId> {
	
	private final static String BASE_URL = "/household/";
	
	protected MockMvc mockMvc;
	
	private HouseholdIdService householdIdService;
	
	private final static String expectedHouseholdIds = "[{\"village_id\":7,\"generated_code\":[\"0001\",\"0002\",\"0003\",\"0004\",\"0005\",\"0006\",\"0007\",\"0008\",\"0009\",\"0010\",\"0011\",\"0012\",\"0013\",\"0014\",\"0015\",\"0016\",\"0017\",\"0018\",\"0019\",\"0020\",\"0021\",\"0022\",\"0023\",\"0024\",\"0025\",\"0026\",\"0027\",\"0028\",\"0029\",\"0030\",\"0031\",\"0032\",\"0033\",\"0034\",\"0035\",\"0036\",\"0037\",\"0038\",\"0039\",\"0040\",\"0041\",\"0042\",\"0043\",\"0044\",\"0045\",\"0046\",\"0047\",\"0048\",\"0049\",\"0050\",\"0051\",\"0052\",\"0053\",\"0054\",\"0055\",\"0056\",\"0057\",\"0058\",\"0059\",\"0060\",\"0061\",\"0062\",\"0063\",\"0064\",\"0065\",\"0066\",\"0067\",\"0068\",\"0069\",\"0070\",\"0071\",\"0072\",\"0073\",\"0074\",\"0075\",\"0076\",\"0077\",\"0078\",\"0079\",\"0080\",\"0081\",\"0082\",\"0083\",\"0084\",\"0085\",\"0086\",\"0087\",\"0088\",\"0089\",\"0090\",\"0091\",\"0092\",\"0093\",\"0094\",\"0095\",\"0096\",\"0097\",\"0098\",\"0099\",\"0100\",\"0101\",\"0102\",\"0103\",\"0104\",\"0105\",\"0106\",\"0107\",\"0108\",\"0109\",\"0110\",\"0111\",\"0112\",\"0113\",\"0114\",\"0115\",\"0116\",\"0117\",\"0118\",\"0119\",\"0120\",\"0121\",\"0122\",\"0123\",\"0124\",\"0125\",\"0126\",\"0127\",\"0128\",\"0129\",\"0130\",\"0131\",\"0132\",\"0133\",\"0134\",\"0135\",\"0136\",\"0137\",\"0138\",\"0139\",\"0140\",\"0141\",\"0142\",\"0143\",\"0144\",\"0145\",\"0146\",\"0147\",\"0148\",\"0149\",\"0150\",\"0151\",\"0152\",\"0153\",\"0154\",\"0155\",\"0156\",\"0157\",\"0158\",\"0159\",\"0160\",\"0161\",\"0162\",\"0163\",\"0164\",\"0165\",\"0166\",\"0167\",\"0168\",\"0169\",\"0170\",\"0171\",\"0172\",\"0173\",\"0174\",\"0175\",\"0176\",\"0177\",\"0178\",\"0179\",\"0180\",\"0181\",\"0182\",\"0183\",\"0184\",\"0185\",\"0186\",\"0187\",\"0188\",\"0189\",\"0190\",\"0191\",\"0192\",\"0193\",\"0194\",\"0195\",\"0196\",\"0197\",\"0198\",\"0199\",\"0200\",\"0201\"]}]";
	
	private final static String expectedGuetHouseholdIds = "[{\"village_id\":7,\"generated_code\":[\"00001\",\"00002\",\"00003\",\"00004\",\"00005\",\"00006\",\"00007\",\"00008\",\"00009\",\"00010\",\"00011\",\"00012\",\"00013\",\"00014\",\"00015\",\"00016\",\"00017\",\"00018\",\"00019\",\"00020\",\"00021\",\"00022\",\"00023\",\"00024\",\"00025\",\"00026\",\"00027\",\"00028\",\"00029\",\"00030\",\"00031\",\"00032\",\"00033\",\"00034\",\"00035\",\"00036\",\"00037\",\"00038\",\"00039\",\"00040\",\"00041\",\"00042\",\"00043\",\"00044\",\"00045\",\"00046\",\"00047\",\"00048\",\"00049\",\"00050\",\"00051\",\"00052\",\"00053\",\"00054\",\"00055\",\"00056\",\"00057\",\"00058\",\"00059\",\"00060\",\"00061\",\"00062\",\"00063\",\"00064\",\"00065\",\"00066\",\"00067\",\"00068\",\"00069\",\"00070\",\"00071\",\"00072\",\"00073\",\"00074\",\"00075\",\"00076\",\"00077\",\"00078\",\"00079\",\"00080\",\"00081\",\"00082\",\"00083\",\"00084\",\"00085\",\"00086\",\"00087\",\"00088\",\"00089\",\"00090\",\"00091\",\"00092\",\"00093\",\"00094\",\"00095\",\"00096\",\"00097\",\"00098\",\"00099\",\"00100\",\"00101\",\"00102\",\"00103\",\"00104\",\"00105\",\"00106\",\"00107\",\"00108\",\"00109\",\"00110\",\"00111\",\"00112\",\"00113\",\"00114\",\"00115\",\"00116\",\"00117\",\"00118\",\"00119\",\"00120\",\"00121\",\"00122\",\"00123\",\"00124\",\"00125\",\"00126\",\"00127\",\"00128\",\"00129\",\"00130\",\"00131\",\"00132\",\"00133\",\"00134\",\"00135\",\"00136\",\"00137\",\"00138\",\"00139\",\"00140\",\"00141\",\"00142\",\"00143\",\"00144\",\"00145\",\"00146\",\"00147\",\"00148\",\"00149\",\"00150\",\"00151\",\"00152\",\"00153\",\"00154\",\"00155\",\"00156\",\"00157\",\"00158\",\"00159\",\"00160\",\"00161\",\"00162\",\"00163\",\"00164\",\"00165\",\"00166\",\"00167\",\"00168\",\"00169\",\"00170\",\"00171\",\"00172\",\"00173\",\"00174\",\"00175\",\"00176\",\"00177\",\"00178\",\"00179\",\"00180\",\"00181\",\"00182\",\"00183\",\"00184\",\"00185\",\"00186\",\"00187\",\"00188\",\"00189\",\"00190\",\"00191\",\"00192\",\"00193\",\"00194\",\"00195\",\"00196\",\"00197\",\"00198\",\"00199\",\"00200\",\"00201\"]}]";
	
	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
	
	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity())
		        .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		householdIdService = mock(HouseholdIdService.class);
		MhealthHouseholdIdController mhealthHouseholdIdController = webApplicationContext
		        .getBean(MhealthHouseholdIdController.class);
		mhealthHouseholdIdController.setHouseholdIdService(householdIdService);
	}
	
	@Test
	public void testGetHouseholdUniqueIdByUsernameWithStatusOK() throws Exception {
		
		when(householdIdService.generateHouseholdId("p1", "")).thenReturn(generateHouseholdId());
		String actualResult = getResponseAsString(BASE_URL + "generated-code/?username=p1&villageId=", null,
		    MockMvcResultMatchers.status().isOk());
		
		verify(householdIdService).generateHouseholdId(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "p1");
		assertEquals(expectedHouseholdIds, actualResult);
	}
	
	@Test
	public void testGetHouseholdUniqueIdByVillageIdWithStatusOK() throws Exception {
		
		when(householdIdService.generateHouseholdId("p1", "7")).thenReturn(generateHouseholdId());
		String actualResult = getResponseAsString(BASE_URL + "generated-code/?username=p1&villageId=7", null,
		    MockMvcResultMatchers.status().isOk());
		
		verify(householdIdService).generateHouseholdId(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "p1");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "7");
		assertEquals(expectedHouseholdIds, actualResult);
	}
	
	@Test
	public void testGetWebNotificationWithStatusBadRequest() throws Exception {
		when(householdIdService.generateHouseholdId("p1", "")).thenReturn(generateHouseholdId());
		getResponseAsString(BASE_URL + "generated-code/?usernames=p1", null, MockMvcResultMatchers.status().isBadRequest());
		
	}
	
	@Test
	public void testGetGuestHouseholdUniqueIdByUsernameWithStatusOK() throws Exception {
		
		when(householdIdService.generateGuestHouseholdId("p1", "")).thenReturn(generateGuestHouseholdId());
		String actualResult = getResponseAsString(BASE_URL + "guest/generated-code/?username=p1&villageId=", null,
		    MockMvcResultMatchers.status().isOk());
		
		verify(householdIdService).generateGuestHouseholdId(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "p1");
		assertEquals(expectedGuetHouseholdIds, actualResult);
	}
	
	@Test
	public void testGetGuestHouseholdUniqueIdByVillageIdWithStatusOK() throws Exception {
		
		when(householdIdService.generateGuestHouseholdId("p1", "7")).thenReturn(generateGuestHouseholdId());
		String actualResult = getResponseAsString(BASE_URL + "guest/generated-code/?username=p1&villageId=7", null,
		    MockMvcResultMatchers.status().isOk());
		
		verify(householdIdService).generateGuestHouseholdId(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getAllValues().get(0), "p1");
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "7");
		assertEquals(expectedGuetHouseholdIds, actualResult);
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<HouseholdId> expectedList, List<HouseholdId> actualList) {
		
	}
	
	private JSONArray generateHouseholdId() {
		JSONArray householdIds = new JSONArray(expectedHouseholdIds);
		return householdIds;
	}
	
	private JSONArray generateGuestHouseholdId() {
		JSONArray guestHouseholdIds = new JSONArray(expectedGuetHouseholdIds);
		return guestHouseholdIds;
	}
	
}

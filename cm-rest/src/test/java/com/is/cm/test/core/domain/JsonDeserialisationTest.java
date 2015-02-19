package com.is.cm.test.core.domain;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.Product;
import com.is.cm.core.domain.Reps;

public class JsonDeserialisationTest {
	private static Logger LOG = LoggerFactory
			.getLogger(JsonDeserialisationTest.class);

	//MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();

	@Test
	public void orderJsonDeserialization() throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Order order = null;
		try {
			order = mapper
					.readValue(
							new File(
									"/home/amit/tools/channelmanagerWS/src/test/java/com/is/cm/test/core/domain/order.json"),
							Order.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(order);
	}

	@Test
	public void allClassesUsedByOurControllersShouldBeDeserialisableByJackson()
			throws Exception {

		assertCanBeMapped(Order.class);
		assertCanBeMapped(Product.class);
		assertCanBeMapped(Reps.class);
		// / ...
	}

	private void assertCanBeMapped(Class<?> classToTest) {
		String message = String
				.format("%s is not deserialisable, check the swallowed exception in StdDeserializerProvider.hasValueDeserializerFor",
						classToTest.getSimpleName());
		LOG.debug(message);
		//boolean b = converter.canRead(classToTest, MediaType.APPLICATION_JSON);
		//assert (b == true);

		//Assert.assertTrue(message,
		//		converter.canRead(classToTest, MediaType.APPLICATION_JSON));

	}

	@Test
	public void test() throws RestClientException, URISyntaxException {
		RestTemplate template = new RestTemplate();
		List<Order> orders = (List<Order>) template.getForObject(
				"http://localhost:8080/channelmanagerWS/aggregators/orders",
				List.class, new HashMap());
		Assert.assertNotNull(orders);

		Order order = orders.get(0);
		template.put(
				"http://localhost:8080/channelmanagerWS/aggregators/orders",
				order);
	}
}

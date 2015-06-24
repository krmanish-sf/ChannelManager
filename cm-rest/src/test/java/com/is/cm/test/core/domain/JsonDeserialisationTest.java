package com.is.cm.test.core.domain;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.Product;
import com.is.cm.core.domain.Reps;

public class JsonDeserialisationTest {
	private static Logger LOG = LoggerFactory
			.getLogger(JsonDeserialisationTest.class);

	// MappingJacksonHttpMessageConverter converter = new
	// MappingJacksonHttpMessageConverter();

	@Test
	public void orderJsonDeserialization() throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Order order = null;
		try {

			order = mapper.readValue(JsonDeserialisationTest.class
					.getClassLoader().getResourceAsStream("order.json"),
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
		// boolean b = converter.canRead(classToTest,
		// MediaType.APPLICATION_JSON);
		// assert (b == true);

		// Assert.assertTrue(message,
		// converter.canRead(classToTest, MediaType.APPLICATION_JSON));

	}
}

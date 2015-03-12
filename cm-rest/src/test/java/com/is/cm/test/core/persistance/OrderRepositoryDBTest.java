package com.is.cm.test.core.persistance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.is.cm.core.domain.Order;
import com.is.cm.core.persistance.OrderRepository;
import com.is.cm.core.persistance.OrderRepositoryDB;
import com.is.cm.core.security.AuthenticationInterceptor.MyThreadLocal;

public class OrderRepositoryDBTest {
	private static final String SERVICE_URL = "http://localhost:8080/cm-rest/aggregators/orders";
	private static Logger LOG = LoggerFactory
			.getLogger(OrderRepositoryDBTest.class);

	
	public void orderDataIntegrityAndConsistencyTest()
			throws InterruptedException {

		MyThreadLocal.set(441325);

		Map<Integer, Double> map = new HashMap<Integer, Double>();
		int i = 0, matchCount = 0, noMatchCount = 0;
		while (i < 3) {
			LOG.debug("Going to sleep for 3 seconds...");
			Thread.sleep(3000);
			OrderRepository repo = new OrderRepositoryDB();
			List<Order> findAll = (List<Order>) repo.findUnresolvedOrders();
			LOG.debug("Fetched {} Order(s)", findAll.size());
			LOG.debug("Map Data: {}", map);
			Random r = new Random();
			for (Order order : findAll) {
				if (i > 0) {
					Double oldPrice = map.get(order.getOrderId());
					if (oldPrice.equals(order.getOrderTotalAmount())) {
						matchCount++;
						LOG.debug("Price Match");
						oldPrice = r.nextDouble() * 100;
						order.setOrderTotalAmount(oldPrice);
						map.put(order.getOrderId(), oldPrice);
						repo.save(order);
						LOG.debug("Map Data: {}", map);
					} else {
						noMatchCount++;
						LOG.error("Price don't Match");
					}
				}
				Double orderTotal = r.nextDouble() * 100;
				map.put(order.getOrderId(), orderTotal);
				order.setOrderTotalAmount(orderTotal);
				repo.save(order);
			}
			i++;
		}
		LOG.debug("Match Count:{} No Match Count:{}", matchCount, noMatchCount);
	}
}

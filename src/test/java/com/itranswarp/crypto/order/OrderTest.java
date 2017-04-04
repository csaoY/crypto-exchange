package com.itranswarp.crypto.order;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.itranswarp.crypto.match.OrderBook;
import com.itranswarp.crypto.order.Order;

public class OrderTest {

	@Test
	public void testOrderInTreeSet() {
		Set<Order> book = new TreeSet<>(OrderBook.SORT_SELL);
		Order o1 = Order.sellLimit(123, 99, 9);
		Order o2 = Order.sellLimit(123, 99, 6);
		assertTrue(o1.equals(o2));
		assertEquals(o1.hashCode(), o2.hashCode());
		assertTrue(book.add(o1));
		assertTrue(book.remove(o2));
	}

	@Test
	public void testOrderInTreeSet2() {
		Set<Order> book = new TreeSet<>(OrderBook.SORT_SELL);
		Random random = new Random();
		for (long price = 100; price < 1000; price++) {
			Order o = Order.sellLimit(random.nextInt(1000000), price, random.nextInt(1000000));
			assertTrue(book.add(o));
			o.amount = 0;
			assertTrue(book.remove(o));
		}
	}

	@Test
	public void testSellComparator() {
		long[][] SELLS = new long[][] { //
				{ 12, 99, 13, 100 }, //
				{ 12, 98, 13, 100 }, //
				{ 12, 100, 13, 100 }, //
				{ 88, 100, 99, 100 }, //
		};
		for (long[] params : SELLS) {
			assertEquals(-1, OrderBook.SORT_SELL.compare(Order.sellLimit(params[0], params[1], 1),
					Order.sellLimit(params[2], params[3], 1)));
			assertEquals(1, OrderBook.SORT_SELL.compare(Order.sellLimit(params[2], params[3], 1),
					Order.sellLimit(params[0], params[1], 1)));
		}
	}

}

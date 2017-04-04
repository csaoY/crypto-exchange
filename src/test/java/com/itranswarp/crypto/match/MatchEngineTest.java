package com.itranswarp.crypto.match;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.crypto.order.Order;
import com.itranswarp.crypto.queue.MessageQueue;
import com.itranswarp.crypto.quotation.Quotation;

public class MatchEngineTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDuplicateEngine() throws Exception {
		MessageQueue<Order> orderQueue1 = new MessageQueue<>(10000);
		MessageQueue<Order> orderQueue2 = new MessageQueue<>(10000);
		MessageQueue<Ticker> tickerQueue1 = new MessageQueue<>(1000);
		MessageQueue<Ticker> tickerQueue2 = new MessageQueue<>(1000);
		MessageQueue<MatchResult> matchResultQueue1 = new MessageQueue<>(1000000);
		MessageQueue<MatchResult> matchResultQueue2 = new MessageQueue<>(1000000);

		Quotation quot1 = new Quotation(tickerQueue1);
		Quotation quot2 = new Quotation(tickerQueue2);

		Thread t1 = new Thread(quot1);
		Thread t2 = new Thread(quot2);
		t1.start();
		t2.start();

		MatchEngine matcher1 = new MatchEngine(orderQueue1, tickerQueue1, matchResultQueue1);
		MatchEngine matcher2 = new MatchEngine(orderQueue2, tickerQueue2, matchResultQueue2);

		matcher1.start();
		matcher2.start();
		long basePrice = 5000;
		orderQueue1.sendMessage(Order.buyLimit(1, basePrice, 1));
		orderQueue2.sendMessage(Order.buyLimit(1, basePrice, 1));
		orderQueue1.sendMessage(Order.sellLimit(2, basePrice, 1));
		orderQueue2.sendMessage(Order.sellLimit(2, basePrice, 1));
		final long NUM = 1000000;
		long totalAmount = 0L;
		long startTime = System.currentTimeMillis();
		for (long id = 10; id < NUM; id++) {
			boolean buy = random.nextInt() % 2 == 0;
			long price = matcher1.marketPrice + (buy ? randomLong(-1000, 500) : randomLong(-500, 1000));
			long amount = 1 + randomLong(1, 500) / (NUM >> 17);
			totalAmount += amount;
			orderQueue1.sendMessage(buy ? Order.buyLimit(id, price, amount) : Order.sellLimit(id, price, amount));
			orderQueue2.sendMessage(buy ? Order.buyLimit(id, price, amount) : Order.sellLimit(id, price, amount));
			if ((id % 1000L) == 0L) {
				Thread.sleep(1);
			}
		}
		// make sure orderQueue is empty:
		orderQueue1.shutdown();
		orderQueue2.shutdown();

		// shutdown:
		matcher1.shutdown();
		matcher2.shutdown();
		long endTime = System.currentTimeMillis();
		matcher1.dump();
		matcher2.dump();
		System.out.println("Time: " + (endTime - startTime));
		System.out.println("\nTickers 1:\n" + quot1);
		System.out.println("\nTickers 2:\n" + quot2);
		assertArrayEquals(matcher1.statusHash, matcher2.statusHash);
		assertEquals(quot1.amount.get(), quot2.amount.get());
		System.out.println("Place orders: " + totalAmount);
		System.out.println("Total tickers: " + tickerQueue1.totalMessages());
		System.out.println("Total tickers: " + tickerQueue2.totalMessages());

		t1.interrupt();
		t2.interrupt();
	}

	static final Random random = new Random();

	static long randomLong(int min, int max) {
		return random.nextInt(max - min) + min;
	}
}

package com.itranswarp.crypto.match;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.itranswarp.crypto.RunnableResource;
import com.itranswarp.crypto.order.Order;
import com.itranswarp.crypto.queue.MessageQueue;

/**
 * Match engine.
 * 
 * @author liaoxuefeng
 */
public class MatchEngine implements RunnableResource {

	// ticker interval = 100ms:
	final static long TICKER_INTERVAL = 100;

	// get order from queue:
	final MessageQueue<Order> orderQueue;

	// send ticker to queue:
	final MessageQueue<Ticker> tickerQueue;

	// send match result to queue:
	final MessageQueue<MatchResult> matchResultQueue;

	// holds order books:
	final OrderBook buyBook;
	final OrderBook sellBook;

	Thread processThread = null;

	long tickerTime = 0L;
	long marketPrice = 0L;
	long tickerAmount = 0L;

	// matcher internal status:
	final MessageDigest md5;
	byte[] statusHash;

	public MatchEngine(MessageQueue<Order> orderQueue, MessageQueue<Ticker> tickerQueue,
			MessageQueue<MatchResult> matchResultQueue) {
		this.orderQueue = orderQueue;
		this.tickerQueue = tickerQueue;
		this.matchResultQueue = matchResultQueue;
		this.buyBook = new OrderBook(OrderBook.SORT_BUY);
		this.sellBook = new OrderBook(OrderBook.SORT_SELL);
		// initial hash:
		try {
			this.md5 = MessageDigest.getInstance("MD5");
			this.statusHash = md5.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void start() {
		if (processThread != null) {
			throw new IllegalStateException("Cannot re-invoke start()");
		}
		processThread = new Thread() {
			public void run() {
				while (!isInterrupted()) {
					try {
						Order order = orderQueue.getMessage();
						processOrder(order);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		};
		processThread.start();
		while (!processThread.isAlive()) {
		}
	}

	@Override
	public synchronized void shutdown() {
		if (processThread != null) {
			processThread.interrupt();
			try {
				processThread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			processThread = null;
		}
	}

	/**
	 * Process an order.
	 * 
	 * @param order
	 *            Order object.
	 */
	void processOrder(Order order) throws InterruptedException {
		switch (order.type) {
		case BUY_LIMIT:
			processBuyLimit(order);
			break;
		case SELL_LIMIT:
			processSellLimit(order);
			break;
		case BUY_MARKET:
			throw new RuntimeException("Unsupported type.");
		case SELL_MARKET:
			throw new RuntimeException("Unsupported type.");
		case BUY_CANCEL:
			throw new RuntimeException("Unsupported type.");
		case SELL_CANCEL:
			throw new RuntimeException("Unsupported type.");
		default:
			throw new RuntimeException("Unsupported type.");
		}
		// update ticker:
		long nextTicker = (order.createdAt / TICKER_INTERVAL) * TICKER_INTERVAL;
		if (nextTicker > this.tickerTime) {
			this.tickerTime = nextTicker;
			long theAmount = this.tickerAmount;
			this.tickerAmount = 0L;
			this.tickerQueue.sendMessage(new Ticker(this.tickerTime, this.marketPrice, theAmount));
		}
	}

	void processBuyLimit(Order buyTaker) throws InterruptedException {
		MatchResult matchResult = new MatchResult();
		for (;;) {
			Order sellMaker = this.sellBook.getFirst();
			if (sellMaker == null) {
				// empty order book:
				break;
			}
			if (buyTaker.price < sellMaker.price) {
				break;
			}
			// match with sellMaker.price:
			this.marketPrice = sellMaker.price;
			// max amount to exchange:
			long amount = Math.min(buyTaker.amount, sellMaker.amount);
			buyTaker.amount -= amount;
			sellMaker.amount -= amount;
			this.tickerAmount += amount;
			matchResult.addMatchRecord(new MatchRecord(buyTaker.id, sellMaker.id, this.marketPrice, amount));
			updateStatusHash(buyTaker, sellMaker, this.marketPrice, amount);
			if (sellMaker.amount == 0L) {
				this.sellBook.remove(sellMaker);
			}
			if (buyTaker.amount == 0L) {
				buyTaker = null;
				break;
			}
		}
		if (buyTaker != null) {
			this.buyBook.add(buyTaker);
		}
		if (matchResult.matchRecords != null) {
			notifyMatchResult(matchResult);
		}
	}

	void processSellLimit(Order sellTaker) throws InterruptedException {
		MatchResult matchResult = new MatchResult();
		for (;;) {
			Order buyMaker = this.buyBook.getFirst();
			if (buyMaker == null) {
				// empty order book:
				break;
			}
			if (sellTaker.price > buyMaker.price) {
				break;
			}
			// match with buyMaker.price:
			this.marketPrice = buyMaker.price;
			// max amount to match:
			long amount = Math.min(sellTaker.amount, buyMaker.amount);
			sellTaker.amount -= amount;
			buyMaker.amount -= amount;
			this.tickerAmount += amount;
			matchResult.addMatchRecord(new MatchRecord(sellTaker.id, buyMaker.id, this.marketPrice, amount));
			updateStatusHash(sellTaker, buyMaker, this.marketPrice, amount);
			if (buyMaker.amount == 0L) {
				this.buyBook.remove(buyMaker);
			}
			if (sellTaker.amount == 0L) {
				sellTaker = null;
				break;
			}
		}
		if (sellTaker != null) {
			this.sellBook.add(sellTaker);
		}
		if (matchResult.matchRecords != null) {
			notifyMatchResult(matchResult);
		}
	}

	void notifyMatchResult(MatchResult matchResult) throws InterruptedException {
		this.matchResultQueue.sendMessage(matchResult);
	}

	private final ByteBuffer hashBuffer = ByteBuffer.allocate(Long.BYTES * 4 + Integer.BYTES * 2);

	private void updateStatusHash(Order taker, Order maker, long price, long amount) {
		hashBuffer.clear();
		hashBuffer.putLong(taker.id);
		hashBuffer.putInt(taker.type.value);
		hashBuffer.putLong(maker.id);
		hashBuffer.putInt(maker.type.value);
		hashBuffer.putLong(price);
		hashBuffer.putLong(amount);
		this.md5.update(this.statusHash);
		this.md5.update(hashBuffer);
		this.statusHash = this.md5.digest();
	}

	public void dump() {
		System.out.println(String.format("S: %5d more", this.sellBook.size()));
		this.sellBook.dump(true);
		System.out.println(String.format("P: $%4d ----------------", this.marketPrice));
		this.buyBook.dump(false);
		System.out.println(String.format("B: %5d more", this.buyBook.size()));
		System.out.println(String.format("%032x\n", new BigInteger(1, this.statusHash)));
	}

}

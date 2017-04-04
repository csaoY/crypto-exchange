package com.itranswarp.crypto.order;

import java.util.concurrent.atomic.AtomicLong;

import com.itranswarp.crypto.RunnableResource;
import com.itranswarp.crypto.queue.MessageQueue;

public class OrderManager implements RunnableResource {

	final MessageQueue<Order> orderQueue;
	final AtomicLong idSequence;

	public OrderManager(MessageQueue<Order> orderQueue, long idStartFrom) {
		this.orderQueue = orderQueue;
		this.idSequence = new AtomicLong(idStartFrom);
	}

	public void createBuyLimitOrder(long price, long amount) throws InterruptedException {
		Order order = new Order(nextId(), Order.OrderType.BUY_LIMIT, price, amount);
		orderQueue.sendMessage(order);
	}

	public void createSellLimitOrder(long price, long amount) throws InterruptedException {
		Order order = new Order(nextId(), Order.OrderType.SELL_LIMIT, price, amount);
		orderQueue.sendMessage(order);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	private long nextId() {
		return idSequence.incrementAndGet();
	}

}

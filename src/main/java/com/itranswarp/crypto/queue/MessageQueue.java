package com.itranswarp.crypto.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.itranswarp.crypto.RunnableResource;

public class MessageQueue<T> implements RunnableResource {

	private final AtomicLong count = new AtomicLong(0);
	private final int maxSize;
	private BlockingQueue<T> queue;

	public MessageQueue(int maxSize) {
		this.maxSize = maxSize;
		this.queue = new ArrayBlockingQueue<>(maxSize);
	}

	public void sendMessage(T t) throws InterruptedException {
		queue.put(t);
		count.incrementAndGet();
	}

	public T getMessage() throws InterruptedException {
		return queue.take();
	}

	public int getMaxSize() {
		return this.maxSize;
	}

	public int size() {
		return this.queue.size();
	}

	public long totalMessages() {
		return count.get();
	}

	@Override
	public synchronized void start() {
	}

	@Override
	public synchronized void shutdown() {
		for (int i = 0; i < 100; i++) {
			if (queue.size() == 0) {
				queue = null;
				return;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		throw new IllegalStateException("Queue still holds " + queue.size() + " message(s).");
	}
}

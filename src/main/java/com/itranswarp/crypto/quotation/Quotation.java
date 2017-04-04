package com.itranswarp.crypto.quotation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.itranswarp.crypto.match.Ticker;
import com.itranswarp.crypto.queue.MessageQueue;

public class Quotation implements Runnable {

	final MessageQueue<Ticker> tickerQueue;

	public Quotation(MessageQueue<Ticker> tickerQueue) {
		this.tickerQueue = tickerQueue;
	}

	public final AtomicLong amount = new AtomicLong(0);
	public final List<String> list = new ArrayList<>();

	@Override
	public void run() {
		try {
			while (true) {
				Ticker ticker = tickerQueue.getMessage();
				System.out.println("Message: " + ticker);
				ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ticker.time), DEFAULT_TIMEZONE);
				list.add(dt.toLocalTime() + " $ " + ticker.price + ", " + amount);
				this.amount.addAndGet(ticker.amount);
			}
		} catch (InterruptedException e) {

		}
	}

	@Override
	public String toString() {
		return String.join("\n", list) + "\nAmount: " + this.amount.get();
	}

	public List<KLine> toSeconds() {
		return null;
	}

	static final ZoneId DEFAULT_TIMEZONE = ZoneId.systemDefault();

}

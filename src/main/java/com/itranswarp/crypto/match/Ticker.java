package com.itranswarp.crypto.match;

public class Ticker {

	public final long time;
	public final long price;
	public final long amount;

	public Ticker(long time, long price, long amount) {
		this.time = time;
		this.price = price;
		this.amount = amount;
	}

}

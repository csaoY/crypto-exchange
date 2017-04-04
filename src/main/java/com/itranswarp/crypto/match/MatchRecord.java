package com.itranswarp.crypto.match;

public class MatchRecord {

	public final long takerOrderId;
	public final long makerOrderId;
	public final long matchPrice;
	public final long matchAmount;

	public MatchRecord(long takerOrderId, long makerOrderId, long matchPrice, long matchAmount) {
		this.takerOrderId = takerOrderId;
		this.makerOrderId = makerOrderId;
		this.matchPrice = matchPrice;
		this.matchAmount = matchAmount;
	}

}

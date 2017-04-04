package com.itranswarp.crypto.message;

import java.io.Serializable;

public class KLine implements Serializable {

	public static enum Type {
		K_1MIN, K_5MIN, K_15MIN, K_30MIN, K_60MIN, K_1DAY, K_1WEEK, K_1MONTH, K_1YEAR
	}

	public Type type;

	public long startTime;
	public long endTime;

	public long openPrice;
	public long closePrice;
	public long highPrice;
	public long lowPrice;
}

package com.itranswarp.crypto.symbol;

public enum Currency {

	USD(2), CNY(2), JPY(2), BTC(4), LTC(4), ETH(4), ETC(4);

	private final int scale;

	private Currency(int scale) {
		this.scale = scale;
	}

	public int getScale() {
		return this.scale;
	}
}

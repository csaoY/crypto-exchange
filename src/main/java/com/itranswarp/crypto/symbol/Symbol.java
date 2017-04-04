package com.itranswarp.crypto.symbol;

public class Symbol {

	public Currency base;
	public Currency quote;

	public String toString() {
		return String.format("%s/%s", this.base.name(), this.quote.name());
	}
}

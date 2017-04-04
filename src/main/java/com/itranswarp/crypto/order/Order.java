package com.itranswarp.crypto.order;

public class Order {

	public static enum OrderType {

		BUY_LIMIT(1), SELL_LIMIT(2), BUY_MARKET(3), SELL_MARKET(4), BUY_CANCEL(5), SELL_CANCEL(6);

		public final int value;

		private OrderType(int value) {
			this.value = value;
		}

	}

	public static final int BUY_LIMIT = 1;
	public static final int SELL_LIMIT = 2;

	public final long id;
	public final OrderType type;
	public final long price;
	public long amount;
	public long createdAt;

	public static Order buyLimit(long id, long price, long amount) {
		return new Order(id, OrderType.BUY_LIMIT, price, amount);
	}

	public static Order sellLimit(long id, long price, long amount) {
		return new Order(id, OrderType.SELL_LIMIT, price, amount);
	}

	public static Order buyCancel(long id) {
		return new Order(id, OrderType.BUY_CANCEL, 0, 0);
	}

	public static Order sellCancel(long id) {
		return new Order(id, OrderType.SELL_CANCEL, 0, 0);
	}

	Order(long id, OrderType type, long price, long amount) {
		this.id = id;
		this.type = type;
		this.price = price;
		this.amount = amount;
		this.createdAt = System.currentTimeMillis();
	}

	@Override
	public boolean equals(Object o) {
		return this.id == ((Order) o).id;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(this.id);
	}

	public String toString() {
		return String.format("%s: $%4d %4d   id:%d", (type == OrderType.BUY_LIMIT ? "B" : "S"), price, amount, id);
	}
}

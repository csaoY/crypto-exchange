package com.itranswarp.crypto.match;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import com.itranswarp.crypto.order.Order;

public class OrderBook {

	public static final Comparator<Order> SORT_SELL = new Comparator<Order>() {
		@Override
		public int compare(Order o1, Order o2) {
			long p1 = o1.price;
			long p2 = o2.price;
			if (p1 < p2) {
				return -1;
			}
			if (p1 > p2) {
				return 1;
			}
			long i1 = o1.id;
			long i2 = o2.id;
			if (i1 < i2) {
				return -1;
			}
			if (i1 > i2) {
				return 1;
			}
			return 0;
		}
	};

	public static final Comparator<Order> SORT_BUY = new Comparator<Order>() {
		@Override
		public int compare(Order o1, Order o2) {
			long p1 = o1.price;
			long p2 = o2.price;
			if (p1 < p2) {
				return 1;
			}
			if (p1 > p2) {
				return -1;
			}
			long i1 = o1.id;
			long i2 = o2.id;
			if (i1 < i2) {
				return -1;
			}
			if (i1 > i2) {
				return 1;
			}
			return 0;
		}
	};

	TreeSet<Order> book;

	public OrderBook(Comparator<Order> comparator) {
		this.book = new TreeSet<>(comparator);
	}

	/**
	 * Get first order from order book, or null if order book is empty.
	 * 
	 * @return Order or null if empty.
	 */
	public Order getFirst() {
		return this.book.isEmpty() ? null : this.book.first();
	}

	public boolean remove(Order order) {
		return this.book.remove(order);
	}

	public boolean add(Order order) {
		return this.book.add(order);
	}

	public int size() {
		return this.book.size();
	}

	public void dump(boolean reverse) {
		int n = 0;
		Iterator<Order> it = reverse ? this.book.descendingIterator() : this.book.iterator();
		while (it.hasNext() && n < 10) {
			n++;
			System.out.println(it.next());
		}
	}

	public static void main(String[] args) {
		// sell:
		OrderBook sell = new OrderBook(OrderBook.SORT_SELL);
		for (long id = 1; id < 20; id++) {
			Order o = Order.sellLimit(id, randomLong(110, 130), randomLong(1, 100));
			sell.add(o);
		}
		sell.dump(true);
		System.out.println("------------------------------");
		// buy:
		OrderBook buy = new OrderBook(OrderBook.SORT_BUY);
		for (long id = 100; id < 120; id++) {
			Order o = Order.buyLimit(id, randomLong(100, 120), randomLong(1, 100));
			buy.add(o);
		}
		buy.dump(false);
	}

	static final Random random = new Random();

	static long randomLong(int min, int max) {
		return random.nextInt(max - min) + min;
	}

}

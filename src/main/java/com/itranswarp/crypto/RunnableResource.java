package com.itranswarp.crypto;

public interface RunnableResource {

	/**
	 * Start this resource.
	 */
	void start();

	/**
	 * Safely close resource.
	 */
	void shutdown();
}

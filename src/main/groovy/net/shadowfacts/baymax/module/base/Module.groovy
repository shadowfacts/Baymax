package net.shadowfacts.baymax.module.base

import com.typesafe.config.Config

/**
 * @author shadowfacts
 */
abstract class Module {

	final String name
	boolean enabled

	public Module(String name) {
		this.name = name
	}

	void configure(Config config) {
		enabled = config.getBoolean(name + ".enabled")
	}

	abstract void init();

}

package net.shadowfacts.baymax

import net.dv8tion.jda.events.Event
import net.dv8tion.jda.hooks.EventListener

import java.util.function.Consumer

/**
 * @author shadowfacts
 */
class Listener implements EventListener {

	static final def INSTANCE = new Listener()

	private Map<Class<? extends Event>, List<Consumer<? extends Event>>> handlers = new HashMap<>();

	private Listener() {

	}

	@Override
	void onEvent(Event event) {
		List<Consumer<? extends Event>> handlers = this.handlers.get(event.getClass())
		if (handlers != null) {
			handlers.forEach({
				it.accept(event)
			})
		}
	}

	public <T extends Event> void register(Class<T> clazz, Consumer<T> handler) {
		if (!handlers.containsKey(clazz)) handlers.put(clazz, new ArrayList<>())
		handlers.get(clazz).add(handler)
	}

}

package net.shadowfacts.baymax.module

import com.typesafe.config.Config
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.Baymax
import net.shadowfacts.baymax.Listener
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.command.exception.WrongUsageException
import net.shadowfacts.baymax.module.base.Module

import java.util.function.Function
import java.util.regex.Pattern;

/**
 * @author shadowfacts
 */
class ModuleTemperature extends Module {

	private static final def USAGE = "Converts the temperature from the given unit to the other. Usage: convtemp <temp>c(elsius)|f(arenheit)"
	private static final def MATCHER = Pattern.compile("(\\d+)\u00B0?(f|c)")


	private boolean command
	private boolean autoConvert

	ModuleTemperature() {
		super("temperature")
	}

	@Override
	void configure(Config config) {
		super.configure(config)
		command = config.getBoolean("temperature.command")
		autoConvert = config.getBoolean("temperature.autoConvert")
	}

	@Override
	void init() {
		if (command) CommandManager.register("convtemp", USAGE, this.&handleCommand)
		if (autoConvert) Listener.INSTANCE.register(MessageReceivedEvent.class, this.&onMessageReceived)
	}

	private static void onMessageReceived(MessageReceivedEvent event) {
		if (event.author.username != event.JDA.selfInfo.username && !event.message.content.startsWith(Baymax.config.getString("baymax.prefix") + "convtemp")) {
			def matcher = MATCHER.matcher(event.message.content)
			if (matcher.find()) {
				float original
				try {
					original = Float.parseFloat(matcher.group(1))
				} catch (NumberFormatException ignored) { return }
				Unit unit
				try {
					unit = Unit.get(matcher.group(2))
				} catch (WrongUsageException ignored) { return }
				def other = unit.converter.apply(original)
				event.channel.sendMessage(String.format("%.1f\u00B0 %s in %s is %.1f\u00B0", original, unit.name, unit.other.name, other))
			}
		}
	}

	private static void handleCommand(MessageReceivedEvent event, String[] args) {
		if (args.length != 1) {
			throw new WrongUsageException("Incorrect usage, " + USAGE);
		}

		def temp = args[0].substring(0, args[0].length() - 1)

		float original

		try {
			original = Float.parseFloat(temp)
		} catch (NumberFormatException e) {
			throw new WrongUsageException("Wrongly formatted number: " + temp);
		}

		def unit = Unit.get(args[0].substring(args[0].length() - 1))
		def other = unit.converter.apply(original)
		event.channel.sendMessage(String.format("%.1f\u00B0 %s in %s is %.1f\u00B0", original, unit.name, unit.other.name, other))
	}

	private static enum Unit {
		FARENHEIT("Farenheit", { return (it - 32) / (9/5) }),
		CELSIUS("Celsius", { return it * 9/5 + 32 })

		private String name
		private Function<Float, Float> converter

		Unit(String name, Function<Float, Float> converter) {
			this.name = name
			this.converter = converter
		}

		Unit getOther() {
			return this == FARENHEIT ? CELSIUS : FARENHEIT
		}

		static Unit get(String s) {
			s = s.toLowerCase()
			if (s == "c" || s == "celsius") return CELSIUS
			else if (s == "f" || s == "farenheit") return FARENHEIT
			else throw new WrongUsageException("Invalid unit: " + s)
		}
	}

}

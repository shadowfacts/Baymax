package net.shadowfacts.baymax.module

import com.typesafe.config.Config
import jdk.nashorn.api.scripting.ClassFilter
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.command.exception.WrongUsageException
import net.shadowfacts.baymax.module.base.Module

import javax.script.ScriptEngine

/**
 * @author shadowfacts
 */
class ModuleJS extends Module {

	private static final def JS_USAGE = "Runs JS in a Nashorn environment. Usage: js <js>"
	private static final def RESET_USAGE = "Resets the Nashorn environment. Usage: jsReset"
	public static final NashornScriptEngineFactory FACTORY = new NashornScriptEngineFactory()

	private boolean resetAfterEach

	private ScriptEngine nashorn

	ModuleJS() {
		super("js")
	}

	@Override
	void configure(Config config) {
		super.configure(config)
		resetAfterEach = config.getBoolean("js.resetAfterEach")
	}

	@Override
	void init() {
		CommandManager.register("js", JS_USAGE, this.&handleJS)
		CommandManager.register("jsReset", RESET_USAGE, this.&handleReset)

		createNashorn()
	}

	private void handleJS(MessageReceivedEvent event, String[] args) {
		if (args.length == 0) {
			throw new WrongUsageException(JS_USAGE)
		}
		def js = String.join(" ", args)

		event.channel.sendMessage(Objects.toString(nashorn.eval(js)))

		if (resetAfterEach) {
			createNashorn()
		}
	}

	private void handleReset(MessageReceivedEvent event, String[] args) {
		if (args.length != 0) {
			throw new WrongUsageException(RESET_USAGE)
		}

		createNashorn()
	}

	private void createNashorn() {
		nashorn = FACTORY.getScriptEngine(new Filter())
	}

	private static class Filter implements ClassFilter {
		@Override
		boolean exposeToScripts(String s) {
			return false
		}
	}

}

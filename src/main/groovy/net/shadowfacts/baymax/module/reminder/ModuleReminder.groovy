package net.shadowfacts.baymax.module.reminder

import net.dv8tion.jda.events.ReadyEvent
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.Baymax
import net.shadowfacts.baymax.Listener
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.command.exception.WrongUsageException
import net.shadowfacts.baymax.module.base.Module

import java.util.function.Function
import java.util.regex.Pattern

/**
 * @author shadowfacts
 */
class ModuleReminder extends Module {

	private static final def USAGE = ""

	private static final def TIME_PATTERN = Pattern.compile("(\\d+)(s|m|h|d)")

	ModuleReminder() {
		super("reminder")
	}

	@Override
	void init() {
		CommandManager.register("remind", USAGE, this.&handle)

		ReminderManager.init()

		Listener.INSTANCE.register(ReadyEvent.class, this.&onReady)
	}

	private static void onReady(ReadyEvent event) {
		if (!ReminderManager.instance.reminders.isEmpty()) {
			def timer = new Timer()
			ReminderManager.instance.reminders.entrySet().each({
				def user = event.JDA.getUserById(it.key)
				it.value.each({
					def delay = it.time = new Date().time
					if (delay > 0) {
						timer.schedule(new TimerTask() {
							@Override
							void run() {
								user.privateChannel.sendMessage("Reminder: " + String.join(" ", it.reminder))
								ReminderManager.instance.remove(user, it)
							}
						}, delay)
					} else {
						user.privateChannel.sendMessage("Reminder: " + String.join(" ", it.reminder))
						ReminderManager.instance.remove(user, it)
					}
				})
			})
		}
	}

	private static void handle(MessageReceivedEvent event, String[] args) {
		if (args.length < 2) {
			throw new WrongUsageException(USAGE)
		}
		def timeMatcher = TIME_PATTERN.matcher(args[0])
		if (!timeMatcher.matches()) {
			throw new WrongUsageException(USAGE)
		}
		long amount
		try {
			amount = Long.parseLong(timeMatcher.group(1))
		} catch (NumberFormatException e) { throw new WrongUsageException("Invalid number: " + e.message) }
		def unit = Unit.get(timeMatcher.group(2))
		def milis = unit.consumer.apply(amount)
		def timer = new Timer()
		def msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length))
		def reminder = ReminderManager.instance.add(event.author, new Date().time + milis, msg)
		timer.schedule(new TimerTask() {
			@Override
			void run() {
				event.author.privateChannel.sendMessage("Reminder: " + msg)
				ReminderManager.instance.remove(event.author, reminder)
			}
		}, milis)
		event.privateChannel.sendMessage("Reminding you in " + timeMatcher.group(1) + timeMatcher.group(2))
	}

	private static enum Unit {
		SECONDS({ return it * 1000 }),
		MINUTES({ return it * 1000 * 60}),
		HOURS({ return it * 1000 * 60 * 60 }),
		DAYS({ return it * 1000 * 60 * 60 * 24 })

		private Function<Long, Long> consumer

		Unit(Function<Long, Long> consumer) {
			this.consumer = consumer
		}

		static Unit get(String s) {
			s = s.toLowerCase()
			return s == "s" ? SECONDS : s == "m" ? MINUTES : s == "h" ? HOURS : s == "d" ? DAYS : null
		}
	}

}

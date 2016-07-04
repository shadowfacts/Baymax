package net.shadowfacts.baymax.module.reminder

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.dv8tion.jda.entities.User
import net.shadowfacts.baymax.Baymax

import java.lang.reflect.Type

/**
 * @author shadowfacts
 */
class ReminderManager {

	private static final def gson = new GsonBuilder().registerTypeAdapter(ReminderManager.class, new Adapter()).setPrettyPrinting().create()

	static ReminderManager instance

	Map<String, List<Reminder>> reminders

	static void init() {
		def f = new File("reminders.json")
		if (f.exists()) {
			instance = gson.fromJson(new FileReader(f), ReminderManager.class)
		} else {
			instance = new ReminderManager()
			instance.reminders = new HashMap<>()
			save()
		}
	}

	private static void save() {
		def writer = new PrintWriter(new File("reminders.json"))
		writer.write(gson.toJson(instance))
		writer.close()
	}

	List<Reminder> get(User user) {
		return reminders.get(user.id)
	}

	Reminder add(User user, long time, String msg) {
		return add(user.id, time, msg)
	}

	Reminder add(String user, long time, String msg) {
		if (!reminders.containsKey(user)) reminders.put(user, new ArrayList<>())
		def reminder = new Reminder(time, msg)
		reminders.get(user).add(reminder)
		save()
		return reminder
	}

	void remove(User user, Reminder reminder) {
		if (reminders.containsKey(user.id)) {
			reminders.get(user.id).remove(reminder)
		}
	}

	private static class Reminder {
		long time
		String reminder

		Reminder(long time, String reminder) {
			this.time = time
			this.reminder = reminder
		}
	}

	private static class Adapter implements JsonSerializer<ReminderManager>, JsonDeserializer<ReminderManager> {

		@Override
		JsonElement serialize(ReminderManager src, Type typeOfSrc, JsonSerializationContext context) {
			def obj = new JsonObject()
			def reminders = new JsonObject()
			src.reminders.entrySet().each({
				def person = new JsonArray()
				it.value.each({
					def reminder = new JsonObject()
					reminder.add("time", new JsonPrimitive(it.time))
					reminder.add("reminder", new JsonPrimitive(it.reminder))
					person.add(reminder)
				})
				reminders.add(it.key, person)
			})
			obj.add("reminders", reminders)
			return obj
		}

		@Override
		ReminderManager deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			def instance = new ReminderManager()
			instance.reminders = new HashMap<>()

			def reminders = json.asJsonObject.get("reminders").asJsonObject

			reminders.entrySet().each({
				def user = it.key
				it.value.asJsonArray.forEach({
					def reminderObj = it.asJsonObject
					instance.add(user, reminderObj.get("time").asLong, reminderObj.get("reminder").asString)
				})
			})


			return instance
		}

	}

}

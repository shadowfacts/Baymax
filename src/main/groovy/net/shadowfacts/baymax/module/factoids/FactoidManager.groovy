package net.shadowfacts.baymax.module.factoids;

import com.google.gson.*

import java.lang.reflect.Type

/**
 * @author shadowfacts
 */
public class FactoidManager {

	private static final def gson = new GsonBuilder().registerTypeAdapter(FactoidManager.class, new Adapter()).setPrettyPrinting().create()

	static FactoidManager instance

	private Map<String, Map<String, String>> factoids

	public static void init() {
		def f = new File("factoids.json")
		if (f.exists()) {
			instance = gson.fromJson(new FileReader(f), FactoidManager.class)
		} else {
			instance = new FactoidManager()
			instance.factoids = new HashMap<>()
			save()
		}
	}

	private static void save() {
		def writer = new PrintWriter(new File("factoids.json"))
		writer.write(gson.toJson(instance))
		writer.close()
	}

	public String get(String server, String id) {
		return factoids.containsKey(server) ? factoids.get(server).get(id) : null;
	}

	public void set(String server, String id, String factoid) {
		if (!factoids.containsKey(server)) {
			factoids.put(server, new HashMap<>())
		}
		factoids.get(server).put(id, factoid)
		save()
	}

	public void remove(String server, String id) {
		if (factoids.containsKey(server)) {
			factoids.get(server).remove(id)
			save()
		}
	}

	private static class Adapter implements JsonSerializer<FactoidManager> {

		@Override
		public JsonElement serialize(FactoidManager src, Type typeOfSrc, JsonSerializationContext context) {
			def obj = new JsonObject()
			def factoids = new JsonObject()

			src.factoids.entrySet().each({
				def serverObj = new JsonObject()
				it.value.entrySet().each({
					serverObj.add(it.key, new JsonPrimitive(it.value))
				})
				factoids.add(it.key, serverObj)
			})

			obj.add("factoids", factoids)

			return obj
		}

	}

}

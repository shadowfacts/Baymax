package net.shadowfacts.baymax.module.mcserver

import com.google.gson.GsonBuilder
import net.shadowfacts.shadowlib.util.InternetUtils

/**
 * @author shadowfacts
 */
class AuthStatus {

	private static final def URL = new URL("https://sessionserver.mojang.com/")
	private static final def GSON = new GsonBuilder().create()

	static String check() {
		return GSON.fromJson(InternetUtils.getResourceAsString(URL), Result.class).Status
	}

	static class Result {
		String Status
	}

}

package net.shadowfacts.baymax.module.mcserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * @author shadowfacts
 */
class PacketPing {

	private static final def GSON = new GsonBuilder().create()

	private InetSocketAddress host
	private int timeout

	PacketPing(InetSocketAddress host, int timeout) {
		this.host = host
		this.timeout = timeout
	}

	PacketPing(InetSocketAddress host) {
		this(host, 7000)
	}

	Response fetch() throws IOException {
		def socket = new Socket()
		OutputStream out
		DataOutputStream dataOutputStream
		InputStream input
		InputStreamReader inputStreamReader

		socket.setSoTimeout(timeout)

		socket.connect(host, timeout)

		out = socket.outputStream
		dataOutputStream = new DataOutputStream(out)

		input = socket.inputStream
		inputStreamReader = new InputStreamReader(input)

		def b = new ByteArrayOutputStream()
		def handshake = new DataOutputStream(b)
		handshake.writeByte(0x00) // handshake packet id
		writeVarInt(handshake, 47) // protocol version
		writeVarInt(handshake, host.hostString.length()) // host string length
		handshake.writeBytes(host.hostString) // host string
		handshake.writeShort(host.port) // port
		writeVarInt(handshake, 1) // next state, 1 for status

		writeVarInt(dataOutputStream, b.size()) // packet size
		dataOutputStream.write(b.toByteArray()) // packet

		dataOutputStream.writeByte(0x01) // size
		dataOutputStream.writeByte(0x00) // packet id for ping
		def dataInputStream = new DataInputStream(input)

		int size = readVarInt(dataInputStream) // packet size
		int id = readVarInt(dataInputStream) // packet id

		if (id == -1) throw new IOException("Premature end of stream")

		if (id != 0x00) throw new IOException("Invalid packet id") // not a status response

		int length = readVarInt(dataInputStream) // length of JSON string

		if (length == -1) throw new IOException("Premature end of stream")

		if (length == 0) throw new IOException("Invalid string length")

		def strBytes = new byte[length]
		dataInputStream.readFully(strBytes)
		def json = new String(strBytes)

		def now = System.currentTimeMillis()
		dataOutputStream.writeByte(0x09) // packet size
		dataOutputStream.writeByte(0x01) // ping id
		dataOutputStream.writeLong(now) // time

		readVarInt(dataInputStream)
		id = readVarInt(dataInputStream)

		if (id == -1) throw new IOException("Premature end of stream")
		if (id != 0x01) throw new IOException("Invalid packet id")

		long pingtime = dataInputStream.readLong()

		Response response = GSON.fromJson(json, Response.class)
		response.time = (int)(now - pingtime)

		dataOutputStream.close()
		out.close()
		inputStreamReader.close()
		input.close()
		socket.close()

		return response
	}

	private static void writeVarInt(DataOutputStream output, int i) {
		while (true) {
			if ((i & 0xFFFFFF80) == 0) {
				output.writeByte(i)
				return
			}
			output.writeByte(i & 0x7f | 0x80)
			i >>>= 7
		}
	}

	private static int readVarInt(DataInputStream input) {
		int i = 0
		int j = 0
		while (true) {
			int k = input.readByte()
			i |= (k & 0x7f) << j++ * 7
			if (j > 5) throw new RuntimeException("VarInt too big")
			if ((k & 0x80) != 128) break
		}
		return i
	}

	static class Response {
		Version version
		PlayerList players
		ChatComponent description
		String favicon

		int time
	}

	static class Version {
		String name
		int protocol
	}

	static class PlayerList {
		int max
		int online
		Player[] sample
	}

	static class Player {
		String name
		String id
	}

	static class ChatComponent {
		String text
	}

}

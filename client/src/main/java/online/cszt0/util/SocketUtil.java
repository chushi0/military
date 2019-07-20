package online.cszt0.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author 初始状态0
 * @date 2019/7/20 2:45
 */
public class SocketUtil {

	private static final byte[] SEND = {0x56, (byte) 0x82, 0x73, (byte) 0xf6, 0x77, 0x69, (byte) 0xac, 0x4d};
	private static final byte[] RECEIVE = {0x4d, (byte) 0xac, 0x69, 0x77, (byte) 0xf6, 0x73, (byte) 0x82, 0x56};

	public static boolean checkServerAvailable(Socket socket) throws IOException {
		OutputStream outputStream = socket.getOutputStream();
		InputStream inputStream = socket.getInputStream();
		outputStream.write(SEND);
		outputStream.flush();
		byte[] buf = new byte[8];
		int len = inputStream.read(buf);
		if (len == 8) {
			return Arrays.equals(buf, RECEIVE);
		}
		return false;
	}
}

package online.cszt0.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 初始状态0
 * @date 2019/7/20 2:45
 */
public class SocketUtil {

	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

	private static final byte[] SEND = {0x56, (byte) 0x82, 0x73, (byte) 0xf6, 0x77, 0x69, (byte) 0xac, 0x4d};
	private static final byte[] RECEIVE = {0x4d, (byte) 0xac, 0x69, 0x77, (byte) 0xf6, 0x73, (byte) 0x82, 0x56};

	/**
	 * 本地棋子与服务器棋子的映射关系
	 */
	private static final byte[] CHESS = {
			-1, -1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 2, 1, 0
	};

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

	/**
	 * 发送布阵信息
	 */
	public static void sendDeployData(Socket socket, byte[] chess) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(0);
		for (byte piece : chess) {
			if (piece == ChessUtil.EMPTY) {
				continue;
			}
			byteArrayOutputStream.write(CHESS[piece]);
		}
		byte[] data = byteArrayOutputStream.toByteArray();
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		dataOutputStream.writeInt(data.length);
		dataOutputStream.write(data);
		dataOutputStream.flush();
	}

	public static void sendMovementData(Socket socket, int from, int to) throws IOException {
		byte[] bytes = {1, (byte) from, (byte) to};
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		dataOutputStream.writeInt(bytes.length);
		dataOutputStream.write(bytes);
		dataOutputStream.flush();
	}

	public static void startSocketListener(Socket socket, SocketListener socketListener) {
		Thread thread = new Thread(() -> {
			try {
				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
				while (true) {
					byte[] data = new byte[4];
					dataInputStream.readFully(data);
					executor.execute(() -> socketListener.onReceive(data));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public interface SocketListener {
		void onReceive(byte[] data);
	}
}

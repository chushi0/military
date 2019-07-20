package online.cszt0.util;

public class BoardUtil {

	public static int xy2index(int x, int y) {
		return y * 5 + x;
	}

	public static int[] index2xy(int index) {
		return new int[]{
				index % 5,
				index / 5
		};
	}
}
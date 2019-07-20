package online.cszt0.util;

/**
 * @author 初始状态0
 * @date 2019/7/20 8:30
 */
public class ChessUtil {
	// 无棋子
	public static final byte EMPTY = 0;
	// 对手棋子
	public static final byte ENEMY = 1;
	// 我方棋子
	public static final byte[] TEAM = {
			2,// 工兵
			3,// 排长
			4,// 连长
			5,// 营长
			6,// 团长
			7,// 旅长
			8,// 师长
			9,// 军长
			10,// 司令
			11,// 炸弹
			12,// 地雷
			13// 军棋
	};

	private static final String[] NAMES = {
			"工兵", "排长", "连长", "营长", "团长", "旅长", "师长", "军长", "司令", "炸弹", "地雷", "军棋"
	};

	public static String getChessName(int piece) {
		return NAMES[piece - 2];
	}

	/**
	 * 随机重置我方棋子
	 */
	public static void reset(byte[] chess) {
		int offset = 0;
		// 3工兵
		chess[offset++] = 2;
		chess[offset++] = 2;
		chess[offset++] = 2;
		// 3排长
		chess[offset++] = 3;
		chess[offset++] = 3;
		chess[offset++] = 3;
		// 3连长
		chess[offset++] = 4;
		chess[offset++] = 4;
		chess[offset++] = 4;
		// 2营长
		chess[offset++] = 5;
		chess[offset++] = 5;
		// 2团长
		chess[offset++] = 6;
		chess[offset++] = 6;
		// 2旅长
		chess[offset++] = 7;
		chess[offset++] = 7;
		// 2师长
		chess[offset++] = 8;
		chess[offset++] = 8;
		// 军长
		chess[offset++] = 9;
		// 司令
		chess[offset++] = 10;
		// 2炸弹
		chess[offset++] = 11;
		chess[offset++] = 11;
		// 3地雷
		chess[offset++] = 12;
		chess[offset++] = 12;
		chess[offset++] = 12;
		// 军棋
		chess[offset] = 13;
		// 洗牌
		for (int i = 24; i > 0; i--) {
			int swap = (int) (Math.random() * i);
			byte tmp = chess[i];
			chess[i] = chess[swap];
			chess[swap] = tmp;
		}
		// 行营后移
		chess[25] = chess[6];
		chess[26] = chess[8];
		chess[27] = chess[12];
		chess[28] = chess[16];
		chess[29] = chess[18];
		chess[6] = EMPTY;
		chess[8] = EMPTY;
		chess[12] = EMPTY;
		chess[16] = EMPTY;
		chess[18] = EMPTY;
		// 保证军棋在大本营
		for (int i = 0; i < 30; i++) {
			if (chess[i] == 13) {
				if (i != 26 && i != 28) {
					int swap = Math.random() > 0.5 ? 26 : 28;
					chess[i] = chess[swap];
					chess[swap] = 13;
				}
				break;
			}
		}
		boolean change;
		do {
			change = false;
			// 保证炸弹不在第一排
			for (int i = 0; i < 5; i++) {
				if (chess[i] == 11) {
					int swap;
					do {
						swap = (int) (Math.random() * 25 + 5);
					} while (chess[swap] == EMPTY || chess[swap] == 11 || chess[swap] == 13);
					chess[i] = chess[swap];
					chess[swap] = 11;
					change = true;
				}
			}
			// 保证地雷只在最后两排
			for (int i = 0; i < 20; i++) {
				if (chess[i] == 12) {
					int swap;
					do {
						swap = (int) (Math.random() * 10 + 20);
					} while (chess[swap] == 12 || chess[swap] == 13);
					chess[i] = chess[swap];
					chess[swap] = 12;
					change = true;
				}
			}
		} while (change);
	}
}

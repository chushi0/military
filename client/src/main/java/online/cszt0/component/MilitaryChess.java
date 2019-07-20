package online.cszt0.component;

import online.cszt0.util.ChessUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * 军棋棋盘界面
 *
 * @author 初始状态0
 * @date 2019/7/20 3:44
 */
public class MilitaryChess extends JComponent {

	private int[] boardPoints;
	private byte[] chess;

	/**
	 * 我方是否准备就绪
	 */
	private boolean isMeReady;
	/**
	 * 敌方是否准备就绪
	 */
	private boolean isOppositeReady;

	private int selection;

	private State state;

	public MilitaryChess() {
		state = State.READY;
		chess = new byte[60];
		ChessUtil.reset(chess);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		selection = -1;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		int width = getWidth();
		int height = getHeight();
		// 绘制棋盘
		drawBoard(g2d, width, height);
		// 根据阶段绘制
		switch (state) {
			case READY:
				// 如果对手已经就绪，填充对手棋盘
				if (isOppositeReady) {
					fillOppositeBoard(g2d, width, height);
				}
				// 绘制棋子
				drawPiece(g2d, width, height);
				break;
			case GAMING:
			case WAITING:
			case END:
				// 绘制棋子
				drawPiece(g2d, width, height);
		}
		// 绘制选择框
		if (selection != -1) {
			drawFrame(g2d, width, height);
		}
	}

	public void finishDeploy() {
		selection = -1;
		isMeReady = true;
		repaint();
	}

	public void oppositeReady() {
		isOppositeReady = true;
	}

	private void drawFrame(Graphics2D g2d, float width, float height) {
		// 为每个棋子留下的空间
		float gridWidth = width / 5;
		float gridHeight = height / 13;
		// 棋子占用空间
		float stationWidth = gridWidth * 3 / 5;
		float stationHeight = gridHeight * 3 / 5;
		// 位置
		int selection = this.selection;
		if (selection < 30) {
			selection += 35;
		} else {
			selection -= 30;
		}
		int[] xy = index2xy(selection);
		int left = (int) ((gridWidth * xy[0] + gridWidth / 2) - stationWidth / 2);
		int top = (int) ((gridHeight * xy[1] + gridHeight / 2) - stationHeight / 2);
		g2d.setColor(Color.green);
		Stroke defaultStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(5));
		g2d.drawRect(left, top, (int) stationWidth, (int) stationHeight);
		g2d.setStroke(defaultStroke);
	}

	private void drawPiece(Graphics2D g2d, float width, float height) {
		// 为每个棋子留下的空间
		float gridWidth = width / 5;
		float gridHeight = height / 13;
		// 棋子占用空间
		float stationWidth = gridWidth * 3 / 5;
		float stationHeight = gridHeight * 3 / 5;
		for (int i = 0; i < 60; i++) {
			int piece = chess[i];
			if (piece == ChessUtil.EMPTY) {
				continue;
			}
			int index = i;
			if (i < 30) {
				index += 35;
			} else {
				index -= 30;
			}
			int[] xy = index2xy(index);
			int left = (int) ((gridWidth * xy[0] + gridWidth / 2) - stationWidth / 2);
			int top = (int) ((gridHeight * xy[1] + gridHeight / 2) - stationHeight / 2);
			if (piece == ChessUtil.ENEMY) {
				g2d.setColor(Color.red);
				g2d.fillRect(left, top, (int) stationWidth, (int) stationHeight);
			} else {
				g2d.setColor(Color.blue);
				g2d.fillRect(left, top, (int) stationWidth, (int) stationHeight);
				g2d.setColor(Color.white);
				g2d.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
				String chessName = ChessUtil.getChessName(piece);
				g2d.drawString(chessName, left + 5, top + stationHeight * 3 / 4);
			}
		}
	}

	private void fillOppositeBoard(Graphics2D g2d, float width, float height) {
		g2d.setColor(Color.red);
		// 为每个棋子留下的空间
		float gridWidth = width / 5;
		float gridHeight = height / 13;
		// 棋子占用空间
		float stationWidth = gridWidth * 3 / 5;
		float stationHeight = gridHeight * 3 / 5;
		// 除行营外均填充棋子
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 5; x++) {
				if (y == 2 || y == 4) {
					if (x == 1 || x == 3) {
						continue;
					}
				}
				if (y == 3 && x == 2) {
					continue;
				}
				int left = (int) ((gridWidth * x + gridWidth / 2) - stationWidth / 2);
				int top = (int) ((gridHeight * y + gridHeight / 2) - stationHeight / 2);
				g2d.fillRect(left, top, (int) stationWidth, (int) stationHeight);
			}
		}
	}

	/**
	 * 绘制棋盘
	 */
	private void drawBoard(Graphics2D g2d, float width, float height) {
		g2d.setColor(Color.black);
		// 为每个棋子留下的空间
		float gridWidth = width / 5;
		float gridHeight = height / 13;
		// 兵站、行营、大本营占用空间
		float stationWidth = gridWidth * 2 / 5;
		float stationHeight = gridHeight * 2 / 5;
		// 连线用的点
		// 每个位置需要8个点
		// 每个点分横纵坐标
		int[] points = new int[5 * 13 * 8 * 2];
		// 绘制兵站、行营、大本营
		for (int y = 0; y < 13; y++) {
			// y==6 为山界
			if (y == 6) {
				continue;
			}
			for (int x = 0; x < 5; x++) {
				// y==0或y==12时，x==1和x==3是大本营
				if (y == 0 || y == 12) {
					if (x == 1 || x == 3) {
						drawBase(g2d, x, y, gridWidth, gridHeight, stationWidth, stationHeight, points);
					} else {
						drawStation(g2d, x, y, gridWidth, gridHeight, stationWidth, stationHeight, points);
					}
					continue;
				}
				// y==2/4/8/10时，x==1/3是行营
				if (y == 2 || y == 4 || y == 8 || y == 10) {
					if (x == 1 || x == 3) {
						drawCamp(g2d, x, y, gridWidth, gridHeight, stationWidth, stationHeight, points);
					} else {
						drawStation(g2d, x, y, gridWidth, gridHeight, stationWidth, stationHeight, points);
					}
					continue;
				}
				// y==3或y==9时，x==2是行营
				if ((y == 3 || y == 9) && x == 2) {
					drawCamp(g2d, x, y, gridWidth, gridHeight, stationWidth, stationHeight, points);
					continue;
				}
				// 除此以外，均为兵站
				drawStation(g2d, x, y, gridWidth, gridHeight, stationWidth, stationHeight, points);
			}
		}
		// 虚线画笔
		BasicStroke stroke = new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, new float[]{5f, 3f}, 3f);
		Stroke defaultStroke = g2d.getStroke();
		// 绘制路线
		// 1.铁路线
		g2d.setStroke(stroke);
		// 横向
		for (int y : new int[]{1, 5, 7, 11}) {
			for (int x = 1; x < 5; x++) {
				int startX = points[(xy2index(x - 1, y) * 8 + 1) * 2];
				int startY = points[(xy2index(x - 1, y) * 8 + 1) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8 + 3) * 2];
				int endY = points[(xy2index(x, y) * 8 + 3) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
		}
		// 纵向
		for (int x : new int[]{0, 4}) {
			for (int y = 2; y < 6; y++) {
				int startX = points[(xy2index(x, y - 1) * 8 + 2) * 2];
				int startY = points[(xy2index(x, y - 1) * 8 + 2) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8) * 2];
				int endY = points[(xy2index(x, y) * 8) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			for (int y = 8; y < 12; y++) {
				int startX = points[(xy2index(x, y - 1) * 8 + 2) * 2];
				int startY = points[(xy2index(x, y - 1) * 8 + 2) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8) * 2];
				int endY = points[(xy2index(x, y) * 8) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
		}
		// 跨过前线的三条线
		for (int x : new int[]{0, 2, 4}) {
			int startX = points[(xy2index(x, 5) * 8 + 2) * 2];
			int startY = points[(xy2index(x, 5) * 8 + 2) * 2 + 1];
			int endX = points[(xy2index(x, 7) * 8) * 2];
			int endY = points[(xy2index(x, 7) * 8) * 2 + 1];
			g2d.drawLine(startX, startY, endX, endY);
		}
		g2d.setStroke(defaultStroke);
		// 2.行营线
		for (int[] xy : new int[][]{{1, 2}, {1, 4}, {2, 3}, {3, 2}, {3, 4}, {1, 8}, {1, 10}, {2, 9}, {3, 8}, {3, 10}}) {
			int x = xy[0];
			int y = xy[1];
			{
				// 向上连接
				int startX = points[(xy2index(x, y - 1) * 8 + 2) * 2];
				int startY = points[(xy2index(x, y - 1) * 8 + 2) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8) * 2];
				int endY = points[(xy2index(x, y) * 8) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			{
				// 向下连接
				int startX = points[(xy2index(x, y) * 8 + 2) * 2];
				int startY = points[(xy2index(x, y) * 8 + 2) * 2 + 1];
				int endX = points[(xy2index(x, y + 1) * 8) * 2];
				int endY = points[(xy2index(x, y + 1) * 8) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			{
				// 向左连接
				int startX = points[(xy2index(x - 1, y) * 8 + 1) * 2];
				int startY = points[(xy2index(x - 1, y) * 8 + 1) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8 + 3) * 2];
				int endY = points[(xy2index(x, y) * 8 + 3) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			{
				// 向右连接
				int startX = points[(xy2index(x, y) * 8 + 1) * 2];
				int startY = points[(xy2index(x, y) * 8 + 1) * 2 + 1];
				int endX = points[(xy2index(x + 1, y) * 8 + 3) * 2];
				int endY = points[(xy2index(x + 1, y) * 8 + 3) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			{
				// 向左上连接
				int startX = points[(xy2index(x - 1, y - 1) * 8 + 6) * 2];
				int startY = points[(xy2index(x - 1, y - 1) * 8 + 6) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8 + 4) * 2];
				int endY = points[(xy2index(x, y) * 8 + 4) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			{
				// 向右上连接
				int startX = points[(xy2index(x + 1, y - 1) * 8 + 7) * 2];
				int startY = points[(xy2index(x + 1, y - 1) * 8 + 7) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8 + 5) * 2];
				int endY = points[(xy2index(x, y) * 8 + 5) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			{
				// 向左下连接
				int startX = points[(xy2index(x - 1, y + 1) * 8 + 5) * 2];
				int startY = points[(xy2index(x - 1, y + 1) * 8 + 5) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8 + 7) * 2];
				int endY = points[(xy2index(x, y) * 8 + 7) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			{
				// 向右下连接
				int startX = points[(xy2index(x + 1, y + 1) * 8 + 4) * 2];
				int startY = points[(xy2index(x + 1, y + 1) * 8 + 4) * 2 + 1];
				int endX = points[(xy2index(x, y) * 8 + 6) * 2];
				int endY = points[(xy2index(x, y) * 8 + 6) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
		}
		// 3.大本营线
		// 对方
		for (int x = 0; x < 5; x++) {
			// 向左连线
			if (x != 0) {
				int startX = points[(xy2index(x - 1, 0) * 8 + 1) * 2];
				int startY = points[(xy2index(x - 1, 0) * 8 + 1) * 2 + 1];
				int endX = points[(xy2index(x, 0) * 8 + 3) * 2];
				int endY = points[(xy2index(x, 0) * 8 + 3) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			// 向右连线
			if (x != 4) {
				int startX = points[(xy2index(x, 0) * 8 + 1) * 2];
				int startY = points[(xy2index(x, 0) * 8 + 1) * 2 + 1];
				int endX = points[(xy2index(x + 1, 0) * 8 + 3) * 2];
				int endY = points[(xy2index(x + 1, 0) * 8 + 3) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			// 向下连线
			int startX = points[(xy2index(x, 0) * 8 + 2) * 2];
			int startY = points[(xy2index(x, 0) * 8 + 2) * 2 + 1];
			int endX = points[(xy2index(x, 1) * 8) * 2];
			int endY = points[(xy2index(x, 1) * 8) * 2 + 1];
			g2d.drawLine(startX, startY, endX, endY);
		}
		// 我方
		for (int x = 0; x < 5; x++) {
			// 向左连线
			if (x != 0) {
				int startX = points[(xy2index(x - 1, 12) * 8 + 1) * 2];
				int startY = points[(xy2index(x - 1, 12) * 8 + 1) * 2 + 1];
				int endX = points[(xy2index(x, 12) * 8 + 3) * 2];
				int endY = points[(xy2index(x, 12) * 8 + 3) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			// 向右连线
			if (x != 4) {
				int startX = points[(xy2index(x, 12) * 8 + 1) * 2];
				int startY = points[(xy2index(x, 12) * 8 + 1) * 2 + 1];
				int endX = points[(xy2index(x + 1, 12) * 8 + 3) * 2];
				int endY = points[(xy2index(x + 1, 12) * 8 + 3) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
			// 向上连线
			int startX = points[(xy2index(x, 11) * 8 + 2) * 2];
			int startY = points[(xy2index(x, 11) * 8 + 2) * 2 + 1];
			int endX = points[(xy2index(x, 12) * 8) * 2];
			int endY = points[(xy2index(x, 12) * 8) * 2 + 1];
			g2d.drawLine(startX, startY, endX, endY);
		}
		// 4.剩下的线
		// 上下连
		for (int y : new int[]{1, 4, 7, 10}) {
			int startX = points[(xy2index(2, y) * 8 + 2) * 2];
			int startY = points[(xy2index(2, y) * 8 + 2) * 2 + 1];
			int endX = points[(xy2index(2, y + 1) * 8) * 2];
			int endY = points[(xy2index(2, y + 1) * 8) * 2 + 1];
			g2d.drawLine(startX, startY, endX, endY);
		}
		// 左右连
		for (int x : new int[]{0, 3}) {
			for (int y : new int[]{3, 9}) {
				int startX = points[(xy2index(x, y) * 8 + 1) * 2];
				int startY = points[(xy2index(x, y) * 8 + 1) * 2 + 1];
				int endX = points[(xy2index(x + 1, y) * 8 + 3) * 2];
				int endY = points[(xy2index(x + 1, y) * 8 + 3) * 2 + 1];
				g2d.drawLine(startX, startY, endX, endY);
			}
		}
		boardPoints = points;
	}

	private void drawCamp(Graphics2D g2d, int x, int y, float gridWidth, float gridHeight, float stationWidth,
	                      float stationHeight, int[] points) {
		stationWidth = gridWidth * 4 / 5;
		stationHeight = gridHeight * 4 / 5;
		int centerX = (int) (gridWidth * x + gridWidth / 2);
		int centerY = (int) (gridHeight * y + gridHeight / 2);
		int left = (int) (centerX - stationWidth / 2);
		int right = (int) (centerX + stationWidth / 2);
		int top = (int) (centerY - stationHeight / 2);
		int bottom = (int) (centerY + stationHeight / 2);
		g2d.drawOval(left, top, (int) stationWidth, (int) stationHeight);
		// 这里a,b分别为椭圆的半长轴、半短轴
		float a = stationWidth / 2;
		float b = stationHeight / 2;
		// p 为椭圆与直线y=x的交点坐标
		float p = (float) (a * b / Math.sqrt(a * a + b * b));
		int offset = xy2index(x, y) * 8 * 2;
		// 上
		points[offset++] = centerX;
		points[offset++] = top;
		// 右
		points[offset++] = right;
		points[offset++] = centerY;
		// 下
		points[offset++] = centerX;
		points[offset++] = bottom;
		// 左
		points[offset++] = left;
		points[offset++] = centerY;
		// 左上
		points[offset++] = (int) (centerX - p);
		points[offset++] = (int) (centerY - p);
		// 右上
		points[offset++] = (int) (centerX + p);
		points[offset++] = (int) (centerY - p);
		// 右下
		points[offset++] = (int) (centerX + p);
		points[offset++] = (int) (centerY + p);
		// 左下
		points[offset++] = (int) (centerX - p);
		points[offset] = (int) (centerY + p);
	}

	private void drawBase(Graphics2D g2d, int x, int y, float gridWidth, float gridHeight, float stationWidth,
	                      float stationHeight, int[] points) {
		// 大本营是行营的上半部分和兵站的下半部分的组合体
		int centerX = (int) (gridWidth * x + gridWidth / 2);
		int centerY = (int) (gridHeight * y + gridHeight / 2);
		int left = (int) (centerX - stationWidth / 2);
		int right = (int) (centerX + stationWidth / 2);
		int top = (int) (centerY - stationHeight / 2);
		int bottom = (int) (centerY + stationHeight / 2);
		// 下面的矩形
		g2d.fillRect(left, centerY - 1, (int) stationWidth, (int) stationHeight / 2);
		// 上面的椭圆
		g2d.fillArc(left, top, (int) stationWidth, (int) stationHeight, 0, 180);
		// 这里a,b分别为椭圆的半长轴、半短轴
		float a = stationWidth / 2;
		float b = stationHeight / 2;
		// p 为椭圆与直线y=x的交点坐标
		float p = (float) (a * b / Math.sqrt(a * a + b * b));
		int offset = xy2index(x, y) * 8 * 2;
		// 上
		points[offset++] = centerX;
		points[offset++] = top;
		// 右
		points[offset++] = right;
		points[offset++] = centerY;
		// 下
		points[offset++] = centerX;
		points[offset++] = bottom;
		// 左
		points[offset++] = left;
		points[offset++] = centerY;
		// 左上
		points[offset++] = (int) (centerX - p);
		points[offset++] = (int) (centerY - p);
		// 右上
		points[offset++] = (int) (centerX + p);
		points[offset++] = (int) (centerY - p);
		// 右下
		points[offset++] = right;
		points[offset++] = bottom;
		// 左下
		points[offset++] = left;
		points[offset] = bottom;
	}

	private void drawStation(Graphics2D g2d, int x, int y, float gridWidth, float gridHeight, float stationWidth,
	                         float stationHeight, int[] points) {
		int centerX = (int) (gridWidth * x + gridWidth / 2);
		int centerY = (int) (gridHeight * y + gridHeight / 2);
		int left = (int) (centerX - stationWidth / 2);
		int right = (int) (centerX + stationWidth / 2);
		int top = (int) (centerY - stationHeight / 2);
		int bottom = (int) (centerY + stationHeight / 2);
		g2d.drawRect(left, top, (int) stationWidth, (int) stationHeight);
		int offset = xy2index(x, y) * 8 * 2;
		// 上
		points[offset++] = centerX;
		points[offset++] = top;
		// 右
		points[offset++] = right;
		points[offset++] = centerY;
		// 下
		points[offset++] = centerX;
		points[offset++] = bottom;
		// 左
		points[offset++] = left;
		points[offset++] = centerY;
		// 左上
		points[offset++] = left;
		points[offset++] = top;
		// 右上
		points[offset++] = right;
		points[offset++] = top;
		// 右下
		points[offset++] = right;
		points[offset++] = bottom;
		// 左下
		points[offset++] = left;
		points[offset] = bottom;
	}

	@Override
	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			int button = e.getButton();
			if (button == MouseEvent.BUTTON1) {
				// 准备阶段且已准备完成，忽略事件
				if (state == State.READY && isMeReady) {
					return;
				}
				// 等待中，已结束，忽略事件
				if (state == State.WAITING || state == State.END) {
					return;
				}
				onMouseClick(e);
			} else if (button == MouseEvent.BUTTON3) {
				selection = -1;
				repaint();
			}
		}
	}

	private void onMouseClick(MouseEvent e) {
		float touchX = e.getX();
		float touchY = e.getY();
		// 为每个棋子留下的空间
		float gridWidth = getWidth() / 5f;
		float gridHeight = getHeight() / 13f;
		// 棋子占用空间
		float stationWidth = gridWidth * 3 / 5;
		float stationHeight = gridHeight * 3 / 5;
		// 计算当前点击位置
		int x = (int) (touchX / gridWidth);
		int y = (int) (touchY / gridHeight);
		// 检查点击是否有效
		if (y == 6) {
			// y==6为山界，认为无效
			return;
		}
		float left = gridWidth * x + (gridWidth - stationWidth) / 2;
		float right = gridWidth * x + (gridWidth + stationWidth) / 2;
		float top = gridHeight * y + (gridHeight - stationHeight) / 2;
		float bottom = gridHeight * y + (gridHeight + stationHeight) / 2;
		if (!(touchX > left && touchX < right && touchY > top && touchY < bottom)) {
			return;
		}
		// 转换棋子坐标
		if (y > 6) {
			y -= 7;
		} else {
			y += 6;
		}
		int index = xy2index(x, y);
		if (index < 0 || index >= 60) {
			return;
		}
		// 如果当前没有选中棋子，且该棋子可选中，则选中
		if (selection == -1) {
			if (chess[index] > 1) {
				selection = index;
				repaint();
			}
			return;
		}
		// 如果已经选中棋子，则根据不同阶段执行不同内容
		if (state == State.READY) {
			clickOnReady(index);
		} else {
			clickOnGaming(index);
		}
		repaint();
	}

	private void clickOnGaming(int index) {
	}

	private void clickOnReady(int index) {
		// 检查目标位置棋子是否有效
		if (chess[index] == ChessUtil.EMPTY) {
			selection = -1;
			return;
		}
		// 检查目标位置是否可以容纳当前选中的棋子
		if (chess[selection] == 13) {
			if (index != 26 && index != 28) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "军棋必须放在大本营", "部署错误", JOptionPane.ERROR_MESSAGE);
				selection = -1;
				return;
			}
		}
		if (chess[selection] == 11) {
			if (index < 5) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "炸弹不能放置在第一排", "部署错误", JOptionPane.ERROR_MESSAGE);
				selection = -1;
				return;
			}
		}
		if (chess[selection] == 12) {
			if (index < 20) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "地雷只能放置在后两排", "部署错误", JOptionPane.ERROR_MESSAGE);
				selection = -1;
				return;
			}
		}
		if (chess[index] == 13) {
			if (selection != 26 && selection != 28) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "军棋必须放在大本营", "部署错误", JOptionPane.ERROR_MESSAGE);
				selection = -1;
				return;
			}
		}
		if (chess[index] == 11) {
			if (selection < 5) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "炸弹不能放置在第一排", "部署错误", JOptionPane.ERROR_MESSAGE);
				selection = -1;
				return;
			}
		}
		if (chess[index] == 12) {
			if (selection < 20) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "地雷只能放置在后两排", "部署错误", JOptionPane.ERROR_MESSAGE);
				selection = -1;
				return;
			}
		}
		// 交换棋子
		byte tmp = chess[index];
		chess[index] = chess[selection];
		chess[selection] = tmp;
		selection = -1;
	}

	private int xy2index(int x, int y) {
		return y * 5 + x;
	}

	private int[] index2xy(int index) {
		return new int[]{
				index % 5,
				index / 5
		};
	}

	private enum State {
		/**
		 * 准备阶段，玩家摆设阵型
		 */
		READY,
		/**
		 * 游戏阶段，玩家操作棋子移动
		 */
		GAMING,
		/**
		 * 等待阶段，玩家等待对手操作
		 */
		WAITING,
		/**
		 * 结束阶段，游戏已经结束
		 */
		END
	}
}

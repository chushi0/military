package online.cszt0.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用于辅助工兵计算的图模型
 *
 * @author 初始状态0
 * @date 2019/7/20 17:31
 */
public class GraphUtil {
	private ArrayList<Line> lineArrayList;

	private int startPoint;
	private int endPoint;

	public GraphUtil() {
		lineArrayList = new ArrayList<>(15);
		startPoint = -1;
		endPoint = -1;
		// 在没有摆放任何棋子的情况下的图
		lineArrayList.addAll(Arrays.asList(
				new Line(5, 9), new Line(5, 25), new Line(9, 29), new Line(25, 27), new Line(27, 29),
				new Line(30, 50), new Line(34, 54), new Line(30, 32), new Line(32, 34), new Line(50, 54),
				new Line(25, 30), new Line(27, 32), new Line(29, 34)
		));
	}

	public void setStartPoint(int start) {
		if (start < 30) {
			start += 30;
		} else {
			start -= 30;
		}
		startPoint = start;
		addPoint(start);
	}

	public void setEndPoint(int end) {
		if (end < 30) {
			end += 30;
		} else {
			end -= 30;
		}
		endPoint = end;
		addPoint(end);
	}

	/**
	 * 依据棋盘切割线
	 *
	 * @param chess
	 * 		棋盘
	 */
	public void cutByBoard(byte[] chess) {
		// 遍历所有铁路上的兵站
		for (int y : new int[]{1, 5, 6, 10}) {
			for (int x = 1; x < 4; x++) {
				int point = BoardUtil.xy2index(x, y);
				int p;
				if (point < 30) {
					p = point + 30;
				} else {
					p = point - 30;
				}
				if (chess[p] == ChessUtil.EMPTY) {
					continue;
				}
				// 忽略起点和终点
				if (point == startPoint || point == endPoint) {
					continue;
				}
				// 切割线
				cutLine(point);
			}
		}
		for(int x:new int[] {0,4}) {
			for(int y=1;y<11;y++) {
				int point = BoardUtil.xy2index(x, y);
				int p;
				if (point < 30) {
					p = point + 30;
				} else {
					p = point - 30;
				}
				if (chess[p] == ChessUtil.EMPTY) {
					continue;
				}
				// 忽略起点和终点
				if (point == startPoint || point == endPoint) {
					continue;
				}
				// 切割线
				cutLine(point);
			}
		}
	}

	public boolean accessible() {
		// 提取出全部的点
		Set<Integer> pointSet = new HashSet<>();
		// 添加起点终点
		pointSet.add(startPoint);
		pointSet.add(endPoint);
		for (Line line : lineArrayList) {
			pointSet.add(BoardUtil.xy2index(line.point1[0], line.point1[1]));
			pointSet.add(BoardUtil.xy2index(line.point2[0], line.point2[1]));
		}
		Integer[] points = pointSet.toArray(new Integer[0]);
		int length = points.length;
		int[] pointAttributes = new int[length];
		// 从点中找到起点和终点
		int startIndex = indexOf(points, startPoint);
		int endIndex = indexOf(points, endPoint);
		pointAttributes[startIndex] = 1;
		// 循环遍历
		while (pointAttributes[endIndex] == 0) {
			boolean change = false;
			// 搜索即将遍历的点
			for (int i = 0; i < length; i++) {
				if (pointAttributes[i] == 1) {
					pointAttributes[i] = 2;
					change = true;
					int point = points[i];
					// 寻找对应的线
					Line[] lines = new Line[3];
					int offset = 0;
					for (Line line : lineArrayList) {
						if (BoardUtil.xy2index(line.point1[0], line.point1[1]) == point || BoardUtil.xy2index(line.point2[0], line.point2[1]) == point) {
							lines[offset++] = line;
						}
					}
					// 另一端设为可达，标记即将遍历
					for (Line line : lines) {
						if (line == null) {
							continue;
						}
						int point1 = BoardUtil.xy2index(line.point1[0], line.point1[1]);
						int point2 = BoardUtil.xy2index(line.point2[0], line.point2[1]);
						int point1Index = indexOf(points, point1);
						int point2Index = indexOf(points, point2);
						if (pointAttributes[point1Index] == 0) {
							pointAttributes[point1Index] = 1;
						}
						if (pointAttributes[point2Index] == 0) {
							pointAttributes[point2Index] = 1;
						}
					}
				}
			}
			if (!change) {
				return false;
			}
		}
		return true;
	}

	private int indexOf(Object[] objects, Object o) {
		int len = objects.length;
		for (int i = 0; i < len; i++) {
			if (o == objects[i]) {
				return i;
			}
		}
		return -1;
	}

	private void cutLine(int point) {
		// 寻找包含这个点的线
		// 最多三条
		Line[] lines = new Line[3];
		int offset = 0;
		for (Line line : lineArrayList) {
			if (line.contains(point)) {
				lines[offset++] = line;
			}
		}
		int[] xy = BoardUtil.index2xy(point);
		// 将每条线拆成两部分
		for (Line line : lines) {
			if (line == null) {
				continue;
			}
			int xOffset = line.point2[0] - line.point1[0];
			int yOffset = line.point2[1] - line.point1[1];
			int xOffsetStep = xOffset == 0 ? 0 : 1;
			int yOffsetStep = yOffset == 0 ? 0 : 1;
			int pointPrev = BoardUtil.xy2index(xy[0] - xOffsetStep, xy[1] - yOffsetStep);
			int pointNext = BoardUtil.xy2index(xy[0] + xOffsetStep, xy[1] + yOffsetStep);
			lineArrayList.remove(line);
			// 考虑点重合问题
			int point1 = BoardUtil.xy2index(line.point1[0], line.point1[1]);
			int point2 = BoardUtil.xy2index(line.point2[0], line.point2[1]);
			if (point1 < pointPrev) {
				lineArrayList.add(new Line(point1, pointPrev));
			}
			if (point2 > pointNext) {
				lineArrayList.add(new Line(pointNext, point2));
			}
		}
	}

	private void addPoint(int point) {
		// 寻找包含这个点的线
		// 最多三条
		Line[] lines = new Line[3];
		int offset = 0;
		for (Line line : lineArrayList) {
			if (line.contains(point)) {
				lines[offset++] = line;
			}
		}
		// 将每条线拆成两部分
		for (Line line : lines) {
			if (line == null) {
				continue;
			}
			lineArrayList.remove(line);
			// 考虑点重合问题
			int point1 = BoardUtil.xy2index(line.point1[0], line.point1[1]);
			int point2 = BoardUtil.xy2index(line.point2[0], line.point2[1]);
			if (point1 != point) {
				lineArrayList.add(new Line(point1, point));
			}
			if (point2 != point) {
				lineArrayList.add(new Line(point, point2));
			}
		}
	}

	static class Line {
		final int[] point1;
		final int[] point2;
		// 这条线是否是横向的
		private final boolean isHorizontal;

		Line(int point1, int point2) {
			if (point1 > point2) {
				int tmp = point1;
				point1 = point2;
				point2 = tmp;
			}
			this.point1 = BoardUtil.index2xy(point1);
			this.point2 = BoardUtil.index2xy(point2);
			isHorizontal = this.point1[1] == this.point2[1];
		}

		boolean contains(int point) {
			int[] p = BoardUtil.index2xy(point);
			if (isHorizontal) {
				if (p[1] != point1[1]) {
					return false;
				}
				return p[0] >= point1[0] && p[0] <= point2[0];
			} else {
				if (p[0] != point1[0]) {
					return false;
				}
				return p[1] >= point1[1] && p[1] <= point2[1];
			}
		}
	}
}

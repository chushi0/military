package online.cszt0.ui;

import online.cszt0.component.MilitaryChess;
import online.cszt0.util.ChessUtil;
import online.cszt0.util.SocketUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;

/**
 * 游戏主界面
 *
 * @author 初始状态0
 * @date 2019/7/20 3:39
 */
public class GameFrame {
	private Socket socket;

	private JFrame frame;
	private GameFrameUI ui;
	private MilitaryChess militaryChess;

	private long enemyCostTime;
	private long teamCostTime;
	private byte lostJustNow;

	private byte id;

	private int currentTurn;

	GameFrame(Socket socket) {
		this.socket = socket;
		frame = new JFrame();
		ui = new GameFrameUI();
		militaryChess = new MilitaryChess();
		ui.chestPanel.add(militaryChess, BorderLayout.CENTER);
		ui.informationLabel.setText("请部署您的军队");
		ui.finishDeploy.addActionListener(e -> {
			try {
				SocketUtil.sendDeployData(socket, militaryChess.getChessData());
			} catch (IOException e1) {
				e1.printStackTrace();
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(frame, e1.getMessage(), "发生错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			militaryChess.finishDeploy();
			ui.finishDeploy.setEnabled(false);
			ui.myLabel.setText("部署完成");
			ui.informationLabel.setText("等待对方部署军队");
		});
		militaryChess.setMoveChessListener(this::onChessMove);
		frame.setContentPane(ui.contentPanel);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		SocketUtil.startSocketListener(socket, this::onReceive);
		Thread thread = new Thread(this::run);
		thread.setDaemon(true);
		thread.start();
	}

	private void run() {
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			switch (currentTurn) {
				case 1:
					enemyCostTime += 100;
					break;
				case 2:
					teamCostTime += 100;
					break;
			}
			if (currentTurn != 0) {
				ui.myLabel.setText(String.format("已用时间：%n%d:%02d", teamCostTime / 60 / 1000, teamCostTime / 1000 % 60));
				ui.oppositeLabel.setText(String.format("已用时间：%n%d:%02d", enemyCostTime / 60 / 1000, enemyCostTime / 1000 % 60));
				if (lostJustNow > 1) {
					ui.informationLabel.setText(String.format("我方刚刚损失棋子：%n%s", ChessUtil.getChessName(lostJustNow)));
				} else {
					ui.informationLabel.setText(null);
				}
			}
		}
	}

	private void onReceive(byte[] bytes) {
		switch (bytes[0]) {
			case 0:
				militaryChess.oppositeReady();
				ui.oppositeLabel.setText("部署完成");
				break;
			case 1:
				id = bytes[1];
				startGame(bytes[2] == 0);
				break;
			case 2:
				lostJustNow = militaryChess.moveChess(bytes[2], bytes[3], parseMoveType(bytes[1]));
				if (currentTurn == 0) {
					currentTurn = 1;
				} else {
					currentTurn = 2;
				}
				militaryChess.changeGameTurn();
				break;
			case 3:
				militaryChess.setEnemyFlag(bytes[1]);
				break;
			case 4:
				militaryChess.endGame();
				int index = (bytes[1] == id ? 0 : 1) << 1 | bytes[2];
				String[] text = {"对手军旗被拔，全军覆没！", "对手无棋可走，全军覆没！", "我方军旗被拔，全军覆没！", "我方无棋可走，全军覆没！"};
				JOptionPane.showMessageDialog(frame, text[index], bytes[1] == id ? "你赢了" : "你输了", JOptionPane.INFORMATION_MESSAGE);
				break;
		}
	}

	private MilitaryChess.MoveType parseMoveType(byte id) {
		switch (id) {
			case 0:
			case 1:
				return MilitaryChess.MoveType.REPLACE;
			case 2:
				return MilitaryChess.MoveType.KEEP;
			case 3:
				return MilitaryChess.MoveType.CLEAR;
			default:
				return null;
		}
	}

	private synchronized void startGame(boolean isFirst) {
		militaryChess.startGame(isFirst);
		ui.informationLabel.setText(null);
		ui.oppositeLabel.setText(null);
		ui.myLabel.setText(null);
		ui.finishDeploy.setVisible(false);
		currentTurn = isFirst ? 2 : 1;
	}

	private void onChessMove(int from, int to) {
		currentTurn = 0;
		try {
			SocketUtil.sendMovementData(socket, from, to);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

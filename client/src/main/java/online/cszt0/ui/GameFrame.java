package online.cszt0.ui;

import online.cszt0.component.MilitaryChess;

import javax.swing.*;
import java.awt.*;
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

	GameFrame(Socket socket) {
		this.socket = socket;
		frame = new JFrame();
		ui = new GameFrameUI();
		militaryChess = new MilitaryChess();
		ui.chestPanel.add(militaryChess, BorderLayout.CENTER);
		ui.informationLabel.setText("请部署您的军队");
		ui.finishDeploy.addActionListener(e -> {
			militaryChess.finishDeploy();
			ui.finishDeploy.setEnabled(false);
			ui.myLabel.setText("部署完成");
			ui.informationLabel.setText("等待对方部署军队");
			startGameIfReady();
		});
		militaryChess.setMoveChessListener(this::onChessMove);
		frame.setContentPane(ui.contentPanel);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		new Thread(() -> {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			militaryChess.oppositeReady();
			ui.oppositeLabel.setText("部署完成");
			startGameIfReady();
		}).start();
	}

	private synchronized void startGameIfReady() {
		if (militaryChess.isAllReady()) {
			militaryChess.startGame(true);
			ui.informationLabel.setText(null);
			ui.oppositeLabel.setText(null);
			ui.myLabel.setText(null);
			ui.finishDeploy.setVisible(false);
		}
	}

	private void onChessMove(int from, int to) {
		militaryChess.moveChess(from, to, MilitaryChess.MoveType.REPLACE);
	}
}

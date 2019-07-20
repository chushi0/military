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
		ui.finishDeploy.addActionListener(e -> {
			militaryChess.finishDeploy();
			ui.finishDeploy.setEnabled(false);
		});
		frame.setContentPane(ui.contentPanel);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}

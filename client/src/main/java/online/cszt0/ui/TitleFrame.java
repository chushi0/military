package online.cszt0.ui;

import javax.swing.*;

/**
 * 标题画面
 *
 * @author 初始状态0
 * @date 2019/7/20 1:39
 */
public class TitleFrame {
	JFrame frame;
	TitleFrameUI ui;

	public TitleFrame() {
		frame = new JFrame("陆战棋");
		ui = new TitleFrameUI();
		ui.playButton.addActionListener(e -> showDialog());
		ui.exitButton.addActionListener(e -> frame.dispose());
		frame.setContentPane(ui.content);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	private void showDialog() {
		JoinGameDialog dialog = new JoinGameDialog();
		dialog.setTitle("对战模式");
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}
}

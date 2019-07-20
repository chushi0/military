package online.cszt0;

import online.cszt0.ui.TitleFrame;

import javax.swing.*;

/**
 * @author 初始状态0
 * @date 2019/7/20 1:30
 */
public class Main {
	public static void main(String[] args) {
		// 使用系统默认风格
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		// 启动标题画面
		new TitleFrame();
	}
}

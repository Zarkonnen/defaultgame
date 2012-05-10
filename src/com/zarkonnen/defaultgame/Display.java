package com.zarkonnen.defaultgame;

import java.awt.Color;
import java.awt.Graphics2D;

public class Display {
	final GameWorld w;
	final int width;
	final int height;

	Display(GameWorld w, int width, int height) {
		this.w = w;
		this.width = width;
		this.height = height;
	}

	void draw(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		g.setColor(Color.RED);
		g.fillOval((int) (System.currentTimeMillis() / 10 % width), height / 2 - 20, 40, 40);
		Draw.text(g, "Hello World!{smiley}How are you?\n[green]I am well[]!\n[black]BLACK TEXT![]", 100, 100);
	}
}

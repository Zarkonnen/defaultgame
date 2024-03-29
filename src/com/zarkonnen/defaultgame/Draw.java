package com.zarkonnen.defaultgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Draw {
	static HashMap<String, BufferedImage> tints = new HashMap<String, BufferedImage>();
	static BufferedImage font;
	static final int F_WIDTH = 8;
	static final int F_DISP_WIDTH = 7;
	static final int F_HEIGHT = 13;
	static final int F_BASE = (int) ' ';
	static final int F_CEIL = (int) '~' + 1;
	static final int F_ERR = (int) '?' - F_BASE;
	static final int IMG_OFFSET = 0;
	
	static void loadFont() {
		if (font == null) {
			font = MediaProvider.it.readImage("font4", Transparency.BITMASK);
		}
	}
	
	public static String getColorHex(Color c) {
		if (c == null) { return ""; }
		String r = Integer.toHexString(c.getRed());
		r = r.length() == 1 ? "0" + r : r;
		String g = Integer.toHexString(c.getGreen());
		g = g.length() == 1 ? "0" + g : g;
		String b = Integer.toHexString(c.getBlue());
		b = b.length() == 1 ? "0" + b : b;
		return "[" + r + g + b + "]";
	}
	
	public static void button(Graphics g, String text, int x, int y, int width) {
		g.setColor(Color.DARK_GRAY);
		g.fill3DRect(x, y, width, 20, true);
		text(g, text, x + 10, y + 4);
	}
	
	public static void text(Graphics g, String text, int x, int y) {
		text(g, text, x, y, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	public static void text(Graphics g, String text, int x, int y, int maxWidth, int maxHeight) {
		text(g, text, x, y, maxWidth, maxHeight, true);
	}
	
	public static String esc(String text) {
		return text.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]");
	}
	
	public static void text(Graphics g, String text, int x, int y, int maxWidth, int maxHeight, boolean allowCommands) {
		loadFont();
		BufferedImage f = font;
		int cols = maxWidth / F_DISP_WIDTH;
		int rows = maxHeight / F_HEIGHT;
		int c = 0;
		int r = 0;
		int n = 0;
		char[] cs = text.toCharArray();
		Color bgC = null;
		while (n < cs.length) {
			if (c >= cols) {
				c = 0;
				r++;
			}
			if (r >= rows) {
				return;
			}
			if (cs[n] == '\\' && allowCommands) {
				n++;
			} else {
				if (cs[n] == '\n') {
					c = 0;
					r++;
					n++;
					continue;
				}
				if (cs[n] == '{' && allowCommands) {
					int n2 = n + 1;
					while (cs[n2] != '}') { n2++; }
					char[] name = new char[n2 - n - 1];
					System.arraycopy(cs, n + 1, name, 0, name.length);
					String nameS = new String(name);
					BufferedImage sym = null;
					if (nameS.startsWith("[") && nameS.contains("]")) {
						String tintN = nameS.substring(1, nameS.indexOf("]"));
						Color tintC = null;
						if (!tintN.isEmpty()) {
							if (tintN.matches("[a-fA-F0-9]{6}")) {
								try {
									tintC = new Color(
											Integer.parseInt(tintN.substring(0, 2), 16),
											Integer.parseInt(tintN.substring(2, 4), 16),
											Integer.parseInt(tintN.substring(4, 6), 16));
								} catch (Exception e) {
									// Ignore
									e.printStackTrace();
								}
							}
							if (tintC == null) {
								try {
									tintC = (Color) Color.class.getField(tintN).get(null);
								} catch (Exception e) {
									// Ignore
									e.printStackTrace();
								}
							}
							if (tintC != null) {
								sym = MediaProvider.it.getImage(nameS.substring(nameS.indexOf("]") + 1));
								sym = MediaProvider.it.tint(sym, tintC);
							}
						}
					}
					
					if (sym == null) { sym = MediaProvider.it.getImage(nameS); }
					int overhang = (F_HEIGHT - sym.getHeight()) / 2;
					g.drawImage(sym, x + (c) * F_DISP_WIDTH, y + r * F_HEIGHT + overhang, null);
					n = n2 + 1;
					c += (sym.getWidth() / F_DISP_WIDTH) + (sym.getWidth() % F_DISP_WIDTH == 0 ? 0 : 1);
					continue;
				}
				if (cs[n] == '[' && allowCommands) {
					int n2 = n + 1;
					while (cs[n2] != ']') { n2++; }
					char[] name = new char[n2 - n - 1];
					System.arraycopy(cs, n + 1, name, 0, name.length);
					String tintN = new String(name);
					boolean bg = false;
					if (tintN.startsWith("bg=")) {
						bg = true;
						tintN = tintN.substring(3);
					}
					Color tintC = null;
					if (bg || !tints.containsKey(tintN)) {
						if (!tintN.isEmpty()) {
							if (tintN.matches("[a-fA-F0-9]{6}")) {
								try {
									tintC = new Color(
											Integer.parseInt(tintN.substring(0, 2), 16),
											Integer.parseInt(tintN.substring(2, 4), 16),
											Integer.parseInt(tintN.substring(4, 6), 16));
								} catch (Exception e) {
									// Ignore
									e.printStackTrace();
								}
							}
							if (tintC == null) {
								try {
									tintC = (Color) Color.class.getField(tintN).get(null);
								} catch (Exception e) {
									// Ignore
									e.printStackTrace();
								}
							}
						}
						if (bg) {
							bgC = tintC;
						} else {
							if (tintC == null) {
								tints.put(tintN, font);
							} else {
								tints.put(tintN, MediaProvider.it.tint(font, tintC));
							}
						}					
					}
					if (!bg) { f = tints.get(tintN); }
					n = n2 + 1;
					continue;
				}
			}
			int val = (int) cs[n];
			val = val < F_CEIL ? val - F_BASE : F_ERR;
			if (bgC != null) {
				g.setColor(bgC);
				g.fillRect(
					x + c * F_DISP_WIDTH - (n == 0 ? -2 : 1), y + r * F_HEIGHT - 2,
					F_DISP_WIDTH + 3, F_HEIGHT + 3);
			}
			g.drawImage(
					f,
					x + c * F_DISP_WIDTH, y + r * F_HEIGHT,
					x + (c + 1) * F_DISP_WIDTH, y + r * F_HEIGHT + F_HEIGHT,
					val * F_WIDTH + IMG_OFFSET, 0,
					(val) * F_WIDTH + F_DISP_WIDTH + IMG_OFFSET, F_HEIGHT,
					null
			);
			c++;
			n++;
		}
	}
}

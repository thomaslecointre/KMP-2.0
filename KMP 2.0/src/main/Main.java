package main;

import ui.Console;

public class Main {

	public static void main(String[] args) {
		Thread thread = new Thread(new Console());
		thread.start();
	}

}

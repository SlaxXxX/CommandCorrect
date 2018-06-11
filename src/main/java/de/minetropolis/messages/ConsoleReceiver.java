package de.minetropolis.messages;

public class ConsoleReceiver extends MessageReceiver {

	@Override
	public void displayMessage(String message) {
		System.out.println(decolorize(message));
	}

}

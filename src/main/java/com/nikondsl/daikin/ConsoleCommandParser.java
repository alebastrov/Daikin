package com.nikondsl.daikin;

public interface ConsoleCommandParser<T> {
	T parseCommand(String consoleCommand);
}

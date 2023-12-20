package net.haspamelodica.parser.tokenizer;

import java.util.function.Predicate;

import net.haspamelodica.parser.grammar.Terminal;

public class CharGroupAndTerminal<V>
{
	private final CharGroup		charGroup;
	private final Terminal<V>	terminal;

	public CharGroupAndTerminal(CharGroup charGroup, Terminal<V> terminal)
	{
		this.charGroup = charGroup;
		this.terminal = terminal;
	}

	public CharGroup getCharGroup()
	{
		return charGroup;
	}
	public Terminal<V> getTerminal()
	{
		return terminal;
	}

	public static <V> CharGroupAndTerminal<V> build(char... chars)
	{
		return chars.length == 0 ? buildEmpty() : build(Character.toString(chars[0]), chars);
	}
	public static <V> CharGroupAndTerminal<V> build(String terminalName, char... chars)
	{
		return build(new Terminal<>(terminalName), chars);
	}
	public static <V> CharGroupAndTerminal<V> build(Terminal<V> terminal, char... chars)
	{
		return chars.length == 0 ? buildEmpty(terminal) : new CharGroupAndTerminal<>(CharGroup.build(chars), terminal);
	}
	public static <V> CharGroupAndTerminal<V> build(Char... chars)
	{
		return chars.length == 0 ? buildEmpty() : build(chars[0].toString(), chars);
	}
	public static <V> CharGroupAndTerminal<V> build(String terminalName, Char... chars)
	{
		return build(new Terminal<>(terminalName), chars);
	}
	public static <V> CharGroupAndTerminal<V> build(Terminal<V> terminal, Char... chars)
	{
		return chars.length == 0 ? buildEmpty(terminal) : new CharGroupAndTerminal<>(CharGroup.build(chars), terminal);
	}
	public static <V> CharGroupAndTerminal<V> buildEmpty()
	{
		return buildAll("empty");
	}
	public static <V> CharGroupAndTerminal<V> buildEmpty(String terminalName)
	{
		return buildEmpty(new Terminal<>(terminalName));
	}
	public static <V> CharGroupAndTerminal<V> buildEmpty(Terminal<V> terminal)
	{
		return new CharGroupAndTerminal<>(CharGroup.buildEmpty(), terminal);
	}
	public static <V> CharGroupAndTerminal<V> buildAll()
	{
		return buildAll("all");
	}
	public static <V> CharGroupAndTerminal<V> buildAll(String terminalName)
	{
		return buildAll(new Terminal<>(terminalName));
	}
	public static <V> CharGroupAndTerminal<V> buildAll(Terminal<V> terminal)
	{
		return new CharGroupAndTerminal<>(CharGroup.buildAll(), terminal);
	}
	public static <V> CharGroupAndTerminal<V> build(String terminalName, String charGroupName, Predicate<Char> predicate)
	{
		return build(new Terminal<>(terminalName), charGroupName, predicate);
	}
	public static <V> CharGroupAndTerminal<V> build(Terminal<V> terminal, String charGroupName, Predicate<Char> predicate)
	{
		return new CharGroupAndTerminal<>(CharGroup.build(charGroupName, predicate), terminal);
	}
}

package net.haspamelodica.parser.grammar.attributes;

import net.haspamelodica.parser.ast.ASTNode;
import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.Symbol;

// TODO find a better class name
public abstract class SymbolValueReference<V, S extends Symbol, N extends ASTNode<S>>
{
	private final NeighboringValuePositionType type;

	private final Production	rootProduction;
	/**
	 * -1 means root node, n (for n>=0) means n-th child node.
	 */
	private final int			position;

	private final S			targetSymbol;
	private final boolean	isRoot;

	public SymbolValueReference(NeighboringValuePositionType type, Production rootProduction, int position)
	{
		this.type = type;
		this.rootProduction = rootProduction;
		this.position = position;
		this.isRoot = position == -1;

		if(position < -1 || position >= rootProduction.getRhs().getSymbols().size())
			throw new IllegalArgumentException("While creating a SymbolValueReference for " + rootProduction + ": Position " + position + " out of range");

		this.targetSymbol = calculateTargetSymbol(rootProduction, position);
	}

	@SuppressWarnings("unchecked")
	private static <S extends Symbol> S calculateTargetSymbol(Production rootProduction, int position)
	{
		return (S) (position < 0 ? rootProduction.getLhs() : rootProduction.getRhs().getSymbols().get(position));
	}

	protected static Symbol getParameterSymbol(Production rootProduction, int position)
	{
		if(position < 0)
			return rootProduction.getLhs();
		else
			return rootProduction.getRhs().getSymbols().get(position);
	}

	public NeighboringValuePositionType getType()
	{
		return type;
	}
	public Production getRootProduction()
	{
		return rootProduction;
	}
	public S getTargetSymbol()
	{
		return targetSymbol;
	}
	public int getPosition()
	{
		return position;
	}
	public boolean isRoot()
	{
		return isRoot;
	}

	public V getValue(InnerNode rootNode)
	{
		checkNode(rootNode);
		return getValueForNode(getTarget(rootNode));
	}
	public void setValue(InnerNode rootNode, V value)
	{
		checkNode(rootNode);
		setValueForNode(getTarget(rootNode), value);
	}

	private void checkNode(InnerNode rootNode)
	{
		if(!rootNode.getProduction().equals(rootProduction))
			throw new IllegalArgumentException("This neighboring attribute is defined only for the production " + rootProduction);
	}
	@SuppressWarnings("unchecked")
	private N getTarget(InnerNode rootNode)
	{
		return (N) (position < 0 ? rootNode : rootNode.getChildren().get(position));
	}

	protected abstract V getValueForNode(N target);
	protected abstract void setValueForNode(N target, V value);

	public static enum NeighboringValuePositionType
	{
		ATTRIBUTE_VALUE,
		TERMINAL_VALUE;
	}
}

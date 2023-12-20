package net.haspamelodica.parser.grammar.attributes;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.generics.QuadFunction;
import net.haspamelodica.parser.generics.QuinFunction;
import net.haspamelodica.parser.generics.TriFunction;
import net.haspamelodica.parser.grammar.Production;

public abstract class AttributeEquation<V>
{
	private final Production							rootProduction;
	private final AttributeValueReference<V>			returnValue;
	private final List<SymbolValueReference<?, ?, ?>>	parameters;

	private AttributeEquation(Production rootProduction, AttributeValueReference<V> returnValue, List<SymbolValueReference<?, ?, ?>> parameters)
	{
		this.rootProduction = rootProduction;
		this.returnValue = returnValue;
		this.parameters = parameters;
	}

	public Production getRootProduction()
	{
		return rootProduction;
	}
	public List<SymbolValueReference<?, ?, ?>> getParameters()
	{
		return parameters;
	}
	public AttributeValueReference<V> getReturnValue()
	{
		return returnValue;
	}

	public void evaluateAndSaveResult(InnerNode rootNode)
	{
		returnValue.setValue(rootNode, evaluateDontSaveResult(rootNode));
	}

	public abstract V evaluateDontSaveResult(InnerNode rootNode);

	@Override
	public String toString()
	{
		return returnValue + " = somefunction(" + parameters.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
	}

	public static <V> AttributeEquation<V> build(Production rootProduction, Supplier<V> function,
			AttributeValueReference<V> returnValue)
	{
		return new AttributeEquation<V>(rootProduction, returnValue, List.of())
		{
			@Override
			public V evaluateDontSaveResult(InnerNode rootNode)
			{
				return function.get();
			}
		};
	}
	public static <V> AttributeEquation<V> buildOnlyAttribs(Production rootProduction, Supplier<V> function,
			int returnPosition, Attribute<V> returnAttribute)
	{
		return build(rootProduction, function, new AttributeValueReference<>(rootProduction, returnPosition, returnAttribute));
	}
	public static <PV1, V> AttributeEquation<V> build(Production rootProduction, Function<PV1, V> function,
			AttributeValueReference<V> returnValue,
			SymbolValueReference<PV1, ?, ?> param1)
	{
		return new AttributeEquation<V>(rootProduction, returnValue, List.of(param1))
		{
			@Override
			public V evaluateDontSaveResult(InnerNode rootNode)
			{
				return function.apply(param1.getValue(rootNode));
			}
		};
	}
	public static <PV1, V> AttributeEquation<V> buildOnlyAttribs(Production rootProduction, Function<PV1, V> function,
			int returnPosition, Attribute<V> returnAttribute,
			int param1Position, Attribute<PV1> param1Attribute)
	{
		return build(rootProduction, function,
				new AttributeValueReference<>(rootProduction, returnPosition, returnAttribute),
				new AttributeValueReference<>(rootProduction, param1Position, param1Attribute));
	}
	public static <V> AttributeEquation<V> buildHandOver(Production rootProduction, int targetPosition, Attribute<V> targetAttribute, int sourcePosition, Attribute<V> sourceAttribute)
	{
		return buildOnlyAttribs(rootProduction, Function.identity(),
				targetPosition, targetAttribute,
				sourcePosition, sourceAttribute);
	}
	public static <V> AttributeEquation<V> buildHandOver(Production rootProduction, Attribute<V> attribute, int targetPosition, int sourcePosition)
	{
		return buildHandOver(rootProduction, targetPosition, attribute, sourcePosition, attribute);
	}
	public static <PV1, PV2, V> AttributeEquation<V> build(Production rootProduction, BiFunction<PV1, PV2, V> function,
			AttributeValueReference<V> returnValue,
			SymbolValueReference<PV1, ?, ?> param1, SymbolValueReference<PV2, ?, ?> param2)
	{
		return new AttributeEquation<V>(rootProduction, returnValue, List.of(param1, param2))
		{
			@Override
			public V evaluateDontSaveResult(InnerNode rootNode)
			{
				return function.apply(param1.getValue(rootNode), param2.getValue(rootNode));
			}
		};
	}
	public static <PV1, PV2, V> AttributeEquation<V> buildOnlyAttribs(Production rootProduction, BiFunction<PV1, PV2, V> function,
			int returnPosition, Attribute<V> returnAttribute,
			int param1Position, Attribute<PV1> param1Attribute,
			int param2Position, Attribute<PV2> param2Attribute)
	{
		return build(rootProduction, function,
				new AttributeValueReference<>(rootProduction, returnPosition, returnAttribute),
				new AttributeValueReference<>(rootProduction, param1Position, param1Attribute),
				new AttributeValueReference<>(rootProduction, param2Position, param2Attribute));
	}
	public static <PV1, PV2, PV3, V> AttributeEquation<V> build(Production rootProduction, TriFunction<PV1, PV2, PV3, V> function,
			AttributeValueReference<V> returnValue,
			SymbolValueReference<PV1, ?, ?> param1, SymbolValueReference<PV2, ?, ?> param2, SymbolValueReference<PV3, ?, ?> param3)
	{
		return new AttributeEquation<V>(rootProduction, returnValue, List.of(param1, param2, param3))
		{
			@Override
			public V evaluateDontSaveResult(InnerNode rootNode)
			{
				return function.apply(param1.getValue(rootNode), param2.getValue(rootNode), param3.getValue(rootNode));
			}
		};
	}
	public static <PV1, PV2, PV3, V> AttributeEquation<V> buildOnlyAttribs(Production rootProduction, TriFunction<PV1, PV2, PV3, V> function,
			int returnPosition, Attribute<V> returnAttribute,
			int param1Position, Attribute<PV1> param1Attribute,
			int param2Position, Attribute<PV2> param2Attribute,
			int param3Position, Attribute<PV3> param3Attribute)
	{
		return build(rootProduction, function,
				new AttributeValueReference<>(rootProduction, returnPosition, returnAttribute),
				new AttributeValueReference<>(rootProduction, param1Position, param1Attribute),
				new AttributeValueReference<>(rootProduction, param2Position, param2Attribute),
				new AttributeValueReference<>(rootProduction, param3Position, param3Attribute));
	}
	public static <PV1, PV2, PV3, PV4, V> AttributeEquation<V> build(Production rootProduction, QuadFunction<PV1, PV2, PV3, PV4, V> function,
			AttributeValueReference<V> returnValue,
			SymbolValueReference<PV1, ?, ?> param1,
			SymbolValueReference<PV2, ?, ?> param2,
			SymbolValueReference<PV3, ?, ?> param3,
			SymbolValueReference<PV4, ?, ?> param4)
	{
		return new AttributeEquation<V>(rootProduction, returnValue, List.of(param1, param2, param3, param4))
		{
			@Override
			public V evaluateDontSaveResult(InnerNode rootNode)
			{
				return function.apply(param1.getValue(rootNode), param2.getValue(rootNode), param3.getValue(rootNode), param4.getValue(rootNode));
			}
		};
	}
	public static <PV1, PV2, PV3, PV4, V> AttributeEquation<V> buildOnlyAttribs(Production rootProduction, QuadFunction<PV1, PV2, PV3, PV4, V> function,
			int returnPosition, Attribute<V> returnAttribute,
			int param1Position, Attribute<PV1> param1Attribute,
			int param2Position, Attribute<PV2> param2Attribute,
			int param3Position, Attribute<PV3> param3Attribute,
			int param4Position, Attribute<PV4> param4Attribute)
	{
		return build(rootProduction, function,
				new AttributeValueReference<>(rootProduction, returnPosition, returnAttribute),
				new AttributeValueReference<>(rootProduction, param1Position, param1Attribute),
				new AttributeValueReference<>(rootProduction, param2Position, param2Attribute),
				new AttributeValueReference<>(rootProduction, param3Position, param3Attribute),
				new AttributeValueReference<>(rootProduction, param4Position, param4Attribute));
	}
	public static <PV1, PV2, PV3, PV4, PV5, V> AttributeEquation<V> build(Production rootProduction, QuinFunction<PV1, PV2, PV3, PV4, PV5, V> function,
			AttributeValueReference<V> returnValue,
			SymbolValueReference<PV1, ?, ?> param1,
			SymbolValueReference<PV2, ?, ?> param2,
			SymbolValueReference<PV3, ?, ?> param3,
			SymbolValueReference<PV4, ?, ?> param4,
			SymbolValueReference<PV5, ?, ?> param5)
	{
		return new AttributeEquation<V>(rootProduction, returnValue, List.of(param1, param2, param3, param4, param5))
		{
			@Override
			public V evaluateDontSaveResult(InnerNode rootNode)
			{
				return function.apply(param1.getValue(rootNode), param2.getValue(rootNode), param3.getValue(rootNode), param4.getValue(rootNode), param5.getValue(rootNode));
			}
		};
	}
	public static <PV1, PV2, PV3, PV4, PV5, V> AttributeEquation<V> buildOnlyAttribs(Production rootProduction, QuinFunction<PV1, PV2, PV3, PV4, PV5, V> function,
			int returnPosition, Attribute<V> returnAttribute,
			int param1Position, Attribute<PV1> param1Attribute,
			int param2Position, Attribute<PV2> param2Attribute,
			int param3Position, Attribute<PV3> param3Attribute,
			int param4Position, Attribute<PV4> param4Attribute,
			int param5Position, Attribute<PV5> param5Attribute)
	{
		return build(rootProduction, function,
				new AttributeValueReference<>(rootProduction, returnPosition, returnAttribute),
				new AttributeValueReference<>(rootProduction, param1Position, param1Attribute),
				new AttributeValueReference<>(rootProduction, param2Position, param2Attribute),
				new AttributeValueReference<>(rootProduction, param3Position, param3Attribute),
				new AttributeValueReference<>(rootProduction, param4Position, param4Attribute),
				new AttributeValueReference<>(rootProduction, param5Position, param5Attribute));
	}
	public static <V> AttributeEquation<V> build(Production rootProduction, Function<Object[], V> function,
			AttributeValueReference<V> returnValue,
			SymbolValueReference<?, ?, ?>... params)
	{
		return new AttributeEquation<V>(rootProduction, returnValue, List.of(params))
		{
			@Override
			public V evaluateDontSaveResult(InnerNode rootNode)
			{
				Object[] arguments = new Object[params.length];
				for(int i = 0; i < params.length; i ++)
					arguments[i] = params[i].getValue(rootNode);

				return function.apply(arguments);
			}
		};
	}
}

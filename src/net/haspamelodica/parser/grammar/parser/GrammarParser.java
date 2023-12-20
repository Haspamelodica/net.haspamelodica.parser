package net.haspamelodica.parser.grammar.parser;

import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.ARROW;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.BRACKET_C;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.BRACKET_O;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.COMMA;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.EQUALS;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.IDENTIFIER;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.INTEGER;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.OR;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.PAREN_C;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.PAREN_O;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.SEMICOLON;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.SQUARE_C;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.SQUARE_O;
import static net.haspamelodica.parser.grammar.parser.GrammarTokenizer.TERMINAL;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.generics.TypedFunction;
import net.haspamelodica.parser.grammar.ContextFreeGrammar;
import net.haspamelodica.parser.grammar.Nonterminal;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.grammar.Terminal;
import net.haspamelodica.parser.grammar.attributes.Attribute;
import net.haspamelodica.parser.grammar.attributes.AttributeEquation;
import net.haspamelodica.parser.grammar.attributes.AttributeSystem;
import net.haspamelodica.parser.grammar.attributes.AttributeValue;
import net.haspamelodica.parser.grammar.attributes.AttributeValueReference;
import net.haspamelodica.parser.grammar.attributes.SymbolValueReference;
import net.haspamelodica.parser.grammar.attributes.TerminalValueReference;
import net.haspamelodica.parser.grammar.attributes.evaluating.lattributed.LAttributedEvaluator;
import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.parser.Parser;
import net.haspamelodica.parser.parser.lrk.LRkParserGenerator;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.Tokenizer;
import net.haspamelodica.parser.utils.CollectionsUtils;

public class GrammarParser
{
	private static final Tokenizer<CharReader>	tokenizer	= new GrammarTokenizer();
	private static final Parser					parser;
	private static final LAttributedEvaluator	attrEvaluator;

	private static final Attribute<Set<AttributeEquation<?>>>	EQUATIONS_U;
	private static final Attribute<Map<String, Attribute<?>>>	ATTRIBUTES_U;
	private static final Attribute<Map<String, Terminal<?>>>	TERMINALS;
	private static final Attribute<Map<String, TypedFunction>>	FUNCTIONS;
	private static final Attribute<Nonterminal>					FIRST_LHS;
	private static final Attribute<Set<Production>>				PRODUCTIONS_U;


	static
	{
		Nonterminal GRAMMAR = new Nonterminal("Grammar");
		Nonterminal FULL_RULE = new Nonterminal("FullRule");
		Nonterminal OR_RULES = new Nonterminal("OrRules");
		Nonterminal RHS = new Nonterminal("Rhs");
		Nonterminal COMPLETE_RHS = new Nonterminal("CompleteRhs");
		Nonterminal EQLIST = new Nonterminal("AttributeEquationList");
		Nonterminal EQUATIONS = new Nonterminal("AttributeEquations");
		Nonterminal EXPRESSION = new Nonterminal("Expression");
		Nonterminal ARGUMENTS = new Nonterminal("Arguments");
		Nonterminal ARGUMENTS_HEAD = new Nonterminal("ArgumentsHead");
		Nonterminal VALUE_REF = new Nonterminal("ValueReference");
		Nonterminal ATTRIBUTE = new Nonterminal("Attribute");
		Nonterminal NONTERMINAL = new Nonterminal("Nonterminal");

		Production G0 = Production.build(GRAMMAR, FULL_RULE);
		Production G1 = Production.build(GRAMMAR, GRAMMAR, FULL_RULE);
		Production F0 = Production.build(FULL_RULE, NONTERMINAL, ARROW, COMPLETE_RHS, EQLIST, OR_RULES, SEMICOLON);
		Production O0 = Production.build(OR_RULES);
		Production O1 = Production.build(OR_RULES, OR, COMPLETE_RHS, EQLIST, OR_RULES);
		Production C0 = Production.build(COMPLETE_RHS, RHS);
		Production R0 = Production.build(RHS);
		Production R1 = Production.build(RHS, RHS, TERMINAL);
		Production R2 = Production.build(RHS, RHS, NONTERMINAL);
		Production L0 = Production.build(EQLIST);
		Production L1 = Production.build(EQLIST, BRACKET_O, EQUATIONS, BRACKET_C);
		Production E0 = Production.build(EQUATIONS);
		Production E1 = Production.build(EQUATIONS, VALUE_REF, EQUALS, EXPRESSION, SEMICOLON, EQUATIONS);
		Production X0 = Production.build(EXPRESSION, VALUE_REF);
		Production X1 = Production.build(EXPRESSION, IDENTIFIER, PAREN_O, ARGUMENTS, PAREN_C);
		Production S0 = Production.build(ARGUMENTS);
		Production S1 = Production.build(ARGUMENTS, ARGUMENTS_HEAD, EXPRESSION);
		Production H0 = Production.build(ARGUMENTS_HEAD);
		Production H1 = Production.build(ARGUMENTS_HEAD, ARGUMENTS_HEAD, EXPRESSION, COMMA);
		Production V0 = Production.build(VALUE_REF, ATTRIBUTE, SQUARE_O, INTEGER, SQUARE_C);
		Production V1 = Production.build(VALUE_REF, SQUARE_O, INTEGER, SQUARE_C);
		Production A0 = Production.build(ATTRIBUTE, IDENTIFIER);
		Production N0 = Production.build(NONTERMINAL, IDENTIFIER);

		ContextFreeGrammar grammar = new ContextFreeGrammar(GRAMMAR, Set.of(G0, G1, F0, O0, O1, C0, R0, R1, R2, L0, L1, E0, E1, X0, X1, S0,
				S1, H0, H1, V0, V1, A0, N0));

		parser = LRkParserGenerator.generate(grammar, 1);


		TERMINALS = new Attribute<>("terminals");
		Attribute<Map<String, Nonterminal>> NONTERMINALS_D = new Attribute<>("nonterminalsDown");
		Attribute<Map<String, Nonterminal>> NONTERMINALS_U = new Attribute<>("nonterminalsUp");
		Attribute<Nonterminal> LHS = new Attribute<>("lhs");
		FIRST_LHS = new Attribute<>("firstLhs");
		Attribute<String> NONTERMINAL_N = new Attribute<>("nonterminalName");
		Attribute<List<Symbol>> RHS_SYMBOLS = new Attribute<>("rhsSymmbols");
		Attribute<Production> PRODUCTION_D = new Attribute<>("productionDown");
		Attribute<Production> PRODUCTION_U = new Attribute<>("productionUp");
		Attribute<Set<Production>> PRODUCTIONS_D = new Attribute<>("productionsDown");
		PRODUCTIONS_U = new Attribute<>("productionsUp");
		Attribute<Map<String, Attribute<?>>> ATTRIBUTES_D = new Attribute<>("attributesDown");
		ATTRIBUTES_U = new Attribute<>("attributesUp");
		Attribute<Set<AttributeEquation<?>>> EQUATIONS_D = new Attribute<>("equationsDown");
		EQUATIONS_U = new Attribute<>("equationsUp");
		FUNCTIONS = new Attribute<>("functions");
		Attribute<TypedFunction> EXPRESSION_VAL = new Attribute<>("expressionValue");
		Attribute<List<TypedFunction>> EXPRESSION_VALS = new Attribute<>("expressionValues");
		Attribute<List<SymbolValueReference<?, ?, ?>>> ARGS_VAL = new Attribute<>("arguments");
		Attribute<List<List<SymbolValueReference<?, ?, ?>>>> PARAMS_ARGS_VAL = new Attribute<>("argumentsPerParam");
		Attribute<SymbolValueReference<?, ?, ?>> VALREF_VAL = new Attribute<>("valueReferenceValue");
		Attribute<String> ATTRIBUTE_N = new Attribute<>("attributeName");

		Set<AttributeEquation<?>> TERMINALS_EQS = Set.of(
				AttributeEquation.buildHandOver(G0, TERMINALS, 0, -1),
				AttributeEquation.buildHandOver(G1, TERMINALS, 0, -1),
				AttributeEquation.buildHandOver(G1, TERMINALS, 1, -1),
				AttributeEquation.buildHandOver(F0, TERMINALS, 2, -1),
				AttributeEquation.buildHandOver(F0, TERMINALS, 4, -1),
				AttributeEquation.buildHandOver(O1, TERMINALS, 1, -1),
				AttributeEquation.buildHandOver(O1, TERMINALS, 3, -1),
				AttributeEquation.buildHandOver(C0, TERMINALS, 0, -1),
				AttributeEquation.buildHandOver(R1, TERMINALS, 0, -1),
				AttributeEquation.buildHandOver(R2, TERMINALS, 0, -1));

		Set<AttributeEquation<?>> NONTERMINALS_D_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(G0, Map::of, 0, NONTERMINALS_D),
				AttributeEquation.buildHandOver(G1, 1, NONTERMINALS_D, 0, NONTERMINALS_U),
				AttributeEquation.buildHandOver(F0, NONTERMINALS_D, 0, -1),
				AttributeEquation.buildHandOver(F0, 2, NONTERMINALS_D, 0, NONTERMINALS_U),
				AttributeEquation.buildHandOver(F0, 4, NONTERMINALS_D, 2, NONTERMINALS_U),
				AttributeEquation.buildHandOver(O1, NONTERMINALS_D, 1, -1),
				AttributeEquation.buildHandOver(O1, 3, NONTERMINALS_D, 1, NONTERMINALS_U),
				AttributeEquation.buildHandOver(C0, NONTERMINALS_D, 0, -1),
				AttributeEquation.buildHandOver(R1, NONTERMINALS_D, 0, -1),
				AttributeEquation.buildHandOver(R2, NONTERMINALS_D, 0, -1),
				AttributeEquation.buildHandOver(R2, 1, NONTERMINALS_D, 0, NONTERMINALS_U));

		Set<AttributeEquation<?>> NONTERMINALS_U_EQS = Set.of(
				AttributeEquation.buildHandOver(G0, NONTERMINALS_U, -1, 0),
				AttributeEquation.buildHandOver(G1, NONTERMINALS_U, -1, 1),
				AttributeEquation.buildHandOver(F0, NONTERMINALS_U, -1, 4),
				AttributeEquation.buildHandOver(O0, -1, NONTERMINALS_U, -1, NONTERMINALS_D),
				AttributeEquation.buildHandOver(O1, NONTERMINALS_U, -1, 3),
				AttributeEquation.buildHandOver(R0, -1, NONTERMINALS_U, -1, NONTERMINALS_D),
				AttributeEquation.buildHandOver(R1, NONTERMINALS_U, -1, 0),
				AttributeEquation.buildHandOver(R2, NONTERMINALS_U, -1, 1),
				AttributeEquation.buildHandOver(C0, NONTERMINALS_U, -1, 0),
				AttributeEquation.build(N0, GrammarParser::newNonterminalIfAbsent,
						new AttributeValueReference<>(N0, -1, NONTERMINALS_U),
						new AttributeValueReference<>(N0, -1, NONTERMINALS_D),
						new TerminalValueReference<>(N0, 0, IDENTIFIER)));

		Set<AttributeEquation<?>> LHS_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(F0, Map::get, 2, LHS, 0, NONTERMINALS_U, 0, NONTERMINAL_N),
				AttributeEquation.buildHandOver(F0, LHS, 4, 2),
				AttributeEquation.buildHandOver(O1, LHS, 1, -1),
				AttributeEquation.buildHandOver(O1, LHS, 3, -1));

		Set<AttributeEquation<?>> FIRST_LHS_EQS = Set.of(
				AttributeEquation.buildHandOver(G0, FIRST_LHS, -1, 0),
				AttributeEquation.buildHandOver(G1, FIRST_LHS, -1, 0),
				AttributeEquation.buildOnlyAttribs(F0, Map::get, -1, FIRST_LHS, 4, NONTERMINALS_U, 0, NONTERMINAL_N));

		Set<AttributeEquation<?>> NONTERMINAL_N_EQS = Set.of(
				AttributeEquation.build(N0, Function.identity(),
						new AttributeValueReference<>(N0, -1, NONTERMINAL_N),
						new TerminalValueReference<>(N0, 0, IDENTIFIER)));

		Set<AttributeEquation<?>> RHS_SYMBOLS_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(R0, List::of, -1, RHS_SYMBOLS),
				AttributeEquation.build(R1, GrammarParser::appendSymbol,
						new AttributeValueReference<>(R1, -1, RHS_SYMBOLS),
						new AttributeValueReference<>(R1, 0, RHS_SYMBOLS),
						new AttributeValueReference<>(R1, -1, TERMINALS),
						new TerminalValueReference<>(R1, 1, TERMINAL)),
				AttributeEquation.buildOnlyAttribs(R2, GrammarParser::appendSymbol,
						-1, RHS_SYMBOLS, 0, RHS_SYMBOLS, 1, NONTERMINALS_U, 1, NONTERMINAL_N));

		Set<AttributeEquation<?>> PRODUCTION_D_EQS = Set.of(
				AttributeEquation.buildHandOver(F0, 3, PRODUCTION_D, 2, PRODUCTION_U),
				AttributeEquation.buildHandOver(O1, 2, PRODUCTION_D, 1, PRODUCTION_U),
				AttributeEquation.buildHandOver(L1, PRODUCTION_D, 1, -1),
				AttributeEquation.buildHandOver(E1, PRODUCTION_D, 0, -1),
				AttributeEquation.buildHandOver(E1, PRODUCTION_D, 2, -1),
				AttributeEquation.buildHandOver(E1, PRODUCTION_D, 4, -1),
				AttributeEquation.buildHandOver(X0, PRODUCTION_D, 0, -1),
				AttributeEquation.buildHandOver(X1, PRODUCTION_D, 2, -1),
				AttributeEquation.buildHandOver(S1, PRODUCTION_D, 0, -1),
				AttributeEquation.buildHandOver(S1, PRODUCTION_D, 1, -1),
				AttributeEquation.buildHandOver(H1, PRODUCTION_D, 0, -1),
				AttributeEquation.buildHandOver(H1, PRODUCTION_D, 1, -1));

		Set<AttributeEquation<?>> PRODUCTION_U_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(C0, Production::build, -1, PRODUCTION_U, -1, LHS, 0, RHS_SYMBOLS));

		Set<AttributeEquation<?>> PRODUCTIONS_D_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(G0, Set::of, 0, PRODUCTIONS_D),
				AttributeEquation.buildHandOver(G1, 1, PRODUCTIONS_D, 0, PRODUCTIONS_U),
				AttributeEquation.buildOnlyAttribs(F0, CollectionsUtils::pseudoAdd,
						4, PRODUCTIONS_D, -1, PRODUCTIONS_D, 2, PRODUCTION_U),
				AttributeEquation.buildOnlyAttribs(O1, CollectionsUtils::pseudoAdd,
						3, PRODUCTIONS_D, -1, PRODUCTIONS_D, 1, PRODUCTION_U));

		Set<AttributeEquation<?>> PRODUCTIONS_U_EQS = Set.of(
				AttributeEquation.buildHandOver(G0, PRODUCTIONS_U, -1, 0),
				AttributeEquation.buildHandOver(G1, PRODUCTIONS_U, -1, 1),
				AttributeEquation.buildHandOver(F0, PRODUCTIONS_U, -1, 4),
				AttributeEquation.buildHandOver(O0, -1, PRODUCTIONS_U, -1, PRODUCTIONS_D),
				AttributeEquation.buildHandOver(O1, PRODUCTIONS_U, -1, 3));

		Set<AttributeEquation<?>> ATTRIBUTES_D_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(G0, Map::of, 0, ATTRIBUTES_D),
				AttributeEquation.buildHandOver(G1, 1, ATTRIBUTES_D, 0, ATTRIBUTES_U),
				AttributeEquation.buildHandOver(F0, ATTRIBUTES_D, 3, -1),
				AttributeEquation.buildHandOver(F0, 4, ATTRIBUTES_D, 3, ATTRIBUTES_U),
				AttributeEquation.buildHandOver(O1, ATTRIBUTES_D, 2, -1),
				AttributeEquation.buildHandOver(O1, 3, ATTRIBUTES_D, 2, ATTRIBUTES_U),
				AttributeEquation.buildHandOver(L1, ATTRIBUTES_D, 1, -1),
				AttributeEquation.buildHandOver(E1, ATTRIBUTES_D, 0, -1),
				AttributeEquation.buildHandOver(E1, 2, ATTRIBUTES_D, 0, ATTRIBUTES_U),
				AttributeEquation.buildHandOver(E1, 4, ATTRIBUTES_D, 2, ATTRIBUTES_U),
				AttributeEquation.buildHandOver(X0, ATTRIBUTES_D, 0, -1),
				AttributeEquation.buildHandOver(X1, ATTRIBUTES_D, 2, -1),
				AttributeEquation.buildHandOver(S1, ATTRIBUTES_D, 0, -1),
				AttributeEquation.buildHandOver(S1, 1, ATTRIBUTES_D, 0, ATTRIBUTES_U),
				AttributeEquation.buildHandOver(H1, ATTRIBUTES_D, 0, -1),
				AttributeEquation.buildHandOver(H1, 1, ATTRIBUTES_D, 0, ATTRIBUTES_U),
				AttributeEquation.buildHandOver(V0, ATTRIBUTES_D, 0, -1));

		Set<AttributeEquation<?>> ATTRIBUTES_U_EQS = Set.of(
				AttributeEquation.buildHandOver(G0, ATTRIBUTES_U, -1, 0),
				AttributeEquation.buildHandOver(G1, ATTRIBUTES_U, -1, 1),
				AttributeEquation.buildHandOver(F0, ATTRIBUTES_U, -1, 4),
				AttributeEquation.buildHandOver(O0, -1, ATTRIBUTES_U, -1, ATTRIBUTES_D),
				AttributeEquation.buildHandOver(O1, ATTRIBUTES_U, -1, 3),
				AttributeEquation.buildHandOver(L0, -1, ATTRIBUTES_U, -1, ATTRIBUTES_D),
				AttributeEquation.buildHandOver(L1, ATTRIBUTES_U, -1, 1),
				AttributeEquation.buildHandOver(E0, -1, ATTRIBUTES_U, -1, ATTRIBUTES_D),
				AttributeEquation.buildHandOver(E1, ATTRIBUTES_U, -1, 4),
				AttributeEquation.buildHandOver(X0, ATTRIBUTES_U, -1, 0),
				AttributeEquation.buildHandOver(X1, ATTRIBUTES_U, -1, 2),
				AttributeEquation.buildHandOver(S0, -1, ATTRIBUTES_U, -1, ATTRIBUTES_D),
				AttributeEquation.buildHandOver(S1, ATTRIBUTES_U, -1, 1),
				AttributeEquation.buildHandOver(H0, -1, ATTRIBUTES_U, -1, ATTRIBUTES_D),
				AttributeEquation.buildHandOver(H1, ATTRIBUTES_U, -1, 1),
				AttributeEquation.buildHandOver(V0, ATTRIBUTES_U, -1, 0),
				AttributeEquation.buildHandOver(V1, -1, ATTRIBUTES_U, -1, ATTRIBUTES_D),
				AttributeEquation.build(A0, GrammarParser::newAttributeIfAbsent,
						new AttributeValueReference<>(A0, -1, ATTRIBUTES_U),
						new AttributeValueReference<>(A0, -1, ATTRIBUTES_D),
						new TerminalValueReference<>(A0, 0, IDENTIFIER)));

		Set<AttributeEquation<?>> EQUATIONS_D_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(G0, Set::of, 0, EQUATIONS_D),
				AttributeEquation.buildHandOver(G1, 1, EQUATIONS_D, 0, EQUATIONS_U),
				AttributeEquation.buildHandOver(F0, EQUATIONS_D, 3, -1),
				AttributeEquation.buildHandOver(F0, 4, EQUATIONS_D, 3, EQUATIONS_U),
				AttributeEquation.buildHandOver(O1, EQUATIONS_D, 2, -1),
				AttributeEquation.buildHandOver(O1, 3, EQUATIONS_D, 2, EQUATIONS_U),
				AttributeEquation.buildHandOver(L1, EQUATIONS_D, 1, -1),
				AttributeEquation.buildHandOver(E1, EQUATIONS_D, 4, -1));

		Set<AttributeEquation<?>> EQUATIONS_U_EQS = Set.of(
				AttributeEquation.buildHandOver(G0, EQUATIONS_U, -1, 0),
				AttributeEquation.buildHandOver(G1, EQUATIONS_U, -1, 1),
				AttributeEquation.buildHandOver(F0, EQUATIONS_U, -1, 4),
				AttributeEquation.buildHandOver(O0, -1, EQUATIONS_U, -1, EQUATIONS_D),
				AttributeEquation.buildHandOver(O1, EQUATIONS_U, -1, 3),
				AttributeEquation.buildHandOver(L0, -1, EQUATIONS_U, -1, EQUATIONS_D),
				AttributeEquation.buildHandOver(L1, EQUATIONS_U, -1, 1),
				AttributeEquation.buildHandOver(E0, -1, EQUATIONS_U, -1, EQUATIONS_D),
				AttributeEquation.buildOnlyAttribs(E1, GrammarParser::pseudoAddGeneralEquation,
						-1, EQUATIONS_U, 4, EQUATIONS_U, 0, VALREF_VAL, 2, EXPRESSION_VAL, 2, ARGS_VAL));

		Set<AttributeEquation<?>> FUNCTIONS_EQS = Set.of(
				AttributeEquation.buildHandOver(G0, FUNCTIONS, 0, -1),
				AttributeEquation.buildHandOver(G1, FUNCTIONS, 0, -1),
				AttributeEquation.buildHandOver(G1, FUNCTIONS, 1, -1),
				AttributeEquation.buildHandOver(F0, FUNCTIONS, 3, -1),
				AttributeEquation.buildHandOver(F0, FUNCTIONS, 4, -1),
				AttributeEquation.buildHandOver(O1, FUNCTIONS, 2, -1),
				AttributeEquation.buildHandOver(O1, FUNCTIONS, 3, -1),
				AttributeEquation.buildHandOver(L1, FUNCTIONS, 1, -1),
				AttributeEquation.buildHandOver(E1, FUNCTIONS, 2, -1),
				AttributeEquation.buildHandOver(E1, FUNCTIONS, 4, -1),
				AttributeEquation.buildHandOver(X1, FUNCTIONS, 2, -1),
				AttributeEquation.buildHandOver(S1, FUNCTIONS, 0, -1),
				AttributeEquation.buildHandOver(S1, FUNCTIONS, 1, -1),
				AttributeEquation.buildHandOver(H1, FUNCTIONS, 0, -1),
				AttributeEquation.buildHandOver(H1, FUNCTIONS, 1, -1));

		Set<AttributeEquation<?>> EXPRESSION_VAL_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(X0, GrammarParser::buildValueReference, -1, EXPRESSION_VAL, 0, VALREF_VAL),
				AttributeEquation.build(X1, GrammarParser::buildFunctionCall,
						new AttributeValueReference<>(X1, -1, EXPRESSION_VAL),
						new AttributeValueReference<>(X1, -1, FUNCTIONS),
						new TerminalValueReference<>(X1, 0, IDENTIFIER),
						new AttributeValueReference<>(X1, 2, ARGS_VAL),
						new AttributeValueReference<>(X1, 2, EXPRESSION_VALS),
						new AttributeValueReference<>(X1, 2, PARAMS_ARGS_VAL)));

		Set<AttributeEquation<?>> EXPRESSION_VALS_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(S0, List::of, -1, EXPRESSION_VALS),
				AttributeEquation.buildOnlyAttribs(S1, CollectionsUtils::pseudoAdd, -1, EXPRESSION_VALS, 0, EXPRESSION_VALS, 1, EXPRESSION_VAL),
				AttributeEquation.buildOnlyAttribs(H0, List::of, -1, EXPRESSION_VALS),
				AttributeEquation.buildOnlyAttribs(H1, CollectionsUtils::pseudoAdd, -1, EXPRESSION_VALS, 0, EXPRESSION_VALS, 1, EXPRESSION_VAL));

		Set<AttributeEquation<?>> ARGS_VAL_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(X0, List::of, -1, ARGS_VAL, 0, VALREF_VAL),
				AttributeEquation.buildHandOver(X1, ARGS_VAL, -1, 2),
				AttributeEquation.buildOnlyAttribs(S0, List::of, -1, ARGS_VAL),
				AttributeEquation.buildOnlyAttribs(S1, GrammarParser::pseudoListUnion, -1, ARGS_VAL, 0, ARGS_VAL, 1, ARGS_VAL),
				AttributeEquation.buildOnlyAttribs(H0, List::of, -1, ARGS_VAL),
				AttributeEquation.buildOnlyAttribs(H1, GrammarParser::pseudoListUnion, -1, ARGS_VAL, 0, ARGS_VAL, 1, ARGS_VAL));

		Set<AttributeEquation<?>> PARAMS_ARGS_VAL_EQS = Set.of(
				AttributeEquation.buildOnlyAttribs(S0, List::of, -1, PARAMS_ARGS_VAL),
				AttributeEquation.buildOnlyAttribs(S1, CollectionsUtils::pseudoAdd, -1, PARAMS_ARGS_VAL, 0, PARAMS_ARGS_VAL, 1, ARGS_VAL),
				AttributeEquation.buildOnlyAttribs(H0, List::of, -1, PARAMS_ARGS_VAL),
				AttributeEquation.buildOnlyAttribs(H1, CollectionsUtils::pseudoAdd, -1, PARAMS_ARGS_VAL, 0, PARAMS_ARGS_VAL, 1, ARGS_VAL));

		Set<AttributeEquation<?>> VALREF_VAL_EQS = Set.of(
				AttributeEquation.build(V0, (pr, pos, attrs, name) -> new AttributeValueReference<>(pr, pos, attrs.get(name)),
						new AttributeValueReference<>(V0, -1, VALREF_VAL),
						new AttributeValueReference<>(V0, -1, PRODUCTION_D),
						new TerminalValueReference<>(V0, 2, INTEGER),
						new AttributeValueReference<>(V0, 0, ATTRIBUTES_U),
						new AttributeValueReference<>(V0, 0, ATTRIBUTE_N)),
				AttributeEquation.build(V1, GrammarParser::createTerminalParam,
						new AttributeValueReference<>(V1, -1, VALREF_VAL),
						new AttributeValueReference<>(V1, -1, PRODUCTION_D),
						new TerminalValueReference<>(V1, 1, INTEGER)));

		Set<AttributeEquation<?>> ATTRIBUTE_N_EQS = Set.of(
				AttributeEquation.build(A0, Function.identity(),
						new AttributeValueReference<>(A0, -1, ATTRIBUTE_N),
						new TerminalValueReference<>(A0, 0, IDENTIFIER)));

		AttributeSystem attributeSystem = new AttributeSystem(grammar, CollectionsUtils.union(
				TERMINALS_EQS, NONTERMINALS_D_EQS, NONTERMINALS_U_EQS, LHS_EQS, FIRST_LHS_EQS, NONTERMINAL_N_EQS, RHS_SYMBOLS_EQS,
				PRODUCTION_D_EQS, PRODUCTION_U_EQS, PRODUCTIONS_D_EQS, PRODUCTIONS_U_EQS, ATTRIBUTES_D_EQS, ATTRIBUTES_U_EQS, EQUATIONS_D_EQS,
				EQUATIONS_U_EQS, FUNCTIONS_EQS, EXPRESSION_VAL_EQS, EXPRESSION_VALS_EQS, ARGS_VAL_EQS, PARAMS_ARGS_VAL_EQS, VALREF_VAL_EQS,
				ATTRIBUTE_N_EQS));

		attrEvaluator = new LAttributedEvaluator(attributeSystem);
	}

	public static AttributeGrammarParseResult parseAttributeGrammar(CharReader in, Set<Terminal<?>> terminals, Map<String, TypedFunction> functionsByName) throws ParseException
	{
		Pair<ContextFreeGrammar, InnerNode> pair = parseGrammarInclAST(in, terminals, functionsByName);
		ContextFreeGrammar grammar = pair.getA();
		InnerNode grammarAST = pair.getB();

		AttributeSystem attributeSystem = new AttributeSystem(grammar, grammarAST.getValueForAttribute(EQUATIONS_U));
		return new AttributeGrammarParseResult(attributeSystem, grammarAST.getValueForAttribute(ATTRIBUTES_U));
	}
	public static ContextFreeGrammar parseGrammar(CharReader in, Set<Terminal<?>> terminals) throws ParseException
	{
		return parseGrammarInclAST(in, terminals, Map.of()).getA();
	}
	private static Pair<ContextFreeGrammar, InnerNode> parseGrammarInclAST(CharReader in, Set<Terminal<?>> terminals, Map<String, TypedFunction> functionsByName) throws ParseException
	{
		InnerNode grammarAST = parser.parse(tokenizer.tokenize(in));

		Set<AttributeValue<?>> rootInheritedValues = Set.of(
				new AttributeValue<>(TERMINALS, terminals.stream().collect(Collectors.toMap(Terminal::getName, Function.identity()))),
				new AttributeValue<>(FUNCTIONS, functionsByName));
		attrEvaluator.evaluate(grammarAST, rootInheritedValues);

		ContextFreeGrammar grammar = new ContextFreeGrammar(
				grammarAST.getValueForAttribute(FIRST_LHS),
				grammarAST.getValueForAttribute(PRODUCTIONS_U));
		return new Pair<>(grammar, grammarAST);
	}

	private static Map<String, Nonterminal> newNonterminalIfAbsent(Map<String, Nonterminal> nonterminals, String nonterminal)
	{
		if(nonterminals.containsKey(nonterminal))
			return nonterminals;
		Map<String, Nonterminal> result = new HashMap<>(nonterminals);
		result.put(nonterminal, new Nonterminal(nonterminal));
		return Collections.unmodifiableMap(result);
	}
	private static Map<String, Attribute<?>> newAttributeIfAbsent(Map<String, Attribute<?>> attributes, String attribute)
	{
		if(attributes.containsKey(attribute))
			return attributes;
		Map<String, Attribute<?>> result = new HashMap<>(attributes);
		result.put(attribute, new Attribute<>(attribute));
		return Collections.unmodifiableMap(result);
	}
	private static List<Symbol> appendSymbol(List<Symbol> oldList, Map<String, ? extends Symbol> symbolsByName, String appended)
	{
		List<Symbol> newList = new ArrayList<>(oldList);
		Symbol appendedSymbol = symbolsByName.get(appended);
		if(appendedSymbol == null)
			throw new IllegalArgumentException("Unknown symbol: " + appended);
		newList.add(appendedSymbol);
		return Collections.unmodifiableList(newList);
	}
	private static Set<AttributeEquation<?>> pseudoAddGeneralEquation(Set<AttributeEquation<?>> oldEquations,
			SymbolValueReference<?, ?, ?> returnValue, TypedFunction function, List<SymbolValueReference<?, ?, ?>> parameters)
	{
		switch(returnValue.getType())
		{
			case ATTRIBUTE_VALUE:
				Set<AttributeEquation<?>> newEquations = new HashSet<>(oldEquations);
				//TODO add some sort of type checking
				@SuppressWarnings("unchecked")
				AttributeValueReference<Object> returnValueCasted = (AttributeValueReference<Object>) returnValue;
				SymbolValueReference<?, ?, ?>[] params = parameters.toArray(SymbolValueReference[]::new);
				newEquations.add(AttributeEquation.build(returnValue.getRootProduction(), function::execute,
						returnValueCasted, params));
				return Collections.unmodifiableSet(newEquations);
			case TERMINAL_VALUE:
				throw new IllegalArgumentException("Can't set the value of a token");
			default:
				throw new IllegalArgumentException("Unknown enum constant: " + returnValue.getType());
		}
	}
	private static TypedFunction buildValueReference(SymbolValueReference<?, ?, ?> valueReference)
	{
		//TODO add some sort of type checking
		return TypedFunction.build(Function.identity(), Object.class, Object.class);
	}
	private static TypedFunction buildFunctionCall(Map<String, TypedFunction> functions, String functionName,
			List<SymbolValueReference<?, ?, ?>> suppliedValueRefs,
			List<TypedFunction> parameters, List<List<SymbolValueReference<?, ?, ?>>> requiredValueRefsPerParam)
	{
		TypedFunction function = functions.get(functionName);
		if(function == null)
			throw new IllegalArgumentException("There is no function with the name " + functionName);

		int suppliedValueRefsCount = suppliedValueRefs.size();
		int paramCount = function.getParameterTypes().size();

		if(parameters.size() < paramCount)
			throw new IllegalArgumentException("Too few arguments for function " + functionName);

		Type[] suppliedParameterTypesArr = new Type[suppliedValueRefsCount];
		//TODO add some sort of type checking
		Arrays.fill(suppliedParameterTypesArr, Object.class);
		List<Type> suppliedParameterTypes = List.of(suppliedParameterTypesArr);

		int maxParamParamCountModifiable = 0;
		int[][] paramArgsReorderingTable = new int[paramCount][];
		for(int i = 0; i < paramCount; i ++)
		{
			List<SymbolValueReference<?, ?, ?>> requiredValueRefs = requiredValueRefsPerParam.get(i);
			int paramParamCount = requiredValueRefs.size();
			if(paramParamCount > maxParamParamCountModifiable)
				maxParamParamCountModifiable = paramParamCount;
			int[] paramArgsReorderingRow = new int[paramParamCount];
			for(int j = 0; j < paramParamCount; j ++)
			{
				int indexOfParam = suppliedValueRefs.indexOf(requiredValueRefs.get(j));
				if(indexOfParam == -1)
					throw new IllegalStateException("A parameter requires a parameter that is not given");
				paramArgsReorderingRow[j] = indexOfParam;
			}
			paramArgsReorderingTable[i] = paramArgsReorderingRow;
		}

		TypedFunction[] paramsArr = parameters.toArray(TypedFunction[]::new);

		int maxParamParamCount = maxParamParamCountModifiable;
		return new TypedFunction()
		{
			@Override
			public Type getResultType()
			{
				return function.getResultType();
			}
			@Override
			public List<Type> getParameterTypes()
			{
				return suppliedParameterTypes;
			}
			@Override
			public Object execute(Object... args)
			{
				Object[] argsForParams = new Object[maxParamParamCount];
				Object[] argsForFunction = new Object[paramCount];
				for(int i = 0; i < argsForFunction.length; i ++)
				{
					int[] paramArgsReorderingRow = paramArgsReorderingTable[i];
					for(int j = 0; j < paramArgsReorderingRow.length; j ++)
						argsForParams[j] = args[paramArgsReorderingRow[j]];
					argsForFunction[i] = paramsArr[i].execute(argsForParams);
				}
				return function.execute(argsForFunction);
			}
		};
	}
	private static TerminalValueReference<?> createTerminalParam(Production production, int pos)
	{
		Symbol symbol = production.getRhs().getSymbols().get(pos);
		switch(symbol.getType())
		{
			case NONTERMINAL:
				throw new IllegalArgumentException("The RHS symbol at " + pos + " in " + production + " is not a terminal");
			case TERMINAL:
				return new TerminalValueReference<>(production, pos, (Terminal<?>) symbol);
			default:
				throw new IllegalArgumentException("Unknown enum constant: " + symbol.getType());
		}
	}
	private static <E> List<E> pseudoListUnion(List<E> a, List<E> b)
	{
		List<E> newList = new ArrayList<>(a);
		b.stream().filter(e -> !a.contains(e)).forEach(newList::add);
		return Collections.unmodifiableList(newList);
	}
}

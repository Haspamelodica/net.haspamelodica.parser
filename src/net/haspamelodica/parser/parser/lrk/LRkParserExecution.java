package net.haspamelodica.parser.parser.lrk;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import net.haspamelodica.parser.ast.ASTNode;
import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.ast.Token;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.grammar.Terminal;
import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.parser.lrk.action.Action;
import net.haspamelodica.parser.parser.lrk.action.ErrorAction;
import net.haspamelodica.parser.parser.lrk.action.FinishAction;
import net.haspamelodica.parser.parser.lrk.action.ReduceAction;
import net.haspamelodica.parser.tokenizer.TokenStream;
import net.haspamelodica.parser.tokenizer.TokenizingException;

public class LRkParserExecution<STATE>
{
	private final GenericLRkParser<STATE>	lrkParser;
	private final TokenStream				tokens;

	private Queue<Token<?>>	lookaheadTokens;
	private Word			lookahead;

	public LRkParserExecution(GenericLRkParser<STATE> lrkParser, TokenStream tokens)
	{
		this.lrkParser = lrkParser;
		this.tokens = tokens;

		this.lookaheadTokens = new LinkedList<>();
		this.lookahead = Word.EPSILON;
	}

	public InnerNode parse() throws ParseException
	{
		Stack<STATE> stateStack = new Stack<>();
		Stack<ASTNode<?>> astNodeStack = new Stack<>();
		stateStack.push(lrkParser.getInitialState());
		astNodeStack.push(null);
		for(;;)
		{
			STATE currentState = stateStack.peek();
			Word lookahead = getLookahead();
			Action action = lookupAction(currentState, lookahead);
			switch(action.getType())
			{
				case SHIFT:
					Token<?> token = consumeToken();
					astNodeStack.push(token);
					stateStack.push(lookupGoto(currentState, token.getSymbol()));
					break;
				case REDUCE:
					ReduceAction reduceAction = (ReduceAction) action;

					int oldStackSize = stateStack.size();
					int newStackSize = oldStackSize - reduceAction.getRhsSize();
					ASTNode<?> newNode = new InnerNode(reduceAction.getProduction(), astNodeStack.subList(newStackSize, oldStackSize));
					stateStack.setSize(newStackSize);
					astNodeStack.setSize(newStackSize);

					stateStack.push(lookupGoto(stateStack.peek(), reduceAction.getProduction().getLhs()));
					astNodeStack.push(newNode);
					break;
				case FINISH:
					FinishAction finishAction = (FinishAction) action;
					if(finishAction.dontIncludeStartSymbol())
						return (InnerNode) astNodeStack.pop();

					oldStackSize = stateStack.size();
					newStackSize = oldStackSize - finishAction.getRhsSize();
					return new InnerNode(finishAction.getProduction(), astNodeStack.subList(newStackSize, oldStackSize));
				case ERROR:
					String locDesc = tokens.getCurrentLocationDescription();
					token = consumeToken();
					throw new ParseException((locDesc != null ? "Near " + locDesc + ": " : "") +
							"Got " + (token == null ? "EOF" : token) + ", expected any of " + lrkParser.getActionTable().get(currentState).keySet());
				default:
					throw new IllegalStateException("Unknown enum constant: " + action.getType());
			}
		}
	}

	private STATE lookupGoto(STATE currentState, Symbol symbol)
	{
		return lrkParser.getGotoTable().getOrDefault(currentState, Collections.emptyMap()).get(symbol);
	}

	private Action lookupAction(STATE currentState, Word lookahead)
	{
		return lrkParser.getActionTable().getOrDefault(currentState, Collections.emptyMap()).getOrDefault(lookahead, ErrorAction.INSTANCE);
	}

	private Token<?> consumeToken() throws TokenizingException
	{
		enforceLookaheadSize(1);
		lookahead = lookahead.suffix(lookahead.getLength() - 1);
		return lookaheadTokens.poll();
	}

	private Word getLookahead() throws TokenizingException
	{
		enforceLookaheadSize(lrkParser.getLookaheadSize());
		return lookahead;
	}

	private void enforceLookaheadSize(int lookaheadSize) throws TokenizingException
	{
		while(lookahead.getLength() < lookaheadSize)
		{
			Token<?> nextToken = tokens.nextToken();
			lookaheadTokens.add(nextToken);
			lookahead = lookahead.append(nextToken == null ? Terminal.EOF : nextToken.getSymbol());
		}
	}
}

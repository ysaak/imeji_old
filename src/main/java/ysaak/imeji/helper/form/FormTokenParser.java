package ysaak.imeji.helper.form;

import com.mitchellbosecke.pebble.error.ParserException;
import com.mitchellbosecke.pebble.lexer.Token;
import com.mitchellbosecke.pebble.lexer.TokenStream;
import com.mitchellbosecke.pebble.node.ArgumentsNode;
import com.mitchellbosecke.pebble.node.BodyNode;
import com.mitchellbosecke.pebble.node.RenderableNode;
import com.mitchellbosecke.pebble.parser.Parser;
import com.mitchellbosecke.pebble.tokenParser.AbstractTokenParser;

public class FormTokenParser extends AbstractTokenParser {
	@Override
	public String getTag() {
		return "form";
	}

	@Override
	public RenderableNode parse(Token token, Parser parser) throws ParserException {
		TokenStream stream = parser.getStream();
		// skip the form token
		stream.next();

		ArgumentsNode args = parser.getExpressionParser().parseArguments(false);

		stream.expect(Token.Type.EXECUTE_END);

		// parse the body
		BodyNode body = parser.subparse(t -> t.test(Token.Type.NAME, "endform"));

		// skip the 'endform' token
		stream.next();

		stream.expect(Token.Type.EXECUTE_END);

		return new FormNode(args, body);
	}
}

package ysaak.imeji.helper.form;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.NodeVisitor;
import com.mitchellbosecke.pebble.node.AbstractRenderableNode;
import com.mitchellbosecke.pebble.node.ArgumentsNode;
import com.mitchellbosecke.pebble.node.BodyNode;
import com.mitchellbosecke.pebble.node.NamedArgumentNode;
import com.mitchellbosecke.pebble.node.PositionalArgumentNode;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class FormNode extends AbstractRenderableNode {
	private final ArgumentsNode args;

	private final BodyNode body;

	public FormNode(ArgumentsNode args, BodyNode body) {
		this.args = args;
		this.body = body;
	}

	@Override
	public void render(PebbleTemplateImpl self, Writer writer, EvaluationContext context) throws PebbleException, IOException {

		for (PositionalArgumentNode node : args.getPositionalArgs()) {
			System.out.println("pos > " + node.getValueExpression().evaluate(self, context));
		}

		List<NamedArgumentNode> namedArgumentNodeList = args.getNamedArgs();
		for (NamedArgumentNode arg : namedArgumentNodeList) {
			System.out.println("name > " + arg.getName());
			if (arg.getValueExpression() != null) {
				System.out.println("value > " + arg.getValueExpression().evaluate(self, context));
			} else {
				System.out.println("name > null");
			}
			System.out.println("======================================");
		}

		writer.write("<form>");
		body.render(self, writer, context);
		writer.write("</form>");
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}
}

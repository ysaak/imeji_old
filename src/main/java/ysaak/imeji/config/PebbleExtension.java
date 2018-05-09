package ysaak.imeji.config;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.node.expression.BinaryExpression;
import com.mitchellbosecke.pebble.operator.Associativity;
import com.mitchellbosecke.pebble.operator.BinaryOperator;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl;

import java.util.Collections;
import java.util.List;

public class PebbleExtension extends AbstractExtension {

	@Override
	public List<BinaryOperator> getBinaryOperators() {
		return Collections.singletonList(
				new StringContactOperator()
		);
	}

	private class StringContactOperator extends BinaryExpression<Object> implements BinaryOperator {
		@Override
		public int getPrecedence() {
			return 40;
		}

		@Override
		public String getSymbol() {
			return "~";
		}

		@Override
		public Class<? extends BinaryExpression<?>> getNodeClass() {
			return StringContactOperator.class;
		}

		@Override
		public Associativity getAssociativity() {
			return Associativity.LEFT;
		}

		@Override
		public Object evaluate(final PebbleTemplateImpl self, final EvaluationContext context) throws PebbleException {
			String left = String.valueOf(getLeftExpression().evaluate(self, context));
			String right = String.valueOf(getRightExpression().evaluate(self, context));

			return left + right;
		}
	}
}

package ysaak.imeji.helper.form;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.tokenParser.TokenParser;

import java.util.Collections;
import java.util.List;

public class FormHelper extends AbstractExtension {

	@Override
	public List<TokenParser> getTokenParsers() {
		return Collections.singletonList(new FormTokenParser());
	}
}

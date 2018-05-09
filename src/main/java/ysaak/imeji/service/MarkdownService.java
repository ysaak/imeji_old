package ysaak.imeji.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import ysaak.imeji.exception.ImejiException;
import ysaak.imeji.exception.MarkdownError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Service
public class MarkdownService {
	private Parser parser = null;
	private HtmlRenderer htmlRenderer = null;

	public String parseFile(File file) throws ImejiException {
		final FileReader reader;

		try {
			reader = new FileReader(file);
		}
		catch (FileNotFoundException e) {
			throw new ImejiException(MarkdownError.FILE_NOT_FOUND, file.getName());
		}

		final Node document;
		try {
			document = getParser().parseReader(reader);
		}
		catch (IOException e) {
			throw new ImejiException(MarkdownError.PARSE_ERROR, file.getName());
		}

		return getRenderer().render(document);
	}


	private Parser getParser() {
		if (parser == null) {
			parser = Parser.builder().build();
		}

		return parser;
	}

	private HtmlRenderer getRenderer() {
		if (htmlRenderer == null) {
			htmlRenderer = HtmlRenderer.builder().build();
		}

		return htmlRenderer;
	}
}

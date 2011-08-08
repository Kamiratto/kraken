package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Sort;
import org.krakenapps.logstorage.query.command.Sort.SortField;

public class SortParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("sort", this, k("sort"), ref("option"), ref("sort_field"));
		syntax.add("sort_field", new SortParser.SortFieldParser(),
				repeat(rule(option(choice(t("+"), t("-"))), new StringPlaceholder(new char[] { ' ', ',' }))));
		syntax.addRoot("sort");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		Map<String, String> option = (Map<String, String>) b.getChildren()[1].getValue();
		Integer count = null;
		SortField[] fields = (SortField[]) b.getChildren()[2].getValue();

		if (option.containsKey("limit"))
			count = Integer.parseInt(option.get("limit"));

		try {
			return new Sort(count, fields);
		} catch (IOException e) {
			return null;
		}
	}

	public static class SortFieldParser implements Parser {
		@Override
		public Object parse(Binding b) {
			List<SortField> fields = new ArrayList<SortField>();
			parse(b, fields);
			String lastName = fields.get(fields.size() - 1).getName();
			if (lastName.equalsIgnoreCase("d") || lastName.equalsIgnoreCase("desc")) {
				fields.remove(fields.size() - 1);
				for (SortField field : fields)
					field.reverseAsc();
			}

			return fields.toArray(new SortField[0]);
		}

		private void parse(Binding b, List<SortField> fields) {
			if (b.getValue() != null)
				fields.add(new SortField((String) b.getValue()));
			else {
				if (b.getChildren().length == 1) {
					if (b.getChildren()[0].getValue() != null)
						fields.add(new SortField((String) b.getChildren()[0].getValue()));
				} else if (b.getChildren().length == 2) {
					String v1 = (String) b.getChildren()[0].getValue();
					String v2 = (String) b.getChildren()[1].getValue();

					if (v1 != null && v2 != null) {
						if (v1.equals("-") || v1.equals("+"))
							fields.add(new SortField(v2, !v1.equals("-")));
						else {
							fields.add(new SortField(v1));
							fields.add(new SortField(v2));
						}
					} else if (v1 != null && v2 == null) {
						fields.add(new SortField(v1));
						parse(b.getChildren()[1], fields);
					} else if (v1 == null && v2 != null) {
						parse(b.getChildren()[0], fields);
						fields.add(new SortField(v2));
					} else {
						parse(b.getChildren()[0], fields);
						parse(b.getChildren()[1], fields);
					}
				}
			}
		}
	}
}

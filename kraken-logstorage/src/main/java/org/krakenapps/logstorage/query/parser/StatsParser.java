package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Function;
import org.krakenapps.logstorage.query.command.Stats;

public class StatsParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("stats", this, k("stats"), ref("option"), ref("function"), option(k("by"), ref("stats_field")));
		syntax.add("stats_field", new StatsFieldParser(), new StringPlaceholder(new char[] { ' ', ',' }),
				option(ref("stats_field")));
		syntax.addRoot("stats");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		List<String> keyFields = null;
		Function[] func = ((List<Function>) b.getChildren()[2].getValue()).toArray(new Function[0]);

		if (b.getChildren().length < 4)
			keyFields = new ArrayList<String>();
		else
			keyFields = (List<String>) b.getChildren()[3].getChildren()[1].getValue();

		return new Stats(keyFields, func);
	}

	public class StatsFieldParser implements Parser {
		@Override
		public Object parse(Binding b) {
			List<String> fields = new ArrayList<String>();
			parse(b, fields);
			return fields;
		}

		@SuppressWarnings("unchecked")
		private void parse(Binding b, List<String> fields) {
			if (b.getValue() != null)
				fields.add((String) b.getValue());
			else {
				for (Binding c : b.getChildren()) {
					if (c.getValue() != null) {
						if (c.getValue() instanceof Collection)
							fields.addAll((List<? extends String>) c.getValue());
						else
							fields.add((String) c.getValue());
					} else
						parse(c, fields);
				}
			}
		}
	}
}

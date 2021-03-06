package org.krakenapps.logdb.cmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.krakenapps.logdb.LogQueryCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cmd extends LogQueryCommand {
	private final Logger logger = LoggerFactory.getLogger(Cmd.class.getName());
	private Process p;
	private Charset charset;
	private int offset;
	private int limit;

	public Cmd(Process p, Charset charset, int offset, int limit) {
		this.p = p;
		this.charset = charset;
		this.offset = offset;
		this.limit = limit;
	}

	@Override
	public void start() {
		status = Status.Running;

		try {
			int i = 0;
			int count = 0;
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), charset));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				if (i < offset) {
					i++;
					continue;
				}

				if (limit != 0 && count >= limit)
					break;

				LogMap m = new LogMap();
				m.put("line", line);
				write(m);
				count++;
				i++;
			}
		} catch (Throwable t) {
			logger.error("kraken logdb cmd: cannot read process stdout", t);
		}

		p.destroy();
		eof();
	}

	@Override
	public void push(LogMap m) {
	}

	@Override
	public boolean isReducer() {
		return false;
	}
}

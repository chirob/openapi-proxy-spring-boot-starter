package com.swisscom.openapi.reverseproxy.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;

public class ProxyResource extends InputStreamResource {

	private InputStream inputStream;

	public ProxyResource(InputStream inputStream) {
		super(inputStream);
		this.inputStream = new CachingInputStream(inputStream);
	}

	@Override
	public InputStream getInputStream() throws IOException, IllegalStateException {
		return inputStream;
	}

	private static final class CachingInputStream extends InputStream {
		private InputStream in;
		private ByteArrayOutputStream buf = new ByteArrayOutputStream();

		@Override
		public int read() throws IOException {
			var bites = new byte[1];
			var rv = read(bites, 0, 1);
			return rv == -1 ? rv : bites[0];
		}

		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			var rv = in.read(b, off, len);
			if (!(in instanceof ByteArrayInputStream)) {
				if (rv == -1) {
					in = new ByteArrayInputStream(buf.toByteArray());
				} else {
					buf.write(b, off, len);
				}
			}
			return rv;
		}

		private CachingInputStream(InputStream in) {
			this.in = in;
		}
	}

}

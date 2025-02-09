/*
 * Copyright 2024-2099 Swisscom (Schweiz) AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swisscom.openapi.reverseproxy.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.AbstractResource;

public class ProxyResource extends AbstractResource {

	private InputStream inputStream;

	public ProxyResource(InputStream inputStream, boolean cache) {
		this.inputStream = cache ? new CachingInputStream(inputStream) : inputStream;
	}

	@Override
	public String getDescription() {
		return "InputStream reverse-proxy resource";
	}

	@Override
	public InputStream getInputStream() {
		return this.inputStream;
	}

	private static final class CachingInputStream extends FilterInputStream {

		private ByteArrayOutputStream buf = new ByteArrayOutputStream();

		@Override
		public int read() throws IOException {
			var bites = new byte[1];
			var rv = read(bites, 0, 1);
			return (rv == -1) ? rv : bites[0];
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
					in = new ByteArrayInputStream(this.buf.toByteArray());
				}
				else {
					this.buf.write(b, off, rv);
				}
			}
			return rv;
		}

		private CachingInputStream(InputStream in) {
			super(in);
		}

	}

}

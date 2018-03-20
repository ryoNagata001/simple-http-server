package gl8080.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class HttpRequest {
	public static final String CRLF = "\r\n";
	
	private final HttpHeader header;
	private final String bodyText;
	
	public HttpRequest(InputStream input) {
		try {
			this.header = new HttpHeader(input);
			this.bodyText = this.readBody(input);
			
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private String readBody(InputStream input) throws IOException {
		if (this.header.isChunkedTransfer()) { // チャンク形式かどうか判断
			return this.readBodyByChankedTransfer(input);
		} else {
			return this.readBodyByContentLength(input);
		}
	}
	
	private String readBodyByChankedTransfer(InputStream input) throws IOException {
		StringBuilder body = new StringBuilder();
		
		int chunkSize = Integer.parseInt(IOUtil.readLine(input), 16);
		
		while(chunkSize != 0) {
			byte[] buffer = new byte[chunkSize];
			input.read(buffer);
			
			body.append(buffer);
			
			input.read(); // chunk-bodyの末尾にある CRLF を読み飛ばす
			chunkSize = Integer.parseInt(IOUtil.readLine(input), 16);
		}
		
		return body.toString();
	}
	
	private String readBodyByContentLength(InputStream input) throws IOException {
		final int contentLength = this.header.getContentLength();
		
		if (contentLength <= 0) {
			return null;
		}
		
		byte[] c = new byte[contentLength];
		input.read(c);
		
		return new String(c);
	}

	
	public String getHeaderText() {
		return this.header.getText();
	}
	
	public String getBodyText() {
		return this.bodyText;
	}
	
	public HttpHeader getHeader() {
		return this.header;
	}
}
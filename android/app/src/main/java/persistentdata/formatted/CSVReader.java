package persistentdata.formatted;

import persistentdata.PersistentDataException;

import java.io.IOException;
import java.io.Reader;

public class CSVReader implements FormattedReader<String[]> {
	private final CSVFormat format;
	private final Reader reader;

	public CSVReader(CSVFormat format, Reader reader) {
		this.format = format;
		this.reader = reader;
	}
//原有问题：
//
//hasNext() 始终返回 true。
// 因此，DataPipeline.readTo() 中的读取循环无法通过 hasNext() 正确判断文件是否已经读完，
// 可能继续调用 getNext() 并导致读取失败。
//
//修改内容：
//
//增加 nextChar 字段，用于缓存预读字符。
//修改 hasNext()：检查 EOF，并在没有缓存字符时预读一个字符。
//修改 getNext() 的读取位置：优先使用缓存字符，避免预读后跳过数据。
	private boolean eof = false;
	private int nextChar = Integer.MIN_VALUE;
	public boolean hasNext() {
		if (eof) {
			return false;
		}

		if (nextChar == Integer.MIN_VALUE) {
			try {
				nextChar = reader.read();
			} catch (IOException e) {
				throw new CSVIOException(e.getMessage());
			}
		}

		if (nextChar == -1) {
			eof = true;
			return false;
		}

		return true;
	}

	// These format strings are provided to give you some ideas about what error cases might be encountered,
	// but they aren't complete. If you haven't seen these before, you can fill in the %s with .formatted:
	// for example, "hello %s".formatted("Bernardo") returns "hello Bernardo"
	private static final String LINE_TOO_SHORT_MESSAGE = "Line was too short: expected %s fields but found %s";
	private static final String LINE_TOO_LONG_MESSAGE = "Line was too long: expected %s fields";
	private static final String IMPROPER_ESCAPE_MESSAGE = "EOF reached unexpectedly while escaped";
	private static final String REACHED_EOF_MESSAGE = "Already reached end of file while reading";

	public String[] getNext() {
		if (format.COLUMN_COUNT == 0) {
			throw new CSVIOException(LINE_TOO_LONG_MESSAGE.formatted(0));
		}

		if (eof) {
			throw new CSVIOException(REACHED_EOF_MESSAGE);
		}

		StringBuilder result = new StringBuilder();
		boolean inSpecialField = false;
		boolean lastWasEscape = false;
		String[] fields = new String[format.COLUMN_COUNT];
		int fieldIndex = 0;
		boolean ignoredFirstQuote = false;
		while (true) {
			char c = format.LINE_SEPARATOR;
			try {
				int i;

				if (nextChar != Integer.MIN_VALUE) {
					i = nextChar;
					nextChar = Integer.MIN_VALUE;
				} else {
					i = reader.read();
				}
				if (i == -1) {
					if (inSpecialField) {
						throw new CSVIOException(IMPROPER_ESCAPE_MESSAGE);
					}
					eof = true;
					reader.close();
				} else {
					c = (char) i;
				}
			} catch (IOException e) {
				throw new CSVIOException(e.getMessage());
			}

			if (c == format.ESCAPE_MARKER) {
				inSpecialField = !inSpecialField;
				if (result.isEmpty() && !ignoredFirstQuote) {
					ignoredFirstQuote = true;
				} else if (lastWasEscape) {
					result.append(c);
					lastWasEscape = false;
				} else {
					lastWasEscape = true;
				}
			} else {
				lastWasEscape = false;
				if (c == format.FIELD_SEPARATOR && !inSpecialField) {
					fields[fieldIndex] = result.toString();
					fieldIndex++;
					if (fieldIndex >= format.COLUMN_COUNT) {
						throw new CSVIOException(LINE_TOO_LONG_MESSAGE.formatted(format.COLUMN_COUNT));
					}
					ignoredFirstQuote = false;
					result = new StringBuilder();
				} else if (c == format.LINE_SEPARATOR && !inSpecialField) {
					fields[fieldIndex] = result.toString();
					fieldIndex++;
					if (fieldIndex < format.COLUMN_COUNT) {
						throw new CSVIOException(LINE_TOO_SHORT_MESSAGE.formatted(format.COLUMN_COUNT, fieldIndex));
					}
					break;
				} else {
					result.append(c);
				}
			}
		}
		return fields;
	}

	public static class CSVIOException extends PersistentDataException {
		private static final long serialVersionUID = 1L;

		public CSVIOException(String message) {
			super(message);
		}
	}
}
package memecoach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过 HTTP 调用我们自己的 Python /api/explain 接口。
 *
 * 这个类不保存 OpenAI API Key，也不直接访问 OpenAI。
 * 使用 HttpURLConnection 是为了同时兼容普通 Java 与 Android。
 */
public class HttpLLMClient implements LLMClient {

    // 当前 Python 后端固定返回 {"explanation": "..."}。
    // 正则只定位这个字段，随后由 decodeJsonString 还原 JSON 字符串。
    private static final Pattern EXPLANATION_PATTERN = Pattern.compile(
            "\"explanation\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
    );

    private static final int MAX_RESPONSE_LENGTH = 65_536;
    private final URI endpoint;

    /**
     * @param endpointUrl Python API 地址，例如 http://127.0.0.1:8000/api/explain。
     */
    public HttpLLMClient(String endpointUrl) {
        Objects.requireNonNull(endpointUrl, "Endpoint URL cannot be null");
        this.endpoint = URI.create(endpointUrl);

        // 接口地址必须是完整的 HTTP 或 HTTPS URL。
        String scheme = endpoint.getScheme();
        if (( !"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || endpoint.getHost() == null) {
            throw new IllegalArgumentException("Invalid HTTP endpoint: " + endpointUrl);
        }
    }

    /**
     * 向 Python 后端发送两个 Prompt，并返回 AI 生成的 explanation。
     * 网络失败、超时或返回格式错误时抛出 IOException，
     * 由上层 LLMFeedbackService 捕获并回退到本地语录。
     */
    @Override
    public String requestExplanation(String systemPrompt, String userPrompt) throws IOException {
        if (systemPrompt == null || systemPrompt.isBlank()
                || userPrompt == null || userPrompt.isBlank()) {
            throw new IllegalArgumentException("Prompts cannot be empty");
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) endpoint.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5_000);  // 最长等待连接 5 秒
            connection.setReadTimeout(25_000);   // 最长等待 AI 回复 25 秒
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");

            // 按 Python ExplanationRequest 约定组装请求 JSON。
            String json = "{\"systemPrompt\":" + encodeJsonString(systemPrompt)
                    + ",\"userPrompt\":" + encodeJsonString(userPrompt) + "}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);

            try (OutputStream out = connection.getOutputStream()) {
                out.write(bytes);
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("Meme Coach backend returned HTTP " + status);
            }

            String responseJson = readLimited(connection);
            Matcher matcher = EXPLANATION_PATTERN.matcher(responseJson);
            if (!matcher.find()) {
                throw new IOException("Backend response has no explanation field");
            }

            String explanation = decodeJsonString(matcher.group(1)).trim();
            if (explanation.isEmpty()) {
                throw new IOException("Backend explanation was empty");
            }
            return explanation;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // 读取响应时限制最大长度，避免异常响应占用过多内存。
    private static String readLimited(HttpURLConnection connection) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            char[] buffer = new char[2_048];
            int size;
            while ((size = reader.read(buffer)) != -1) {
                if (result.length() + size > MAX_RESPONSE_LENGTH) {
                    throw new IOException("Backend response is too large");
                }
                result.append(buffer, 0, size);
            }
        }
        return result.toString();
    }

    // 把普通 Java 字符串编码成 JSON 字符串，防止引号或换行破坏请求。
    private static String encodeJsonString(String text) {
        StringBuilder result = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                default -> {
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        result.append("\\u").append("0000", 0, 4 - hex.length()).append(hex);
                    } else {
                        result.append(c);
                    }
                }
            }
        }
        return result.append('"').toString();
    }

    // 解析后端返回的 explanation 字符串中的 JSON 转义字符。
    private static String decodeJsonString(String raw) throws IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c != '\\') {
                result.append(c);
                continue;
            }
            if (++i >= raw.length()) {
                throw new IOException("Invalid JSON escape");
            }
            switch (raw.charAt(i)) {
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                case '/' -> result.append('/');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'u' -> {
                    if (i + 4 >= raw.length()) {
                        throw new IOException("Invalid JSON Unicode escape");
                    }
                    try {
                        int codePoint = Integer.parseInt(raw.substring(i + 1, i + 5), 16);
                        result.append((char) codePoint);
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid JSON Unicode escape", e);
                    }
                    i += 4;
                }
                default -> throw new IOException("Unknown JSON escape");
            }
        }
        return result.toString();
    }
}

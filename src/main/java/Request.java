import java.util.List;

public class Request {
    private final String requestLine;
    private final List<String> headers;
    private final String body;

    public Request(String requestLine, List<String> headers, String body) {

        if (requestLine == null || requestLine.isBlank()) {
            throw new IllegalArgumentException("Request line '%s' is not valid.".formatted(requestLine));
        }

        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
    }

    public String getRequestTarget() {
        return requestLine.split("\s")[1];
    }

    public String getRequestLine() {
        return requestLine;
    }

    public List<String> getHeaders() {
        return List.copyOf(headers);
    }

    public String getHeaderByName(String headerName) {
        for (String header : headers) {
            if (header.toLowerCase().startsWith("user-agent")) {
                return (header.split(":")[1]).trim();
            }
        }
        return "";
    }

    public String getRequestMethod() {
        return requestLine.split("\s")[0];
    }

    @Override
    public String toString() {
        return """
                requestLine=%s
                Headers=%s
                Body=%s
                """.formatted(requestLine, headers, body);
    }

    public String getBody() {
        return body;
    }
}

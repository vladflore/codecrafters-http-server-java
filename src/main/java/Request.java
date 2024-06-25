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
}

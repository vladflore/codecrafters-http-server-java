import java.util.List;

public class RequestParser {

    public static Request parse(String request) {
        if (request == null || request.isBlank()) {
            return null;
        }
        String[] requestParts = request.split("\r\n");
        return new Request(
                requestParts[0],
                List.of(),
                "");
    }

}
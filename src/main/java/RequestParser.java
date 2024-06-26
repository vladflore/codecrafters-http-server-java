import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestParser {

    public static Request parse(List<String> request) {
        if (request == null || request.size() == 0) {
            return null;
        }
        String requestLine = request.get(0);
        List<String> headers = new ArrayList<>();
        for (int i = 1; i < request.size() && !request.get(i).isBlank(); i++) {
            headers.add(request.get(i));
        }

        return new Request(
                requestLine,
                List.copyOf(headers),
                "");
    }

    public static void main(String[] args) {
        String request = "GET /index.html HTTP/1.1\r\nHost: localhost:4221\r\nUser-Agent: curl/7.64.1\r\nAccept: */*\r\n\r\nbody";
        Request r = RequestParser.parse(Arrays.asList(request.split("\r\n")));
        System.out.println(r.getRequestLine());
        System.out.println(r.getHeaders());
        System.out.println(r.getHeaderByName("User-Agent"));
    }

}
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RequestParser {

    public static Request parse(List<String> request) {
        if (request == null || request.size() == 0) {
            return null;
        }
        String requestLine = request.get(0);
        List<String> headers = new ArrayList<>();
        for (int i = 1; i < request.size() - 1; i++) {
            headers.add(request.get(i));
        }

        return new Request(
                requestLine,
                List.copyOf(headers),
                request.getLast());
    }

    public static void main(String[] args) {
        String request = "POST /files/orange_raspberry_pear_grape HTTP/1.1\r\nHost: localhost:4221\r\nContent-Length: 61\r\nContent-Type: application/octet-stream\r\nAccept-Encoding: encoding-1, encoding-2, encoding-3\r\n\r\ngrape orange mango pineapple pineapple strawberry banana pear";
        List<String> lines = Stream.of(request.split("\r\n")).filter(el -> !el.isEmpty()).toList();
        System.out.println(lines);
        Request r = RequestParser.parse(lines);
        System.out.println(r.getRequestLine());
        System.out.println(r.getHeaders());
        System.out.println(r.getHeaderByName("User-Agent"));
        System.out.println(r.getHeaderByName("Accept-Encoding"));
        System.out.println(r.getBody());
    }

}
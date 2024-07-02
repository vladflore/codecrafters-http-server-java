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
                request.get(request.size() - 1));
    }

    public static void main(String[] args) {
        String request = "POST /files/orange_raspberry_pear_grape HTTP/1.1\r\nHost: localhost:4221\r\nContent-Length: 61\r\nContent-Type: application/octet-stream\r\nAccept-Encoding: gzip\r\n\r\ngrape orange mango pineapple pineapple strawberry banana pear";
        Request r = RequestParser.parse(Arrays.asList(request.split("\r\n")));
        System.out.println(r.getRequestLine());
        System.out.println(r.getHeaders());
        System.out.println(r.getHeaderByName("User-Agent"));
        System.out.println(r.getHeaderByName("Accept-Encoding"));
        System.out.println(r.getBody());
    }

}
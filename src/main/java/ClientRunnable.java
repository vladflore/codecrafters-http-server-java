import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientRunnable implements Runnable {
    private static final String NOT_FOUND = "HTTP/1.1 404 Not Found\r\n\r\n";
    private static final String OK = "HTTP/1.1 200 OK\r\n\r\n";
    private static final String CREATED = "HTTP/1.1 201 Created\r\n\r\n";

    private final Socket socket;
    private final String rootDirectory;

    public ClientRunnable(Socket clientSocket, String rootDirectory) {
        this.socket = clientSocket;
        this.rootDirectory = rootDirectory;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            List<String> lines = new ArrayList<>();
            String clientInput;
            // read until the body
            while ((clientInput = in.readLine()) != null && !clientInput.isEmpty()) {
                lines.add(clientInput);
            }
            // read the body
            StringBuffer sb = new StringBuffer();
            while (in.ready()) {
                sb.append((char) in.read());
            }
            lines.add(sb.toString());

            Request request = RequestParser.parse(lines);

            if (request == null) {
                return;
            }

            String requestTarget = request.getRequestTarget();

            Pattern echoPattern = Pattern.compile("/echo/(?<echo>.+)");
            Matcher echoMatcher = echoPattern.matcher(requestTarget);

            Pattern filesPattern = Pattern.compile("/files/(?<filename>.+)");
            Matcher filesMatcher = filesPattern.matcher(requestTarget);

            if (requestTarget.equals("/")) {
                out.write(OK);
            } else if (echoMatcher.matches()) {
                String echo = echoMatcher.group("echo");
                out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + echo.getBytes().length
                        + "\r\n\r\n" + echo);
            } else if (requestTarget.equals("/user-agent")) {
                System.out.println(request);
                String userAgent = request.getHeaderByName("User-Agent");
                out.write(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + userAgent.getBytes().length
                                + "\r\n\r\n" + userAgent);
            } else if (request.getRequestMethod().equals("GET") && filesMatcher.matches()) {
                String fileName = filesMatcher.group("filename");
                Path path = Paths.get(rootDirectory, fileName);
                if (!Files.exists(path)) {
                    out.write(NOT_FOUND);
                } else {
                    String fileContent = Files.readString(path);
                    out.write(
                            "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: "
                                    + fileContent.getBytes().length
                                    + "\r\n\r\n" + fileContent);
                }
            } else if (request.getRequestMethod().equals("POST") && filesMatcher.matches()) {
                String fileName = filesMatcher.group("filename");
                Path path = Paths.get(rootDirectory, fileName);
                Path newFile = Files.createFile(path);
                Files.writeString(newFile, request.getBody());
                out.write(CREATED);
            } else {
                out.write(NOT_FOUND);
            }

            out.flush();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}

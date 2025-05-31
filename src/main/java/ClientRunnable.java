
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import java.util.zip.GZIPOutputStream;

public class ClientRunnable implements Runnable {

    private static final String NOT_FOUND = "HTTP/1.1 404 Not Found%s\r\n\r\n";
    private static final String OK = "HTTP/1.1 200 OK%s\r\n\r\n";
    private static final String CREATED = "HTTP/1.1 201 Created%s\r\n\r\n";

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

            while (true) {
                List<String> lines = new ArrayList<>();
                String clientInput;
                // read until the body
                while ((clientInput = in.readLine()) != null && !clientInput.isEmpty()) {
                    lines.add(clientInput);
                }
                // read the body
                var sb = new StringBuilder();
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

                var shouldCloseConnection = "close".equalsIgnoreCase(request.getHeaderByName("Connection"));
                var connectionHeader = "";
                if (shouldCloseConnection) {
                    connectionHeader = "\r\nConnection: close";
                }

                if (requestTarget.equals("/")) {
                    out.write(OK.formatted(connectionHeader));
                } else if (echoMatcher.matches()) {
                    String echo = echoMatcher.group("echo");
                    String encodingHeader = request.getHeaderByName("Accept-Encoding");
                    if (encodingHeader.contains("gzip")) {
                        try (var obj = new ByteArrayOutputStream(); var gzip = new GZIPOutputStream(obj);) {
                            gzip.write(echo.getBytes());
                            gzip.finish();
                            byte[] compressed = obj.toByteArray();
                            var toSend = "HTTP/1.1 200 OK%s\r\nContent-Type: text/plain\r\nContent-Encoding: gzip\r\nContent-Length: ".formatted(connectionHeader)
                                    + compressed.length
                                    + "\r\n\r\n";
                            socket.getOutputStream().write(toSend.getBytes());
                            socket.getOutputStream().write(compressed);
                        }
                    } else {
                        out.write("HTTP/1.1 200 OK%s\r\nContent-Type: text/plain\r\nContent-Length: ".formatted(connectionHeader) + echo.getBytes().length
                                + "\r\n\r\n" + echo);
                    }
                } else if (requestTarget.equals("/user-agent")) {
                    String userAgent = request.getHeaderByName("User-Agent");
                    out.write(
                            "HTTP/1.1 200 OK%s\r\nContent-Type: text/plain\r\nContent-Length: ".formatted(connectionHeader) + userAgent.getBytes().length
                            + "\r\n\r\n" + userAgent);
                } else if (request.getRequestMethod().equals("GET") && filesMatcher.matches()) {
                    String fileName = filesMatcher.group("filename");
                    Path path = Paths.get(rootDirectory, fileName);
                    if (!Files.exists(path)) {
                        out.write(NOT_FOUND.formatted(connectionHeader));
                    } else {
                        String fileContent = Files.readString(path);
                        out.write(
                                "HTTP/1.1 200 OK%s\r\nContent-Type: application/octet-stream\r\nContent-Length: ".formatted(connectionHeader)
                                + fileContent.getBytes().length
                                + "\r\n\r\n" + fileContent);
                    }
                } else if (request.getRequestMethod().equals("POST") && filesMatcher.matches()) {
                    String fileName = filesMatcher.group("filename");
                    Path path = Paths.get(rootDirectory, fileName);
                    Path newFile = Files.createFile(path);
                    Files.writeString(newFile, request.getBody());
                    out.write(CREATED.formatted(connectionHeader));
                } else {
                    out.write(NOT_FOUND.formatted(connectionHeader));
                }
                out.flush();

                if (shouldCloseConnection) {
                    socket.close();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}

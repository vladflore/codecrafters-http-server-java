import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

  private static final String NOT_FOUND = "HTTP/1.1 404 Not Found\r\n\r\n";
  private static final String OK = "HTTP/1.1 200 OK\r\n\r\n";

  public static void main(String[] args) {
    Socket clientSocket = null;

    try (ServerSocket serverSocket = new ServerSocket(4221)) {
      serverSocket.setReuseAddress(true);
      clientSocket = serverSocket.accept();

      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
      
      List<String> lines = new ArrayList<>();
      String clientInput;
      while ((clientInput = in.readLine()) != null && !clientInput.isEmpty()) {
        lines.add(clientInput);
      }

      Request request = RequestParser.parse(lines);

      if (request == null) {
        return;
      }

      String requestTarget = request.getRequestTarget();

      Pattern echoPattern = Pattern.compile("/echo/(?<echo>.+)");
      Matcher echoMatcher = echoPattern.matcher(requestTarget);

      if (requestTarget.equals("/")) {
        out.write(OK);
      } else if (echoMatcher.matches()) {
        String echo = echoMatcher.group("echo");
        out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + echo.getBytes().length
            + "\r\n\r\n" + echo);
      } else if (requestTarget.equals("/user-agent")) {
        System.out.println(request);
        String userAgent = request.getHeaderByName("User-Agent");
        out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + userAgent.getBytes().length
            + "\r\n\r\n" + userAgent);
      } else {
        out.write(NOT_FOUND);
      }

      out.flush();
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}

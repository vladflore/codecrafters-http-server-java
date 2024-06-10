import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
      String line = in.readLine();
      if (line != null) {
        String[] requestParts = line.split("\r\n");
        String requestLine = requestParts[0];
        String[] requestLineParts = requestLine.split("\s");
        String requestTarget = requestLineParts[1];
        if (requestTarget.equals("/")) {
          out.write(OK);
        } else {
          out.write(NOT_FOUND);
        }
        out.flush();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}

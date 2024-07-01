import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

  public static void main(String[] args) {

    try (ServerSocket serverSocket = new ServerSocket(4221)) {
      serverSocket.setReuseAddress(true);

      String rootDirectory = "";

      if (args.length > 1 && args[0].equals("--directory")) {
        rootDirectory = args[1];
      }

      while (true) {
        Socket clientSocket = serverSocket.accept();
        Thread clientThread = new Thread(new ClientRunnable(clientSocket, rootDirectory));
        clientThread.start();
      }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}

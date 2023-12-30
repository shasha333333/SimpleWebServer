import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

public class SimpleWebServer {
    private static final int PORT = 8080;
    private static final String DOCUMENT_ROOT = "."; // 使用程序启动目录作为文档的根目录

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName("0.0.0.0"));

            System.out.println("SimpleWebServer listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClientRequest(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        String requestLine = reader.readLine();
        System.out.println("Received request: " + requestLine);

        // 解析请求行
        String[] requestTokens = requestLine.split("\\s+");
        String method = requestTokens[0];
        String path = requestTokens[1];

        if ("GET".equals(method)) {
            // 处理 GET 请求
            serveStaticFile(path, writer);
        } else {
            // 不支持的方法
            sendResponse(writer, "HTTP/1.1 501 Not Implemented\r\n\r\n", "501 Not Implemented");
        }

        reader.close();
        writer.close();
        clientSocket.close();
    }

    private static void serveStaticFile(String path, BufferedWriter writer) throws IOException {
        String filePath = DOCUMENT_ROOT + path;
        File file = new File(filePath);

        if (file.exists() && file.isFile()) {
            // 文件存在，返回 200 OK
            sendResponse(writer, "HTTP/1.1 200 OK\r\n\r\n", readFileContents(file));
        } else {
            // 文件不存在，返回 404 Not Found
            sendResponse(writer, "HTTP/1.1 404 Not Found\r\n\r\n", "404 Not Found");
        }
    }

    private static void sendResponse(BufferedWriter writer, String statusLine, String responseBody) throws IOException {
        writer.write(statusLine);
        writer.write(responseBody);
        writer.flush();
    }

    private static String readFileContents(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }

        reader.close();
        return content.toString();
    }
}

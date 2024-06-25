package Socket;


	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.io.PrintWriter;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.sql.Connection;
	import java.sql.DriverManager;
	import java.sql.ResultSet;
	import java.sql.SQLException;
	import java.sql.Statement;

public class Sockett {
	    private static final int PORT = 5432;
	    private static final String DB_URL = "jdbc:postgresql://localhost:5432/producto";
	    private static final String DB_USER = "postgres";
	    private static final String DB_PASSWORD = "1234";

	    public static void main(String[] args) {
	        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
	            System.out.println("Servidor iniciado. En el puerto:" + PORT);

	            while (true) {
	                Socket clientSocket = serverSocket.accept();
	                System.out.println("Nuevo cliente conectado " + clientSocket.getInetAddress().getHostAddress());

	                // Crear un nuevo hilo para manejar la conexión del cliente
	                new ClientHandler(clientSocket).start();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    private static class ClientHandler extends Thread {
	        private final Socket clientSocket;

	        public ClientHandler(Socket socket) {
	            this.clientSocket = socket;
	        }

	        @Override
	        public void run() {
	            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
	                 Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

	                String query = in.readLine();
	                System.out.println("Received query: " + query);

	                try (Statement statement = connection.createStatement();
	                     ResultSet resultSet = statement.executeQuery(query)) {
	                    while (resultSet.next()) {
	                        out.println(resultSet.getString(1));
	                    }
	                } catch (SQLException e) {
	                    out.println("Error ejecutandose: " + e.getMessage());
	                }
	            } catch (IOException | SQLException e) {
	                e.printStackTrace();
	            } finally {
	                try {
	                    clientSocket.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }
}

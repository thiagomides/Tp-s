import java.net.ServerSocket;
import java.net.Socket;

import Servidor.ServidorTCP;

public class PedroServer {

	public static void main(String[] args) {
		
		int porta = Integer.parseInt(args[1]);
		
		try {
			/*
			 * Socket de entrada por onde o servidor esperará por requisições
			 * de clientes que queiram iniciar uma conexão
			 */
			ServerSocket servidor = new ServerSocket(porta);
			System.out.println("Servidor inicializado na porta: " + porta);
	      
			while(true) {
			    /*
			     * Esperando concexões
			     */
				Socket clienteConectado = servidor.accept();
				/*
				 * Quando o servidor aceita a conexão
				 */
				ServidorTCP server = new ServidorTCP(clienteConectado, args[0], porta); 
				Thread clienteDedicado = new Thread(server);
				clienteDedicado.start();
				    
			}
		}   
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Erro: " + e.getMessage());
		}

	}

}

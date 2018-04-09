package Servidor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/*
 * Servidor implementado para receber conexões simultâneas
 */
public class ServidorTCP implements Runnable{
	
	private String host_directory; //Diretório em que irá funcionar
	private int porta;
	private Socket clienteWeb;
	
	public ServidorTCP(Socket client, String path, int porta) {
		
		this.clienteWeb = client;
		this.host_directory = path;
		this.porta = porta;
		
	}
	
	private void IniciarServidor() {
		
		try {
			/*
			 * Mapeamento da entrada e saída do socket com o cliente	    	    
			 */
			InputStream dados_entrada = this.clienteWeb.getInputStream();
			OutputStream dados_saida = this.clienteWeb.getOutputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(dados_entrada));
			
			Requisicao requisicao = new Requisicao(reader, this.host_directory);
			String resposta = requisicao.tratarRequisicao();
			
			dados_saida.flush();
			dados_saida.write(resposta.getBytes());
			
			this.clienteWeb.close();		    
			
		}   
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Erro: " + e.getMessage());
		}
	}

	@Override
	public void run() {
		
		IniciarServidor();
		
	}
}

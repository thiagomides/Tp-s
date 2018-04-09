package Cliente;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClienteTCP {
	
	private String url; //url (hostname + uri) digitada pelo usuário como parâmetro
	private int porta; //porta para conexão com o servidor
	private String hostname;
	private String uri;
	
	public ClienteTCP(String url, int porta) {
		
		this.url = url;
		this.porta = porta;
		
	}
	
	/*
	 * Separa a url devolvendo o endereço IP obtido através do hostname 
	 */
	private InetAddress obterEnderecoHost() throws UnknownHostException{
		
		InetAddress ip = null;
		
		/*
		 * Suporte em caso do endereço ser digitado com "http://" antes do
		 * seu host name
		 */
		if(url.contains("http")) {
			
			String url_completa = "";
			/*
			 * Separa a entrada "http://" da url
			 */
			if(this.url != null && this.url != "")
				url_completa = this.url.replaceFirst("/", " ");
			String[] aux = url_completa.split(" ");
			String auxiliar = aux[1].substring(1, aux[1].length() - 1).replaceFirst("/", " ");
			/*
			 * Separa o hostname da uri
			 */
			String[] host = auxiliar.split(" ");
			if(host.length <= 1)
				this.uri = "/";
			else
				this.uri = auxiliar.split(" ")[1];
			this.hostname = host[0];
			ip = InetAddress.getByName(this.hostname);
		}
		else {
			
			String url_completa = "";
			if(this.url != null && this.url != "")
				url_completa = this.url.replaceFirst("/", " ");
			String[] host = url_completa.split(" ");
			if(host.length <= 1)
				this.uri = "/";
			else
				this.uri = url_completa.split(" ")[1];
			this.hostname = host[0];
			ip = InetAddress.getByName(this.hostname);
			
		}
		
		return ip;
		
	}
	
	/*
	 * Montagem da mensagem de requisição apenas para o método GET do HTTP
	 */
	private String montarRequisicao(String hostname, String uri) {
		
		StringBuilder aux = new StringBuilder();
        
        aux.append("GET ").append(this.uri).append(" HTTP/1.1").append("\r\n");
        aux.append("Host: ").append(this.hostname).append("\r\n");
        aux.append("Connection: keep-alive").append("\r\n");
        aux.append("User-Agent: ").append("Pedro ClienteTCP").append("\r\n");
        aux.append("Accept: ").append("text/html,application/xhtml+xml,application/xml;q=0.9").append("\r\n");
        aux.append("Accept-Language: ").append("pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7").append("\r\n");
        aux.append("\r\n");
        
        return aux.toString();
		
	}
	
	/*
	 * Nomeia o arquivo para realizar o Download do mesmo contido no servidor
	 */
	private String nomearArquivoDownload(String content_type) {
		
		String[] aux;
		String aux2;
		
		if(this.uri == "/") {
			
			aux2 = "index";
			if(content_type.contains("text/html"))
				aux2 += ".html";
			
		}
		/*
		 * Recebe o mesmo nome do arquivo
		 */
		else {
			
			aux = this.uri.split("/");
			aux2 = aux[aux.length - 1];
			if(content_type.contains("text/html"))
				aux2 += ".html";
			
		}
		
		return aux2;
		
	}
	
	private void fazerDownloadArquivo(BufferedReader reader, String content_type) throws IOException{
		
		FileOutputStream f;
		
		String nome = nomearArquivoDownload(content_type);
		f = new FileOutputStream(new File(nome));
			
		StringBuffer sb = new StringBuffer();
        if (reader.ready()) {
            int i = 0;
            while ((i = reader.read()) != -1) {
                sb.append((char) i);
            }
        }
        
		f.write(sb.toString().getBytes());
		
		f.close();
		
	}
	
	public void iniciarConexao() {
		
		
		try {
			
			String hostname = null, uri = null;
			
			InetAddress ip = obterEnderecoHost();
			/*
			 * Inicia a conexão com o servidor
			 */
			Socket cliente = new Socket(ip,this.porta);
			/*
			 * Mapeamento da saída e da entrada do socket
			 */
			DataOutputStream saida = new DataOutputStream(cliente.getOutputStream());
			BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            
            String requisicao = montarRequisicao(hostname, uri);
			saida.flush();
			saida.write(requisicao.getBytes());
			String line = entrada.readLine();
			String content_type = null;
			while(!line.isEmpty()) {
				if(line.contains("Content-Type:"))
					content_type = line;
				line = entrada.readLine();
			}
			BufferedReader aux = entrada;
			fazerDownloadArquivo(aux, content_type);
			
			cliente.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}

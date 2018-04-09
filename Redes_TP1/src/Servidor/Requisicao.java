package Servidor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import Exceptions.GETException;
import Exceptions.HttpException;

/*
 * Classe para tratar requisições feitas ao servidor
 * pelos clientes
 */
public class Requisicao {
	
	private final String VERSAO_HTTP = "HTTP/1.1"; //Versão HTTP que o servidor utiliza
	private BufferedReader mensagem_requisicao = null; //Mensagem de requisição recebida por um cliente
	/*
	 * Campos da mensagem de resposta
	 */
	private String content_type;
	private int codigo_de_retorno;
	private String ultima_modificacao;
	private String diretorio_hospedado; //Diretório onde o servidor ficerá hospedado
	
	public Requisicao(BufferedReader mensagem, String host_directory) {
		
		this.mensagem_requisicao = mensagem;
		this.diretorio_hospedado = host_directory;
			
	}
	
	/*
	 * Converte a data que está em formato local para o formato GMT
	 */
	private String converterFormatoData(Date data) {
		
		StringBuilder nova_data = new StringBuilder();
		String[] aux;
		DateFormat df = new SimpleDateFormat("HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		aux = data.toString().split(" ");
		
		nova_data.append(aux[0]).append(", ").append(aux[2]).append(" ").append(aux[1]).append(" ");
		nova_data.append(aux[5]).append(" ").append(df.format(data)).append(" GMT");
		
		return nova_data.toString();
		
	}
	
	private int getTamanhoEmBytes(String mensagem) {
		
		int tamanho;
		
		try {
		    byte[] utf8Bytes = mensagem.getBytes("UTF-8");
		    tamanho = utf8Bytes.length;
		}
		catch(UnsupportedEncodingException e) {
			tamanho = -1;
		}
		
		return tamanho;
	}
	
	/*
	 * Monta a mensagem de resposta a requisições dos clientes
	 */
	private String montarMensagemResposta(String dados) {
		
		StringBuilder respostaFinal = new StringBuilder();
		String resposta = "";
		String tamanhoDados = "";
		
		switch(this.codigo_de_retorno) {
		
			case 200:
				resposta = " 200 OK";
				tamanhoDados = getTamanhoEmBytes(dados) + "";
				break;
			case 400:
				resposta = " 400 Bad Request";
				tamanhoDados = getTamanhoEmBytes(dados) + "";
				break;
			case 404:
				resposta = " 404 Not Found";
				tamanhoDados = getTamanhoEmBytes(dados) + "";
				break;
			case 505:
				resposta = " 505 HTTP Version Not Supported";
				tamanhoDados = getTamanhoEmBytes(dados) + "";
				break;
			default:
				break;
		
		}
		
		respostaFinal.append(VERSAO_HTTP).append(resposta).append("\r\n");
		respostaFinal.append("Connection: close").append("\r\n");
		respostaFinal.append("Location: ").append("http://").append(this.diretorio_hospedado).append("\r\n");
		respostaFinal.append("Date: ").append(converterFormatoData(new Date())).append("\r\n");
		respostaFinal.append("Server: Pedro Souza Servidor").append("\r\n");
		respostaFinal.append("Last-Modified: ").append(this.ultima_modificacao).append("\r\n");
		respostaFinal.append("Content-Lenght: ").append(tamanhoDados).append("\r\n");
		respostaFinal.append("Content-Type: ").append(this.content_type).append("\r\n");
		respostaFinal.append("\r\n");
		respostaFinal.append(dados);
		
		return respostaFinal.toString();
		
	}
	
	private String criarHttpParaErros(String titulo, String descricao) {
		
		StringBuilder mensagem_resposta = new StringBuilder();
		
		mensagem_resposta.append("<!DOCTYPE html><head><meta charset=\"UTF-8\"/><title>").append(titulo)
							.append("</title></head><body>");
		mensagem_resposta.append("<h1>").append(titulo).append("</h1>");
		mensagem_resposta.append("<p>").append(descricao).append("</p>");
		mensagem_resposta.append("</body></html>");
		
		return mensagem_resposta.toString();
		
	}
	
	/*
	 * Cria corpo de entrada em caso a mensagem a uma requisição for uma mensagem
	 * de erro ao objeto requisitado
	 */
	private String criarCorpoDeEntidade(int codigo) {
		
		String mensagem_resposta = "";
		String titulo = "";
		String descricao = "";
		
		switch(codigo) {
			case 400:
				titulo = "400 Bad Request";
				descricao = "The server could not understand the request due to invalid syntax.";
				mensagem_resposta = criarHttpParaErros(titulo, descricao);
				break;
			case 404:
				titulo = "404 Not Found";
				descricao = "The server can't find the requested resource.";
				mensagem_resposta = criarHttpParaErros(titulo, descricao);
				break;
			case 505:
				titulo = "505 HTTP Version Not Supported";
				descricao = "The HTTP version used in the request is not supported by the server.";
				mensagem_resposta = criarHttpParaErros(titulo, descricao);
				break;
			default:
				break;
		}
		return mensagem_resposta;
		
	}
	
	/*
	 * Cria o corpo de entidade em caso do objeto requisitado
	 * for um diretório, onde deve ser listado as possibilidades 
	 * dentro desse diretório
	 */
	private String criarCorpoDeEntidade(String[] arquivosExistentes) {
		
		StringBuilder mensagem_resposta = new StringBuilder();
		mensagem_resposta.append("<!DOCTYPE html><head><meta charset=\"UTF-8\"/><title>Servidor Pedro</title></head><body>");
		mensagem_resposta.append("<b>Opções de pesquisa:</b></br><ul>");
		for (String nomeArquivo : arquivosExistentes) {
			mensagem_resposta.append("<li>").append(nomeArquivo).append("</li>");
		}
		mensagem_resposta.append("</ul></body></html>");
		return mensagem_resposta.toString();
	}
	
	/*
	 * Cria o corpo de entidade em caso do objeto requisitado
	 * for um arquivo, onde deve ser enviado o conteúdo do arquivo para download
	 */
	private String criarCorpoDeEntidade(File arquivo) {
		
		StringBuilder mensagem_resposta = new StringBuilder();
		
		try {
			FileReader r = new FileReader(arquivo);
			BufferedReader reader = new BufferedReader(r);
			String line = null;
			
			while((line = reader.readLine()) != null)
				mensagem_resposta.append(line + '\n');
			
			
			reader.close();
		}
		catch(Exception e) {
			
		}
		
		return mensagem_resposta.toString();
	}
	
	/*
	 * Verifica a extensão do arquivo
	 * para enviar o devido Content-Type
	 */
	private void verificarExtensaoArquivo(String nome) {
		
		String extensao = "";
		
		if(nome != null && nome != "")
			extensao = nome.substring(nome.lastIndexOf("."), nome.length());
		
		switch(extensao) {
			case ".txt":
				this.content_type = "text/plain";
				break;
			default:
				this.content_type = "text/html";
				break;
 		}
		
	}
	
	private String verificarURI(String uri) {
		
		File entrada = new File(uri);
		String retorno;
		
		/*
		 * Verifica se o bojeto requisitado encontra-se no servidor
		 */
		if(entrada.exists()) {
			this.codigo_de_retorno = 200;
			/*
			 * Diferencia o objeto requisitado
			 * se é um arquivo ou um diretório
			 */
			if(entrada.isDirectory()) {
				
				String[] lista = entrada.list();
				retorno = criarCorpoDeEntidade(lista);
				verificarExtensaoArquivo("");
				this.ultima_modificacao = converterFormatoData(new Date(entrada.lastModified()));
				
			}
			else {
				
				verificarExtensaoArquivo(entrada.toString());
				retorno = criarCorpoDeEntidade(entrada);
			}
		}
		else {
			
			this.codigo_de_retorno = 404;
			verificarExtensaoArquivo("");
			retorno = criarCorpoDeEntidade(404);
		}
		
		return retorno;
	}
	
	public String tratarRequisicao() {
		
		String resposta_final = null;
		String corpo_de_entidade = "";
		
		try {
			
			String cabecalho_requisicao = null;
			String[] aux = null;
			
			if(this.mensagem_requisicao != null) {
		        cabecalho_requisicao = this.mensagem_requisicao.readLine();
		        /*
		         * Separa o cabeçalho de requisição do resto da
		         * mensagem de requisição
		         */
		        if(cabecalho_requisicao != null && cabecalho_requisicao != "") 
		        	aux = cabecalho_requisicao.split(" ");
		        
		        
		        if(!aux[aux.length - 1].equals(VERSAO_HTTP))
		        	throw new HttpException("Versão HTTP diferente. Versão HTTP do servidor: " + VERSAO_HTTP);
		        
		        /*
		         * O servidor só aceita o método GET do HTTP
		         */
		        if(aux[0].equals("GET")) {
		        	
		        	String objeto_requisitado = this.diretorio_hospedado + aux[1];
		        	
		        	System.out.println(this.diretorio_hospedado);
		        	System.out.println(aux[1]);
		        	
		        	corpo_de_entidade = verificarURI(objeto_requisitado);
		        	resposta_final = montarMensagemResposta(corpo_de_entidade);
		        	
		        }
		        else {
		        	throw new GETException("Inválido.Metódo diferente do método 'GET'.");
		        }
			}
			else
				throw new IOException();
		
		}
		catch(GETException | IOException excessao) {
			this.codigo_de_retorno = 400;
			verificarExtensaoArquivo("");
			resposta_final = montarMensagemResposta(criarCorpoDeEntidade(400));
		}
		catch(HttpException excessao) {
			this.codigo_de_retorno = 505;
			verificarExtensaoArquivo("");
			resposta_final = montarMensagemResposta(criarCorpoDeEntidade(505));
		}
		
		return resposta_final;
	}
	
}

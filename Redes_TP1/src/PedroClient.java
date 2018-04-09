import Cliente.ClienteTCP;

public class PedroClient {

	public static void main(String[] args) {
		
		int porta;
		
		/*
		 * Se não for especificado uma porta 
		 * o cliente enviará a requisição para a porta 80
		 */
		if(args.length == 1)
			porta = 80;
		else
			porta = Integer.parseInt(args[1]);
		
		ClienteTCP c = new ClienteTCP(args[0], porta);
		c.iniciarConexao();

	}

}

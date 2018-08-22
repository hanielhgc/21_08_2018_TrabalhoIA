package algoritmo;

public class Poupador extends ProgramaPoupador {
	
	public int acao() {
		
		int [] visao = sensor.getVisaoIdentificacao();
		
		return (int) (Math.random() * 5);
	}

}
package algoritmo;

public class Ladrao extends ProgramaLadrao {

	public int acao() {

		int direcao = (int) (Math.random() * 5);

		if ((direcao == 1 || direcao == 4)
				&& (sensor.getAmbienteOlfatoLadrao()[1] == 1 || sensor.getAmbienteOlfatoLadrao()[1] == 4)
				&& sensor.getAmbienteOlfatoLadrao()[1] != -1) {
			darVolta(direcao);
		}

		// adicionar metodo - se tentar ir para a direita e não conseguir: ir para baixo
		// adicionar metodo - se nao puder ir nem para a direita nem para baixo: ir para cima e para a esquerda X vezes

		System.out.println(sensor.getAmbienteOlfatoLadrao()[1]);

		return direcao;

	}

	public int darVolta(int direcao) {
		System.out.println("dar a volta!");

		direcao = 3;

		return 3;
	}

}
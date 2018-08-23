package algoritmo;

import java.util.concurrent.ThreadLocalRandom;

public class Ladrao extends ProgramaLadrao {

	public int acao() {

//		int direcao = (int) (Math.random() * 5);
//
//		if ((direcao == 1 || direcao == 4)
//				&& (sensor.getAmbienteOlfatoLadrao()[1] == 1 || sensor.getAmbienteOlfatoLadrao()[1] == 4)
//				&& sensor.getAmbienteOlfatoLadrao()[1] != -1) {
//			darVolta(direcao);
//		}
//
//		// adicionar metodo - se tentar ir para a direita e n�o conseguir: ir para baixo
//		// adicionar metodo - se nao puder ir nem para a direita nem para baixo: ir para cima e para a esquerda X vezes
//
//		System.out.println(sensor.getAmbienteOlfatoLadrao()[1]);
//
//		return direcao;

        int[] destino = definirDestino(sensor.getVisaoIdentificacao(), sensor.getAmbienteOlfatoLadrao());
        return andar(destino);

	}

	public int darVolta(int direcao) {
		System.out.println("dar a volta!");

		direcao = 3;

		return 3;
	}

	private int[] definirDestino(int[] visao, int[] olfato){
		int x = -2;
		int y = -2;

		for (int cod : visao) {
			if (isPoupador(cod)) {
				System.out.println("Viu poupador");
				return new int[]{x, y};
			}

			if (x < 5) {
				x++;
			} else {
				x = -2;
				y++;
			}
		}

		return new int[]{random(-2, 2), random(-2, 2)};
	}

	private int andar(int[] destino){
		int xDest = destino[0];
		int yDest = destino[1];
		if(xDest == 0){
			if(yDest > 0){
				return 1;
			}else{
				return 2;
			}
		} else {
			if(xDest > 0){
				return 3;
			}else{
				return 4;
			}
		}
	}

	private boolean isPoupador(int cod){
		return cod >= 100 && cod < 200;
	}

	/**
	 * Converte uma coordenada vinda da visão para uma coordenada local que possa ser usada pelo andar()
	 **/
	private int[] visaoParaLocal(int[] coordVisao){
		coordVisao[0] -= 2;
		coordVisao[1] -= 2;
		return coordVisao;
	}

	/**
	 * Converte uma coordenada vinda da visão para uma coordenada local que possa ser usada pelo andar()
	 **/
	private int[] olfatoParaLocal(int[] coordVisao){
		coordVisao[0] -= 1;
		coordVisao[1] -= 1;
		return coordVisao;
	}

	private int random(int min, int max){
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

}
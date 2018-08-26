package algoritmo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import controle.Constantes;

public class Poupador extends ProgramaPoupador {
	private final int dimensao = 30;

	// Matriz da Memória
	private Celula[][] matrizMemoria = new Celula[dimensao][dimensao];

	// Movimentos
	private final int mov_P = 0;
	private final int mov_C = 1;
	private final int mov_B = 2;
	private final int mov_D = 3;
	private final int mov_E = 4;

	private double notaTotal;

	// Qtds. da Percepção
	private int moedasPercepcao;
	private int pastilhasPercepcao;
	private int ladroesPercepcao;
	private int bancoPercepcao;

	public Poupador() {
		iniciarMatriz();
	}

    /**
     * Poupador Aleatório
     **/
//	public int acao() {
//
//		int [] visao = sensor.getVisaoIdentificacao();
//
//		return (int) (Math.random() * 5);
//	}

    /**
    * Poupador do Germano
     **/
	public int acao() {
		incrementarPeso(1);
		int movimentoEscolhido = decidirMovimento();
		resetDistanciasCelulas();
		return movimentoEscolhido;
	}

	public void resetDistanciasCelulas() {
		for (int i = 0; i < dimensao; i++) {
			for (int j = 0; j < dimensao; j++) {
				matrizMemoria[i][j].setDistanciaAgente(Integer.MAX_VALUE);
			}
		}
	}

	public void iniciarMatriz() {
		for (int i = 0; i < dimensao; i++) {
			for (int j = 0; j < dimensao; j++) {
				// Celula = x, y, peso, código (Código 7 = Espaço não observado)
				matrizMemoria[i][j] = new Celula(i, j, 1, 7);
			}
		}
	}

	public void printMatriz(Celula[][] matriz) {
		for (int i = 0; i < dimensao; i++) {
			for (int j = 0; j < dimensao; j++) {
				System.out.print(matriz[j][i].getCodigo() + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	private void incrementarPeso(int quantidade) {
		Point p = sensor.getPosicao();
		matrizMemoria[p.x][p.y].aumentarPeso(quantidade);
	}

	private int decidirMovimento() {
		ArrayList<Movimento> movimentos = new ArrayList<Movimento>();

		// Dois últimos valores dos construtores são seus "offsets" de
		// coordenada (Cima tem -1 no y, Direita tem +1 no x, etc.)
		Movimento movParado = new Movimento(mov_P, 0, 0);
		movimentos.add(movParado);

		if (isOnBounds(sensor.getVisaoIdentificacao()[7])) {
			Movimento movCima = new Movimento(mov_C, 0, -1);
			movimentos.add(movCima);
		}

		if (isOnBounds(sensor.getVisaoIdentificacao()[11])) {
			Movimento movEsquerda = new Movimento(mov_E, -1, 0);
			movimentos.add(movEsquerda);
		}

		if (isOnBounds(sensor.getVisaoIdentificacao()[12])) {
			Movimento movDireita = new Movimento(mov_D, 1, 0);
			movimentos.add(movDireita);
		}

		if (isOnBounds(sensor.getVisaoIdentificacao()[16])) {
			Movimento movBaixo = new Movimento(mov_B, 0, 1);
			movimentos.add(movBaixo);
		}

		atribuirCelulas(movimentos);

		this.observar(sensor.getVisaoIdentificacao(), movimentos);

		this.cheirar(sensor.getAmbienteOlfatoLadrao(), sensor.getAmbienteOlfatoPoupador());

		avaliarMovimentos(movimentos);

		Movimento movEscolhido = roleta(movimentos);

		return movEscolhido.getDirecao();
	}

	private boolean isOnBounds(int codigo) {
		if (codigo == -1 || codigo == 1) {
			return false;
		}
		return true;
	}

	private void atribuirCelulas(ArrayList<Movimento> movimentos) {
		Point p = sensor.getPosicao();
		for (Movimento movimento : movimentos) {
			movimento.setCelulaMov(matrizMemoria[p.x + movimento.getOffsetX()][p.y + movimento.getOffsetY()]);
		}
	}

	private void observar(int[] visao, ArrayList<Movimento> movimentos) {

		int posicaoEquivalente = 0;

		this.moedasPercepcao = 0;
		this.pastilhasPercepcao = 0;
		this.ladroesPercepcao = 0;
		this.bancoPercepcao = 0;

		int visaoX = -2;
		int visaoY = -2;

		Point p = sensor.getPosicao();

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				// Tratando onde o Poupador está:
				if (i == 2 && j == 2) {
					visaoX++;
					matrizMemoria[p.x][p.y].setCodigo(0);
					continue;
				}

				// Memorizando observações se tem visão do local:
				if (visao[posicaoEquivalente] >= 0) {
					matrizMemoria[p.x + visaoX][p.y + visaoY].setCodigo(visao[posicaoEquivalente]);
				}

				// Observou um Banco:
				if (visao[posicaoEquivalente] == 3) {
					bancoPercepcao++;
				}

				// Observou uma Moeda:
				if (visao[posicaoEquivalente] == 4) {
					moedasPercepcao++;
					for (Movimento movimento : movimentos) {
						int distAposMov = distancia(movimento.getOffsetX(), visaoX, movimento.getOffsetY(), visaoY);
						movimento.setManhattanMoedas(distAposMov + movimento.getManhattanMoedas());
					}
				}

				// Observou uma Pastilha:
				if (visao[posicaoEquivalente] == 5) {
					pastilhasPercepcao++;
					for (Movimento movimento : movimentos) {
						int distAposMov = distancia(movimento.getOffsetX(), visaoX, movimento.getOffsetY(), visaoY);
						movimento.setManhattanPastilhas(distAposMov + movimento.getManhattanPastilhas());

						// Atualizando distância da pastilha mais próxima.
						if (distAposMov < movimento.getMenorDistanciaPastilha()) {
							movimento.setMenorDistanciaPastilha(distAposMov);
						}
					}
				}

				// Observou um Ladrão:
				if (visao[posicaoEquivalente] >= 200) {
					ladroesPercepcao++;
					for (Movimento movimento : movimentos) {
						int distAposMov = distancia(movimento.getOffsetX(), visaoX, movimento.getOffsetY(), visaoY);
						movimento.setManhattanLadroes(distAposMov + movimento.getManhattanLadroes());

						// Atualizando distância do ladrão mais próximo
						if (distAposMov < movimento.getMenorDistanciaLadrao()) {
							movimento.setMenorDistanciaLadrao(distAposMov);
						}

					}
				}
				posicaoEquivalente++;
				visaoX++;
			}
			visaoY++;
			visaoX = -2;
		}

	}

	private void cheirar(int[] cheiroLadrao, int[] cheiroPoupador) {

		int olfatoX = -1;
		int olfatoY = -1;
		int posicaoEquivalente = 0;
		Point p = sensor.getPosicao();

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (i == 1 && j == 1) {
					olfatoX++;
					continue;
				}

				if (cheiroLadrao[posicaoEquivalente] != -1 || cheiroPoupador[posicaoEquivalente] != -1) {
					matrizMemoria[p.x + olfatoX][p.y + olfatoY].setCheiroLadrao(cheiroLadrao[posicaoEquivalente]);
					matrizMemoria[p.x + olfatoX][p.y + olfatoY].setCheiroPoupador(cheiroPoupador[posicaoEquivalente]);
				}

				olfatoX++;
				posicaoEquivalente++;
			}
			olfatoY++;
			olfatoX = -1;
		}

	}

	private int distancia(int x1, int x2, int y1, int y2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}


	private void avaliarMovimentos(ArrayList<Movimento> movimentos) {

		sort(movimentos);
		double somaPesos = getSomaPesosProporcional(movimentos);

		int iteracaoLista = 0;
		int ranking = 0;
		int pesoAnterior = 0;

		for (Movimento movimento : movimentos) {

			iteracaoLista++;
			if (movimento.getCelulaMov().getPeso() > pesoAnterior) {
				ranking = iteracaoLista;
			}

			ArrayList<Double> fatores = new ArrayList<>();

			pesoAnterior = movimento.getCelulaMov().getPeso();
			double fatorExploracao = calcularFatorExploracao(movimento, somaPesos, ranking);
			fatores.add(fatorExploracao);
			movimento.setFatorExploracao(fatorExploracao);

			double fatorMoedas = calcularFatorMoedas(movimento);
			fatores.add(fatorMoedas);
			movimento.setFatorMoedas(fatorMoedas);

			ArrayList<Celula> caminhoPastilha = buscaCaminhoPara(5);
			double fatorPastilha = calcularFatorPastilha(movimento, caminhoPastilha);
			fatores.add(fatorPastilha);
			movimento.setFatorPastilha(fatorPastilha);

			double fatorLadrao = calcularFatorLadrao(movimento);
			fatores.add(fatorLadrao);
			movimento.setFatorLadrao(fatorLadrao);

			ArrayList<Celula> caminhoBanco = buscaCaminhoPara(3);
			double fatorBancoGlobal = calcularFatorBancoGlobal(movimento, caminhoBanco);
			fatores.add(fatorBancoGlobal);
			movimento.setFatorBancoGlobal(fatorBancoGlobal);

			movimento.calcularNota(fatores);
		}
	}

	private double getSomaPesosProporcional(ArrayList<Movimento> movimentos) {
		double soma = 0;
		for (Movimento movimento : movimentos) {
			soma += 1 / (double) movimento.getCelulaMov().getPeso();
		}
		return soma;
	}

	private void sort(ArrayList<Movimento> movimentos) {
		Collections.sort(movimentos, new Comparator<Movimento>() {
			public int compare(Movimento m1, Movimento m2) {
				if (m1.getCelulaMov().getPeso() < m2.getCelulaMov().getPeso()) {
					return -1;
				} else if (m1.getCelulaMov().getPeso() > m2.getCelulaMov().getPeso()) {
					return 1;
				}
				return 0;
			}
		});
	}

	private double calcularFatorExploracao(Movimento movimento, double somaPesos, int ranking) {

		Celula movCelula = movimento.getCelulaMov();

		if (movCelula.getCheiroLadrao() > 0) {
			return (double) (movCelula.getCheiroLadrao()) / (double) movCelula.getPeso() / (double) somaPesos / (double) ranking;

		} else {
			// 6 = Máx. de Cheiro Ladrão + 1
			return 6 / (double) movCelula.getPeso() / (double) somaPesos / (double) ranking;
		}
	}

	private double calcularFatorMoedas(Movimento movimento) {
		if (this.moedasPercepcao > 0) {
			return Math.pow((double) (moedasPercepcao / ((double) movimento.getManhattanMoedas() + 1)), 1 / (double) moedasPercepcao);
		}
		return 1;
	}

	private double calcularFatorPastilha(Movimento movimento, ArrayList<Celula> caminhoPastilha) {

		if (movimento.getMenorDistanciaPastilha() == 0) {
			if (ladroesPercepcao == 0 || sensor.getNumeroJogadasImunes() > 1) {
				return 0;
			}
		}

		if (caminhoPastilha == null) {
			return 1;
		}

		if (caminhoPastilha.contains(movimento.getCelulaMov()) && ladroesPercepcao > 0) {

			return Math.pow((((double)sensor.getNumeroDeMoedas() + 1)/ (double)Constantes.custoPastinha), ladroesPercepcao) * caminhoPastilha.size() / (double)(sensor.getNumeroJogadasImunes() + 1);

		}

		return 1;

	}

	private double calcularFatorLadrao(Movimento movimento) {
		if (this.ladroesPercepcao > 0) {

			double fatorLadrao = Math.pow((double) movimento.getManhattanLadroes(), movimento.getMenorDistanciaLadrao()) * (movimento.getMenorDistanciaLadrao() - 1) / (double)(sensor.getNumeroJogadasImunes() + 1);
			if (fatorLadrao < 0) {
				return 0;
			}
			return fatorLadrao;

		} else {
			return 1;
		}
	}

	private double calcularFatorBancoGlobal (Movimento movimento, ArrayList<Celula> caminhoAteBanco) {
		if (caminhoAteBanco == null) {
			return 1;
		} else {

			if (caminhoAteBanco.contains(movimento.getCelulaMov())) {
				return (double)(sensor.getNumeroDeMoedas() + 1) / (double)(sensor.getNumeroDeMoedasBanco() + 1) * ((double)caminhoAteBanco.size() / (double)(moedasPercepcao + 1));
			} else {
				return 1;
			}
		}
	}

	private Movimento roleta(ArrayList<Movimento> movimentos) {
		notaTotal = getNotaTotal(movimentos);
		calcularChances(movimentos, notaTotal);
		return sortear(movimentos);
	}

	private double getNotaTotal(ArrayList<Movimento> movimentos) {

		notaTotal = 0;

		for (Movimento movimento : movimentos) {
			notaTotal += movimento.getNota();
		}

		return notaTotal;
	}

	private void calcularChances(ArrayList<Movimento> movimentos, double notaTotal) {
		// Aleatório se todos os movimentos tiverem nota 0.
		if (notaTotal == 0) {
			for (Movimento movimento : movimentos) {
				movimento.setChance(1 / movimentos.size());
			}
			return;
		}

		for (Movimento movimento : movimentos) {
			double chance = movimento.getNota() / notaTotal;
			movimento.setChance(chance);
		}
	}

	private Movimento sortear(ArrayList<Movimento> movimentos) {
		double numeroSorteado = new Random().nextDouble();
		double soma = 0;

		for (Movimento mov : movimentos) {
			soma += mov.getChance();
			if (soma > numeroSorteado) {
				return mov;
			}
		}
		// Retorna PARADO se Agente determinou que
		// todos os movimentos possíveis eram Nota 0
		// (Completamente irracionais).
		for (Movimento movimento : movimentos) {
			if (movimento.getDirecao() == 0) {
				return movimento;
			}
		}

		// Retorna null se Parado for inválido (???)
		return null;
	}

	private ArrayList<Celula> buscaCaminhoPara(int codigoAlvo) {
		Point p = sensor.getPosicao();

		// Inicializa com a célula do Agente
		Celula celulaAtual = matrizMemoria[p.x][p.y];
		celulaAtual.setDistanciaAgente(0);

		// Lista da borda da busca
		ArrayList<Celula> borda = new ArrayList<>();
		borda.add(celulaAtual);

		// Estrutura de Dados p/ Poda
		ArrayList<Celula> visitados = new ArrayList<>();
		visitados.add(celulaAtual);

		while (borda.size() > 0) {

			celulaAtual = borda.get(0);

			if (celulaAtual.getCodigo() == codigoAlvo) {
				// Achou!
				ArrayList<Celula> caminho = gerarCaminho(celulaAtual);
				return caminho;
			}

			inserirAdjacentes(celulaAtual, borda, visitados, codigoAlvo);

			borda.remove(celulaAtual);
		}

		// Retorna null se não achou caminho.
		return null;

	}

	private void inserirAdjacentes(Celula celulaCentral, ArrayList<Celula> borda, ArrayList<Celula> visitados, int codigoAlvo) {

		int distancia = celulaCentral.getDistanciaAgente();

		ArrayList<Celula> adjacentes = getAdjacentes(celulaCentral);

		for (Celula celula : adjacentes) {
			inserirSeNovo(celula, borda, visitados, distancia + 1, codigoAlvo);
		}

	}

	private ArrayList<Celula> getAdjacentes(Celula celulaCentral) {
		ArrayList<Celula> adjacentes = new ArrayList<>();
		int x = celulaCentral.getX();
		int y = celulaCentral.getY();

		// Cima
		if (y > 0 && isPathable(matrizMemoria[x][y - 1])) {
			adjacentes.add((matrizMemoria[x][y - 1]));
		}

		// Esquerda
		if (x > 0 && isPathable(matrizMemoria[x - 1][y])) {
			adjacentes.add((matrizMemoria[x - 1][y]));
		}

		// Direita
		if (x < dimensao - 1 && isPathable(matrizMemoria[x + 1][y])) {
			adjacentes.add((matrizMemoria[x + 1][y]));
		}

		// Baixo
		if (y < dimensao - 1 && isPathable(matrizMemoria[x][y + 1])) {
			adjacentes.add((matrizMemoria[x][y + 1]));
		}

		return adjacentes;
	}

	private boolean inserirSeNovo(Celula novaCelula, ArrayList<Celula> borda, ArrayList<Celula> visitados, int distancia, int codigoAlvo) {

		if (!(visitados.contains(novaCelula))) {
			novaCelula.setDistanciaAgente(distancia);
			visitados.add(novaCelula);
			// Checa na expansão se é o Alvo!
			if (novaCelula.getCodigo() == codigoAlvo) {
				borda.add(1, novaCelula);
			} else {
				borda.add(novaCelula);
			}
			return true;
		}
		return false;
	}

	private boolean isPathable(Celula celula) {

		// Filtrando com as condições:
		// - Não é parede;
		// - Não é fora do observado;

		if (celula.getCodigo() == 1 || celula.getCodigo() == 7) {
			return false;
		}

		return true;

	}

	private ArrayList<Celula> gerarCaminho(Celula celulaFim) {
		Celula celulaAtual = celulaFim;

		ArrayList<Celula> caminho = new ArrayList<>();

		caminho.add(celulaAtual);

		// > 1 para não incluir a célula do próprio Agente
		while (celulaAtual.getDistanciaAgente() > 1) {
			celulaAtual = getMenorDistancia(getAdjacentes(celulaAtual));
			caminho.add(celulaAtual);
		}

		return caminho;
	}



	private Celula getMenorDistancia(ArrayList<Celula> listaCelulas) {
		Celula celulaMenorDistancia = listaCelulas.get(0);

		for (Celula celula : listaCelulas) {
			if (celula.getDistanciaAgente() < celulaMenorDistancia.getDistanciaAgente()) {
				celulaMenorDistancia = celula;
			}
		}

		return celulaMenorDistancia;
	}

	public class Celula {
		private int x;
		private int y;
		private int peso;
		private int codigo;
		private int distanciaAgente;
		private int cheiroLadrao;
		private int cheiroPoupador;

		public Celula() {}

		public Celula(Point coordenada, int peso, int codigo) {
			this.x = coordenada.x;
			this.y = coordenada.y;
			this.peso = peso;
			this.codigo = codigo;
			this.distanciaAgente = Integer.MAX_VALUE;
			this.cheiroLadrao = 0;
			this.cheiroPoupador = 0;
		}

		public Celula(int x, int y, int distanciaAgente) {
			this.x = x;
			this.y = y;
			this.distanciaAgente = distanciaAgente;
		}

		public Celula(int x, int y, int peso, int codigo) {
			this.x = x;
			this.y = y;
			this.peso = peso;
			this.codigo = codigo;
			this.distanciaAgente = Integer.MAX_VALUE;
		}

		public void incrementarDistancia(int distancia) {
			this.distanciaAgente = distancia;
		}

		public void aumentarPeso (int quantidade) {
			this.peso += quantidade;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getPeso() {
			return peso;
		}

		public void setPeso(int peso) {
			this.peso = peso;
		}

		public int getCodigo() {
			return codigo;
		}

		public void setCodigo(int codigo) {
			this.codigo = codigo;
		}

		public int getDistanciaAgente() {
			return distanciaAgente;
		}

		public void setDistanciaAgente(int distanciaAgente) {
			this.distanciaAgente = distanciaAgente;
		}

		public int getCheiroLadrao() {
			return cheiroLadrao;
		}

		public void setCheiroLadrao(int cheiroLadrao) {
			this.cheiroLadrao = cheiroLadrao;
		}

		public int getCheiroPoupador() {
			return cheiroPoupador;
		}

		public void setCheiroPoupador(int cheiroPoupador) {
			this.cheiroPoupador = cheiroPoupador;
		}
	}

	public class Movimento {
		private int direcao;
		private double chance;
		private double nota;
		private int offsetX;
		private int offsetY;
		private Celula celulaMov;

		private int manhattanMoedas;
		private int manhattanPastilhas;
		private int manhattanLadroes;
		private int menorDistanciaLadrao;
		private int menorDistanciaPastilha;

		private double fatorExploracao;
		private double fatorMoedas;
		private double fatorPastilha;
		private double fatorLadrao;
		private double fatorBancoLocal;
		private double fatorBancoGlobal;

		public Movimento(int direcao, int offsetX, int offsetY) {
			this.direcao = direcao;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.nota = 1;
			this.chance = 0;
			this.manhattanMoedas = 0;
			this.manhattanPastilhas = 0;
			this.manhattanLadroes = 0;
			this.menorDistanciaLadrao = Integer.MAX_VALUE;
			this.menorDistanciaPastilha = Integer.MAX_VALUE;
			this.fatorExploracao = 1;
			this.fatorMoedas= 1;
			this.fatorPastilha= 1;
			this.fatorLadrao = 1;
			this.fatorBancoLocal = 1;
			this.fatorBancoGlobal = 1;
		}

		public void calcularNota (ArrayList<Double> fatores) {
			for (Double f : fatores) {
				this.nota *= f;
			}
		}

		public int getDirecao() {
			return direcao;
		}

		public void setDirecao(int direcao) {
			this.direcao = direcao;
		}

		public double getChance() {
			return chance;
		}

		public void setChance(double chance) {
			this.chance = chance;
		}

		public double getNota() {
			return nota;
		}

		public void setNota(double nota) {
			this.nota = nota;
		}

		public int getOffsetX() {
			return offsetX;
		}

		public void setOffsetX(int offsetX) {
			this.offsetX = offsetX;
		}

		public int getOffsetY() {
			return offsetY;
		}

		public void setOffsetY(int offsetY) {
			this.offsetY = offsetY;
		}

		public Celula getCelulaMov() {
			return celulaMov;
		}

		public void setCelulaMov(Celula celulaMov) {
			this.celulaMov = celulaMov;
		}

		public int getManhattanMoedas() {
			return manhattanMoedas;
		}

		public void setManhattanMoedas(int manhattanMoedas) {
			this.manhattanMoedas = manhattanMoedas;
		}

		public int getManhattanPastilhas() {
			return manhattanPastilhas;
		}

		public void setManhattanPastilhas(int manhattanPastilhas) {
			this.manhattanPastilhas = manhattanPastilhas;
		}

		public int getManhattanLadroes() {
			return manhattanLadroes;
		}

		public void setManhattanLadroes(int manhattanLadroes) {
			this.manhattanLadroes = manhattanLadroes;
		}

		public int getMenorDistanciaLadrao() {
			return menorDistanciaLadrao;
		}

		public void setMenorDistanciaLadrao(int menorDistanciaLadrao) {
			this.menorDistanciaLadrao = menorDistanciaLadrao;
		}

		public int getMenorDistanciaPastilha() {
			return menorDistanciaPastilha;
		}

		public void setMenorDistanciaPastilha(int menorDistanciaPastilha) {
			this.menorDistanciaPastilha = menorDistanciaPastilha;
		}

		public double getFatorExploracao() {
			return fatorExploracao;
		}

		public void setFatorExploracao(double fatorExploracao) {
			this.fatorExploracao = fatorExploracao;
		}

		public double getFatorMoedas() {
			return fatorMoedas;
		}

		public void setFatorMoedas(double fatorMoedas) {
			this.fatorMoedas = fatorMoedas;
		}

		public double getFatorPastilha() {
			return fatorPastilha;
		}

		public void setFatorPastilha(double fatorPastilha) {
			this.fatorPastilha = fatorPastilha;
		}

		public double getFatorLadrao() {
			return fatorLadrao;
		}

		public void setFatorLadrao(double fatorLadrao) {
			this.fatorLadrao = fatorLadrao;
		}

		public double getFatorBancoLocal() {
			return fatorBancoLocal;
		}

		public void setFatorBancoLocal(double fatorBancoLocal) {
			this.fatorBancoLocal = fatorBancoLocal;
		}

		public double getFatorBancoGlobal() {
			return fatorBancoGlobal;
		}

		public void setFatorBancoGlobal(double fatorBancoGlobal) {
			this.fatorBancoGlobal = fatorBancoGlobal;
		}

	}

}

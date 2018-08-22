package algoritmo;

public class Ladrao extends ProgramaLadrao {
	
	public int acao() {
		
		int direcao=(int) (Math.random() * 5);
		
		if(direcao ==1 && (sensor.getAmbienteOlfatoLadrao()[1]==1 ||sensor.getAmbienteOlfatoLadrao()[1]==4) ){
			darVolta();
		}
		
		
			System.out.println(sensor.getAmbienteOlfatoLadrao()[1]);
			
		
		return direcao;
	
	}
	
	public int darVolta(){
		return 3;
	}
	
	

}
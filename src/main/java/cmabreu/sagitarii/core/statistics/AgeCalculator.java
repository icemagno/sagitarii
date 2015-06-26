package cmabreu.sagitarii.core.statistics;

import java.util.ArrayList;
import java.util.List;

import cmabreu.sagitarii.core.delivery.DeliveryUnit;

public class AgeCalculator {
	private List<Accumulator> lista;
	private static AgeCalculator instance;
	
	private AgeCalculator() {
		lista = new ArrayList<Accumulator>();
	}
	
	public static AgeCalculator getInstance() {
		if( instance == null ) {
			instance = new AgeCalculator();
		}
		return instance;
	}
	
	public List<Accumulator> getList() {
		return new ArrayList<Accumulator>(lista);
	}
	

	public Accumulator getAccumulator( String hash ) {
		for ( Accumulator accumulator : lista  ) {
			if( accumulator.getHash().equals(hash)  ) {
				return accumulator;
			}
		}
		return null;
	}

	public void addToStatistics( DeliveryUnit du ) {
		boolean found = false;
		for ( Accumulator accumulator : lista  ) {
			if( accumulator.getHash().equals( du.getHash() )  ) {
				accumulator.addToStack( du );
				found = true;
			}
		}
		if ( !found ) {
			lista.add ( new Accumulator( du ) );
		}
	}
	
}
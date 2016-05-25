package unito.model;

import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;
import microsim.space.turtle.DigitalTurtle;
import microsim.statistics.IIntSource;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

// The Guava library allows to have table with two 'keys' -- the two coordinates -- and one value
import com.google.common.collect.HashBasedTable; 
import com.google.common.collect.Table;

@Entity
public class Firm extends DigitalTurtle implements EventListener, IIntSource{

	private static final long serialVersionUID = 8617322451447943042L;

	@Id
	private PanelEntityKey key;
	
	@Transient
	HotellingModel model;
	
	private int revenues;

	// ---------------------------------------------------------------------
	// Constructor	
	// ---------------------------------------------------------------------
	
	protected Firm(){
		super(null);
	}
	
	public Firm(HotellingModel model, int n, int x, int y){
		super(model.getFirmGrid(), x, y);
		
		this.model = model;
		key = new PanelEntityKey((long) n);
		this.revenues = 0;
	}
	
	
	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		Move,
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case Move:
			move();
			break;
		}
	}
	
	// ---------------------------------------------------------------------
	// IIntSource
	// ---------------------------------------------------------------------
	
	public enum Variables{
		Revenues;
	}

	@Override
	public int getIntValue(Enum<?> variable) {
		switch((Variables) variable){
			case Revenues:
				return revenues;
			default: 
				throw new IllegalArgumentException("Unsupported variable"); 		
		}
	}

	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------
	
	public void move() {
		Table<Integer, Integer, Integer> potentialConsumers = potentialConsumers();
		
		// different locations could yield an identical number of consumers (tie). These two loops are taking care of this
		Table<Integer, Integer, Integer> locationTableForMaxConsumers = HashBasedTable.create();	
		Table.Cell<Integer, Integer, Integer> maxEntry = null;
		
		// take one of the max
		for(Table.Cell<Integer, Integer, Integer> entry : potentialConsumers.cellSet()){
			if(maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0){
				maxEntry = entry;
			}	
		}
		locationTableForMaxConsumers.put(maxEntry.getRowKey(), maxEntry.getColumnKey(), maxEntry.getValue());
		
		// include all other locations yielding the same number of consumers
		for(Table.Cell<Integer, Integer, Integer> entry : potentialConsumers.cellSet()){
			if(entry.getValue().equals(maxEntry.getValue())){
				locationTableForMaxConsumers.put(maxEntry.getRowKey(), maxEntry.getColumnKey(), maxEntry.getValue());
			}	
		}
		
		Integer idealX = 0;
		Integer idealY = 0;
		
		if(locationTableForMaxConsumers.size()==1){
			idealX = maxEntry.getRowKey();
			idealY = maxEntry.getColumnKey();
		 }
		 else {
			 // if there is a tie, then firms pick their optimal location randomly
//			 Collection<Integer> consumers = new ArrayList<Integer>(locationTableForMaxConsumers.values());
//			 List<Integer> consumersAsList = new ArrayList<Integer>(consumers);

			 //XXX: I don't understand what the rndConsumers number is supposed to represent? The locationTableForMaxConsumers already contains positions with the same value of number of consumers.
//			 List<Integer> consumersAsList = new ArrayList<Integer>(locationTableForMaxConsumers.values());
//			 int rndConsumers = consumersAsList.get(SimulationEngine.getRnd().nextInt(locationTableForMaxConsumers.size())); 

			 //XXX: Don't all entries have the same values, equal to rndConsumers?  If so, this will always set X, Y to the last entry to be iterated over...? 
//			for(Table.Cell<Integer, Integer, Integer> entry : locationTableForMaxConsumers.cellSet()){
//				if(entry.getValue().equals(rndConsumers)){
//					idealX = entry.getRowKey();
//					idealY = entry.getColumnKey();
//				}
//			}

			int randomTableIndex = SimulationEngine.getRnd().nextInt(locationTableForMaxConsumers.size());
			int count = 0;
			for(Table.Cell<Integer, Integer, Integer> entry : locationTableForMaxConsumers.cellSet()){
				if(count == randomTableIndex){
					idealX = entry.getRowKey();
					idealY = entry.getColumnKey();
					break;		//No need to iterate through all entries
				}
				count++;
			}
		 }
		setXY(idealX, idealY);
	}
	
	public Table<Integer, Integer, Integer> potentialConsumers(){
		Integer xx = getX();
		Integer yy = getY();
		int potentialConsumerCounter;
		Table<Integer, Integer, Integer> expectedConsumers = HashBasedTable.create();
		
		// firms go in each of the eight positions around their current location and compute the number of consumers they would have
		// XXX: Actually, this checks all 9 positions, including the starting position.  Check Extended Schelling model to see how they implement a similar grid search, spiralling out to different lengths.
		for(int i = Math.max(0, xx - 1); i <= Math.min(model.getxSize() - 1, xx + 1); i++){
			for (int j = Math.max(0, yy - 1); j <= Math.min(model.getySize() - 1, yy + 1); j++){
				
				this.setXY(i, j);
				potentialConsumerCounter = 0;
//				for(int ii = 0; ii < model.getConsumerList().size(); ii++){
//					if(model.getConsumerList().get(ii).closestFirm().equals(this)){
//						potentialConsumerCounter ++;
//					 }
//				}
				for(Consumer consumer : model.getConsumerList()){
					if(consumer.closestFirm().equals(this)){
						potentialConsumerCounter ++;
					}
				}
				
				// ensure that if two firms are on the same spot, they share the associated number of consumers
				
				// XXX: Will this if check ever be false if the location of this firm is currently at (i,j)?  I haven't seen it be false yet!  If it's always true, can we remove the check, or do you need to change some logic to get what you intended? 
				if(model.getFirmGrid().get(i, j) != null){
					int nbOfFirmsHere = 0;
//					for(int ii = 0; ii < model.getNumberOfFirms(); ii++){
//						if(model.getFirmList().get(ii).getPosition().equals(model.getFirmGrid().get(i, j))){
//							nbOfFirmsHere ++;
//						}
//					}
					for(Firm firm : model.getFirmList()){	
						if(firm.getPosition().equals(model.getFirmGrid().get(i, j))){
							nbOfFirmsHere ++;
						}
					}
					potentialConsumerCounter = potentialConsumerCounter / nbOfFirmsHere;
					
					if(i == xx && j == yy)
						revenues = potentialConsumerCounter;
					
					expectedConsumers.put(getX(), getY(), potentialConsumerCounter);
				} 
				else System.out.println("It's empty!!!");	//XXX: CHECK to see if this ever occurs.  Remove when satisfied it is working as expected.
			}
		}
		
		return expectedConsumers;
	}
	
	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public void setModel(HotellingModel model) {this.model = model; }
	public HotellingModel getModel() { return model; }
		
	public void setRevenues(int revenues){ this.revenues = revenues; }
	public int getRevenues(){ return revenues; }
	
//	public int getFirmIndex(){
//		return model.getFirmList().indexOf(this);
//	}

	public PanelEntityKey getKey() {
		return key;
	}

	public void setKey(PanelEntityKey key) {
		this.key = key;
	}

}
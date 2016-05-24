package unito.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class Firm extends DigitalTurtle implements EventListener, IIntSource, Serializable{
	
	 private static final long serialVersionUID = 7731999082865205069L;

	@Id
	private PanelEntityKey id;
	
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
		id = new PanelEntityKey();
		id.setId((long) n);
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
		
		// different locations could yield identical number of consumers (tie). These two loops are taking care of this
		Table<Integer, Integer, Integer> maxConsumersTable = HashBasedTable.create();	
		Table.Cell<Integer, Integer, Integer> maxEntry = null;
		
		// take one of the max
		for(Table.Cell<Integer, Integer, Integer> entry : potentialConsumers.cellSet()){
			if(maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0){
				maxEntry = entry;
			}	
		}
		maxConsumersTable.put(maxEntry.getRowKey(), maxEntry.getColumnKey(), maxEntry.getValue());
		
		// include all the other locations yielding the same number of consumers
		for(Table.Cell<Integer, Integer, Integer> entry : potentialConsumers.cellSet()){
			if(entry.getValue().equals(maxEntry.getValue())){
				maxConsumersTable.put(maxEntry.getRowKey(), maxEntry.getColumnKey(), maxEntry.getValue());
			}	
		}
		
		Integer idealX = 0;
		Integer idealY = 0;
		
		if(maxConsumersTable.size()==1){
			idealX = maxEntry.getRowKey();
			idealY = maxEntry.getColumnKey();
		 }
		 else {
			 // if there is a tie, then firms pick their optimal location randomly
			 Collection<Integer> consumers = new ArrayList<Integer>(maxConsumersTable.values());
			 List<Integer> consumersAsList = new ArrayList<Integer>(consumers);
			 int rndConsumers = consumersAsList.get(SimulationEngine.getRnd().nextInt(maxConsumersTable.size())); 
						
			for(Table.Cell<Integer, Integer, Integer> entry : maxConsumersTable.cellSet()){
				if(entry.getValue().equals(rndConsumers)){
					idealX = entry.getRowKey();
					idealY = entry.getColumnKey();
				}
			}
		 }
		setXY(idealX, idealY);
	}
	
	public Table<Integer, Integer, Integer> potentialConsumers(){
		Integer xx = getX();
		Integer yy = getY();
		int potentialConsumerCounter;
		Table<Integer, Integer, Integer> expectedConsumers = HashBasedTable.create();
		
		// firms go in each of the eight patches around its location and compute the number of consumers he would have
		for(int i = Math.max(0, xx - 1); i <= Math.min(model.getxSize() - 1, xx + 1); i++){
			for (int j = Math.max(0, yy - 1); j <= Math.min(model.getySize() - 1, yy + 1); j++){
				
				this.setXY(i, j);
				potentialConsumerCounter = 0;
				for(int ii = 0; ii < model.getConsumerList().size(); ii++){
					if(model.getConsumerList().get(ii).closestFirm().equals(this)){
						potentialConsumerCounter ++;
					 }
				 }
				
				// ensure that if two firms are on the same spot, they share the associated number of consumers
				if(model.getFirmGrid().get(i, j) != null){
					int nbOfFirmsHere = 0;
					for(int ii = 0; ii < model.getNumberOfFirms(); ii++){
						if(model.getFirmList().get(ii).getPosition().equals(model.getFirmGrid().get(i, j))){
							nbOfFirmsHere ++;
						}
					}
					potentialConsumerCounter = potentialConsumerCounter / nbOfFirmsHere;
					
					if(i == xx && j == yy)
						revenues = potentialConsumerCounter;
					
					expectedConsumers.put(getX(), getY(), potentialConsumerCounter);
				}
			}
		}
		
		return expectedConsumers;
	}
	
	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public void setModel(HotellingModel model) {this.model = model; }
	public HotellingModel getModel() { return model; }
	
	public void setId(PanelEntityKey id) {this.id = id; }
	public PanelEntityKey getId() { return id; }
	
	public void setRevenues(int revenues){ this.revenues = revenues; }
	public int getRevenues(){ return revenues; }
	
	public int getFirmIndex(){
		return model.getFirmList().indexOf(this);
	}

}
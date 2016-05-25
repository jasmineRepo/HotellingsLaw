package unito.model;

import java.util.HashMap;
import java.util.Map;

import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;
import microsim.space.turtle.DigitalTurtle;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Consumer extends DigitalTurtle implements EventListener {
	
	private static final long serialVersionUID = 1219220590143482752L;

	@Id
	private PanelEntityKey key;
	
	@Transient
	private HotellingModel model;
	
	@Transient
	private Firm chosenFirm;
	
	private int chosenFirmAsInt; // used to create a color map indicating the preference of each consumer, see Observer

	// ---------------------------------------------------------------------
	// Constructor	
	// ---------------------------------------------------------------------
	
	protected Consumer(){
		super(null);
	}
	
	public Consumer(HotellingModel model, int n, int x, int y){
		super(model.getConsumerGrid(), x, y);
		
		this.model = model;
		key = new PanelEntityKey((long) n);
		
		this.chosenFirm = null;
		this.chosenFirmAsInt = 0;
	}
	
	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		Consume,
	    ClosestFirm;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		
		case ClosestFirm:
			closestFirm();
			break;
			
		case Consume:
			consume();
			break;
		}
	}

	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------

	public Firm closestFirm(){
		 // this method is used by firms to compute their expected consumers if they were to change their location
		 
		Map<Firm, Integer> distance = distance(getX(), getY());
			
		// different firms could be located at identical distance (tie). These two loops are taking care of this
		Map<Firm, Integer> closestFirms = new HashMap<>(); 
		Map.Entry<Firm, Integer> minEntry = null;
		
		// take one of the min
		for(Map.Entry<Firm, Integer> entry : distance.entrySet() ){
			if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0){
				minEntry = entry;
			} 
		}
		closestFirms.put(minEntry.getKey(), minEntry.getValue());
		
		// include all other firms located at the same distance 
		for(Map.Entry<Firm, Integer> entry : distance.entrySet() ){
			if(entry.getValue().equals(minEntry.getValue()))
				closestFirms.put(entry.getKey(), entry.getValue());
		}
		
		Firm closestFirm;
		
		if(closestFirms.keySet().size() == 1){
			closestFirm = closestFirms.keySet().toArray(new Firm[0])[0];
		} else {
			// if there is a tie, then consumers pick their preferred stores randomly
			closestFirm = closestFirms.keySet().toArray(new Firm[closestFirms.size()])[SimulationEngine.getRnd().nextInt(closestFirms.size())];
		}
			
		return closestFirm;
	}
	
	public void consume(){
		// this method is used once firms have actually changed their location
		this.chosenFirm = closestFirm();
		
//		int chosenFirmAsInt = 0;
//		
//		for(int i = 0; i < model.getNumberOfFirms(); i++){
//			if(chosenFirm.equals(model.getFirmList().get(i))){
//				chosenFirmAsInt = i;
//			}
//		}
		this.chosenFirmAsInt = (int)chosenFirm.getKey().getId();
	}
	
	public Map<Firm, Integer> distance(int xx, int yy){
		Map<Firm, Integer> distance = new HashMap<>();
		
//		for(int i = 0; i < model.getNumberOfFirms(); i++){
//			int xFirm = model.getFirmList().get(i).getX();
//			int yFirm = model.getFirmList().get(i).getY();
//			int xDistance = Math.abs(xFirm - xx);
//			int yDistance = Math.abs(yFirm - yy);
//			int maxDistance = Math.max(xDistance, yDistance);
//			distance.put(model.getFirmList().get(i), maxDistance);
//		}
		for(Firm firm : model.getFirmList()){
			int xFirm = firm.getX();
			int yFirm = firm.getY();
			int xDistance = Math.abs(xFirm - xx);
			int yDistance = Math.abs(yFirm - yy);
			int maxDistance = Math.max(xDistance, yDistance);
			distance.put(firm, maxDistance);
		}
		
		return distance;
	}

	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public void setModel(HotellingModel model) {this.model = model; }
	public HotellingModel getModel() { return model; }
	
	public void setChosenFirm(Firm chosenFirm) { this.chosenFirm = chosenFirm; }
	public Firm getChosenFirm() { return chosenFirm; }
	
	public void setChosenFirmAsInt(Integer chosenFirmAsInt) { this.chosenFirmAsInt = chosenFirmAsInt; }
	public int getChosenFirmAsInt(){ return chosenFirmAsInt; }

	public PanelEntityKey getKey() {
		return key;
	}

	public void setKey(PanelEntityKey key) {
		this.key = key;
	}
	
}
package unito.model;

import microsim.engine.AbstractSimulationManager;
import microsim.engine.SimulationEngine;
import microsim.annotation.GUIparameter;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SystemEventType;
import microsim.space.DenseObjectSpace;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class HotellingModel extends AbstractSimulationManager implements EventListener {

	private final static Logger log = Logger.getLogger(HotellingModel.class);

	@GUIparameter
	Integer numberOfFirms = 2;
	
	@GUIparameter
	Integer endTime = 200;
	
	@GUIparameter
	Integer xSize = 50;
	
	@GUIparameter
	Integer ySize = 50;
	
	@GUIparameter
	Boolean colorSurface = false;
	
	public List<Firm> firmList;
	public List<Consumer> consumerList;
	
	DenseObjectSpace firmGrid;
	DenseObjectSpace consumerGrid;

	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		}
	}

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {
		
		firmGrid = new DenseObjectSpace(xSize, ySize);
		consumerGrid = new DenseObjectSpace(xSize, ySize);
		
		firmList = new ArrayList<Firm>();
		consumerList = new ArrayList<Consumer>();
		
		// allows for two firms to be initially on the same spot
		for(int i = 0; i < numberOfFirms; i++){
					int x = SimulationEngine.getRnd().nextInt(xSize);
					int y = SimulationEngine.getRnd().nextInt(ySize);
					Firm firm = new Firm(this, i, x, y);
					firmList.add(firm);
		}
			
		// consumers are uniformly distributed across the grid
		for(int i = 0; i < xSize*ySize; i++){
			boolean done = false;
			while(!done){
				int x = SimulationEngine.getRnd().nextInt(xSize);
				int y = SimulationEngine.getRnd().nextInt(ySize);
				if(consumerGrid.get(x, y) == null){
					Consumer consumer = new Consumer(this, i, x, y);
					consumerList.add(consumer);
					done = true;
				}	
			}
		}
		log.debug("Model objects created");
	}

	public void buildSchedule() {
		EventGroup eventGroup = new EventGroup();
		
		eventGroup.addCollectionEvent(consumerList, Consumer.Processes.ClosestFirm);
		eventGroup.addCollectionEvent(firmList, Firm.Processes.Move);
		eventGroup.addCollectionEvent(consumerList, Consumer.Processes.Consume);

		getEngine().getEventList().scheduleRepeat(eventGroup, 0., 0, 1.);
		getEngine().getEventList().scheduleSystem(endTime, Order.AFTER_ALL.getOrdering(), 0., getEngine(), SystemEventType.Stop);
		
		log.debug("Model schedule created");
	}

	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public Integer getNumberOfFirms() { return numberOfFirms; }
	public void setNumberOfFirms(Integer numberOfFirms) { this.numberOfFirms = numberOfFirms; }
	
	public void setFirmGrid(DenseObjectSpace firmGrid) {this.firmGrid = firmGrid; }
	public DenseObjectSpace getFirmGrid() {return firmGrid; }
	
	public void setConsumerGrid(DenseObjectSpace consumerGrid) { this.consumerGrid = consumerGrid; }
	public DenseObjectSpace getConsumerGrid(){ return consumerGrid; }
	
	public Integer getxSize() {return xSize; }
	public void setxSize(Integer xSize) {this.xSize = xSize; }
	public Integer getySize() {return ySize; }
	public void setySize(Integer ySize) {this.ySize = ySize; }
	
	public List<Firm> getFirmList() {return firmList; }
	public void setFirmList(List<Firm> firmList ) {this.firmList = firmList; }
	
	public void setConsumerList(List<Consumer> consumerList) { this.consumerList = consumerList; }
	public List<Consumer> getConsumerList() { return consumerList; }
	
	public void setColorSurface(Boolean colorSurface){ this.colorSurface = colorSurface; }
	public Boolean getColorSurface(){ return colorSurface; }
	
	public void setEndTime(Integer endTime){ this.endTime = endTime; }
	public Integer getEndTime(){ return endTime; }

}
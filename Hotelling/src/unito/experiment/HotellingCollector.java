package unito.experiment;

import microsim.data.db.DatabaseUtils;
import microsim.engine.AbstractSimulationCollectorManager;
import microsim.engine.SimulationEngine;
import microsim.engine.SimulationManager;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.statistics.CrossSection;
import microsim.statistics.functions.MeanArrayFunction;

import org.apache.log4j.Logger;

import unito.model.Firm;
import unito.model.HotellingModel;

public class HotellingCollector extends AbstractSimulationCollectorManager implements EventListener {

	private final static Logger log = Logger.getLogger(HotellingCollector.class);
	
	public CrossSection.Integer csRevenues;
	public MeanArrayFunction fMeanRevenues;
	
	public HotellingCollector(SimulationManager manager) {
		super(manager);
	}

	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		DumpFirm,
		DumpConsumer,
		Update;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

		case DumpFirm:
			try {

				DatabaseUtils.snap(DatabaseUtils.getOutEntityManger(),
						(long) SimulationEngine.getInstance().getCurrentRunNumber(),
						getEngine().getTime(),
						((HotellingModel) getManager()).firmList); 
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			break;
		case DumpConsumer:
			try {

				DatabaseUtils.snap(DatabaseUtils.getOutEntityManger(),
						(long) SimulationEngine.getInstance().getCurrentRunNumber(),
						getEngine().getTime(),
						((HotellingModel) getManager()).consumerList); 
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			break;
			
		case Update:
			csRevenues.updateSource();
			fMeanRevenues.updateSource();
			break;

		}
	}

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {
		csRevenues = new CrossSection.Integer(((HotellingModel) getManager()).getFirmList(), Firm.class, "getRevenues", true); 
		fMeanRevenues = new MeanArrayFunction(csRevenues);
	} 
	
	public void buildSchedule() {

		EventGroup eventGroup = new EventGroup();

		eventGroup.addEvent(this, Processes.DumpConsumer);
		eventGroup.addEvent(this, Processes.DumpFirm);
		eventGroup.addEvent(this, Processes.Update);

		getEngine().getEventList().scheduleRepeat(eventGroup, 0., Order.AFTER_ALL.getOrdering()-1, 1.);

	}

}
package unito.experiment;

import microsim.annotation.GUIparameter;
import microsim.data.DataExport;
import microsim.engine.AbstractSimulationCollectorManager;
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
	
	@GUIparameter
	private boolean exportToCSV = false;
	
	@GUIparameter
	private boolean persistToDatabase = false;
	
	private DataExport firmData;
	private DataExport consumerData;
	
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
			firmData.export();

		case DumpConsumer:
			consumerData.export();
			
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
		
		if(exportToCSV || persistToDatabase) {
			firmData = new DataExport(((HotellingModel) getManager()).firmList, persistToDatabase, exportToCSV);
			consumerData = new DataExport(((HotellingModel) getManager()).consumerList, persistToDatabase, exportToCSV);
		}		
	} 
	
	public void buildSchedule() {

		if(exportToCSV || persistToDatabase) {
			EventGroup eventGroup = new EventGroup();
	
			eventGroup.addEvent(this, Processes.DumpConsumer);
			eventGroup.addEvent(this, Processes.DumpFirm);
			eventGroup.addEvent(this, Processes.Update);
	
			getEngine().getEventQueue().scheduleRepeat(eventGroup, 0., Order.AFTER_ALL.getOrdering()-1, 1.);
		}
	}

	// Access Methods
	
	public boolean isPersistToDatabase() {
		return persistToDatabase;
	}

	public void setPersistToDatabase(boolean persistToDatabase) {
		this.persistToDatabase = persistToDatabase;
	}

	public boolean isExportToCSV() {
		return exportToCSV;
	}

	public void setExportToCSV(boolean exportToCSV) {
		this.exportToCSV = exportToCSV;
	}

}
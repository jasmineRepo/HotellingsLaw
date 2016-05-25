package unito.experiment;

import java.awt.Color;

import microsim.annotation.GUIparameter;
import microsim.engine.AbstractSimulationObserverManager;
import microsim.engine.SimulationCollectorManager;
import microsim.engine.SimulationManager;
import microsim.event.CommonEventType;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.gui.GuiUtils;
import microsim.gui.colormap.FixedColorMap;
import microsim.gui.plot.TimeSeriesSimulationPlotter;
import microsim.gui.space.LayerObjectGridDrawer;
import microsim.gui.space.LayeredSurfaceFrame;
import microsim.statistics.IIntSource;
import microsim.statistics.functions.MultiTraceFunction;

import org.apache.log4j.Logger;

import unito.model.*;

public class HotellingObserver extends AbstractSimulationObserverManager implements EventListener {

	private final static Logger log = Logger.getLogger(HotellingObserver.class);
	
	@GUIparameter
	private boolean showGraphs = true;
	
	private LayeredSurfaceFrame map; 
	private FixedColorMap consumerColorMap;
	private TimeSeriesSimulationPlotter meanRevenuesPlotter, csRevenuesPlotter;

	public HotellingObserver(SimulationManager manager, SimulationCollectorManager collectorManager) {
		super(manager, collectorManager);
	}


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
		
		if(showGraphs) {
			final HotellingModel model = (HotellingModel) getManager();
			final HotellingCollector collector = (HotellingCollector) getCollectorManager();
			
			// MAPS/GRIDS
			
			consumerColorMap = new FixedColorMap(model.getNumberOfFirms());
			LayerObjectGridDrawer firms = null;
			
			int step = 200 / model.getNumberOfFirms();
			
			for(int i = 0; i < model.getNumberOfFirms(); i++){
				consumerColorMap.addColor(i, i*step + 5, i*step + 30, i*step + 50);
			}
				
			if(model.getColorSurface())
				 firms = new LayerObjectGridDrawer("firms", model.getFirmGrid(), Color.red);
			else 
				 firms = new LayerObjectGridDrawer("firms", model.getFirmGrid(), Color.black);
				
			LayerObjectGridDrawer consumers = new LayerObjectGridDrawer("consumers", model.getConsumerGrid(), Color.gray);
			LayerObjectGridDrawer consumerPreference = new LayerObjectGridDrawer("consumerPreference", model.getConsumerGrid(), Consumer.class, "chosenFirmAsInt", false, consumerColorMap);
				
			map = new LayeredSurfaceFrame(model.getxSize(), model.getySize(), 8);
			map.setTitle("map");
				
			if(model.getColorSurface()){
				map.addLayer(consumerPreference);
			} else {
				map.addLayer(consumers);
			}
				
			map.addLayer(firms);
			GuiUtils.addWindow(map, 0, 150, model.getxSize()*8+10, model.getySize()*8+30);
			
			// PLOTS
			
			meanRevenuesPlotter = new TimeSeriesSimulationPlotter("mean revenues", "revenues");
			meanRevenuesPlotter.addSeries("", collector.fMeanRevenues);
			GuiUtils.addWindow(meanRevenuesPlotter, 420, 360, 700, 300);
			
			// one time series per firm
			csRevenuesPlotter = new TimeSeriesSimulationPlotter("firms' revenues", "revenues");
			for(Firm firm : model.getFirmList()){
				csRevenuesPlotter.addSeries("Firm " + firm.getKey().getId(), (IIntSource) new MultiTraceFunction.Integer(firm, Firm.Variables.Revenues));
			}
			GuiUtils.addWindow(csRevenuesPlotter, 420, 60, 700, 300); 
			
			log.debug("Observer objects created");
		}
	}

	public void buildSchedule() {
		
		if(showGraphs) {
			EventGroup eventGroup = new EventGroup();
	
		    eventGroup.addEvent(map, CommonEventType.Update);
		    eventGroup.addEvent(meanRevenuesPlotter, CommonEventType.Update);
		    eventGroup.addEvent(csRevenuesPlotter, CommonEventType.Update);
	
			getEngine().getEventList().scheduleRepeat(eventGroup, 0., Order.AFTER_ALL.getOrdering()-1, 1.);
			
			log.debug("Observer schedule created");
		}
	}

	// Access Methods
	
	public boolean isShowGraphs() {
		return showGraphs;
	}

	public void setShowGraphs(boolean showGraphs) {
		this.showGraphs = showGraphs;
	}
}

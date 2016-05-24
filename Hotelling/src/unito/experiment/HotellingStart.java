package unito.experiment;

import microsim.engine.ExperimentBuilder;
import microsim.engine.SimulationEngine;
import microsim.gui.shell.MicrosimShell;
import unito.model.HotellingModel;
import unito.experiment.HotellingCollector;
import unito.experiment.HotellingObserver;

public class HotellingStart implements ExperimentBuilder {

	public static void main(String[] args) {
		boolean showGui = true;

		SimulationEngine engine = SimulationEngine.getInstance();
		MicrosimShell gui = null;
		if (showGui) {
			gui = new MicrosimShell(engine);
			gui.setVisible(true);
		}

		HotellingStart experimentBuilder = new HotellingStart();
		engine.setExperimentBuilder(experimentBuilder);

		engine.setup();
	}

	public void buildExperiment(SimulationEngine engine) {
		HotellingModel model = new HotellingModel();
		HotellingCollector collector = new HotellingCollector(model);
		HotellingObserver observer = new HotellingObserver(model, collector);

		engine.addSimulationManager(model);
		engine.addSimulationManager(collector);
		engine.addSimulationManager(observer);	
	}
}

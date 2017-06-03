package com.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.Beans.GreenHouse;
import com.Beans.OptimalConditions;
import com.Beans.Plant;
import com.Scheduler.Data;

@ManagedBean(name = "MonitoringController")
@SessionScoped
public class MonitoringController {

	// Properties
	private GreenHouse selectedGreenHouse = new GreenHouse();
	private List<String> greenHouses = new ArrayList<>();
	private LineChartModel temperatureModel;
	private LineChartModel moistureModel;

	private final static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy hh:mm:ss");

	// Methods

	@PostConstruct
	public void init() throws OWLOntologyCreationException {
		getGreenHouses();
		initTemperatureModel();
	}

	public void onGreenHouseChange() throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("/home/andra/git/ontology-frontend/Files/knowledge.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/semantics");
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);
		reasoner.precomputeInferences();

		OWLNamedIndividual greenHouseInd = factory.getOWLNamedIndividual(ontologyIRI + "#" + selectedGreenHouse);
		OWLObjectProperty growIn = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#growIn"));

		NodeSet<OWLNamedIndividual> growValuesNodeSet = reasoner.getObjectPropertyValues(greenHouseInd, growIn);
		Set<OWLNamedIndividual> values2 = growValuesNodeSet.getFlattened();
		System.out.println("The Grow property values for GreenHouse1:");
		for (OWLNamedIndividual ind : values2) {
			System.out.println("    " + ind);
		}

	}

	public List<String> getGreenHouses() throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("/home/andra/git/ontology-frontend/Files/knowledge.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/semantics");
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);
		reasoner.precomputeInferences();

		// populate green houses combo
		OWLClass greenHouse = factory.getOWLClass(IRI.create(ontologyIRI + "#GreenHouse"));
		NodeSet<OWLNamedIndividual> ghsNodeSet = reasoner.getInstances(greenHouse, true);
		Set<OWLNamedIndividual> ghIndividuals = ghsNodeSet.getFlattened();
		System.out.println("Instances of greenHouses: ");
		for (OWLNamedIndividual ind : ghIndividuals) {
			if (!greenHouses.contains(ind.getIRI().getShortForm().toString()))
				greenHouses.add(ind.getIRI().getShortForm().toString());

		}

		String firstGreenHouseName = greenHouses.get(0);
		selectedGreenHouse = getGreenHouseByName(firstGreenHouseName);

		return greenHouses;
	}

	public GreenHouse getGreenHouseByName(String greenHouseName) throws OWLOntologyCreationException {
		GreenHouse house = new GreenHouse();
		Plant plant = new Plant();
		house.setGreenHouseName(greenHouseName);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("/home/andra/git/ontology-frontend/Files/knowledge.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/semantics");
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);
		reasoner.precomputeInferences();

		OWLNamedIndividual houseInd = factory.getOWLNamedIndividual(ontologyIRI + "#" + greenHouseName);
		OWLObjectProperty grow = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#grow"));
		NodeSet<OWLNamedIndividual> houseValues = reasoner.getObjectPropertyValues(houseInd, grow);
		Set<OWLNamedIndividual> values = houseValues.getFlattened();
		for (OWLNamedIndividual gh : values) {
			String plantName = gh.getIRI().getShortForm().toString();
			plant.setName(plantName);
			// TODO set plant optimal conditions

			OWLNamedIndividual plantInd = factory.getOWLNamedIndividual(ontologyIRI + "#" + plantName);
			OWLObjectProperty has = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#has"));
			NodeSet<OWLNamedIndividual> condValues = reasoner.getObjectPropertyValues(plantInd, has);
			Set<OWLNamedIndividual> conditions = condValues.getFlattened();
			for (OWLNamedIndividual c : conditions) {
				OptimalConditions oc = getJavaConditions(myOntology, c);
				plant.setOptimalConditions(oc);
				house.setPlant(plant);
			}
		}

		return house;
	}

	private OptimalConditions getJavaConditions(OWLOntology myOntology, OWLNamedIndividual c) {
		OptimalConditions cond = new OptimalConditions();

		Set<OWLDataPropertyAssertionAxiom> properties = myOntology.getDataPropertyAssertionAxioms(c);
		for (OWLDataPropertyAssertionAxiom pa : properties) {
			OWLDataProperty p = (OWLDataProperty) pa.getProperty();
			String xx = p.getIRI().getShortForm().toString();
			OWLLiteral v = pa.getObject();

			if (xx.equals("Tmin"))
				cond.setMinTemperature(Integer.parseInt(v.getLiteral()));
			if (xx.equals("Tmax"))
				cond.setMaxTemperature(Integer.parseInt(v.getLiteral()));
			if (xx.equals("Hmin"))
				cond.setMinMoisture(Integer.parseInt(v.getLiteral()));
			if (xx.equals("Hmax"))
				cond.setMaxMoisture(Integer.parseInt(v.getLiteral()));

		}

		return cond;
	}

	// Getters and Setters
	public GreenHouse getSelectedGreenHouse() {
		return selectedGreenHouse;
	}

	public void setSelectedGreenHouse(GreenHouse greenHouse) {
		this.selectedGreenHouse = greenHouse;
	}

	public void setGreenHouses(List<String> greenHouses) {
		this.greenHouses = greenHouses;
	}

	public LineChartModel getTemperatureModel() {

		return temperatureModel;
	}

	public void setTemperatureModel(LineChartModel temperatureModel) {
		this.temperatureModel = temperatureModel;
	}

	public LineChartModel getMoistureModel() {
		int minH = selectedGreenHouse.getPlant().getOptimalConditions().getMinMoisture();
		int maxH = selectedGreenHouse.getPlant().getOptimalConditions().getMaxMoisture();

		moistureModel = new LineChartModel();

		moistureModel.setTitle("Humidity representation");
		moistureModel.setLegendPosition("e");

		LineChartSeries series1 = new LineChartSeries();
		series1.setLabel("Maximum Reccomended");
		series1.set("2014-01-01 09:00:00", maxH);
		series1.set("2014-01-01 18:00:00", maxH);

		LineChartSeries series2 = new LineChartSeries();
		series2.setLabel("Actual Humidity");
		series2.set("2014-01-01 09:00:00", 66);
		series2.set("2014-01-01 10:00:00", 66);
		series2.set("2014-01-01 11:00:00", 67);
		series2.set("2014-01-01 12:00:00", 68);
		series2.set("2014-01-01 13:00:00", 69);

		LineChartSeries series3 = new LineChartSeries();
		series3.setLabel("Minimum Reccomended");
		series3.set("2014-01-01 09:00:00", minH);
		series3.set("2014-01-01 18:00:00", minH);

		moistureModel.addSeries(series1);
		moistureModel.addSeries(series2);
		moistureModel.addSeries(series3);

		moistureModel.getAxis(AxisType.Y).setLabel("Celsius");
		DateAxis axis = new DateAxis("");
		axis.setTickAngle(-50);
		axis.setMin("2014-01-01 09:00:00");
		axis.setMax("2014-01-01 18:00:00");
		axis.setTickFormat("%H:%#M");
		moistureModel.getAxes().put(AxisType.X, axis);

		return moistureModel;

	}

	public void setMoistureModel(LineChartModel moistureModel) {
		this.moistureModel = moistureModel;
	}

	public void readData() throws FileNotFoundException {

		double avgT = 0;
		double sT = 0;

		Data.br = new BufferedReader(new FileReader(Data.csvFile));
		List<String> lines = Data.br.lines().skip(Data.last).limit(5).collect(Collectors.toList());
		if (lines.size() > 0) {
			for (String l : lines) {
				String[] p = l.split(Data.cvsSplitBy);
				System.out.println("GreenHouse [name= " + p[2] + ", TInt =" + p[8] + ", TExt = " + p[3] + ", MInt= "
						+ p[11] + " ]");
				sT = sT + Double.parseDouble(p[8]);
			}
			Data.last = Data.last + 10;
			lines.clear();

			avgT = sT / 5;

			Calendar now = Calendar.getInstance();
			now.setTime(new Date());
			ChartSeries temp = this.temperatureModel.getSeries().get(1);
			temp.set(dateFormat.format(now.getTime()), avgT);
		} else
			Data.last = 1;
	}

	public void initTemperatureModel() {
		int minT = selectedGreenHouse.getPlant().getOptimalConditions().getMinTemperature();
		int maxT = selectedGreenHouse.getPlant().getOptimalConditions().getMaxTemperature();

		temperatureModel = new LineChartModel();

		temperatureModel.setTitle("Temperature representation");
		temperatureModel.setLegendPosition("e");

		Calendar min = Calendar.getInstance();
		min.setTime(new Date());

		Calendar max = Calendar.getInstance();
		max.setTime(min.getTime());
		max.add(Calendar.MINUTE, 10);

		LineChartSeries series1 = new LineChartSeries();
		series1.setLabel("Maximum Reccomended");

		String mi = dateFormat.format(min.getTime());
		String ma = dateFormat.format(max.getTime());

		series1.set(mi, maxT + 2);
		series1.set(ma, maxT + 2);

		LineChartSeries series2 = new LineChartSeries();
		series2.setLabel("Actual Temperature");

		/*
		 * Random random = new Random(); series2.set("2014-01-01 10:00:00",
		 * random.nextInt(25 - 20 + 1) + 20); series2.set("2014-01-01 11:00:00",
		 * 23); series2.set("2014-01-01 12:00:00", 25);
		 * series2.set("2014-01-01 13:00:00", 27);
		 */

		LineChartSeries series3 = new LineChartSeries();
		series3.setLabel("Minimum Reccomended");
		series3.set(mi, minT - 2);
		series3.set(ma, minT - 2);

		temperatureModel.addSeries(series1);
		temperatureModel.addSeries(series2);
		temperatureModel.addSeries(series3);

		temperatureModel.getAxis(AxisType.Y).setLabel("Celsius");
		DateAxis axis = new DateAxis("");
		axis.setTickAngle(-50);
		axis.setMin(mi);
		axis.setMax(ma);
		axis.setTickFormat("%H:%M:%S");
		temperatureModel.getAxes().put(AxisType.X, axis);

	}
}

package com.Controllers;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
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
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.Beans.Environment;
import com.Beans.GreenHouse;
import com.Beans.OptimalConditions;
import com.Beans.Plant;
import com.Scheduler.Data;

import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

@ManagedBean(name = "MonitoringController")
@SessionScoped
public class MonitoringController {

	// Properties
	private GreenHouse selectedGreenHouse = new GreenHouse();
	private List<String> greenHouses = new ArrayList<>();
	private LineChartModel temperatureModel;
	private LineChartModel moistureModel;
	private LineChartModel co2Model;

	private String alert = "";
	private String sugestion = "Inchide geamurile! Completeaza cu compost!";

	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private final static String knwoledgeOntologyPath = "/home/andra/git/ontology-frontend/Files/knowledge.owl";
	private final static String iri = "http://www.semanticweb.org/andra/semantics";

	// Methods

	@PostConstruct
	public void init() throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
		getGreenHouses();
		initTemperatureModel();
		initHumidityModel();
		initCo2Model();
		readData();
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

		if (greenHouses.size() > 0) {
			String firstGreenHouseName = greenHouses.get(0);
			selectedGreenHouse = getGreenHouseByName(firstGreenHouseName);
		}

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
			if (xx.equals("Co2Min"))
				cond.setMinCo2(Integer.parseInt(v.getLiteral()));
			if (xx.equals("Co2Max"))
				cond.setMaxCo2(Integer.parseInt(v.getLiteral()));
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

	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = alert;
	}

	public LineChartModel getMoistureModel() {

		return moistureModel;

	}

	public void setMoistureModel(LineChartModel moistureModel) {
		this.moistureModel = moistureModel;
	}

	public LineChartModel getCo2Model() {
		return co2Model;
	}

	public void setCo2Model(LineChartModel co2Model) {
		this.co2Model = co2Model;
	}

	public void readData() throws FileNotFoundException, OWLOntologyCreationException, OWLOntologyStorageException {

		double avgT = 0;
		double sT = 0;
		double avgH = 0;
		double sH = 0;
		double avgCo2 = 0;
		double sCo2 = 0;

		Data.br = new BufferedReader(new FileReader(Data.csvFile));
		List<String> lines = Data.br.lines().skip(Data.last).limit(Data.inteval).collect(Collectors.toList());
		if (lines.size() > 0) {
			for (String l : lines) {
				String[] p = l.split(Data.cvsSplitBy);
				System.out.println(
						"GreenHouse [name= " + p[2] + ", TInt =" + p[4] + ", TExt = " + p[3] + ", MInt= " + p[5] + "]");
				sT = sT + Double.parseDouble(p[4]);
				sH = sH + Double.parseDouble(p[5]);
				sCo2 = sCo2 + Double.parseDouble(p[6]);
			}
			Data.last = Data.last + Data.inteval;
			lines.clear();

			avgT = sT / Data.inteval;
			avgH = sH / Data.inteval;
			avgCo2 = sCo2 / Data.inteval;

			ProcessValues(avgT,avgH, avgCo2);

			Calendar now = Calendar.getInstance();
			now.setTime(new Date());

			ChartSeries temp = this.temperatureModel.getSeries().get(1);
			temp.set(dateFormat.format(now.getTime()), avgT);

			ChartSeries hum = this.moistureModel.getSeries().get(1);
			hum.set(dateFormat.format(now.getTime()), avgH);

			ChartSeries co2 = this.co2Model.getSeries().get(1);
			co2.set(dateFormat.format(now.getTime()), avgCo2);

			// set actual values
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			File file = new File("/home/andra/git/ontology-frontend/Files/knowledge.owl");
			OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
			IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/semantics");
			OWLDataFactory factory = manager.getOWLDataFactory();

			OWLNamedIndividual greenHouseInd = factory
					.getOWLNamedIndividual(ontologyIRI + "#" + selectedGreenHouse.getGreenHouseName());

			OWLDataProperty hasActualT = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#hasActualT"));

			// delete previous configuration
			Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
			Set<OWLDataPropertyAssertionAxiom> set = myOntology.getDataPropertyAssertionAxioms(greenHouseInd);

			for (OWLDataPropertyAssertionAxiom a : set) {
				OWLDataProperty growInProp = (OWLDataProperty) a.getProperty();
				if (growInProp.equals(hasActualT))
					changes.add(new RemoveAxiom(myOntology, a));
			}
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(changes);
			manager.applyChanges(list);

			OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(hasActualT,
					greenHouseInd, (int) avgT);
			AddAxiom ax1 = new AddAxiom(myOntology, dataPropertyAssertion);
			manager.applyChange(ax1);

			File fileformated = new File("/home/andra/git/ontology-frontend/Files/knowledge.owl");
			File newOntologyFile = fileformated.getAbsoluteFile();
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newOntologyFile));

			manager.saveOntology(myOntology, new OWLXMLDocumentFormat(), outputStream);

		} else
			Data.last = 1;
	}

	private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

	private void ProcessValues(double avgT, double avgH, double avgCo2) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File(knwoledgeOntologyPath);
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI ontologyIRI = IRI.create(iri);
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);

		OWLClass plantClass = factory.getOWLClass(ontologyIRI + "#Plant");

		for (OWLNamedIndividual plant : reasoner.getInstances(plantClass, false).getFlattened()) {
			System.out.println("plant : " + renderer.render(plant));
		}

		OWLNamedIndividual plant = factory
				.getOWLNamedIndividual(ontologyIRI + "#" + selectedGreenHouse.getPlant().getName());
		OWLNamedIndividual greenHouse = factory
				.getOWLNamedIndividual(ontologyIRI + "#" + selectedGreenHouse.getGreenHouseName());
		OWLObjectProperty growInProperty = factory.getOWLObjectProperty(ontologyIRI + "#growIn");

		boolean result = reasoner
				.isEntailed(factory.getOWLObjectPropertyAssertionAxiom(growInProperty, plant, greenHouse));
		System.out.println("Do tomatoes grow in GreenHouseA ? : " + result);

		OWLClass tHighAlertClass = factory.getOWLClass(ontologyIRI + "#THighAlert");
		OWLClassAssertionAxiom axiomTHigh = factory.getOWLClassAssertionAxiom(tHighAlertClass, greenHouse);
		boolean tHIgh = reasoner.isEntailed(axiomTHigh);
		//System.out.println("Is the temperature from HouseA too high ? : " + tHIgh);
		if (tHIgh) {
			OWLNamedIndividual THigh = factory.getOWLNamedIndividual(ontologyIRI + "#THigh");
			Set<OWLDataPropertyAssertionAxiom> properties = myOntology.getDataPropertyAssertionAxioms(THigh);
			for (OWLDataPropertyAssertionAxiom pa : properties) {
				OWLDataProperty p = (OWLDataProperty) pa.getProperty();
				String xx = p.getIRI().getShortForm().toString();
				OWLLiteral v = pa.getObject();
			}

		}

		OWLClass tLowAlertClass = factory.getOWLClass(ontologyIRI + "#TLowAlert");
		OWLClassAssertionAxiom axiomTLow = factory.getOWLClassAssertionAxiom(tLowAlertClass, greenHouse);
		//System.out.println("Is the temperature from HouseA too low ? : " + reasoner.isEntailed(axiomTLow));
		StringBuilder b = new StringBuilder();
		
		if(avgT < selectedGreenHouse.getPlant().getOptimalConditions().getMinTemperature())
		{
			b.append(String.format("The temperature from [%s] is too low", selectedGreenHouse.getGreenHouseName()));
			b.append("\r\n");
		}
		else if(avgT > selectedGreenHouse.getPlant().getOptimalConditions().getMaxTemperature()){
			b.append(String.format("The temperature from [%s] is too high", selectedGreenHouse.getGreenHouseName()));
			//b.append(System.getProperty("line.separator"));
			b.append("\n");
		}
		if(avgH < selectedGreenHouse.getPlant().getOptimalConditions().getMinMoisture())
		{
			b.append(String.format("The humidity from [%s] is too low", selectedGreenHouse.getGreenHouseName()));
			b.append("\r\n");
		}
		else if(avgH > selectedGreenHouse.getPlant().getOptimalConditions().getMaxMoisture()){
			b.append(String.format("The humidity from [%s] is too high", selectedGreenHouse.getGreenHouseName()));
			b.append("\r\n");
		}
		if(avgCo2 < selectedGreenHouse.getPlant().getOptimalConditions().getMinCo2())
		{
			b.append(String.format("The CO2 level from [%s] is too low", selectedGreenHouse.getGreenHouseName()));
			b.append("\r\n");
		}
		else if(avgCo2 > selectedGreenHouse.getPlant().getOptimalConditions().getMaxCo2()){
			b.append(String.format("The CO2 level from [%s] is too high", selectedGreenHouse.getGreenHouseName()));
			b.append("\r\n");
		}
		
		setAlert(b.toString());
		

	}

	private static void printIndented(ExplanationTree node, String indent) {
		OWLAxiom axiom = node.getUserObject();
		System.out.println(indent + renderer.render(axiom));
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

		String mi = dateFormat.format(min.getTime());
		String ma = dateFormat.format(max.getTime());

		LineChartSeries series1 = new LineChartSeries();
		series1.setLabel("Maximum Reccomended");
		series1.set(mi, maxT);
		series1.set(ma, maxT);

		LineChartSeries series2 = new LineChartSeries();
		series2.setLabel("Current Temperature");

		LineChartSeries series3 = new LineChartSeries();
		series3.setLabel("Minimum Reccomended");
		series3.set(mi, minT);
		series3.set(ma, minT);

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

	public void initHumidityModel() {
		int minH = selectedGreenHouse.getPlant().getOptimalConditions().getMinMoisture();
		int maxH = selectedGreenHouse.getPlant().getOptimalConditions().getMaxMoisture();

		moistureModel = new LineChartModel();

		moistureModel.setTitle("Humidity representation");
		moistureModel.setLegendPosition("e");

		Calendar min = Calendar.getInstance();
		min.setTime(new Date());

		Calendar max = Calendar.getInstance();
		max.setTime(min.getTime());
		max.add(Calendar.MINUTE, 10);

		String mi = dateFormat.format(min.getTime());
		String ma = dateFormat.format(max.getTime());

		LineChartSeries series1 = new LineChartSeries();
		series1.setLabel("Maximum Reccomended");
		series1.set(mi, maxH);
		series1.set(ma, maxH);

		LineChartSeries series2 = new LineChartSeries();
		series2.setLabel("Current Humidity");

		LineChartSeries series3 = new LineChartSeries();
		series3.setLabel("Minimum Reccomended");
		series3.set(mi, minH);
		series3.set(ma, minH);

		moistureModel.addSeries(series1);
		moistureModel.addSeries(series2);
		moistureModel.addSeries(series3);

		moistureModel.getAxis(AxisType.Y).setLabel("Level");
		DateAxis axis = new DateAxis("");
		axis.setTickAngle(-50);
		axis.setMin(mi);
		axis.setMax(ma);
		axis.setTickFormat("%H:%M:%S");
		moistureModel.getAxes().put(AxisType.X, axis);

	}

	public void initCo2Model() {
		int minCo2 = selectedGreenHouse.getPlant().getOptimalConditions().getMinCo2();
		int maxCo2 = selectedGreenHouse.getPlant().getOptimalConditions().getMaxCo2();

		co2Model = new LineChartModel();

		co2Model.setTitle("CO2 representation");
		co2Model.setLegendPosition("e");

		Calendar min = Calendar.getInstance();
		min.setTime(new Date());

		Calendar max = Calendar.getInstance();
		max.setTime(min.getTime());
		max.add(Calendar.MINUTE, 10);

		String mi = dateFormat.format(min.getTime());
		String ma = dateFormat.format(max.getTime());

		LineChartSeries series1 = new LineChartSeries();
		series1.setLabel("Maximum Reccomended");
		series1.set(mi, maxCo2);
		series1.set(ma, maxCo2);

		LineChartSeries series2 = new LineChartSeries();
		series2.setLabel("Current CO2 level");

		LineChartSeries series3 = new LineChartSeries();
		series3.setLabel("Minimum Reccomended");
		series3.set(mi, minCo2);
		series3.set(ma, minCo2);

		co2Model.addSeries(series1);
		co2Model.addSeries(series2);
		co2Model.addSeries(series3);

		co2Model.getAxis(AxisType.Y).setLabel("PPM");
		DateAxis axis = new DateAxis("");
		axis.setTickAngle(-50);
		axis.setMin(mi);
		axis.setMax(ma);
		axis.setTickFormat("%H:%M:%S");
		co2Model.getAxes().put(AxisType.X, axis);

	}

	public String getSugestion() {
		return sugestion;
	}

	public void setSugestion(String sugestion) {
		this.sugestion = sugestion;
	}
}

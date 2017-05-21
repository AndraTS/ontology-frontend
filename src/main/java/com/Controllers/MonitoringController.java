package com.Controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
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

@ManagedBean(name = "MonitoringController")
@SessionScoped
public class MonitoringController {

	// Properties
	private GreenHouse selectedGreenHouse = new GreenHouse();
	private List<String> greenHouses = new ArrayList<>();

	// Methods
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

	// Getters and Setters
	public GreenHouse getSelectedGreenHouse() {
		return selectedGreenHouse;
	}

	public GreenHouse getGreenHouseByName(String greenHouseName) throws OWLOntologyCreationException
	{
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
	
	public void setSelectedGreenHouse(GreenHouse greenHouse){
		this.selectedGreenHouse = greenHouse;
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

	public void setGreenHouses(List<String> greenHouses) {
		this.greenHouses = greenHouses;
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

}

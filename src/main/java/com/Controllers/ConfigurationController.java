package com.Controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.Beans.Environment;

@ManagedBean(name = "ConfigurationController")
@SessionScoped
public class ConfigurationController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Environment environment = new Environment();
	private List<String> greenHouses = new ArrayList<>();
	private String selectedGreenHouse = "";

	public String getSelectedGreenHouse() {
		return selectedGreenHouse;
	}

	public void setSelectedGreenHouse(String selectedGreenHouse) {
		this.selectedGreenHouse = selectedGreenHouse;
	}

	public List<String> getGreenHouses() throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("/home/andra/workspace/ontology-frontend/Files/Configuration.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/configuration");
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
		return greenHouses;
	}

	public void setGreenHouses(List<String> greenHouses) {
		this.greenHouses = greenHouses;
	}

	@PostConstruct
	public void init() throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {

		this.environment = ReadConfigurationFromOntology();
	}

	private Environment ReadConfigurationFromOntology() throws OWLOntologyCreationException {

		Environment env = new Environment();

		// IRI ontologyIRI =
		// IRI.create("http://www.ontology.ro/myOntology2.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("/home/andra/workspace/ontology-frontend/Files/Ontology1.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/ontology1");// manager.getOntologyDocumentIRI(myOntology);
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);
		reasoner.precomputeInferences();

		OWLClass plant = factory.getOWLClass(IRI.create(ontologyIRI + "#Plant"));
		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(plant, true);
		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
		System.out.println("Instances of plant: ");
		for (OWLNamedIndividual ind : individuals) {
			System.out.println("    " + ind.getIRI().getShortForm());
			env.setPlantName(ind.getIRI().getShortForm().toString());
			OWLObjectProperty growIn = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#growIn"));

			NodeSet<OWLNamedIndividual> houseValuesNodeSet = reasoner.getObjectPropertyValues(ind, growIn);
			Set<OWLNamedIndividual> values = houseValuesNodeSet.getFlattened();
			System.out.println("The GrowIn property values for Tomatoes:");
			for (OWLNamedIndividual gh : values) {
				System.out.println("    " + gh.getIRI().getShortForm());
				env.setGreenHouseName(gh.getIRI().getShortForm().toString());
			}

		}
		return env;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void submitConfiguration()
			throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
		updateOntology(this.environment.getPlantName(), this.environment.getGreenHouseName());
		FacesMessage message = null;
		try {
			message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration updated successfully!", null);

		} catch (Exception e) {
			message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to update the configuration!",
					e.getMessage());
		} finally {
			FacesContext.getCurrentInstance().addMessage(null, message);
		}

		// test1();
	}

	private static void updateOntology(String plantName, String greenHouseName)

			throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {

		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/owlapi/ontologies/ontology1");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("/home/andra/workspace/ontology-frontend/Files/Ontology1.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);
		reasoner.precomputeInferences();

		OWLNamedIndividual plantInd = factory.getOWLNamedIndividual(ontologyIRI + "#" + plantName);
		OWLNamedIndividual greenHouseInd = factory.getOWLNamedIndividual(ontologyIRI + "#" + greenHouseName);
		OWLObjectProperty growIn = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#growIn"));

		OWLAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom(growIn, plantInd, greenHouseInd);
		AddAxiom addAxiomChange = new AddAxiom(myOntology, assertion);
		manager.applyChange(addAxiomChange);

		// create the inverse properties
		OWLClass greenHouseClass = factory.getOWLClass(IRI.create(ontologyIRI + "#GreenHouse"));
		OWLObjectProperty grow = factory.getOWLObjectProperty(ontologyIRI + "#grow");
		manager.addAxiom(myOntology, factory.getOWLInverseObjectPropertiesAxiom(growIn, grow));
		
		Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
		domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(grow, greenHouseClass));
		for (OWLAxiom a : domainsAndRanges)
			manager.addAxiom(myOntology, a);


		// createProperty("#growIn", beans, greenHouse1, manager, factory,
		// ontologyIRI, myOntology);

		// save the ontology on the disk
		File fileformated = new File("/home/andra/workspace/ontology-frontend/Files/new1.owl");
		File newOntologyFile = fileformated.getAbsoluteFile();
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newOntologyFile));

		manager.saveOntology(myOntology, new OWLXMLDocumentFormat(), outputStream);

		// manager.saveOntology(myOntology);
	}

	public void onGreenHouseChange() throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("/home/andra/workspace/ontology-frontend/Files/Configuration.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/configuration");
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

	private static void test1()
			throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		IRI ontologyIRI = IRI.create("http://www.ontology.ro/myOntology2.owl");
		OWLOntology myOntology = manager.createOntology(ontologyIRI);

		OWLClass plantClass = addClassToOntology(manager, myOntology, ontologyIRI, "Plant");
		OWLClass greenHouseClass = addClassToOntology(manager, myOntology, ontologyIRI, "GreenHouse");
		OWLClass sensorClass = addClassToOntology(manager, myOntology, ontologyIRI, "Sensor");
		OWLClass phaseClass = addPhaseToOntology(manager, myOntology, ontologyIRI, "Phase");
		OWLClass conditionsClass = addClassToOntology(manager, myOntology, ontologyIRI, "Conditions");

		// create Tomatoes individual for Plant class
		OWLNamedIndividual tomatoes = createClassIndividual(plantClass, "#Tomatoes", manager, factory, ontologyIRI,
				myOntology);

		// create GreenHouse1 individual for GreenHouse class
		OWLNamedIndividual greenHouse1 = createClassIndividual(greenHouseClass, "#GreenHouse1", manager, factory,
				ontologyIRI, myOntology);

		// create ST1 individual for Sensor class
		OWLNamedIndividual ST1 = createClassIndividual(sensorClass, "#ST1", manager, factory, ontologyIRI, myOntology);

		// create GrowIn property between Tomatoes and GreenHouse1
		OWLObjectProperty growIn = createProperty("#GrowIn", tomatoes, greenHouse1, manager, factory, ontologyIRI,
				myOntology);

		// create From property between ST1 and GreenHouse1
		OWLObjectProperty from = createProperty("#From", ST1, greenHouse1, manager, factory, ontologyIRI, myOntology);

		// create the inverse properties
		OWLObjectProperty grow = factory.getOWLObjectProperty(ontologyIRI + "#Grow");
		manager.addAxiom(myOntology, factory.getOWLInverseObjectPropertiesAxiom(growIn, grow));

		// add ranges for domains
		Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
		domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(growIn, plantClass));
		domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(from, sensorClass));
		domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(grow, greenHouseClass));
		for (OWLAxiom a : domainsAndRanges)
			manager.addAxiom(myOntology, a);

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);
		reasoner.precomputeInferences();

		// Reasoner reasoner=new Reasoner(myOntology);

		boolean consistent = reasoner.isConsistent();
		System.out.println("Consistent: " + consistent);
		System.out.println("\n");

		OWLClass phase = factory.getOWLClass(IRI.create(ontologyIRI + "#Phase"));
		NodeSet<OWLClass> subClses = reasoner.getSubClasses(phase, true);
		@SuppressWarnings("deprecation")
		Set<OWLClass> clses = subClses.getFlattened();
		System.out.println("Subclasses of phase: ");
		for (OWLClass cls : clses) {
			System.out.println("    " + cls);
		}

		OWLClass plant = factory.getOWLClass(IRI.create(ontologyIRI + "#Plant"));
		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(plant, true);
		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
		System.out.println("Instances of plant: ");
		for (OWLNamedIndividual ind : individuals) {
			System.out.println("    " + ind);
		}

		NodeSet<OWLNamedIndividual> houseValuesNodeSet = reasoner.getObjectPropertyValues(tomatoes, growIn);
		Set<OWLNamedIndividual> values = houseValuesNodeSet.getFlattened();
		System.out.println("The GrowIn property values for Tomatoes:");
		for (OWLNamedIndividual ind : values) {
			System.out.println("    " + ind);
		}

		NodeSet<OWLNamedIndividual> sensorValuesNodeSet = reasoner.getObjectPropertyValues(ST1, from);
		Set<OWLNamedIndividual> sensorvalues = sensorValuesNodeSet.getFlattened();
		System.out.println("The sensor ST1 is from:");
		for (OWLNamedIndividual ind : sensorvalues) {
			System.out.println("    " + ind);
		}

		NodeSet<OWLNamedIndividual> growValuesNodeSet = reasoner.getObjectPropertyValues(greenHouse1, grow);
		Set<OWLNamedIndividual> values2 = growValuesNodeSet.getFlattened();
		System.out.println("The Grow property values for GreenHouse1:");
		for (OWLNamedIndividual ind : values2) {
			System.out.println("    " + ind);
		}

		// save the ontology on the disk
		File fileformated = new File("/home/andra/workspace/ontology-frontend/Files/fullOntology.owl");
		File newOntologyFile = fileformated.getAbsoluteFile();
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newOntologyFile));

		manager.saveOntology(myOntology, new OWLXMLDocumentFormat(), outputStream);

		/*
		 * OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
		 * File file1 = new
		 * File("/home/andra/workspace/ontology-frontend/Files/env1.owl"); //
		 * Now load the local copy File file2 = new
		 * File("/home/andra/workspace/ontology-frontend/Files/beans.owl");
		 * manager1.loadOntologyFromOntologyDocument(file1);
		 * manager1.loadOntologyFromOntologyDocument(file2); // Create our
		 * ontology merger OWLOntologyMerger merger = new
		 * OWLOntologyMerger(manager1); IRI mergedOntologyIRI =
		 * IRI.create("http://www.semanticweb.com/mymergedont"); OWLOntology
		 * merged = merger.createMergedOntology(manager1, mergedOntologyIRI);
		 * 
		 * File fileMerged = new
		 * File("/home/andra/workspace/ontology-frontend/Files/merged.owl");
		 * File mergedFile = fileMerged.getAbsoluteFile(); BufferedOutputStream
		 * mergedStream = new BufferedOutputStream(new
		 * FileOutputStream(mergedFile)); manager1.saveOntology(merged, new
		 * OWLXMLDocumentFormat(), mergedStream);
		 */
	}

	private static OWLObjectProperty createProperty(String propertyName, OWLNamedIndividual ind1,
			OWLNamedIndividual ind2, OWLOntologyManager manager, OWLDataFactory factory, IRI ontologyIRI,
			OWLOntology myOntology) {
		OWLObjectProperty property = factory.getOWLObjectProperty(ontologyIRI + propertyName);
		OWLObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(property, ind1,
				ind2);
		manager.addAxiom(myOntology, propertyAssertion);
		return property;
	}

	private static OWLNamedIndividual createClassIndividual(OWLClass theClass, String indName,
			OWLOntologyManager manager, OWLDataFactory factory, IRI ontologyIRI, OWLOntology myOntology) {

		OWLNamedIndividual theIndividual = factory.getOWLNamedIndividual(ontologyIRI + indName);
		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(theClass, theIndividual);
		manager.addAxiom(myOntology, classAssertion);
		return theIndividual;
	}

	private static OWLClass addClassToOntology(OWLOntologyManager manager, OWLOntology myOntology, IRI ontologyIRI,
			String name) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		IRI classIri = IRI.create(ontologyIRI + "#" + name);
		OWLClass owlClass = factory.getOWLClass(classIri);
		manager.addAxiom(myOntology, factory.getOWLDeclarationAxiom(owlClass));
		return owlClass;

		/*
		 * OWLDatatype stringDatatype = factory.getStringOWLDatatype();
		 * OWLDataProperty greenHouseName =
		 * factory.getOWLDataProperty(IRI.create(ontologyIRI + "name"));
		 * OWLFunctionalDataPropertyAxiom funcAx = factory
		 * .getOWLFunctionalDataPropertyAxiom(greenHouseName);
		 * manager.applyChange(new AddAxiom(myOntology, funcAx));
		 */

	}

	private static OWLClass addPhaseToOntology(OWLOntologyManager manager, OWLOntology myOntology, IRI ontologyIRI,
			String name) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		IRI classIri = IRI.create(ontologyIRI + "#" + name);
		OWLClass phaseClass = factory.getOWLClass(classIri);

		IRI germinationIri = IRI.create(ontologyIRI + "#" + "Germination");
		OWLClass germinationClass = factory.getOWLClass(germinationIri);

		IRI vegetationIri = IRI.create(ontologyIRI + "#" + "Vegetation");
		OWLClass vegetationClass = factory.getOWLClass(vegetationIri);

		OWLAxiom axiom1 = factory.getOWLSubClassOfAxiom(germinationClass, phaseClass);
		OWLAxiom axiom2 = factory.getOWLSubClassOfAxiom(vegetationClass, phaseClass);

		AddAxiom addAxiom1 = new AddAxiom(myOntology, axiom1);
		AddAxiom addAxiom2 = new AddAxiom(myOntology, axiom2);

		manager.applyChange(addAxiom1);
		manager.applyChange(addAxiom2);

		manager.addAxiom(myOntology, factory.getOWLDeclarationAxiom(phaseClass));
		return phaseClass;
	}

}
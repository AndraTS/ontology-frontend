package com.Controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
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
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.Beans.Environment;

@ManagedBean(name = "ConfigurationController")
@SessionScoped
public class ConfigurationController implements Serializable {

	// Properties
	private Environment environment = new Environment();
	private List<String> plants = new ArrayList<>();
	private String selectedPlant = "";

	public String getSelectedPlant() {
		return selectedPlant;
	}

	public void setSelectedPlant(String selectedPlant) {
		this.selectedPlant = selectedPlant;
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
		File file = new File("/home/andra/git/ontology-frontend/Files/knowledge.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/semantics");
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);
		reasoner.precomputeInferences();

		// set plant if greenHouse already configured
		OWLClass plant = factory.getOWLClass(IRI.create(ontologyIRI + "#Plant"));
		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(plant, true);
		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
		System.out.println("Instances of plant: ");
		for (OWLNamedIndividual ind : individuals) {
			plants.add(ind.getIRI().getShortForm());
		}

		OWLClass greenHouseClass = factory.getOWLClass(IRI.create(ontologyIRI + "#GreenHouse"));
		NodeSet<OWLNamedIndividual> housesNodeSet = reasoner.getInstances(greenHouseClass, true);
		Set<OWLNamedIndividual> houseIndividuals = housesNodeSet.getFlattened();
		for (OWLNamedIndividual ind : houseIndividuals) {
			env.setGreenHouseName(ind.getIRI().getShortForm().toString());
			OWLObjectProperty grow = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#grow"));
			NodeSet<OWLNamedIndividual> houseValues = reasoner.getObjectPropertyValues(ind, grow);
			Set<OWLNamedIndividual> values = houseValues.getFlattened();
			for (OWLNamedIndividual gh : values) {
				env.setPlantName(gh.getIRI().getShortForm().toString());
				setSelectedPlant(gh.getIRI().getShortForm().toString());
			}

		}

		/*
		 * OWLClass plant = factory.getOWLClass(IRI.create(ontologyIRI +
		 * "#Plant")); NodeSet<OWLNamedIndividual> individualsNodeSet =
		 * reasoner.getInstances(plant, true); Set<OWLNamedIndividual>
		 * individuals = individualsNodeSet.getFlattened();
		 * System.out.println("Instances of plant: "); for (OWLNamedIndividual
		 * ind : individuals) { System.out.println("    " +
		 * ind.getIRI().getShortForm());
		 * env.setPlantName(ind.getIRI().getShortForm().toString());
		 * OWLObjectProperty growIn =
		 * factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#growIn"));
		 * 
		 * NodeSet<OWLNamedIndividual> houseValuesNodeSet =
		 * reasoner.getObjectPropertyValues(ind, growIn);
		 * Set<OWLNamedIndividual> values = houseValuesNodeSet.getFlattened();
		 * System.out.println("The GrowIn property values for Tomatoes:"); for
		 * (OWLNamedIndividual gh : values) { System.out.println("    " +
		 * gh.getIRI().getShortForm());
		 * env.setGreenHouseName(gh.getIRI().getShortForm().toString()); }
		 * 
		 * }
		 */
		return env;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	private static final long serialVersionUID = 1L;

	public List<String> getPlants() {
		return plants;
	}

	public void setPlants(List<String> plants) {
		this.plants = plants;
	}

	public void submitConfiguration()
			throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {

		FacesMessage message = null;
		try {
			updateOntology(getSelectedPlant(), this.environment.getGreenHouseName(), this.environment.getSeedingMoment());
			message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration updated successfully!", null);

		} catch (Exception e) {
			message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to update the configuration!",
					e.getMessage());
		} finally {
			FacesContext.getCurrentInstance().addMessage(null, message);
		}

		// test1();
	}

	private static void updateOntology(String plantName, String greenHouseName, Date seedingMoment)

			throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {

		IRI ontologyIRI = IRI.create("http://www.semanticweb.org/andra/semantics");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("/home/andra/git/ontology-frontend/Files/knowledge.owl");
		OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(myOntology));

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(myOntology, config);
		reasoner.precomputeInferences();

		// classes
		OWLClass greenHouseClass = factory.getOWLClass(IRI.create(ontologyIRI + "#GreenHouse"));
		OWLClass plantClass = factory.getOWLClass(IRI.create(ontologyIRI + "#Plant"));

		OWLNamedIndividual greenHouseInd = factory.getOWLNamedIndividual(ontologyIRI + "#" + greenHouseName);
		OWLNamedIndividual plantInd = factory.getOWLNamedIndividual(ontologyIRI + "#" + plantName);
		OWLObjectProperty growIn = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#growIn"));

		// delete previous configuration
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		Set<OWLObjectPropertyAssertionAxiom> set = myOntology.getObjectPropertyAssertionAxioms(plantInd);

		for (OWLObjectPropertyAssertionAxiom a : set) {
			OWLObjectProperty growInProp = (OWLObjectProperty) a.getProperty();
			if (growInProp.equals(growIn))
				changes.add(new RemoveAxiom(myOntology, a));
		}

		Set<OWLObjectPropertyAssertionAxiom> set2 = myOntology.getObjectPropertyAssertionAxioms(greenHouseInd);
		for (OWLObjectPropertyAssertionAxiom a : set2)
			changes.add(new RemoveAxiom(myOntology, a));

		NodeSet<OWLNamedIndividual> housesNodeSet = reasoner.getInstances(greenHouseClass, true);
		Set<OWLNamedIndividual> houseIndividuals = housesNodeSet.getFlattened();

		for (OWLNamedIndividual ind : houseIndividuals) {
			remover.visit(ind);
		}

		List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(changes);
		manager.applyChanges(list);

		OWLAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom(growIn, plantInd, greenHouseInd);
		AddAxiom addAxiomChange = new AddAxiom(myOntology, assertion);
		manager.applyChange(addAxiomChange);

		OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(greenHouseClass, greenHouseInd);
		manager.addAxiom(myOntology, ax);

		OWLObjectPropertyDomainAxiom growInAx = factory.getOWLObjectPropertyDomainAxiom(growIn, plantClass);
		manager.addAxiom(myOntology, growInAx);

		OWLObjectProperty grow = factory.getOWLObjectProperty(ontologyIRI + "#grow");
		manager.addAxiom(myOntology, factory.getOWLInverseObjectPropertiesAxiom(growIn, grow));

		Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
		domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(grow, greenHouseClass));
		for (OWLAxiom a : domainsAndRanges)
			manager.addAxiom(myOntology, a);

	/*	// update seedingMoment
		OWLDataProperty seedingMom = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#SeedingMoment"));
		Set<OWLDataPropertyAssertionAxiom> plantProps = myOntology.getDataPropertyAssertionAxioms(plantInd);
		for (OWLDataPropertyAssertionAxiom pa : plantProps) {
			OWLDataProperty p = (OWLDataProperty) pa.getProperty();
			remover.visit(p);
		}
		OWLLiteral dataLiteral = factory.getOWLLiteral("06.10.2010 00:00:00", OWL2Datatype.XSD_DATE_TIME);

		
	*/	
		// grow assertion
		OWLAxiom assertion2 = factory.getOWLObjectPropertyAssertionAxiom(grow, greenHouseInd, plantInd);
		AddAxiom addAxiomChange2 = new AddAxiom(myOntology, assertion2);
		manager.applyChange(addAxiomChange2);

		// createProperty("#growIn", beans, greenHouse1, manager, factory,
		// ontologyIRI, myOntology);

		// save the ontology on the disk

		manager.applyChanges(remover.getChanges());
		File fileformated = new File("/home/andra/git/ontology-frontend/Files/knowledge.owl");
		File newOntologyFile = fileformated.getAbsoluteFile();
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newOntologyFile));

		manager.saveOntology(myOntology, new OWLXMLDocumentFormat(), outputStream);

		// manager.saveOntology(myOntology);
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
		File fileformated = new File("/home/andra/git/ontology-frontend/Files/fullOntology.owl");
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

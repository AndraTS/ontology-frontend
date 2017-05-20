package com.Beans;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

@ManagedBean(name = "Start")
@ApplicationScoped
public class Start {
	
	private String alerta = "Alert!!!";
	
	
		
	public void CreateBeansOntology()
			throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		IRI ontologyIRI = IRI.create("http://www.ontology.ro/myOntology.owl");
		OWLOntology myOntology = manager.createOntology(ontologyIRI);

		OWLClass plantClass = addClassToOntology(manager, myOntology, ontologyIRI, "Plant");
		OWLNamedIndividual beans = createClassIndividual(plantClass, "#Beans", manager, factory, ontologyIRI,
				myOntology);

		OWLClass phaseClass = addClassToOntology(manager, myOntology, ontologyIRI, "Phase");
		OWLNamedIndividual germination = createClassIndividual(phaseClass, "#Germination", manager, factory,
				ontologyIRI, myOntology);
		OWLNamedIndividual vegetation = createClassIndividual(phaseClass, "#Vegetation", manager, factory, ontologyIRI,
				myOntology);

		OWLObjectProperty hasPhase = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#hasPhase"));
		OWLDataProperty takes = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#takes"));
		OWLDataProperty hasTmin = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#hasTmin"));
		OWLDataProperty hasTmax = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#hasTmax"));

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasPhase, beans, germination));
		axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasPhase, beans, vegetation));
		axioms.add(factory.getOWLDataPropertyAssertionAxiom(takes, germination, 10));
		axioms.add(factory.getOWLDataPropertyAssertionAxiom(takes, vegetation, 100));
		axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasTmin, germination, 20));
		axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasTmax, germination, 25));
		for (OWLAxiom a : axioms)
			manager.addAxiom(myOntology, a);

		// save the ontology on the disk
		File fileformated = new File("/home/andra/workspace/ontology-frontend/Files/beans.owl");
		File newOntologyFile = fileformated.getAbsoluteFile();
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newOntologyFile));

		manager.saveOntology(myOntology, new OWLXMLDocumentFormat(), outputStream);

	}

	private static OWLClass addClassToOntology(OWLOntologyManager manager, OWLOntology myOntology, IRI ontologyIRI,
			String name) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		IRI classIri = IRI.create(ontologyIRI + "#" + name);
		OWLClass owlClass = factory.getOWLClass(classIri);
		manager.addAxiom(myOntology, factory.getOWLDeclarationAxiom(owlClass));
		return owlClass;

	}

	private static OWLNamedIndividual createClassIndividual(OWLClass theClass, String indName,
			OWLOntologyManager manager, OWLDataFactory factory, IRI ontologyIRI, OWLOntology myOntology) {

		OWLNamedIndividual theIndividual = factory.getOWLNamedIndividual(ontologyIRI + indName);
		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(theClass, theIndividual);
		manager.addAxiom(myOntology, classAssertion);
		return theIndividual;
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

	public String getAlerta() {
		return alerta;
	}

	public void setAlerta(String alerta) {
		this.alerta = alerta;
	}

}

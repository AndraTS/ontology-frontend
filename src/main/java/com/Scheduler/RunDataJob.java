package com.Scheduler;

import java.io.File;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class RunDataJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		System.out.println("Reading data");

		// IRI ontologyIRI =
		// IRI.create("http://www.semanticweb.org/owlapi/ontologies/ontology1");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		File file = new File("/home/andra/workspace/ontology-frontend/Files/Ontology1.owl");
		String base = "http://www.semanticweb.org/owlapi/ontologies/ontology1";
		PrefixManager pm = new DefaultPrefixManager(base);
		try {
			OWLOntology myOntology = manager.loadOntologyFromOntologyDocument(file);
			OWLClass ghClass = dataFactory.getOWLClass(":GreenHouse", pm);
			OWLNamedIndividual ghInd = dataFactory.getOWLNamedIndividual(":GreenHouse1", pm);

			OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(ghClass, ghInd);
			
			OWLOntology ontology = manager.createOntology(IRI.create(base));

			manager.addAxiom(ontology, classAssertion);

			 

			// Dump the ontology to stdout

			manager.saveOntology(ontology, new SystemOutDocumentTarget());

		} catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OWLDataFactory factory = manager.getOWLDataFactory();

	}

}
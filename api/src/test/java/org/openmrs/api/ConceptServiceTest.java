/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.openmrs.api.context.Context.getObsService;
import static org.openmrs.test.OpenmrsMatchers.hasConcept;
import static org.openmrs.test.OpenmrsMatchers.hasId;
import static org.openmrs.test.TestUtil.containsId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.dbunit.dataset.IDataSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptAttributeType;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptStopWord;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.ConceptMapTypeComparator;
import org.openmrs.util.DateUtil;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.validation.Errors;

/**
 * This test class (should) contain tests for all of the ConceptService methods TODO clean up and
 * finish this test class
 * 
 * @see org.openmrs.api.ConceptService
 */
public class ConceptServiceTest extends BaseContextSensitiveTest {
	
	protected ConceptService conceptService = null;

	protected static final String INITIAL_CONCEPTS_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	protected static final String GET_CONCEPTS_BY_SET_XML = "org/openmrs/api/include/ConceptServiceTest-getConceptsBySet.xml";
	
	protected static final String GET_DRUG_MAPPINGS = "org/openmrs/api/include/ConceptServiceTest-getDrugMappings.xml";

	protected static final String CONCEPT_ATTRIBUTE_TYPE_XML = "org/openmrs/api/include/ConceptServiceTest-conceptAttributeType.xml";
	
	protected static final String CONCEPT_WITH_CONCEPT_REFERENCE_RANGES_XML = "org/openmrs/api/include/ConceptServiceTest-conceptReferenceRange.xml";

	@Autowired
	CacheManager cacheManager;

	// For testing concept lookups by static constant
	private static final String TEST_CONCEPT_CONSTANT_ID = "3";
 
	private static final String TEST_CONCEPT_CONSTANT_UUID = "35d3346a-6769-4d52-823f-b4b234bac3e3";
	
	private static final String TEST_CONCEPT_CONSTANT_NAME = "COUGH SYRUP";
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeAllTests() {
		conceptService = Context.getConceptService();
	}
	
	@AfterEach
	public void revertToDefaultLocale() {
		Context.setLocale(Locale.US);
	}
	
	/**
	 * Updates the search index to clean up after each test.
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#updateSearchIndex()
	 */
	@BeforeEach
	@Override
	public void updateSearchIndex() {
		super.updateSearchIndex();
	}
	
	/**
	 * Updates the search index after executing each dataset.
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#executeDataSet(org.dbunit.dataset.IDataSet)
	 */
	@Override
	public void executeDataSet(IDataSet dataset) {
		super.executeDataSet(dataset);
		
		updateSearchIndex();
	}
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 */
	// @Test
	public void getConceptByName_shouldGetConceptByName() {
		
		String nameToFetch = "Some non numeric concept name";
		
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept conceptByName = conceptService.getConceptByName(nameToFetch);
		assertEquals(1, conceptByName.getId().intValue(), "Unable to fetch concept by name");
	}
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 */
	// @Test
	public void getConceptByName_shouldGetConceptByPartialName() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// substring of the name
		String partialNameToFetch = "Some";
		
		List<Concept> firstConceptsByPartialNameList = conceptService.getConceptsByName(partialNameToFetch);
		assertThat(firstConceptsByPartialNameList, containsInAnyOrder(hasId(1), hasId(2)));
	}

	/**
	 * @see ConceptService#getPrevConcept(Concept) 
	 */
	// @Test
	public void getPrevConcept_shouldReturnPreviousConceptBasedOnConceptId() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		Concept currentConcept = conceptService.getConcept(4);
		assertNotNull(currentConcept);

		Concept previousConcept = conceptService.getPrevConcept(currentConcept);

		assertNotNull(previousConcept);
		assertEquals((Integer)(currentConcept.getConceptId() - 1), previousConcept.getConceptId());
	}

	/**
	 * @see ConceptService#getPrevConcept(Concept)
	 */
	// @Test
	public void getPrevConcept_shouldReturnNullIfNoPrevConceptId() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		Concept currentConcept = conceptService.getConcept(1);
		assertNotNull(currentConcept);

		Concept previousConcept = conceptService.getPrevConcept(currentConcept);

		assertNull(previousConcept);
	}

	/**
	 * @see ConceptService#getNextConcept(Concept) 
	 */
	// @Test
	public void getNextConcept_shouldReturnNextConceptBasedOnConceptId() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		Concept currentConcept = conceptService.getConcept(3);
		assertNotNull(currentConcept);

		Concept nextConcept = conceptService.getNextConcept(currentConcept);

		assertNotNull(nextConcept);
		assertEquals((Integer)(currentConcept.getConceptId() + 1), nextConcept.getConceptId());
	}

	/**
	 * @see ConceptService#getNextConcept(Concept)
	 */
	// @Test
	public void getNextConcept_shouldReturnNullIfNoNextConceptId() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		// Use the highest concept ID in your dataset
		Concept currentConcept = conceptService.getConcept(5497);
		assertNotNull(currentConcept);

		Concept nextConcept = conceptService.getNextConcept(currentConcept);

		assertNull(nextConcept);
	}

	/**
	 * @see ConceptService#getAllConceptProposals(boolean)  
	 */
	// @Test
	public void getAllConceptProposals_whenIncludeCompletedIsFalse_shouldReturnOnlyUncompletedProposals() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		List<ConceptProposal> proposals = conceptService.getAllConceptProposals(false);

		ConceptProposal previousProposal = null;
		for (ConceptProposal proposal : proposals) {
			assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED, proposal.getState());

			if (previousProposal != null) {
				assertTrue(previousProposal.getOriginalText().compareTo(proposal.getOriginalText()) <= 0);
			}
			previousProposal = proposal;
		}
	}

	/**
	 * @see ConceptService#getConceptProposals(String))
	 */
	// @Test
	public void getConceptProposals_shouldReturnProposalsMatchingTextAndUnmappedState() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		String searchText = "unmapped concept proposal";
		
		List<ConceptProposal> proposals = conceptService.getConceptProposals(searchText);

		assertEquals(1, proposals.size());
		for (ConceptProposal proposal : proposals) {
			assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED, proposal.getState());
			assertEquals(searchText, proposal.getOriginalText());
		}
	}

	/**
	 * @see ConceptService#getProposedConcepts(String))
	 */
	// @Test
	public void getProposedConcepts_shouldReturnConceptsMatchingTextAndExcludingUnmappedState() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		String searchText = "mapped concept proposal";

		List<ConceptProposal> allProposals = conceptService.getAllConceptProposals(true);
		assertTrue(allProposals.size() > 0);

		List<Concept> concepts = conceptService.getProposedConcepts(searchText);
		assertEquals(1, concepts.size());
		
		Concept actualConcept = concepts.get(0);
		for (ConceptProposal proposal : allProposals) {
			if (proposal.getMappedConcept() == null) {
				continue;
			}
			
			if (proposal.getMappedConcept().getConceptId().intValue() == actualConcept.getConceptId().intValue()) {
				assertNotNull(proposal.getMappedConcept());
				assertNotEquals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED, proposal.getState());
				assertEquals(actualConcept.getConceptId(), proposal.getMappedConcept().getConceptId());
			}
		}
	}

	/**
	 * @see ConceptService#getProposedConcepts(String))
	 */
	// @Test
	public void getProposedConcepts_shouldReturnEmptyWhenNoMappedConcepts() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		String searchText = "unmapped concept proposal";

		List<Concept> concepts = conceptService.getProposedConcepts(searchText);
		assertEquals(0, concepts.size());
	}

	/**
	 * @see ConceptService#getConceptSetsByConcept_shouldReturnConceptSetsOrderedBySortWeight() 
	 */
	// @Test
	public void getConceptSetsByConcept_shouldReturnConceptSetsOrderedBySortWeight() {
		executeDataSet(INITIAL_CONCEPTS_XML);


		Concept concept = conceptService.getConceptByUuid("0f97e14e-cdc2-49ac-9255-b5126f8a5147");

		List<ConceptSet> conceptSets = conceptService.getConceptSetsByConcept(concept);
		assertEquals(3, conceptSets.size());

		for (int i = 0; i < conceptSets.size() - 1; i++) {
			ConceptSet current = conceptSets.get(i);
			ConceptSet next = conceptSets.get(i + 1);

			assertNotNull(current.getSortWeight());
			assertNotNull(next.getSortWeight());
			assertTrue(current.getSortWeight() <= next.getSortWeight());
		}
	}
	
	/**
	 * @see ConceptService#getAllConceptProposals(boolean)
	 */
	// @Test
	public void getAllConceptProposals_WhenIncludeCompletedIsTrue_shouldReturnAllProposals() {
		executeDataSet(INITIAL_CONCEPTS_XML);

		List<ConceptProposal> proposals = conceptService.getAllConceptProposals(true);

		assertFalse(proposals.isEmpty());


		boolean foundCompletedProposal = false;
		ConceptProposal previousProposal = null;
		for (ConceptProposal proposal : proposals) {
			if (!proposal.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED)) {
				foundCompletedProposal = true;
			}
			
			if (previousProposal != null) {
				assertTrue(previousProposal.getOriginalText().compareTo(proposal.getOriginalText()) <= 0);
			}
			previousProposal = proposal;
		}
		assertTrue(foundCompletedProposal, "No completed proposals were returned.");
	}


	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldSaveAConceptNumericAsAConcept() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		//This will automatically add the given locale to the list of allowed locales
		Context.setLocale(Locale.US);
		// this tests saving a previously conceptnumeric as just a concept
		Concept c2 = new Concept(2);
		ConceptName cn = new ConceptName("not a numeric anymore", Locale.US);
		c2.addName(cn);
		c2.addDescription(new ConceptDescription("some description",null));
		c2.setDatatype(new ConceptDatatype(3));
		c2.setConceptClass(new ConceptClass(1));
		conceptService.saveConcept(c2);
		
		Concept secondConcept = conceptService.getConcept(2);
		// this will probably still be a ConceptNumeric object.  what to do about that?
		// revisit this problem when discriminators are in place
		//assertFalse(secondConcept instanceof ConceptNumeric);
		// this shouldn't think its a conceptnumeric object though
		assertFalse(secondConcept.isNumeric());
		assertEquals("not a numeric anymore", secondConcept.getName(Locale.US).getName());
		
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldSaveANewConceptNumeric() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		Context.setLocale(Locale.US);
		// this tests saving a never before in the database conceptnumeric
		ConceptNumeric cn3 = new ConceptNumeric();
		cn3.setDatatype(new ConceptDatatype(1));
		cn3.setConceptClass(new ConceptClass(1));

		ConceptName cn = new ConceptName("a brand new conceptnumeric", Locale.US);
		cn3.addName(cn);
		cn3.addDescription(new ConceptDescription("some description",null));
		cn3.setHiAbsolute(50.0);
		conceptService.saveConcept(cn3);
		
		Concept thirdConcept = conceptService.getConcept(cn3.getConceptId());
		assertTrue(thirdConcept instanceof ConceptNumeric);
		ConceptNumeric thirdConceptNumeric = (ConceptNumeric) thirdConcept;
		assertEquals("a brand new conceptnumeric", thirdConceptNumeric.getName(Locale.US).getName());
		assertEquals(50.0, thirdConceptNumeric.getHiAbsolute(), 0);
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldSaveNonConceptNumericObjectAsConceptNumeric() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// this tests saving a current concept as a newly changed conceptnumeric
		// assumes there is already a concept in the database
		// with a concept id of #1
		ConceptNumeric cn = new ConceptNumeric(1);
		cn.setDatatype(new ConceptDatatype(1));
		cn.setConceptClass(new ConceptClass(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.addDescription(new ConceptDescription("some description",null));
		cn.setHiAbsolute(20.0);
		conceptService.saveConcept(cn);
		
		Concept firstConcept = conceptService.getConceptNumeric(1);
		firstConcept.addDescription(new ConceptDescription("some description",null));
		assertEquals("a new conceptnumeric", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptNumeric);
		ConceptNumeric firstConceptNumeric = (ConceptNumeric) firstConcept;
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute(), 0);
		
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldSaveNonConceptComplexObjectAsConceptComplex() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// this tests saving a current concept as a newly changed conceptComplex
		// assumes there is already a concept in the database
		// with a concept id of #1
		ConceptComplex cn = new ConceptComplex(1);
		cn.setDatatype(new ConceptDatatype(13));
		cn.setConceptClass(new ConceptClass(1));
		cn.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn.addDescription(new ConceptDescription("some description",null));
		cn.setHandler("SomeHandler");
		conceptService.saveConcept(cn);
		
		Concept firstConcept = conceptService.getConceptComplex(1);
		assertEquals("a new conceptComplex", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptComplex);
		ConceptComplex firstConceptComplex = (ConceptComplex) firstConcept;
		assertEquals("SomeHandler", firstConceptComplex.getHandler());
		
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldSaveChangesBetweenConceptNumericAndComplex() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		//save a concept numeric
		ConceptNumeric cn = new ConceptNumeric(1);
		cn.setDatatype(new ConceptDatatype(1));
		cn.setConceptClass(new ConceptClass(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.addDescription(new ConceptDescription("some description",null));
		cn.setHiAbsolute(20.0);
		conceptService.saveConcept(cn);
		
		//confirm that we saved a concept numeric
		Concept firstConcept = conceptService.getConceptNumeric(1);
		assertEquals("a new conceptnumeric", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptNumeric);
		ConceptNumeric firstConceptNumeric = (ConceptNumeric) firstConcept;
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute(), 0);
		
		//change to concept complex
		ConceptComplex cn2 = new ConceptComplex(1);
		cn2.setDatatype(new ConceptDatatype(13));
		cn2.setConceptClass(new ConceptClass(1));
		cn2.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn2.addDescription(new ConceptDescription("some description",null));
		cn2.setHandler("SomeHandler");
		conceptService.saveConcept(cn2);
		
		//confirm that we saved a concept complex
		firstConcept = conceptService.getConceptComplex(1);
		assertEquals("a new conceptComplex", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptComplex);
		ConceptComplex firstConceptComplex = (ConceptComplex) firstConcept;
		assertEquals("SomeHandler", firstConceptComplex.getHandler());
		
		//change to concept numeric
		cn = new ConceptNumeric(1);
		ConceptDatatype dt = new ConceptDatatype(1);
		dt.setName("Numeric");
		cn.setDatatype(dt);
		cn.setConceptClass(new ConceptClass(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.addDescription(new ConceptDescription("some description",null));
		cn.setHiAbsolute(20.0);
		conceptService.saveConcept(cn);
		
		//confirm that we saved a concept numeric
		firstConcept = conceptService.getConceptNumeric(1);
		assertEquals("a new conceptnumeric", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptNumeric);
		firstConceptNumeric = (ConceptNumeric) firstConcept;
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute(), 0);
		
		//change to concept complex
		cn2 = new ConceptComplex(1);
		cn2.setDatatype(new ConceptDatatype(13));
		cn2.setConceptClass(new ConceptClass(1));
		cn2.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn2.addDescription(new ConceptDescription("some description",null));
		cn2.setHandler("SomeHandler");
		conceptService.saveConcept(cn2);
		
		//confirm we saved a concept complex
		firstConcept = conceptService.getConceptComplex(1);
		assertEquals("a new conceptComplex", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptComplex);
		firstConceptComplex = (ConceptComplex) firstConcept;
		assertEquals("SomeHandler", firstConceptComplex.getHandler());
	}
	
	/**
	 * @see ConceptService#getConceptComplex(Integer)
	 */
	// @Test
	public void getConceptComplex_shouldReturnAConceptComplexObject() {
		executeDataSet("org/openmrs/api/include/ObsServiceTest-complex.xml");
		ConceptComplex concept = Context.getConceptService().getConceptComplex(8473);
		assertNotNull(concept);
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldGenerateIdForNewConceptIfNoneIsSpecified() {
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("Weight", Context.getLocale());
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		
		concept.setConceptId(null);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Numeric"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		concept = Context.getConceptService().saveConcept(concept);
		assertFalse(concept.getConceptId().equals(5089));
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldKeepIdForNewConceptIfOneIsSpecified() {
		Integer conceptId = 343434; // a nonexistent concept id;
		assertNull(conceptService.getConcept(conceptId)); // sanity check
		
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("Weight", Context.getLocale());
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptId(conceptId);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Numeric"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		concept = Context.getConceptService().saveConcept(concept);
		assertEquals(concept.getConceptId(), conceptId);
	}

	/**
	 * @see ConceptService#getAllConceptClasses(boolean)
	 */
	// @Test
	public void getAllConceptClasses_whenIncludeRetiredIsFalse_shouldNotReturnRetiredConceptClasses() {
		boolean includeRetired = false;
		
		List<ConceptClass> conceptClasses = conceptService.getAllConceptClasses(includeRetired);

		assertNotNull(conceptClasses);
		assertTrue(conceptClasses.size() > 0);
		for (ConceptClass conceptClass : conceptClasses) {
			assertFalse(conceptClass.isRetired());
		}
	}

	/**
	 * @see ConceptService#getAllConceptDatatypes(boolean)
	 */
	// @Test
	public void getAllConceptDatatypes_whenIncludeRetiredIsFalse_shouldNotReturnRetiredConceptDatatypes() {
		boolean includeRetired = false;

		List<ConceptDatatype> conceptDatatypes = conceptService.getAllConceptDatatypes(includeRetired);

		assertNotNull(conceptDatatypes);
		assertTrue(conceptDatatypes.size() > 0);
		for (ConceptDatatype conceptDatatype : conceptDatatypes) {
			assertFalse(conceptDatatype.isRetired());
		}
	}

	/**
	 * @see ConceptService#getAllConceptClasses(boolean)
	 */
	// @Test
	public void getAllConceptClasses_whenIncludeRetiredIsTrue_shouldReturnAllConceptClasses() {
		boolean includeRetired = true;

		List<ConceptClass> conceptClasses = conceptService.getAllConceptClasses(includeRetired);

		assertNotNull(conceptClasses);
		assertTrue(conceptClasses.size() > 0);
		
		boolean foundRetired = false;
		boolean foundNonRetired = false;
		for (ConceptClass conceptClass : conceptClasses) {
			if (conceptClass.isRetired()) {
				foundRetired = true;
			} else {
				foundNonRetired = true;
			}
			if (foundRetired && foundNonRetired) {
				break;
			}
		}

		assertTrue(foundRetired, "No retired concept classes found.");
		assertTrue(foundNonRetired, "No non-retired concept classes found.");
	}

	/**
	 * @see ConceptService#conceptIterator()
	 */
	// @Test
	public void conceptIterator_shouldIterateOverAllConcepts() {
		Iterator<Concept> iterator = Context.getConceptService().conceptIterator();
		
		assertTrue(iterator.hasNext());
		assertEquals(3, iterator.next().getConceptId().intValue());
	}
	
	/**
	 * This test will fail if it takes more than 15 seconds to run. (Checks for an error with the
	 * iterator looping forever) The @Timed annotation is used as an alternative to
	// * "@Test(timeout=15000)" so that the Spring transactions work correctly. Junit has a "feature"
	 * where it executes the befores/afters in a thread separate from the one that the actual test
	 * ends up running in when timed.
	 * 
	 * @see ConceptService#conceptIterator()
	 */
	// @Test()
	public void conceptIterator_shouldStartWithTheSmallestConceptId() {
		List<Concept> allConcepts = Context.getConceptService().getAllConcepts();
		int numberofconcepts = allConcepts.size();
		
		// sanity check
		assertTrue(numberofconcepts > 0);
		
		// now count up the number of concepts the iterator returns
		int iteratorCount = 0;
		Iterator<Concept> iterator = Context.getConceptService().conceptIterator();
		while (iterator.hasNext() && iteratorCount < numberofconcepts + 5) { // the lt check is in case of infinite loops
			iterator.next();
			iteratorCount++;
		}
		assertEquals(numberofconcepts, iteratorCount);
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldReuseConceptNameTagsThatAlreadyExistInTheDatabase() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-tags.xml");
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the name tag exists already
		ConceptNameTag cnt = cs.getConceptNameTagByName("preferred_en");
		assertNotNull(cnt);
		
		ConceptName cn = new ConceptName("Some name", Locale.ENGLISH);
		cn.addTag(new ConceptNameTag("preferred_en", "preferred name in a language"));
		Concept concept = new Concept();
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		
		cs.saveConcept(concept);
		
		Collection<ConceptNameTag> savedConceptNameTags = concept.getName(Locale.ENGLISH, false).getTags();
		ConceptNameTag savedConceptNameTag = (ConceptNameTag) savedConceptNameTags.toArray()[0];
		assertEquals(cnt.getConceptNameTagId(), savedConceptNameTag.getConceptNameTagId());
	}
	
	/**
	 * @see ConceptService#saveConceptSource(ConceptSource)
	 */
	// @Test
	public void saveConceptSource_shouldNotSetCreatorIfOneIsSuppliedAlready() {
		User expectedCreator = new User(501); // a user that isn't logged in now
		
		ConceptSource newConceptSource = new ConceptSource();
		newConceptSource.setName("name");
		newConceptSource.setDescription("desc");
		newConceptSource.setHl7Code("hl7Code");
		newConceptSource.setCreator(expectedCreator);
		Context.getConceptService().saveConceptSource(newConceptSource);
		
		assertEquals(newConceptSource.getCreator(), expectedCreator);
	}
	
	/**
	 * @see ConceptService#saveConceptSource(ConceptSource)
	 */
	// @Test
	public void saveConceptSource_shouldNotSetDateCreatedIfOneIsSuppliedAlready() {
		Date expectedDate = new Date(new Date().getTime() - 10000);
		
		ConceptSource newConceptSource = new ConceptSource();
		newConceptSource.setName("name");
		newConceptSource.setDescription("desc");
		newConceptSource.setHl7Code("hl7Code");
		
		newConceptSource.setDateCreated(expectedDate);
		Context.getConceptService().saveConceptSource(newConceptSource);
		
		assertEquals(DateUtil.truncateToSeconds(expectedDate), newConceptSource.getDateCreated());
	}
	
	/**
	 * @see ConceptService#getConcept(String)
	 */
	// @Test
	public void getConcept_shouldReturnNullGivenNullParameter() {
		assertNull(Context.getConceptService().getConcept((String) null));
	}
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 */
	// @Test
	public void getConceptByName_shouldReturnNullGivenNullParameter() {
		assertNull(Context.getConceptService().getConceptByName(null));
	}
	
	/**
	 * This test verifies that {@link ConceptName}s are fetched correctly from the hibernate cache.
	 * (Or really, not fetched from the cache but instead are mapped with lazy=false. For some
	 * reason Hibernate isn't able to find objects in the cache if a parent object was the one that
	 * loaded them)
	 * 
	 * @throws Exception
	 */
	// @Test
	public void shouldFetchNamesForConceptsThatWereFirstFetchedAsNumerics() {
		Concept concept = Context.getConceptService().getConcept(5089);
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(5089);
		
		conceptNumeric.getNames().size();
		concept.getNames().size();
	}
	
	/**
	 * This test verifies that {@link ConceptDescription}s are fetched correctly from the hibernate
	 * cache. (Or really, not fetched from the cache but instead are mapped with lazy=false. For
	 * some reason Hibernate isn't able to find objects in the cache if a parent object was the one
	 * loaded them)
	 * 
	 * @throws Exception
	 */
	// @Test
	public void shouldFetchDescriptionsForConceptsThatWereFirstFetchedAsNumerics() {
		Concept concept = Context.getConceptService().getConcept(5089);
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(5089);
		
		conceptNumeric.getDescriptions().size();
		concept.getDescriptions().size();
	}
	
	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	// @Test
	public void getConceptByMapping_shouldGetConceptWithGivenCodeAndSourceHl7Code() {
		Concept concept = conceptService.getConceptByMapping("WGT234", "SSTRM");
		assertEquals(5089, concept.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	// @Test
	public void getConceptByMapping_shouldGetConceptWithGivenCodeAndSourceName() {
		Concept concept = conceptService.getConceptByMapping("WGT234", "Some Standardized Terminology");
		assertEquals(5089, concept.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	// @Test
	public void getConceptByMapping_shouldReturnNullIfSourceCodeDoesNotExist() {
		Concept concept = conceptService.getConceptByMapping("A random concept code", "A random source code");
		assertNull(concept);
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	// @Test
	public void getConceptByMapping_shouldReturnNullIfNoMappingExists() {
		Concept concept = conceptService.getConceptByMapping("A random concept code", "SSTRM");
		assertNull(concept);
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	// @Test
	public void getConceptByMapping_shouldReturnRetiredConceptByDefaultIfOnlyMatch() {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM");
		assertEquals(24, concept.getId().intValue());
		
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	// @Test
	public void getConceptByMapping_shouldReturnRetiredConceptIfOnlyMatch() {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM", true);
		assertEquals(24, concept.getId().intValue());
		
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	// @Test
	public void getConceptByMapping_shouldNotReturnRetiredConcept() {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM", false);
		assertNull(concept);
		
	}
	
	// @Test
	public void getConceptByMapping_shouldThrowExceptionIfTwoConceptsHaveSameMapping() {
		assertThrows(APIException.class, () -> conceptService.getConceptByMapping("127689", "Some Standardized Terminology"));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	// @Test
	public void getConceptsByMapping_shouldGetConceptsWithGivenCodeAndSourceH17Code() {
		List<Concept> concepts = conceptService.getConceptsByMapping("127689", "Some Standardized Terminology");
		assertEquals(2, concepts.size());
		assertTrue(containsId(concepts, 16));
		assertTrue(containsId(concepts, 6));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	// @Test
	public void getConceptsByMapping_shouldGetConceptsWithGivenCodeAndSourceName() {
		List<Concept> concepts = conceptService.getConceptsByMapping("127689", "SSTRM");
		assertEquals(2, concepts.size());
		assertTrue(containsId(concepts, 16));
		assertTrue(containsId(concepts, 6));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	// @Test
	public void getConceptsByMapping_shouldReturnEmptyListIfSourceCodeDoesNotExist() {
		List<Concept> concept = conceptService.getConceptsByMapping("A random concept code", "A random source code");
		assertThat(concept, is(empty()));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	// @Test
	public void getConceptsByMapping_shouldReturnEmptyListIfNoMappingsExist() {
		List<Concept> concept = conceptService.getConceptsByMapping("A random concept code", "SSTRM");
		assertThat(concept, is(empty()));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	// @Test
	public void getConceptsByMapping_shouldReturnRetiredAndNonRetiredConceptsByDefault() {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM");
		assertEquals(2, concepts.size());
		assertTrue(containsId(concepts, 16));
		assertTrue(containsId(concepts, 24));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	// @Test
	public void getConceptsByMapping_shouldOnlyReturnNonRetiredConcepts() {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", false);
		assertEquals(1, concepts.size());
		assertTrue(containsId(concepts, 16));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	// @Test
	public void getConceptsByMapping_shouldReturnRetiredAndNonRetiredConcepts() {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", true);
		assertEquals(2, concepts.size());
		assertTrue(containsId(concepts, 16));
		assertTrue(containsId(concepts, 24));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	// @Test
	public void getConceptsByMapping_shouldSortNonRetiredConceptsFirst() {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", true);
		assertEquals(16, concepts.get(0).getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	// @Test
	public void getConceptByMapping_shouldIgnoreCase() {
		Concept concept = conceptService.getConceptByMapping("wgt234", "sstrm");
		assertEquals(5089, concept.getId().intValue());
	}

	/**
	 * @see ConceptService#getConceptIdsByMapping(String,String)
	 */
	// @Test
	public void getConceptIdsByMapping_shouldPopulateCache() {
		Cache cache = cacheManager.getCache("conceptIdsByMapping");
		cache.clear();
		SimpleKey key = new SimpleKey("wgt234", "sstrm", true);
		assertThat(cache.get(key), is(nullValue()));
		List<Integer> conceptIdsByMapping = conceptService.getConceptIdsByMapping("wgt234", "sstrm", true);
		assertThat(cache.get(key).get(), is(conceptIdsByMapping));
	}

	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	// @Test
	public void shouldEvictConceptIdsIfSourceOrTermsAreUpdated() {
		Cache cache = cacheManager.getCache("conceptIdsByMapping");
		ConceptSource cs = conceptService.getConceptSourceByHL7Code("SSTRM");
		ConceptReferenceTerm crt = conceptService.getConceptReferenceTermByCode("WGT234", cs);
		ConceptReferenceTerm dummyTerm = new ConceptReferenceTerm(cs, "DUMMY", "DummyTerm");
		conceptService.saveConceptReferenceTerm(dummyTerm);

		// Update Concept Source
		SimpleKey cacheKey = new SimpleKey(crt.getCode(), cs.getHl7Code(), true);
		List<Integer> conceptIdsByMapping = conceptService.getConceptIdsByMapping(crt.getCode(), cs.getHl7Code(), true);
		assertThat(cache.get(cacheKey).get(), is(conceptIdsByMapping));
		cs.setDateChanged(new Date());
		conceptService.saveConceptSource(cs);
		assertThat(cache.get(cacheKey), is(nullValue()));

		// Save Concept Reference Term
		conceptIdsByMapping = conceptService.getConceptIdsByMapping(crt.getCode(), cs.getHl7Code(), true);
		assertThat(cache.get(cacheKey).get(), is(conceptIdsByMapping));
		crt.setDateChanged(new Date());
		conceptService.saveConceptReferenceTerm(crt);
		assertThat(cache.get(cacheKey), is(nullValue()));

		// purgeConceptReferenceTerm
		conceptIdsByMapping = conceptService.getConceptIdsByMapping(crt.getCode(), cs.getHl7Code(), true);
		assertThat(cache.get(cacheKey).get(), is(conceptIdsByMapping));
		conceptService.purgeConceptReferenceTerm(dummyTerm);
		assertThat(cache.get(cacheKey), is(nullValue()));
	}
	
	/**
	 * @see ConceptService#getConceptAnswerByUuid(String)
	 */
	// @Test
	public void getConceptAnswerByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "b1230431-2fe5-49fc-b535-ae42bc849747";
		ConceptAnswer conceptAnswer = Context.getConceptService().getConceptAnswerByUuid(uuid);
		assertEquals(1, (int) conceptAnswer.getConceptAnswerId());
	}
	
	/**
	 * @see ConceptService#getConceptAnswerByUuid(String)
	 */
	// @Test
	public void getConceptAnswerByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptAnswerByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptByUuid(String)
	 */
	// @Test
	public void getConceptByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
		Concept concept = Context.getConceptService().getConceptByUuid(uuid);
		assertEquals(3, (int) concept.getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptByUuid(String)
	 */
	// @Test
	public void getConceptByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptClassByUuid(String)
	 */
	// @Test
	public void getConceptClassByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "97097dd9-b092-4b68-a2dc-e5e5be961d42";
		ConceptClass conceptClass = Context.getConceptService().getConceptClassByUuid(uuid);
		assertEquals(1, (int) conceptClass.getConceptClassId());
	}
	
	/**
	 * @see ConceptService#getConceptClassByUuid(String)
	 */
	// @Test
	public void getConceptClassByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptClassByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByUuid(String)
	 */
	// @Test
	public void getConceptDatatypeByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "8d4a4488-c2cc-11de-8d13-0010c6dffd0f";
		ConceptDatatype conceptDatatype = Context.getConceptService().getConceptDatatypeByUuid(uuid);
		assertEquals(1, (int) conceptDatatype.getConceptDatatypeId());
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByUuid(String)
	 */
	// @Test
	public void getConceptDatatypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptDatatypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptDescriptionByUuid(String)
	 */
	// @Test
	public void getConceptDescriptionByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "5f4d710b-d333-40b7-b449-6e0e739d15d0";
		ConceptDescription conceptDescription = Context.getConceptService().getConceptDescriptionByUuid(uuid);
		assertEquals(1, (int) conceptDescription.getConceptDescriptionId());
	}
	
	/**
	 * @see ConceptService#getConceptDescriptionByUuid(String)
	 */
	// @Test
	public void getConceptDescriptionByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptDescriptionByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptNameByUuid(String)
	 */
	// @Test
	public void getConceptNameByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "9bc5693a-f558-40c9-8177-145a4b119ca7";
		ConceptName conceptName = Context.getConceptService().getConceptNameByUuid(uuid);
		assertEquals(1439, (int) conceptName.getConceptNameId());
	}
	
	/**
	 * @see ConceptService#getConceptNameByUuid(String)
	 */
	// @Test
	public void getConceptNameByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptNameByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptNameTagByUuid(String)
	 */
	// @Test
	public void getConceptNameTagByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "9e9df183-2328-4117-acd8-fb9bf400911d";
		ConceptNameTag conceptNameTag = Context.getConceptService().getConceptNameTagByUuid(uuid);
		assertEquals(1, (int) conceptNameTag.getConceptNameTagId());
	}
	
	/**
	 * @see ConceptService#getConceptNameTagByUuid(String)
	 */
	// @Test
	public void getConceptNameTagByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptNameTagByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptNumericByUuid(String)
	 */
	// @Test
	public void getConceptNumericByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumericByUuid(uuid);
		assertEquals(5089, (int) conceptNumeric.getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptNumericByUuid(String)
	 */
	// @Test
	public void getConceptNumericByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptNumericByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptProposalByUuid(String)
	 */
	// @Test
	public void getConceptProposalByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "57a68666-5067-11de-80cb-001e378eb67e";
		ConceptProposal conceptProposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		assertEquals(1, (int) conceptProposal.getConceptProposalId());
	}
	
	/**
	 * @see ConceptService#getConceptProposalByUuid(String)
	 */
	// @Test
	public void getConceptProposalByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptProposalByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptSetByUuid(String)
	 */
	// @Test
	public void getConceptSetByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "1a111827-639f-4cb4-961f-1e025bf88d90";
		ConceptSet conceptSet = Context.getConceptService().getConceptSetByUuid(uuid);
		assertEquals(1, (int) conceptSet.getConceptSetId());
	}
	
	/**
	 * @see ConceptService#getConceptSetByUuid(String)
	 */
	// @Test
	public void getConceptSetByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptSetByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptSourceByUuid(String)
	 */
	// @Test
	public void getConceptSourceByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "75f5b378-5065-11de-80cb-001e378eb67e";
		ConceptSource conceptSource = Context.getConceptService().getConceptSourceByUuid(uuid);
		assertEquals(3, (int) conceptSource.getConceptSourceId());
	}
	
	/**
	 * @see ConceptService#getConceptSourceByUuid(String)
	 */
	// @Test
	public void getConceptSourceByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptSourceByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getDrugByUuid(String)
	 */
	// @Test
	public void getDrugByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "3cfcf118-931c-46f7-8ff6-7b876f0d4202";
		Drug drug = Context.getConceptService().getDrugByUuid(uuid);
		assertEquals(2, (int) drug.getDrugId());
	}
	
	/**
	 * @see ConceptService#getDrugByUuid(String)
	 */
	// @Test
	public void getDrugByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getDrugByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getDrugIngredientByUuid(String)}
	 */
	// @Test
	public void getDrugIngredientByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "6519d653-393d-4118-9c83-a3715b82d4dc";
		DrugIngredient ingredient = Context.getConceptService().getDrugIngredientByUuid(uuid);
		assertEquals(88, (int) ingredient.getIngredient().getConceptId());
	}
	
	/**
	 * @see {@link ConceptService#getDrugIngredientByUuid(String)}
	 */
	// @Test
	public void getDrugIngredientByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getDrugIngredientByUuid("some invalid uuid"));
	}
	
	// @Test
	public void getDrugs_shouldReturnDrugsThatAreNotRetired() {
		List<Drug> drugs = Context.getConceptService().getDrugs("ASPIRIN" /* is not retired */);
		assertFalse(drugs.get(0).getRetired());
	}
	
	/**
	 * @see ConceptService#getDrugs(String)
	 */
	// @Test
	public void getDrugs_shouldNotReturnDrugsThatAreRetired() {
		List<Drug> drugs = Context.getConceptService().getDrugs("TEST_DRUG_NAME_RETIRED" /* is retired */);
		assertEquals(0, drugs.size());
	}
	
	/**
	 * @see ConceptService#getDrugs(String)
	 */
	// @Test
	public void getDrugs_shouldReturnDrugsByDrugId() {
		Integer drugId = 2;
		Drug drug = Context.getConceptService().getDrug(drugId);
		List<Drug> drugs = Context.getConceptService().getDrugs(String.valueOf(drugId));
		assertTrue(drugs.contains(drug));
	}
	
	/**
	 * @see ConceptService#getDrugs(String)
	 */
	// @Test
	public void getDrugs_shouldNotFailIfThereisNoDrugByGivenDrugId() {
		List<Drug> drugs = Context.getConceptService().getDrugs("123456");
		assertNotNull(drugs);
	}
	
	/**
	 * @see ConceptService#getDrugs(String)
	 */
	// @Test
	public void getDrugs_shouldReturnDrugsByDrugConceptId() {
		Integer conceptId = 792;
		Drug drug = Context.getConceptService().getDrug(2);
		
		// assert that given drug has concept with tested id
		assertNotNull(drug.getConcept());
		assertEquals(drug.getConcept().getConceptId(), conceptId);
		
		List<Drug> drugs = Context.getConceptService().getDrugs(String.valueOf(conceptId));
		assertTrue(drugs.contains(drug));
	}
	
	/**
	 * This tests for being able to find concepts with names in en_GB locale when the user is in the
	 * en locale.
	 * 
	 * @see ConceptService#getConceptByName(String)
	 */
	// @Test
	public void getConceptByName_shouldFindConceptsWithNamesInMoreSpecificLocales() {
		Locale origLocale = Context.getLocale();
		
		executeDataSet(INITIAL_CONCEPTS_XML);
		Context.setLocale(Locale.ENGLISH);
		
		// make sure that concepts are found that have a specific locale on them
		assertNotNull(Context.getConceptService().getConceptByName("Numeric name with en_GB locale"));
		
		// find concepts with same generic locale
		assertNotNull(Context.getConceptService().getConceptByName("Some numeric concept name"));
		
		// reset the locale for the next test
		Context.setLocale(origLocale);
	}
	
	/**
	 * This tests for being able to find concepts with names in the en locale when the user is in
	 * the en_GB locale
	 * 
	 * @see ConceptService#getConceptByName(String)
	 */
	// @Test
	public void getConceptByName_shouldFindConceptsWithNamesInMoreGenericLocales() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		//prior tests have changed the locale to 'en_US', so we need to set it back
		Context.setLocale(Locale.UK);
		// make sure that concepts are found that have a specific locale on them
		assertNotNull(Context.getConceptService().getConceptByName("Some numeric concept name"));
	}
	
	/**
	 * This tests for being able to find concepts with names in en_GB locale when the user is in the
	 * en_GB locale.
	 * 
	 * @see ConceptService#getConceptByName(String)
	 */
	// @Test
	public void getConceptByName_shouldFindConceptsWithNamesInSameSpecificLocale() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Context.setLocale(Locale.UK);
		
		// make sure that concepts are found that have a specific locale on them
		assertNotNull(Context.getConceptService().getConceptByName("Numeric name with en_GB locale"));
	}
	
	/**
	 * @see ConceptService#retireConceptSource(ConceptSource,String)
	 */
	// @Test
	public void retireConceptSource_shouldRetireConceptSource() {
		ConceptSource cs = conceptService.getConceptSource(3);
		conceptService.retireConceptSource(cs, "dummy reason for retirement");
		
		cs = conceptService.getConceptSource(3);
		assertTrue(cs.getRetired());
		assertEquals("dummy reason for retirement", cs.getRetireReason());
	}
	
	// @Test
	public void saveConcept_shouldCreateNewConceptInDatabase() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept conceptToAdd = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		conceptToAdd.addName(cn);
		conceptToAdd.addDescription(new ConceptDescription("some description",null));
		conceptToAdd.setDatatype(new ConceptDatatype(1));
		conceptToAdd.setConceptClass(new ConceptClass(1));
		assertFalse(conceptService.getAllConcepts().contains(conceptToAdd));
		conceptService.saveConcept(conceptToAdd);
		assertTrue(conceptService.getAllConcepts().contains(conceptToAdd));
	}
	
	// @Test
	public void saveConcept_shouldUpdateConceptAlreadyExistingInDatabase() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// using isSet() as a value to check and change
		assertFalse(conceptService.getConcept(2).getSet());
		Concept concept = conceptService.getConcept(2);
		// change a value
		concept.setSet(true);
		
		// save the concept
		conceptService.saveConcept(concept);
		// see if the value was updated in the database
		assertTrue(conceptService.getConcept(2).getSet());
	}
	
	// @Test
	public void getConceptSourceByName_shouldGetConceptSourceWithTheGivenName() {
		ConceptSource conceptSource = conceptService.getConceptSourceByName("SNOMED CT");
		assertEquals(Integer.valueOf(2), conceptSource.getConceptSourceId(), "Method did not retrieve ConceptSource by name");
	}
	
	// @Test
	public void getConceptSourceByName_shouldReturnNullIfNoConceptSourceWithThatNameIsFound() {
		ConceptSource conceptSource = conceptService.getConceptSourceByName("Some invalid name");
		assertNull(conceptSource, "Method did not return null when no ConceptSource with that name is found");
	}

	/**
	 * @see ConceptService#getConceptSourceByUniqueId(String)
	 */
	// @Test
	public void getConceptSourceByUniqueId_shouldGetConceptSourceWithTheGivenUniqueId() {

		String existingUniqueId = "2.16.840.1.113883.6.96";
		ConceptSource conceptSource = conceptService.getConceptSourceByUniqueId(existingUniqueId);
		assertThat(conceptSource, is(not(nullValue())));
		assertThat(conceptSource.getUniqueId(), is(existingUniqueId));
	}

	/**
	 * @see ConceptService#getConceptSourceByUniqueId(String)
	 */
	// @Test public void getConceptSourceByUniqueId_shouldReturnNullIfNoConceptSourceWithGivenUniqueIdIsFound()
	{

		assertThat(conceptService.getConceptSourceByUniqueId("9.99999.999.9999.999.999.999.9999.99"), is(nullValue()));
	}

	/**
	 * @see ConceptService#getConceptSourceByUniqueId(String)
	 */
	// @Test
	public void getConceptSourceByUniqueId_shouldReturnNullIfGivenAnEmptyString() {

		assertThat(conceptService.getConceptSourceByUniqueId(""), is(nullValue()));
		assertThat(conceptService.getConceptSourceByUniqueId("    "), is(nullValue()));
	}

	/**
	 * @see ConceptService#getConceptSourceByUniqueId(String)
	 */
	// @Test
	public void getConceptSourceByUniqueId_shouldFailIfGivenNull() {

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> conceptService.getConceptSourceByUniqueId(null));
		assertThat(exception.getMessage(), is("uniqueId is required"));
	}
	
	/**
	 * @see ConceptService#getConceptSourceByHL7Code(String)
	 */
	// @Test
	public void getConceptSourceByHL7Code_shouldGetConceptSourceWithTheGivenUniqueId() {

		String existinghl7Code = "SCT";
		ConceptSource conceptSource = conceptService.getConceptSourceByHL7Code(existinghl7Code);
		assertThat(conceptSource, is(not(nullValue())));
		assertThat(conceptSource.getHl7Code(), is(existinghl7Code));
	}	
	
	/**
	 * @see ConceptService#getConceptSourceByHL7Code(String)
	 */
	// @Test public void getConceptSourceByHL7Code_shouldReturnNullIfNoConceptSourceWithGivenUniqueIdIsFound()
	{

		assertThat(conceptService.getConceptSourceByHL7Code("XXXXX"), is(nullValue()));
	}
	
	/**
	 * @see ConceptService#getConceptSourceByHL7Code(String)
	 */
	// @Test
	public void getConceptSourceByHL7Code_shouldReturnNullIfGivenAnEmptyString() {

		assertThat(conceptService.getConceptSourceByHL7Code(""), is(nullValue()));
		assertThat(conceptService.getConceptSourceByHL7Code("    "), is(nullValue()));
	}

	/**
	 * @see ConceptService#getConceptSourceByHL7Code(String)
	 */
	// @Test
	public void getConceptSourceByHL7Code_shouldFailIfGivenNull() {

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> conceptService.getConceptSourceByHL7Code(null));
		assertThat(exception.getMessage(), is("hl7Code is required"));
	}
	
	// @Test
	public void getConceptsByConceptSource_shouldReturnAListOfConceptMapsIfConceptMappingsFound() {
		List<ConceptMap> list = conceptService
		        .getConceptMappingsToSource(conceptService.getConceptSourceByName("SNOMED CT"));
		assertEquals(2, list.size());
	}
	
	// @Test
	public void getConceptsByConceptSource_shouldReturnEmptyListOfConceptMapsIfNoneFound() {
		List<ConceptMap> list = conceptService.getConceptMappingsToSource(conceptService
		        .getConceptSourceByName("Some invalid name"));
		assertEquals(0, list.size());
	}
	
	// @Test
	public void saveConceptSource_shouldSaveAConceptSourceWithANullHl7Code() {
		ConceptSource source = new ConceptSource();
		String aNullString = null;
		String sourceName = "A concept source with null HL7 code";
		source.setName(sourceName);
		source.setDescription("A concept source description");
		source.setHl7Code(aNullString);
		conceptService.saveConceptSource(source);
		assertEquals(source, conceptService.getConceptSourceByName(sourceName), "Did not save a ConceptSource with a null hl7Code");
		
	}
	
	// @Test
	public void saveConceptSource_shouldNotSaveAConceptSourceIfVoidedIsNull() {
		ConceptSource source = new ConceptSource();
		source.setRetired(null);
		assertNull(source.getRetired());
		
		assertThrows(Exception.class, () -> conceptService.saveConceptSource(source));
		
	}
	
	// @Test
	public void saveConceptNameTag_shouldSaveAConceptNameTagIfATagDoesNotExist() {
		ConceptNameTag nameTag = new ConceptNameTag();
		nameTag.setTag("a new tag");
		
		ConceptNameTag savedNameTag = conceptService.saveConceptNameTag(nameTag);
		
		assertNotNull(nameTag.getId());
		assertEquals(savedNameTag.getId(), nameTag.getId());
	}
	
	// @Test
	public void saveConceptNameTag_shouldNotSaveAConceptNameTagIfTagExists() {
		String tag = "a new tag";
		
		ConceptNameTag nameTag = new ConceptNameTag();
		nameTag.setTag(tag);
		
		conceptService.saveConceptNameTag(nameTag);
		
		ConceptNameTag secondNameTag = new ConceptNameTag();
		secondNameTag.setTag(tag);
		
		assertThrows(Exception.class, () -> conceptService.saveConceptNameTag(secondNameTag));
		assertNull(secondNameTag.getId());
		
	}
	
	// @Test
	public void saveConcept_shouldNotUpdateConceptDataTypeIfConceptIsAttachedToAnObservation() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept concept = conceptService.getConcept(2);
		assertNotNull(concept);
		
		ObsService obsService = Context.getObsService();
		Obs obs = new Obs(Context.getPersonService().getPerson(1), concept, new Date(), Context.getLocationService()
		        .getLocation(1));
		obs.setValueCoded(Context.getConceptService().getConcept(7));
		obsService.saveObs(obs, "Creating a new observation with a concept");
		
		ConceptDatatype newDatatype = conceptService.getConceptDatatypeByName("Text");
		concept.setDatatype(newDatatype);
		assertThrows(ConceptInUseException.class, () -> conceptService.saveConcept(concept));
	}
	
	// @Test
	public void saveConcept_shouldUpdateConceptIfConceptIsAttachedToAnObservationAndItIsANonDatatypeChange()
	{
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept concept = conceptService.getConcept(1);
		assertNotNull(concept);
		
		ObsService obsService = Context.getObsService();
		Obs obs = new Obs(new Person(1), concept, new Date(), new Location(1));
		obs.setValueCoded(Context.getConceptService().getConcept(7));
		obsService.saveObs(obs, "Creating a new observation with a concept");
		
		conceptService.saveConcept(concept);
	}
	
	/**
	 * @see ConceptService#getFalseConcept()
	 */
	// @Test
	public void getFalse_shouldReturnTheFalseConcept() {
		createTrueFalseGlobalProperties();
		assertNotNull(conceptService.getFalseConcept());
		assertEquals(8, conceptService.getFalseConcept().getId().intValue());
	}
	
	/**
	 * @see ConceptService#getTrueConcept()
	 */
	// @Test
	public void getTrue_shouldReturnTheTrueConcept() {
		createTrueFalseGlobalProperties();
		assertNotNull(conceptService.getTrueConcept());
		assertEquals(7, conceptService.getTrueConcept().getId().intValue());
	}
	
	/**
	 * @see ConceptService#getUnknownConcept()
	 */
	// @Test
	public void getUnknownConcept_shouldReturnTheUnknownConcept() {
		createUnknownConceptGlobalProperty();
		assertNotNull(conceptService.getUnknownConcept());
		assertEquals(9, conceptService.getUnknownConcept().getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByName(String)
	 */
	// @Test
	public void changeConceptFromBooleanToCoded_shouldConvertTheDatatypeOfABooleanConceptToCoded() {
		Concept concept = conceptService.getConcept(18);
		assertEquals(conceptService.getConceptDatatypeByName("Boolean").getConceptDatatypeId(), concept.getDatatype()
		        .getConceptDatatypeId());
		conceptService.convertBooleanConceptToCoded(concept);
		assertEquals(conceptService.getConceptDatatypeByName("Coded").getConceptDatatypeId(), concept.getDatatype()
		        .getConceptDatatypeId());
	}
	
	/**
	 * @see ConceptService#getConcept(Integer)
	 */
	// @Test
	public void changeConceptFromBooleanToCoded_shouldFailIfTheDatatypeOfTheConceptIsNotBoolean() {
		Concept concept = conceptService.getConcept(5497);
		assertThrows(APIException.class, () -> conceptService.convertBooleanConceptToCoded(concept));
	}
	
	/**
	 * @see ConceptService#convertBooleanConceptToCoded(Concept)
	 */
	// @Test
	public void changeConceptFromBooleanToCoded_shouldExplicitlyAddFalseConceptAsAValue_CodedAnswer() {
		Concept concept = conceptService.getConcept(18);
		Collection<ConceptAnswer> answers = concept.getAnswers(false);
		boolean falseConceptFound = false;
		//initially the concept shouldn't present
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getFalseConcept())) {
				falseConceptFound = true;
			}
		}
		assertFalse(falseConceptFound);
		conceptService.convertBooleanConceptToCoded(concept);
		answers = concept.getAnswers(false);
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getFalseConcept())) {
				falseConceptFound = true;
			}
		}
		assertTrue(falseConceptFound);
	}
	
	/**
	 * @see ConceptService#convertBooleanConceptToCoded(Concept)
	 */
	// @Test
	public void changeConceptFromBooleanToCoded_shouldExplicitlyAddTrueConceptAsAValue_CodedAnswer() {
		Concept concept = conceptService.getConcept(18);
		Collection<ConceptAnswer> answers = concept.getAnswers(false);
		boolean trueConceptFound = false;
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getTrueConcept())) {
				trueConceptFound = true;
			}
		}
		assertFalse(trueConceptFound);
		conceptService.convertBooleanConceptToCoded(concept);
		answers = concept.getAnswers(false);
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getTrueConcept())) {
				trueConceptFound = true;
			}
		}
		assertTrue(trueConceptFound);
	}
	
	/**
	 * @see ConceptService#getFalseConcept()
	 */
	// @Test
	public void getFalseConcept_shouldReturnTheFalseConcept() {
		createTrueFalseGlobalProperties();
		assertEquals(8, conceptService.getFalseConcept().getConceptId().intValue());
	}
	
	/**
	 * @see ConceptService#getTrueConcept()
	 */
	// @Test
	public void getTrueConcept_shouldReturnTheTrueConcept() {
		createTrueFalseGlobalProperties();
		assertEquals(7, conceptService.getTrueConcept().getConceptId().intValue());
	}
	
	/**
	 * Utility method that creates the global properties 'concept.true' and 'concept.false'
	 */
	private static void createTrueFalseGlobalProperties() {
		GlobalProperty trueConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT, "7",
		        "Concept id of the concept defining the TRUE boolean concept");
		GlobalProperty falseConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT, "8",
		        "Concept id of the concept defining the TRUE boolean concept");
		Context.getAdministrationService().saveGlobalProperty(trueConceptGlobalProperty);
		Context.getAdministrationService().saveGlobalProperty(falseConceptGlobalProperty);
	}
	
	/**
	 * Utility method that creates the global property concept.unknown'
	 */
	private static void createUnknownConceptGlobalProperty() {
		GlobalProperty unknownConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_UNKNOWN_CONCEPT,
		        "9", "Concept id of the concept defining the UNKNOWN concept");
		Context.getAdministrationService().saveGlobalProperty(unknownConceptGlobalProperty);
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByName(String)
	 */
	// @Test
	public void getConceptDatatypeByName_shouldNotReturnAFuzzyMatchOnName() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		ConceptDatatype result = conceptService.getConceptDatatypeByName("Tex");
		assertNull(result);
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByName(String)
	 */
	// @Test
	public void getConceptDatatypeByName_shouldReturnAnExactMatchOnName() {
		// given
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// when
		ConceptDatatype result = conceptService.getConceptDatatypeByName("Text");
		
		// then
		assertEquals("Text", result.getName());
	}
	
	/**
	 * @see ConceptService#purgeConcept(Concept)
	 */
	// @Test
	public void purgeConcept_shouldFailIfAnyOfTheConceptNamesOfTheConceptIsBeingUsedByAnObs() {
		Obs o = new Obs();
		o.setConcept(Context.getConceptService().getConcept(3));
		o.setPerson(new Patient(2));
		o.setEncounter(new Encounter(3));
		o.setObsDatetime(new Date());
		o.setLocation(new Location(1));
		ConceptName conceptName = new ConceptName(1847);
		o.setValueCodedName(conceptName);
		Context.getObsService().saveObs(o, null);
		//ensure that the association between the conceptName and the obs has been established
		assertTrue(conceptService.hasAnyObservation(conceptName));
		
		Concept concept = conceptService.getConceptByName("cd4 count");
		//make sure the name concept name exists
		assertNotNull(concept);
		assertThrows(ConceptNameInUseException.class, () -> conceptService.purgeConcept(concept));
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldCreateANewConceptNameWhenTheOldNameIsChanged() {
		Concept concept = conceptService.getConceptByName("cd4 count");
		assertEquals(3, concept.getNames(true).size());
		ConceptName oldName = null;
		for (ConceptName cn : concept.getNames()) {
			if (cn.getConceptNameId().equals(1847)) {
				oldName = cn;
				cn.setName("new name");
			}
		}
		
		conceptService.saveConcept(concept);
		
		//force Hibernate interceptor to set dateCreated
		Context.flushSession();
		
		assertEquals(4, concept.getNames(true).size());
		
		for (ConceptName cn : concept.getNames()) {
			if (cn.getName().equals("new name")) {
				assertTrue(oldName.getDateCreated().before(cn.getDateCreated()));
			}
		}
		
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldVoidTheConceptNameIfTheTextOfTheNameHasChanged() {
		Concept concept = conceptService.getConceptByName("cd4 count");
		assertFalse(conceptService.getConceptName(1847).getVoided());
		for (ConceptName cn : concept.getNames()) {
			if (cn.getConceptNameId().equals(1847)) {
				cn.setName("new name");
			}
		}
		//ensure that the conceptName has actually been found and replaced
		assertTrue(concept.hasName("new name", new Locale("en", "GB")));
		conceptService.saveConcept(concept);
		assertTrue(conceptService.getConceptName(1847).getVoided());
	}
	
	// @Test
	public void getConceptsByConceptSet_shouldReturnAllConceptsInSet() {
		
		executeDataSet(GET_CONCEPTS_BY_SET_XML);
		
		Concept concept = conceptService.getConcept(1);
		
		List<Concept> conceptSet = conceptService.getConceptsByConceptSet(concept);
		
		assertThat(conceptSet, containsInAnyOrder(hasId(2), hasId(3), hasId(4), hasId(5), hasId(6)));
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(org.openmrs.ConceptStopWord)
	 */
	// @Test
	public void saveConceptStopWord_shouldSaveConceptStopWordIntoDatabase() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("AND", Locale.FRANCE);
		conceptService.saveConceptStopWord(conceptStopWord);
		
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.FRANCE);
		assertEquals(1, conceptStopWords.size());
		assertEquals("AND", conceptStopWords.get(0));
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(ConceptStopWord)
	 */
	// @Test
	public void saveConceptStopWord_shouldSaveConceptStopWordAssignDefaultLocaleIsItNull() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("The");
		conceptService.saveConceptStopWord(conceptStopWord);
		
		List<String> conceptStopWords = conceptService.getConceptStopWords(Context.getLocale());
		assertThat(conceptStopWords, hasItem("THE"));
	}
	
	/**
	 * @see ConceptService#getConceptStopWords(Locale)
	 */
	// @Test
	public void getConceptStopWords_shouldReturnDefaultLocaleConceptStopWordsIfLocaleIsNull() {
		List<String> conceptStopWords = conceptService.getConceptStopWords(null);
		assertEquals(1, conceptStopWords.size());
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(ConceptStopWord)
	 */
	// @Test
	public void saveConceptStopWord_shouldSaveReturnConceptStopWordWithId() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("A", Locale.UK);
		ConceptStopWord savedConceptStopWord = conceptService.saveConceptStopWord(conceptStopWord);
		
		assertNotNull(savedConceptStopWord.getId());
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(ConceptStopWord)
	 */
	// @Test
	public void saveConceptStopWord_shouldFailIfADuplicateConceptStopWordInALocaleIsAdded() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("A");
		try {
			conceptService.saveConceptStopWord(conceptStopWord);
			assertThrows(ConceptStopWordException.class, () -> conceptService.saveConceptStopWord(conceptStopWord));
		}
		catch (ConceptStopWordException e) {
			assertEquals("ConceptStopWord.duplicated", e.getMessage());
			throw e;
		}
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(ConceptStopWord)
	 */
	// @Test
	public void saveConceptStopWord_shouldSaveConceptStopWordInUppercase() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("lowertoupper");
		ConceptStopWord savedConceptStopWord = conceptService.saveConceptStopWord(conceptStopWord);
		
		assertEquals("LOWERTOUPPER", savedConceptStopWord.getValue());
	}
	
	/**
	 * @see ConceptService#getConceptStopWords(Locale)
	 */
	// @Test
	public void getConceptStopWords_shouldReturnListOfConceptStopWordsForGivenLocale() {
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.ENGLISH);
		
		assertThat(conceptStopWords, containsInAnyOrder("A", "AN"));
	}
	
	/**
	 * @see ConceptService#getAllConceptStopWords()
	 */
	// @Test
	public void getAllConceptStopWords_shouldReturnAllConceptStopWords() {
		List<ConceptStopWord> conceptStopWords = conceptService.getAllConceptStopWords();
		assertEquals(4, conceptStopWords.size());
	}
	
	/**
	 * @see ConceptService#getAllConceptStopWords()
	 */
	// @Test
	public void getAllConceptStopWords_shouldReturnEmptyListIfNoRecordFound() {
		conceptService.deleteConceptStopWord(1);
		conceptService.deleteConceptStopWord(2);
		conceptService.deleteConceptStopWord(3);
		conceptService.deleteConceptStopWord(4);
		
		List<ConceptStopWord> conceptStopWords = conceptService.getAllConceptStopWords();
		assertEquals(0, conceptStopWords.size());
	}
	
	/**
	 * @see ConceptService#getConceptStopWords(Locale)
	 */
	// @Test
	public void getConceptStopWords_shouldReturnEmptyListIfNoConceptStopWordsForGivenLocale() {
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.GERMANY);
		assertEquals(0, conceptStopWords.size());
	}
	
	/**
	 * @see ConceptService#deleteConceptStopWord(Integer)
	 */
	// @Test
	public void deleteConceptStopWord_shouldDeleteTheGivenConceptStopWord() {
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.US);
		
		assertEquals(1, conceptStopWords.size());
		
		conceptService.deleteConceptStopWord(4);
		
		conceptStopWords = conceptService.getConceptStopWords(Locale.US);
		assertEquals(0, conceptStopWords.size());
	}
	
	/**
	 * This test fetches all concepts in the xml test dataset and ensures that every locale for a
	 * concept name is among those listed in the global property 'locale.allowed.list' and default
	 * locale. NOTE that it doesn't test a particular API method directly.
	 */
	// @Test
	public void saveConcept_shouldNotAcceptALocaleThatIsNeitherAmongTheLocaleAllowedListNorADefaultLocale() {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			if (!CollectionUtils.isEmpty(concept.getNames())) {
				for (ConceptName cn : concept.getNames()) {
					assertTrue(allowedLocales.contains(cn.getLocale()), "The locale '" + cn.getLocale() + "' of conceptName with id: " + cn.getConceptNameId() + " is not among those listed in the global property 'locale.allowed.list'");
				}
			}
		}
	}
	
	/**
	 * This test fetches all concepts in the xml test dataset and ensures that every locale that has
	 * atleast one conceptName has a name marked as preferred. NOTE that it doesn't test a
	 * particular API method directly.
	 */
	// @Test
	public void saveConcept_shouldAlwaysReturnAPreferredNameForEveryLocaleThatHasAtleastOneUnvoidedName() {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			for (Locale locale : allowedLocales) {
				if (!CollectionUtils.isEmpty(concept.getNames(locale))) {
					assertNotNull(concept.getPreferredName(locale), "Concept with Id: " + concept.getConceptId() + " has no preferred name in locale:" + locale);
					assertTrue(concept.getPreferredName(locale).getLocalePreferred());
				}
			}
		}
	}
	
	/**
	 * This test is run against the xml test dataset for all concepts to ensure that in every locale
	 * with one or more names, there isn't more than one name explicitly marked as locale preferred.
	 * NOTE that it doesn't test a particular API method directly
	 */
	// @Test
	public void saveConcept_shouldEnsureThatEveryConcepNameLocaleHasExactlyOnePreferredName() {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			for (Locale locale : allowedLocales) {
				Collection<ConceptName> namesInLocale = concept.getNames(locale);
				if (!CollectionUtils.isEmpty(namesInLocale)) {
					int preferredNamesFound = 0;
					for (ConceptName conceptName : namesInLocale) {
						if (conceptName.getLocalePreferred()) {
							preferredNamesFound++;
							assertTrue(preferredNamesFound < 2, "Found multiple preferred names for conceptId: " + concept.getConceptId() + " in the locale '" + locale + "'");
						}
					}
				}
			}
		}
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldSetAPreferredNameForEachLocaleIfNoneIsMarked() {
		//add some other locales to locale.allowed.list for testing purposes
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST);
		gp.setPropertyValue(gp.getPropertyValue().concat(",fr,ja,en_GB"));
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("name1", Locale.ENGLISH));
		concept.addName(new ConceptName("name2", Locale.ENGLISH));
		concept.addName(new ConceptName("name3", Locale.FRENCH));
		concept.addName(new ConceptName("name4", Locale.FRENCH));
		concept.addName(new ConceptName("name5", Locale.JAPANESE));
		concept.addName(new ConceptName("name6", Locale.JAPANESE));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));

		concept = Context.getConceptService().saveConcept(concept);
		assertNotNull(concept.getPreferredName(Locale.ENGLISH));
		assertNotNull(concept.getPreferredName(Locale.FRENCH));
		assertNotNull(concept.getPreferredName(Locale.JAPANESE));
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept)
	 */
	// @Test
	public void mapConceptProposalToConcept_shouldNotRequireMappedConceptOnRejectAction() {
		String uuid = "af4ae460-0e2b-11e0-a94b-469c3c5a0c2f";
		ConceptProposal proposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		assertNotNull(proposal, "could not find proposal " + uuid);
		proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		try {
			Context.getConceptService().mapConceptProposalToConcept(proposal, null);
		}
		catch (APIException ex) {
			fail("cought APIException when rejecting a proposal with null mapped concept");
		}
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept)
	 */
	// @Test
	public void mapConceptProposalToConcept_shouldAllowRejectingProposals() {
		String uuid = "af4ae460-0e2b-11e0-a94b-469c3c5a0c2f";
		ConceptProposal proposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		assertNotNull(proposal, "could not find proposal " + uuid);
		//because there is a  different unit test for the case when mapped proposal is null, we use a non-null concept here for our testing
		Concept concept = conceptService.getConcept(3);
		assertNotNull(concept, "could not find target concept to use for the test");
		proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		Context.getConceptService().mapConceptProposalToConcept(proposal, concept);
		//retrieve the proposal from the model and check its new state
		ConceptProposal persisted = Context.getConceptService().getConceptProposalByUuid(uuid);
		assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT, persisted.getState());
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	// @Test
	public void getConcepts_shouldReturnConceptSearchResultsThatMatchUniqueConcepts() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("trust", Collections
		        .singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
		//trust is included in 2 names for conceptid=3000 and in one name for conceptid=4000.
		//So we should see 2 results only
		assertEquals(2, searchResults.size());
	}

    /**
     * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
     */
	// @Test
	public void getConcepts_shouldReturnConceptSearchResultsThatMatchUniqueConceptsEvenIfDifferentMatchingWords() {
        executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
        List<ConceptSearchResult> searchResults = conceptService.getConcepts("now", Collections
                .singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
        // "now matches both concept names "TRUST NOW" and "TRUST NOWHERE", but these are for the same concept (4000), so there should only be one item in the result set
        assertEquals(1, searchResults.size());
        assertEquals(new Integer(4000), searchResults.get(0).getConcept().getId());
	}

	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	// @Test
	public void getConcepts_shouldReturnConceptSearchResultsThatContainAllSearchWordsAsFirst() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("trust now", Collections
		        .singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
		//"trust now" must be first hit
		assertThat(searchResults.get(0).getWord(), is("trust now"));
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermByName(String,ConceptSource)
	 */
	// @Test
	public void getConceptReferenceTermByName_shouldReturnAConceptReferenceTermThatMatchesTheGivenNameFromTheGivenSource()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByName("weight term",
		    new ConceptSource(1));
		assertNotNull(term);
		assertEquals("weight term", term.getName());
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermByCode(String,ConceptSource)
	 */
	// @Test
	public void getConceptReferenceTermByCode_shouldReturnAConceptReferenceTermThatMatchesTheGivenCodeFromTheGivenSource()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByCode("2332523",
		    new ConceptSource(2));
		assertNotNull(term);
		assertEquals("2332523", term.getCode());
	}

	/**
	 * @see ConceptService#getConceptReferenceTermByCode(String,ConceptSource)
	 */
	// @Test
	public void getConceptReferenceTermByCode_shouldReturnUnretiredTermIfRetiredAlsoExists()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByCode("898989",
			new ConceptSource(1));
		assertNotNull(term);
		assertEquals("898989", term.getCode());
		assertFalse(term.getRetired());
	}

	/**
	 * @see ConceptService#getConceptReferenceTermByCode(String,ConceptSource, boolean)
	 */
	// @Test
	public void getConceptReferenceTermByCode_shouldReturnBothRetiredAndUnretiredTerms() {
		List<ConceptReferenceTerm> terms = Context.getConceptService().getConceptReferenceTermByCode(
			"898989", new ConceptSource(1), true);

		assertEquals(2, terms.size());

		List<Boolean> termStates = terms.stream().map(ConceptReferenceTerm::getRetired)
			.sorted().collect(Collectors.toList());

        List<Boolean> expectedStates = Arrays.asList(false, true);

		assertEquals(expectedStates, termStates);
	}

	/**
	 * @see ConceptService#getConceptReferenceTermByCode(String,ConceptSource, boolean)
	 */
	// @Test
	public void getConceptReferenceTermByCode_shouldExcludeRetiredConcepts() {
		List<ConceptReferenceTerm> terms = Context.getConceptService().getConceptReferenceTermByCode(
			"898989", new ConceptSource(1), false);

		assertEquals(1, terms.size());
		assertFalse(terms.get(0).getRetired());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypes(null,null)
	 */
	// @Test
	public void getConceptMapTypes_shouldNotIncludeHiddenConceptMapTypesIfIncludeHiddenIsSetToFalse() {
		assertEquals(6, Context.getConceptService().getConceptMapTypes(true, false).size());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypes(null,null)
	 */
	// @Test
	public void getConceptMapTypes_shouldReturnSortedList() {
		List<ConceptMapType> conceptMapTypes = Context.getConceptService().getConceptMapTypes(true, true);
		
		for (int i = 0; i < conceptMapTypes.size() - 1; i++) {
			ConceptMapType current = conceptMapTypes.get(i);
			ConceptMapType next = conceptMapTypes.get(i + 1);
			int currentWeight = ConceptMapTypeComparator.getConceptMapTypeSortWeight(current);
			int nextWeight = ConceptMapTypeComparator.getConceptMapTypeSortWeight(next);
			
			assertTrue(currentWeight <= nextWeight);
		}
	}
	
	/**
	 * @see ConceptService#getConceptMapTypes(null,null)
	 */
	// @Test
	public void getConceptMapTypes_shouldReturnAllTheConceptMapTypesIfIncludeRetiredAndHiddenAreSetToTrue() {
		assertEquals(8, Context.getConceptService().getConceptMapTypes(true, true).size());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypes(null,null)
	 */
	// @Test
	public void getConceptMapTypes_shouldReturnOnlyUnRetiredConceptMapTypesIfIncludeRetiredIsSetToFalse() {
		assertEquals(6, Context.getConceptService().getConceptMapTypes(false, true).size());
	}
	
	/**
	 * @see ConceptService#getActiveConceptMapTypes()
	 */
	// @Test
	public void getActiveConceptMapTypes_shouldReturnAllTheConceptMapTypesExcludingHiddenOnes() {
		assertEquals(6, Context.getConceptService().getActiveConceptMapTypes().size());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypeByName(String)
	 */
	// @Test
	public void getConceptMapTypeByName_shouldReturnAConceptMapTypeMatchingTheSpecifiedName() {
		assertEquals("same-as", Context.getConceptService().getConceptMapTypeByName("same-as").getName());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypeByUuid(String)
	 */
	// @Test
	public void getConceptMapTypeByUuid_shouldReturnAConceptMapTypeMatchingTheSpecifiedUuid() {
		assertEquals("is-parent-to", Context.getConceptService().getConceptMapTypeByUuid(
		    "0e7a8536-49d6-11e0-8fed-18a905e044dc").getName());
	}
	
	/**
	 * @see ConceptService#purgeConceptMapType(ConceptMapType)
	 */
	// @Test
	public void purgeConceptMapType_shouldDeleteTheSpecifiedConceptMapTypeFromTheDatabase() {
		//sanity check
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		assertNotNull(mapType);
		Context.getConceptService().purgeConceptMapType(mapType);
		assertNull(Context.getConceptService().getConceptMapType(1));
	}
	
	/**
	 * @see ConceptService#purgeConceptNameTag(ConceptNameTag)
	 */
	// @Test
	public void purgeConceptNameTag_shouldDeleteTheSpecifiedConceptNameTagFromTheDatabase() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-tags.xml");
		//sanity check
		ConceptNameTag nameTag = Context.getConceptService().getConceptNameTagByName("preferred_en");
		assertNotNull(nameTag);
		Context.getConceptService().purgeConceptNameTag(nameTag);
		assertNull(Context.getConceptService().getConceptNameTagByName("preferred_en"));
	}
	
	/**
	 * @see ConceptService#saveConceptMapType(ConceptMapType)
	 */
	// @Test
	public void saveConceptMapType_shouldAddTheSpecifiedConceptMapTypeToTheDatabaseAndAssignToItAnId() {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("test type");
		mapType = Context.getConceptService().saveConceptMapType(mapType);
		assertNotNull(mapType.getId());
		assertNotNull(Context.getConceptService().getConceptMapTypeByName("test type"));
	}
	
	/**
	 * @see ConceptService#saveConceptMapType(ConceptMapType)
	 */
	// @Test
	public void saveConceptMapType_shouldUpdateAnExistingConceptMapType() {
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		//sanity checks
		assertNull(mapType.getDateChanged());
		assertNull(mapType.getChangedBy());
		mapType.setName("random name");
		mapType.setDescription("random description");
		ConceptMapType editedMapType = Context.getConceptService().saveConceptMapType(mapType);
		Context.flushSession();
		assertEquals("random name", editedMapType.getName());
		assertEquals("random description", editedMapType.getDescription());
		//date changed and changed by should have been updated
		assertNotNull(editedMapType.getDateChanged());
		assertNotNull(editedMapType.getChangedBy());
	}
	
	/**
	 * @see ConceptService#retireConceptMapType(ConceptMapType,String)
	 */
	// @Test
	public void retireConceptMapType_shouldRetireTheSpecifiedConceptMapTypeWithTheGivenRetireReason() {
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		assertFalse(mapType.getRetired());
		assertNull(mapType.getRetiredBy());
		assertNull(mapType.getDateRetired());
		assertNull(mapType.getRetireReason());
		ConceptMapType retiredMapType = Context.getConceptService().retireConceptMapType(mapType, "test retire reason");
		assertTrue(retiredMapType.getRetired());
		assertEquals(retiredMapType.getRetireReason(), "test retire reason");
		assertNotNull(retiredMapType.getRetiredBy());
		assertNotNull(retiredMapType.getDateRetired());
	}
	
	/**
	 * @see ConceptService#retireConceptMapType(ConceptMapType,String)
	 */
	// @Test
	public void retireConceptMapType_shouldShouldSetTheDefaultRetireReasonIfNoneIsGiven() {
		//sanity check
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		assertNull(mapType.getRetireReason());
		ConceptMapType retiredMapType = Context.getConceptService().retireConceptMapType(mapType, null);
		assertNotNull(retiredMapType.getRetireReason());
	}
	
	/**
	 * @see ConceptService#getAllConceptReferenceTerms(null)
	 */
	// @Test
	public void getConceptReferenceTerms_shouldReturnAllTheConceptReferenceTermsIfIncludeRetiredIsSetToTrue()
	{
		assertEquals(13, Context.getConceptService().getConceptReferenceTerms(true).size());
	}
	
	/**
	 * @see ConceptService#getAllConceptReferenceTerms(null)
	 */
	// @Test
	public void getConceptReferenceTerms_shouldReturnOnlyUnRetiredConceptReferenceTermsIfIncludeRetiredIsSetToFalse()
	{
		assertEquals(11, Context.getConceptService().getConceptReferenceTerms(false).size());
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermByUuid(String)
	 */
	// @Test
	public void getConceptReferenceTermByUuid_shouldReturnTheConceptReferenceTermThatMatchesTheGivenUuid() {
		assertEquals("weight term2", Context.getConceptService().getConceptReferenceTermByUuid("SNOMED CT-2332523")
		        .getName());
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermsBySource(ConceptSource)
	 */
	// @Test
	public void getConceptReferenceTerms_shouldReturnOnlyTheConceptReferenceTermsFromTheGivenConceptSource()
	{
		assertEquals(11, conceptService.getConceptReferenceTerms(null, conceptService.getConceptSource(1), 0, null,
		    true).size());
	}
	
	/**
	 * @see ConceptService#retireConceptReferenceTerm(ConceptReferenceTerm,String)
	 */
	// @Test
	public void retireConceptReferenceTerm_shouldRetireTheSpecifiedConceptReferenceTermWithTheGivenRetireReason()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		assertFalse(term.getRetired());
		assertNull(term.getRetireReason());
		assertNull(term.getRetiredBy());
		assertNull(term.getDateRetired());
		ConceptReferenceTerm retiredTerm = Context.getConceptService()
		        .retireConceptReferenceTerm(term, "test retire reason");
		assertTrue(retiredTerm.getRetired());
		assertEquals("test retire reason", retiredTerm.getRetireReason());
		assertNotNull(retiredTerm.getRetiredBy());
		assertNotNull(retiredTerm.getDateRetired());
	}
	
	/**
	 * @see ConceptService#retireConceptReferenceTerm(ConceptReferenceTerm,String)
	 */
	// @Test
	public void retireConceptReferenceTerm_shouldShouldSetTheDefaultRetireReasonIfNoneIsGiven() {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		term = Context.getConceptService().retireConceptReferenceTerm(term, null);
		assertNotNull(term.getRetireReason());
	}
	
	/**
	 * @see ConceptService#saveConceptReferenceTerm(ConceptReferenceTerm)
	 */
	// @Test
	public void saveConceptReferenceTerm_shouldAddAConceptReferenceTermToTheDatabaseAndAssignAnIdToIt() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("test term");
		term.setCode("test code");
		ConceptSource source = Context.getConceptService().getConceptSource(1);
		term.setConceptSource(source);
		ConceptReferenceTerm savedTerm = Context.getConceptService().saveConceptReferenceTerm(term);
		assertNotNull(savedTerm.getId());
		assertNotNull(Context.getConceptService().getConceptReferenceTermByName("test term", source));
	}
	
	/**
	 * @see ConceptService#saveConceptReferenceTerm(ConceptReferenceTerm)
	 */
	// @Test
	public void saveConceptReferenceTerm_shouldUpdateChangesToTheConceptReferenceTermInTheDatabase() {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		//sanity checks
		assertEquals(Context.getConceptService().getConceptSource(1), term.getConceptSource());
		assertNull(term.getChangedBy());
		assertNull(term.getDateChanged());
		term.setName("new name");
		term.setCode("new code");
		term.setDescription("new descr");
		ConceptSource conceptSource2 = Context.getConceptService().getConceptSource(2);
		term.setConceptSource(conceptSource2);
		
		ConceptReferenceTerm editedTerm = Context.getConceptService().saveConceptReferenceTerm(term);
		Context.flushSession();
		assertEquals("new name", editedTerm.getName());
		assertEquals("new code", editedTerm.getCode());
		assertEquals("new descr", editedTerm.getDescription());
		assertEquals(conceptSource2, editedTerm.getConceptSource());
		//The auditable fields should have been set
		assertNotNull(term.getChangedBy());
		assertNotNull(term.getDateChanged());
	}
	
	/**
	 * @see ConceptService#unretireConceptMapType(ConceptMapType)
	 */
	// @Test
	public void unretireConceptMapType_shouldUnretireTheSpecifiedConceptMapTypeAndDropAllRetireRelatedFields()
	{
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(6);
		assertTrue(mapType.getRetired());
		assertNotNull(mapType.getRetiredBy());
		assertNotNull(mapType.getDateRetired());
		assertNotNull(mapType.getRetireReason());
		ConceptMapType unRetiredMapType = Context.getConceptService().unretireConceptMapType(mapType);
		assertFalse(unRetiredMapType.getRetired());
		assertNull(unRetiredMapType.getRetireReason());
		assertNull(unRetiredMapType.getRetiredBy());
		assertNull(unRetiredMapType.getDateRetired());
	}
	
	/**
	 * @see ConceptService#unretireConceptReferenceTerm(ConceptReferenceTerm)
	 */
	// @Test
	public void unretireConceptReferenceTerm_shouldUnretireTheSpecifiedConceptReferenceTermAndDropAllRetireRelatedFields()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(11);
		assertTrue(term.getRetired());
		assertNotNull(term.getRetireReason());
		assertNotNull(term.getRetiredBy());
		assertNotNull(term.getDateRetired());
		ConceptReferenceTerm retiredTerm = Context.getConceptService().unretireConceptReferenceTerm(term);
		assertFalse(retiredTerm.getRetired());
		assertNull(retiredTerm.getRetireReason());
		assertNull(retiredTerm.getRetiredBy());
		assertNull(retiredTerm.getDateRetired());
	}
	
	/**
	 * @see ConceptService#getAllConceptReferenceTerms()
	 */
	// @Test
	public void getAllConceptReferenceTerms_shouldReturnAllConceptReferenceTermsInTheDatabase() {
		assertEquals(13, Context.getConceptService().getAllConceptReferenceTerms().size());
	}
	
	/**
	 * @see ConceptService#getConceptMappingsToSource(ConceptSource)
	 */
	// @Test
	public void getConceptMappingsToSource_shouldReturnAListOfConceptMapsFromTheGivenSource() {
		assertEquals(12, Context.getConceptService().getConceptMappingsToSource(
		    Context.getConceptService().getConceptSource(1)).size());
	}
	
	/**
	 * @see ConceptService#getReferenceTermMappingsTo(ConceptReferenceTerm)
	 */
	// @Test
	public void getReferenceTermMappingsTo_shouldReturnAllConceptReferenceTermMapsWhereTheSpecifiedTermIsTheTermB()
	{
		assertEquals(2, Context.getConceptService().getReferenceTermMappingsTo(
		    Context.getConceptService().getConceptReferenceTerm(4)).size());
	}
	
	/**
	 * @see ConceptService#getCountOfConcepts(String, List, boolean, List, List, List, List, Concept)
	 */
	// @Test
	public void getCountOfConcepts_shouldReturnACountOfUniqueConcepts() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		assertEquals(2, conceptService.getCountOfConcepts("trust", Collections.singletonList(Locale.ENGLISH), false,
		    null, null, null, null, null).intValue());
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldNotFailWhenADuplicateNameIsEditedToAUniqueValue() {
		//Insert a row to simulate an existing duplicate fully specified/preferred name that needs to be edited
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-conceptWithDuplicateName.xml");
		Concept conceptToEdit = conceptService.getConcept(10000);
		Locale locale = new Locale("en", "GB");
		conceptToEdit.addDescription(new ConceptDescription("some description",locale));
		ConceptName duplicateNameToEdit = conceptToEdit.getFullySpecifiedName(locale);
		//Ensure the name is a duplicate in it's locale
		Concept otherConcept = conceptService.getConcept(5497);
		assertTrue(duplicateNameToEdit.getName().equalsIgnoreCase(
		    otherConcept.getFullySpecifiedName(locale).getName()));
		
		duplicateNameToEdit.setName("new unique name");
		conceptService.saveConcept(conceptToEdit);
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTerms(String,ConceptSource,Integer,Integer,null)
	 */
	// @Test
	public void getConceptReferenceTerms_shouldReturnUniqueTermsWithACodeOrNameContainingTheSearchPhrase() {
		List<ConceptReferenceTerm> matches = Context.getConceptService().getConceptReferenceTerms("cd4", null, null, null,
		    true);
		assertEquals(3,

		matches.size());
		Set<ConceptReferenceTerm> uniqueTerms = new HashSet<>();
		//check that we have only unique terms
		for (ConceptReferenceTerm conceptReferenceTerm : matches) {
			assertTrue(uniqueTerms.add(conceptReferenceTerm));
		}
	}
	
	/**
	 * @see ConceptService#getConceptsByAnswer(ConceptClass)
	 */
	// @Test
	public void getConceptsByAnswer_shouldFindAnswersForConcept() {
		Concept concept = conceptService.getConcept(7);
		assertNotNull(concept);
		List<Concept> concepts = conceptService.getConceptsByAnswer(concept);
		assertEquals(1, concepts.size());
		assertEquals(21, concepts.get(0).getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptsByClass(ConceptClass)
	 */
	// @Test
	public void getConceptsByClass_shouldGetConceptsByClass() {				
		// replay
		List<Concept> actualConcepts = conceptService.getConceptsByClass(new ConceptClass(3));
		
		// verify
		assertThat(actualConcepts.size(), is(6));
		assertThat(actualConcepts,
		    containsInAnyOrder(conceptService.getConcept(3), conceptService.getConcept(60), conceptService.getConcept(64),
		        conceptService.getConcept(67), conceptService.getConcept(88), conceptService.getConcept(792)));
	}
	
	/**
	 * @see ConceptService#getConceptsByClass(ConceptClass)
	 */
	// @Test
	public void getConceptsByClass_shouldReturnAnEmptyListIfNoneWasFound() {
		// setup
		ConceptClass cc = new ConceptClass(23);
		
		// replay
		List<Concept> concepts = conceptService.getConceptsByClass(cc);
		
		// verify
		assertThat(concepts, is(empty()));
	}
	
	/**
	 * @see ConceptService#getCountOfConceptReferenceTerms(String,ConceptSource,null)
	 */
	// @Test
	public void getCountOfConceptReferenceTerms_shouldIncludeRetiredTermsIfIncludeRetiredIsSetToTrue() {
		assertEquals(13, conceptService.getCountOfConceptReferenceTerms("", null, true).intValue());
	}
	
	/**
	 * @see ConceptService#getCountOfConceptReferenceTerms(String,ConceptSource,null)
	 */
	// @Test
	public void getCountOfConceptReferenceTerms_shouldNotIncludeRetiredTermsIfIncludeRetiredIsSetToFalse() {
		assertEquals(11, conceptService.getCountOfConceptReferenceTerms("", null, false).intValue());
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	// @Test
	public void getConcepts_shouldPassWithAndOrNotWords() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		
		//search phrase with AND
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("AND SALBUTAMOL INHALER", Collections
		        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
		
		assertEquals(1, searchResults.size());
		assertThat(searchResults.get(0).getWord(), is("AND SALBUTAMOL INHALER"));
		
		//search phrase with OR
		searchResults = conceptService.getConcepts("SALBUTAMOL OR INHALER", Collections
	        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
	
		assertEquals(1, searchResults.size());
		assertThat(searchResults.get(0).getWord(), is("SALBUTAMOL OR INHALER"));
		
		//search phrase with NOT
		searchResults = conceptService.getConcepts("SALBUTAMOL INHALER NOT", Collections
	        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
	
		assertEquals(1, searchResults.size());
		assertThat(searchResults.get(0).getWord(), is("SALBUTAMOL INHALER NOT"));
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	// @Test
	public void getConceptByReference_shouldFindAConceptByItsConceptName() {
		assertEquals(3, conceptService.getConceptByReference(TEST_CONCEPT_CONSTANT_NAME).getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	// @Test
	public void getConceptByReference_shouldFindAConceptByItsConceptId() {
		assertEquals("COUGH SYRUP", conceptService.getConceptByReference(TEST_CONCEPT_CONSTANT_ID).getName().toString());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	// @Test
	public void getConceptByReference_shouldFindAConceptByItsMapping() {
		Concept concept = conceptService.getConceptByReference("SSTRM:454545");
		assertEquals(24, concept.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	// @Test
	public void getConceptByReference_shouldFindAConceptByItsUuid() {
		assertEquals(60, conceptService.getConceptByReference(TEST_CONCEPT_CONSTANT_UUID).getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	// @Test
	public void getConcept_shouldFindAConceptWithNonStandardUuid() throws Exception {
		String nonStandardUuid = "1000AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		assertEquals(64, conceptService.getConceptByReference(nonStandardUuid).getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String) tests static constant containing ids and
	 *      UUIDs
	 */
	// @Test
	public void getConceptByReference_shouldFindAConceptWithStaticConstant() {
		assertNotNull(conceptService.getConceptByReference("org.openmrs.api.ConceptServiceTest.TEST_CONCEPT_CONSTANT_UUID"));
		assertNotNull(conceptService.getConceptByReference("org.openmrs.api.ConceptServiceTest.TEST_CONCEPT_CONSTANT_ID"));
		assertNotNull(conceptService.getConceptByReference("org.openmrs.api.ConceptServiceTest.TEST_CONCEPT_CONSTANT_NAME"));
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	// @Test
	public void getConceptByReference_shouldReturnNullWhenEitherConceptRefIsInvalidOrDoesNotMatchAnyConcept() {
		assertNull(conceptService.getConceptByReference(null));  //given null 
		assertNull(conceptService.getConceptByReference(""));  //with empty string
		assertNull(conceptService.getConceptByReference("id, name or map which does not match to any concept"));
		assertNull(conceptService.getConceptByReference("1000")); //invalid uuid but exists in standardTestDataset
	}

	/**
	 * @see ConceptService#getConceptReferenceRangesByConceptId(Integer) 
	 */
	// @Test
	public void getConceptReferenceRangeByConceptId_shouldReturnConceptReferencesRanges() {
		executeDataSet(CONCEPT_WITH_CONCEPT_REFERENCE_RANGES_XML);

		List<ConceptReferenceRange> conceptReferenceRanges = conceptService.getConceptReferenceRangesByConceptId(5089);
		
		assertFalse(conceptReferenceRanges.isEmpty());
		
		assertEquals(3, conceptReferenceRanges.get(0).getId());
	}

	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	// @Test
	public void saveConcept_shouldSaveANewConceptReferenceRange() {
		Context.setLocale(Locale.US);
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setDatatype(new ConceptDatatype(1));
		conceptNumeric.setConceptClass(new ConceptClass(1));
		
		ConceptName conceptName = new ConceptName("a new conceptnumeric", Locale.US);
		conceptNumeric.addName(conceptName);
		conceptNumeric.addDescription(new ConceptDescription("some description",null));
		conceptNumeric.setHiAbsolute(50.0);
		conceptNumeric.setLowAbsolute(20.0);

		ConceptReferenceRange conceptReferenceRange = new ConceptReferenceRange();
		conceptReferenceRange.setCriteria("$patient.getAge() >= 1 && $patient.getAge() <= 70");
		conceptReferenceRange.setConceptNumeric(conceptNumeric);
		conceptReferenceRange.setHiAbsolute(conceptNumeric.getHiAbsolute());
		conceptReferenceRange.setLowAbsolute(conceptNumeric.getLowAbsolute());
		
		conceptNumeric.setReferenceRanges(Collections.singleton(conceptReferenceRange));
		conceptService.saveConcept(conceptNumeric);

		ConceptNumeric savedConceptNumeric = conceptService.getConceptNumeric(conceptNumeric.getConceptId());
		
		assertEquals("a new conceptnumeric", savedConceptNumeric.getName(Locale.US).getName());
		assertEquals(50.0, savedConceptNumeric.getHiAbsolute(), 0);
		assertEquals(1, savedConceptNumeric.getReferenceRanges().size());
		assertEquals(50.0, savedConceptNumeric.getReferenceRanges().stream().findFirst().get().getHiAbsolute());
	}

	// @Test
	public void getConceptReferenceRangeByUuid_shouldReturnAConceptReferenceRange() {
		ConceptReferenceRange conceptReferenceRange = conceptService.getConceptReferenceRangeByUuid("2c5972e8-aee5-468c-8216-369a1b60723d");

		assertNotNull(conceptReferenceRange);

		assertEquals(34, conceptReferenceRange.getId());
	}
	
	// @Test
	public void purgeConceptReferenceRange_shouldPurgeAConceptReferenceRange() {
		
		final String CONCEPT_REFERENCE_RANGE_UUID = "2c5972e8-aee5-468c-8216-369a1b60723d";
		ConceptReferenceRange conceptReferenceRange = conceptService.getConceptReferenceRangeByUuid(CONCEPT_REFERENCE_RANGE_UUID);
		assertNotNull(conceptReferenceRange);
		
		conceptService.purgeConceptReferenceRange(conceptReferenceRange);

		conceptReferenceRange = conceptService.getConceptReferenceRangeByUuid(CONCEPT_REFERENCE_RANGE_UUID);
		assertNull(conceptReferenceRange);
	}
	
	@Test
	public void shouldSaveDrugIngredientsAndUnits() {
		// Crear un nuevo medicamento
		Drug drug = new Drug();
		drug.setName("Test Drug");
		drug.setConcept(conceptService.getConcept(3)); // Usa un concepto válido de tu base de datos de pruebas

		// Crear un ingrediente
		DrugIngredient ingredient = new DrugIngredient();
		ingredient.setDrug(drug);
		ingredient.setIngredient(conceptService.getConcept(3)); // Usa un concepto válido
		ingredient.setStrength(500.0);
		ingredient.setUnits(conceptService.getConcept(50)); // Concept for "mg" units

		// Añadir el ingrediente al medicamento
		java.util.Set<DrugIngredient> ingredients = new java.util.HashSet<>();
		ingredients.add(ingredient);
		drug.setIngredients(ingredients);

		// Guardar el medicamento
		conceptService.saveDrug(drug);

		// Limpiar y borrar la sesión para simular una recarga real desde la base de datos
		Context.flushSession();
		Context.clearSession();

		// Recargar el medicamento
		Drug reloadedDrug = conceptService.getDrug(drug.getDrugId());
		assertNotNull(reloadedDrug);
		assertNotNull(reloadedDrug.getIngredients());
		assertFalse(reloadedDrug.getIngredients().isEmpty());

		DrugIngredient reloadedIngredient = reloadedDrug.getIngredients().iterator().next();
		assertEquals(conceptService.getConcept(50), reloadedIngredient.getUnits(), "La unidad del ingrediente debe guardarse correctamente");
	}
}

package com.lsmsdb.jamsync;

import com.lsmsdb.jamsync.dao.BandDAO;
import com.lsmsdb.jamsync.dao.MusicianDAO;
import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.client.MongoCollection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

@SpringBootTest
class JamsyncApplicationTests {

	@Test
	void contextLoads() {
	}

//package com.lsmsdb.jamsync;
//import com.lsmsdb.jamsync.dao.BandDAO;
//import com.lsmsdb.jamsync.dao.exception.DAOException;
//import com.lsmsdb.jamsync.model.Band;
//import com.mongodb.client.MongoCollection;
//import org.bson.Document;
//import org.junit.jupiter.api.AfterEach;
//import org.bson.conversions.Bson;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentMatchers;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.Arrays;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.reset;
//import static org.mockito.Mockito.when;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.fail;
//
//import com.mongodb.client.FindIterable;
//import com.mongodb.client.MongoCursor;
//import java.util.Collections;
//
//
//class JamsyncApplicationTests {
//	@Mock
//	private MongoCollection<Document> mongoCollection;
//	@InjectMocks
//	private BandDAO bandDAO;
//	@Test
//	void testCreateBand() {
//		MockitoAnnotations.initMocks(this);
//
//		Band testBand = new Band();
//		testBand.set_id("1");
//		testBand.setUsername("TestBand");
//		testBand.setGenres(Collections.singletonList("Rock"));
//
//		try {
//			when(mongoCollection.insertOne(any())).thenReturn(null);
//			bandDAO.createBand(testBand);
//
//			// Add assertions as needed to verify the behavior or check the results.
//		} catch (DAOException e) {
//			// Handle exception or fail the test if unexpected exception occurs.
//			e.printStackTrace();
//			fail("Unexpected exception: " + e.getMessage());
//		}
//	}
//	@Test
//	void testGetBandById() {
//		MockitoAnnotations.initMocks(this);
//
//		String bandId = "1";
//
//		try {
//			// Create a sample band document for testing
//			Document sampleBandDocument = new Document("_id", bandId).append("username", "TestBand").append("genres", Arrays.asList("Rock", "Pop"));
//
//			// Mock the behavior of the MongoDB collection find method
//			FindIterable<Document> findIterableMock = mock(FindIterable.class);
//			when(mongoCollection.find(any(Bson.class))).thenReturn(findIterableMock);
//			MongoCursor<Document> mongoCursorMock = mock(MongoCursor.class);
//			when(findIterableMock.iterator()).thenReturn(mongoCursorMock);
//			when(mongoCursorMock.hasNext()).thenReturn(true);
//			when(mongoCursorMock.next()).thenReturn(sampleBandDocument);
//
//			// Call the actual method to get the band
//			Band retrievedBand = bandDAO.getBandById(bandId);
//
//			// Verify that the band is retrieved correctly
//			assertNotNull(retrievedBand);
//			assertEquals(bandId, retrievedBand.get_id());
//			assertEquals("TestBand", retrievedBand.getUsername());
//			assertEquals(Arrays.asList("Rock", "Pop"), retrievedBand.getGenres());
//		} catch (DAOException e) {
//			// Handle exception or fail the test if an unexpected exception occurs.
//			e.printStackTrace();
//			fail("Unexpected exception: " + e.getMessage());
//		} finally {
//			reset(mongoCollection);
//		}
//	}
//
//	@Test
//	void testUpdateBandById() {
//		MockitoAnnotations.initMocks(this);
//
//		String bandId = "43a96558-37d4-4ac8-9f16-1793b3f36011";
//
//		try {
//			// Create a sample band document for testing
//			Document oldBandDocument = new Document("_id", bandId).append("username", "OldBand").append("genres", Arrays.asList("OldGenre1", "OldGenre2"));
//
//			// Mock the behavior of the MongoDB collection find method to return the old band document
//			FindIterable<Document> findIterableMock = mock(FindIterable.class);
//			MongoCursor<Document> mongoCursorMock = mock(MongoCursor.class);
//			when(mongoCollection.find(ArgumentMatchers.eq("_id", bandId))).thenReturn(findIterableMock);
//			when(findIterableMock.iterator()).thenReturn(mongoCursorMock);
//			when(mongoCursorMock.hasNext()).thenReturn(true);
//			when(mongoCursorMock.next()).thenReturn(oldBandDocument);
//
//			// Mock the behavior of the MongoDB collection findOneAndReplace method
//			Document updatedBandDocument = new Document("_id", bandId).append("username", "UpdatedBand").append("genres", Arrays.asList("UpdatedGenre1", "UpdatedGenre2"));
//			when(mongoCollection.findOneAndReplace(ArgumentMatchers.eq("_id", bandId), any(), any())).thenReturn(updatedBandDocument);
//
//			// Call the actual method to update the band
//			Band updatedBand = bandDAO.updateBandById(bandId, createUpdatedBand());
//
//			// Verify that the band is updated correctly
//			assertNotNull(updatedBand);
//			assertEquals(bandId, updatedBand.get_id());
//			assertEquals("UpdatedBand", updatedBand.getUsername());
//			assertEquals(Arrays.asList("UpdatedGenre1", "UpdatedGenre2"), updatedBand.getGenres());
//		} catch (DAOException e) {
//			// Handle exception or fail the test if an unexpected exception occurs.
//			e.printStackTrace();
//			fail("Unexpected exception: " + e.getMessage());
//		} finally {
//			reset(mongoCollection);
//		}
//	}
//
//	private Band createUpdatedBand() {
//		Band updatedBand = new Band();
//		updatedBand.set_id("1");
//		updatedBand.setUsername("NewBand");
//		updatedBand.setGenres(Arrays.asList("NewGenre1", "NewGenre2"));
//		return updatedBand;
//	}
//
//
//	@AfterEach
//	void cleanup() {
//		@SuppressWarnings("unchecked")
//		MongoCollection<Document>[] mongoCollectionArray = new MongoCollection[]{mongoCollection};
//		reset(mongoCollectionArray);
//	}
//
//
//}

		@Test
		public void testGetSuggestedOpportunitiesToMusician() {
			MusicianDAO musicianDAO = new MusicianDAO();
			System.out.println("salam");

			// Retrieve a musician document from MongoDB
			// Replace 'musicians' with the name of your MongoDB collection for musicians
			MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.MUSICIAN);
			Document musicianDoc = collection.find().first();
			System.out.println("salam"+musicianDoc);

			if (musicianDoc != null) {
				Musician testMusician = new Musician(musicianDoc);
				System.out.println(testMusician.getLocation().getCity());
				System.out.println(testMusician.getLocation().getCountry());
				try {
					List<Opportunity> suggestedOpportunities = musicianDAO.getSuggestedOpportunities(testMusician);
					//System.out.println("salam"+suggestedOpportunities);
					// Process the suggested opportunities as per your requirements
					// For example, you can print the details of each opportunity
					for (Opportunity opportunity : suggestedOpportunities) {
						System.out.println(opportunity.getLocation().getState());
						System.out.println(opportunity.getCreatedAt());
						System.out.println(opportunity.getGenres());
						System.out.println(opportunity.getInstruments());
						System.out.println(opportunity.getPublisher());

					}
				} catch (DAOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("No musician document found in MongoDB.");
			}
		}
	@Test
	public void testGetSuggestedOpportunitiesToBand() {
		BandDAO bandDAO = new BandDAO();
		System.out.println("salam");

		// Retrieve a musician document from MongoDB
		// Replace 'musicians' with the name of your MongoDB collection for musicians
		MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.BAND);
		Document bandDoc = collection.find().first();
		System.out.println("salam"+bandDoc);

		if (bandDoc != null) {
			Band testBand = new Band(bandDoc);
			System.out.println(testBand.getLocation().getCity());
			System.out.println(testBand.getLocation().getCountry());
			System.out.println(testBand.getGenres());
			try {
				List<Opportunity> suggestedOpportunities = bandDAO.getSuggestedOpportunities(testBand);
				//System.out.println("salam"+suggestedOpportunities);
				// Process the suggested opportunities as per your requirements
				// For example, you can print the details of each opportunity
				for (Opportunity opportunity : suggestedOpportunities) {
					System.out.println(opportunity.getLocation().getState());
					System.out.println(opportunity.getCreatedAt());
					System.out.println(opportunity.getGenres());
					System.out.println(opportunity.getInstruments());
					System.out.println(opportunity.getPublisher());

				}
			} catch (DAOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No band document found in MongoDB.");
		}
	}
	}



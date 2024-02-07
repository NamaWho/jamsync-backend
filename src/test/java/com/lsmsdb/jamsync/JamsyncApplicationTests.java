package com.lsmsdb.jamsync;

import com.lsmsdb.jamsync.dao.MusicianDAO;
import com.lsmsdb.jamsync.dao.exception.DAOException;
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


		@Test
		public void testGetSuggestedOpportunities() {
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
	}



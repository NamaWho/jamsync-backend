package com.lsmsdb.jamsync;

import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.neo4j.driver.Session;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class JamsyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(JamsyncApplication.class, args);
		testMongoDriver();
		testNeo4jDriver();
	}

	private static void testMongoDriver() {
		MongoDriver mongoDriver = MongoDriver.getInstance();
		MongoCollection<Document> collection = mongoDriver.getCollection(MongoCollectionsEnum.MUSICIAN);
		LogManager.getLogger().info("JamSync Application started [MongoDB]");
		//mongoDriver.closeConnection();
	}

	private static void testNeo4jDriver() {
		Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();
		Session session = neo4jDriver.getDriver().session();
		LogManager.getLogger().info("JamSync Application started [Neo4j]");
	}
}

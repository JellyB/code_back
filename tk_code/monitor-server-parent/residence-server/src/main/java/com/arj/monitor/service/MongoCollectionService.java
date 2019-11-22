package com.arj.monitor.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MongoCollectionService {

	@Value("${collect.mongodb.uri}")
	private String mongoUri;

	private MongoClient mongoClient;

	public String collect() {
		String ret = "";
		try {
			mongoClient = MongoClients.create(mongoUri);
			MongoDatabase database = mongoClient.getDatabase("huatu_ztk");
			MongoCollection<Document> collection = database.getCollection("ztk_match");
			Document myDoc = collection.find().first();
			if (myDoc != null) {
				log.info("check mongo ret:{}", myDoc.get("name"));
			}
		} catch (Exception e) {
			ret = e.getMessage();
			log.error("check mongo error:{}", e.getMessage());
		} finally {
			mongoClient.close();
		}
		return ret;
	}
}

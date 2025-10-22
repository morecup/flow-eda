package com.flow.eda.runner.node.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class MongodbNodeTest {

    @Test
    void run() {
        Document params = new Document();
        params.append("url", "mongodb://root:admin@127.0.0.1:27017/admin");
        params.append("db", "test_db");
        params.append("command", "{\"find\": \"test_coll\"}");
        params.append("flowId", "f1");
        params.append("nodeId", "mongo");

        MongoClient client = Mockito.mock(MongoClient.class);
        MongoDatabase database = Mockito.mock(MongoDatabase.class);
        Document result = new Document("ok", 1);

        try (MockedStatic<MongoClients> mocked = Mockito.mockStatic(MongoClients.class)) {
            mocked.when(() -> MongoClients.create(params.getString("url"))).thenReturn(client);
            Mockito.when(client.getDatabase("test_db")).thenReturn(database);
            Mockito.when(database.runCommand(Mockito.any(Document.class))).thenReturn(result);

            MongodbNode node = new MongodbNode(params);
            node.run(p -> {
                Assertions.assertEquals(result, p.get("result"));
            });

            Mockito.verify(client).close();
        }
    }
}

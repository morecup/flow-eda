package com.flow.eda.runner.node.redis;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import redis.clients.jedis.Jedis;

class RedisNodeTest {

    @Test
    void run() {
        Document params = new Document();
        params.append("uri", "127.0.0.1:6379");
        params.append("method", "get");
        params.append("args", "test");
        params.append("flowId", "f1");
        params.append("nodeId", "redis");

        try (MockedConstruction<Jedis> construction =
                Mockito.mockConstruction(
                        Jedis.class,
                        (mock, context) -> Mockito.when(mock.get("test")).thenReturn("value"))) {
            RedisNode node = new RedisNode(params);
            node.run(p -> Assertions.assertEquals("value", p.get("result")));

            Jedis mock = construction.constructed().get(0);
            Mockito.verify(mock).close();
        }
    }
}

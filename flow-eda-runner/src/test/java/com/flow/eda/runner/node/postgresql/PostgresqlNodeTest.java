package com.flow.eda.runner.node.postgresql;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

class PostgresqlNodeTest {

    @Test
    void run() {
        Document params = new Document();
        params.append("url", "jdbc:postgresql://localhost:5432/postgres");
        params.append("username", "postgres");
        params.append("password", "123456");
        params.append("sql", "UPDATE test SET value='v'");
        params.append("flowId", "f1");
        params.append("nodeId", "postgres");

        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mocked = Mockito.mockStatic(DriverManager.class)) {
            mocked.when(() -> DriverManager.getConnection(params.getString("url"), "postgres", "123456"))
                    .thenReturn(connection);

            Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
            Mockito.when(statement.executeUpdate()).thenReturn(1);

            PostgresqlNode node = new PostgresqlNode(params);
            node.run(p -> Assertions.assertEquals("Affected rows: 1", p.getString("result")));

            Mockito.verify(connection).commit();
            Mockito.verify(connection).close();
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}

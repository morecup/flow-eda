package com.flow.eda.runner.node.mysql;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

class MysqlNodeTest {

    @Test
    void run() {
        Document params = new Document();
        params.append("url", "jdbc:mysql://localhost:3306/flow_eda");
        params.append("username", "root");
        params.append("password", "123456");
        params.append("sql", "UPDATE test SET value='v'");
        params.append("flowId", "f1");
        params.append("nodeId", "mysql");

        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mocked = Mockito.mockStatic(DriverManager.class)) {
            mocked.when(() -> DriverManager.registerDriver(Mockito.any(Driver.class))).thenAnswer(inv -> null);
            String expectedUrl = "jdbc:mysql://localhost:3306/flow_eda?useSSL=false";
            mocked.when(() -> DriverManager.getConnection(expectedUrl, "root", "123456"))
                    .thenReturn(connection);

            Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
            Mockito.when(statement.executeUpdate()).thenReturn(1);

            MysqlNode node = new MysqlNode(params);
            node.run(p -> Assertions.assertEquals("Affected rows: 1", p.getString("result")));

            Mockito.verify(connection).commit();
            Mockito.verify(connection).close();
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}

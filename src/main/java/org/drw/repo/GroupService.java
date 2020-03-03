package org.drw.repo;

import graphql.schema.DataFetchingEnvironment;
import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.SqlResult;
import io.vertx.axle.sqlclient.Tuple;

import org.drw.model.Group;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class GroupService {

    @Inject
    @ConfigProperty(name = "group.schema.create", defaultValue = "true")
    boolean createSchema;

    @Inject
    MySQLPool client;

    @PostConstruct
    private void initData() {
        if (createSchema)
            client.preparedQuery(
                    "INSERT INTO tbl_mygroups (id, name, blacklisted, createdBy, createdDate) VALUES (?, ?, ?, ?, ?)",
                    Tuple.of("7d60aacb-267b-403f-9aad-ae730ece2d75",
                            "AdminGroup",
                            false,
                            "Duminda",
                            LocalDate.now()))
                    .whenComplete((content, err) -> {
                        if (err != null) {
                            System.out.println("Error: " + err.getMessage());
                        } else {
                            System.out.println("Content: " + content);
                        }
                    });
    }

    private static Group from(Row row) {
        return new Group(row.getString("id"),
                row.getString("name"),
                row.getBoolean("blacklisted"),
                row.getString("createdBy"),
                row.getLocalDate("createdDate"));
    }

    public CompletionStage<List<Group>> allAvailableGroups(DataFetchingEnvironment env) {
        boolean isBlackListed = env.getArgument("isBlackListed");

        return client.query("SELECT id, name, createdBy, createdDate, blacklisted FROM tbl_mygroups")
                .thenApply(rowSet -> {
                    List<Group> list = new ArrayList<>(rowSet.size());
                    for (Row row : rowSet)
                        if (!isBlackListed)
                            list.add(from(row));

                    return list;
                });
    }

    public CompletionStage<Boolean> addGroup(DataFetchingEnvironment env) {
        String name = env.getArgument("name");
        String createdBy = env.getArgument("createdBy");

        return client.preparedQuery("SELECT id, name FROM tbl_mygroups WHERE name = ?", Tuple.of(name))
                .thenApply(rows -> {
                            System.out.println(rows.size());
                            return rows.size() <= 0;
                        }
                ).whenComplete((content, err) -> {
                    if (err != null) {
                        System.out.println("Error: " + err.getMessage());
                    } else {
                        System.out.println("Group Exists: " + content);
                    }
                })
                .thenCompose(r -> r ? client.preparedQuery(
                        "INSERT INTO tbl_mygroups (id, name, blacklisted, createdBy, createdDate) VALUES (?, ?, ?, ?, ?)",
                        Tuple.of(UUID.randomUUID().toString(), name, false, createdBy, LocalDate.now()))
                        .thenApply(rows -> rows.rowCount() > 0) : client.query("SELECT 1 FROM DUAL").thenApply(rows -> false))
                .whenComplete((content, err) -> {
                    if (err != null) {
                        System.out.println("Error: " + err.getMessage());
                    } else {
                        System.out.println("Content: " + content);
                    }
                });

    }

}

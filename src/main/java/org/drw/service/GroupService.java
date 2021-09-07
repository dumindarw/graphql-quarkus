package org.drw.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.drw.model.GeoJSON;
import org.drw.model.Group;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import graphql.schema.DataFetchingEnvironment;

@ApplicationScoped
@Slf4j
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
                    "INSERT INTO tbl_groups (id, name, location, blacklisted, createdBy, createdDate) VALUES (?, ?, ?, ?, ?)")
                .execute(Tuple.of("7d60aacb-267b-403f-9aad-ae730ece2d75",
                    "AdminGroup",
                    new JsonObject(),
                    false,
                    "Duminda",
                    LocalDate.now())).toCompletionStage()
                    .whenComplete((content, err) -> {
                        if (err != null) {
                            System.out.println("Error: " + err.getMessage());
                        } else {
                            System.out.println("Content: " + content);
                        }
                    });
    }

    private static GeoJSON getLocationFromString(String locationString){
        GeoJSON location = null;
        try {
            location = new ObjectMapper().readValue(locationString, GeoJSON.class);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return location;
    }

    private static Group from(Row row) {

        return new Group(
            row.getString("id"),
                row.getString("name"),
                getLocationFromString(row.getString("location")),
                row.getBoolean("blacklisted"),
                row.getString("createdBy"),
                row.getLocalDate("createdDate"));
    }

    public CompletionStage<List<Group>> allAvailableGroups(DataFetchingEnvironment env) {
        boolean isBlackListed = env.getArgument("isBlackListed");

        return client.query("SELECT id, name, location, createdBy, createdDate, blacklisted FROM tbl_groups")
            .execute().toCompletionStage()
                .thenApply(rowSet -> {
                    List<Group> list = new ArrayList<>(rowSet.size());
                    for (Row row : rowSet)
                        if (!isBlackListed)
                            list.add(from(row));

                    return list;
                });
    }

    public CompletionStage<Group> getGroupById(DataFetchingEnvironment env) {
        String groupId = env.getArgument("id");

        return client.preparedQuery("SELECT id, name, location, createdBy, createdDate, blacklisted from tbl_groups WHERE id = ?")
            .execute(Tuple.of(groupId))
            .toCompletionStage().thenApply(rows -> {
                if (rows.size() > 0)
                    return from(rows.iterator().next());
                else
                    return new Group();
            });
    }

    public CompletionStage<Boolean> addGroup(DataFetchingEnvironment env)  {
        String name = env.getArgument("name");
        String createdBy = env.getArgument("createdBy");
        String location = null;

        try {
            location = new ObjectMapper().writeValueAsString(env.getArgument("location"));
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String loc = location;
        return client.preparedQuery("SELECT id, name FROM tbl_groups WHERE name = ?")
            .execute(Tuple.of(name))
            .toCompletionStage()
                .thenApply(rows ->  rows.size() <= 0 )
                .whenComplete((content, err) -> {
                    if (err != null) {
                        System.out.println("Error: " + err.getMessage());
                    } else {
                        System.out.println("Group Exists: " + content);
                    }
                })
                .thenCompose(r -> r ? client.preparedQuery(
                        "INSERT INTO tbl_groups (id, name, location, blacklisted, createdBy, createdDate) VALUES (?, ?, ?, ?, ?, ?)")
                    .execute(Tuple.of(UUID.randomUUID().toString(), name, loc, false, createdBy, LocalDate.now()))
                    .toCompletionStage()
                        .thenApply(rows -> rows.rowCount() > 0) : client.query("SELECT 1 FROM DUAL").execute().toCompletionStage().thenApply(rows -> false))
                .whenComplete((content, err) -> {
                    if (err != null) {
                        System.out.println("Error: " + err.getMessage());
                    } else {
                        System.out.println("Content: " + content);
                    }
                });

    }

}

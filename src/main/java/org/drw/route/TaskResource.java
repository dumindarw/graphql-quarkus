package org.drw.route;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import org.drw.model.Task;
import org.drw.repo.TaskService;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.Map;

@Path("/")
public class TaskResource {

    @Inject
    Vertx vertx;

    @Inject
    TaskService taskRepo;

    private Map<String, Task> tasks;

    public void init(@Observes Router router) {


        GraphQL graphQL = setupGraphQL();
        GraphQLHandler graphQLHandler = GraphQLHandler.create(graphQL);

        router.route("/graphql").handler(graphQLHandler);
    }

    private GraphQL setupGraphQL(){

        String schema = vertx.fileSystem().readFileBlocking("tasks.graphqls").toString();

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder-> builder.dataFetcher("allTasks", taskRepo::allTasks))
                .type("Mutation",builder -> builder.dataFetcher("complete", taskRepo::complete))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        return new GraphQL.Builder(graphQLSchema).build();
    }

}

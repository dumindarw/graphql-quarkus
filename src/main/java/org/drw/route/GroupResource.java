package org.drw.route;

import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import org.drw.repo.GroupService;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/")
public class GroupResource {

    @Inject
    Vertx vertx;

    @Inject
    GroupService taskRepo;

    public void init(@Observes Router router) {

        GraphQL graphQL = setupGraphQL();
        GraphQLHandler graphQLHandler = GraphQLHandler.create(graphQL);

        router.route("/graphql").handler(graphQLHandler);
    }

    private GraphQL setupGraphQL(){

        String schema = vertx.fileSystem().readFileBlocking("group.graphqls").toString();

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder-> builder.dataFetcher("allGroups", taskRepo::allAvailableGroups))
                .type("Mutation",builder -> builder.dataFetcher("addGroup", taskRepo::addGroup))
                .scalar(ExtendedScalars.Date)
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        return new GraphQL.Builder(graphQLSchema).build();
    }

}

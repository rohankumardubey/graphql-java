package graphql.schema;


import com.google.common.collect.ImmutableList;
import graphql.DirectivesUtil;
import graphql.Internal;
import graphql.PublicApi;
import graphql.language.ScalarTypeDefinition;
import graphql.language.ScalarTypeExtensionDefinition;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static graphql.Assert.assertNotNull;
import static graphql.Assert.assertValidName;
import static graphql.schema.SchemaElementChildrenContainer.newSchemaElementChildrenContainer;
import static java.util.Collections.emptyList;

/**
 * A scalar type is a leaf node in the graphql tree of types.  This class allows you to define new scalar types.
 *
 * <blockquote>
 * GraphQL provides a number of built‐in scalars, but type systems can add additional scalars with semantic meaning,
 * for example, a GraphQL system could define a scalar called Time which, while serialized as a string, promises to
 * conform to ISO‐8601. When querying a field of type Time, you can then rely on the ability to parse the result with an ISO‐8601 parser and use a client‐specific primitive for time.
 * <p>
 * From the spec : http://facebook.github.io/graphql/#sec-Scalars
 * </blockquote>
 * <p>
 * graphql-java ships with a set of predefined scalar types via {@link graphql.Scalars}
 *
 * @see graphql.Scalars
 */
@PublicApi
public class GraphQLScalarType implements GraphQLNamedInputType, GraphQLNamedOutputType, GraphQLUnmodifiedType, GraphQLNullableType, GraphQLDirectiveContainer {

    private final String name;
    private final String description;
    private final Coercing coercing;
    private final ScalarTypeDefinition definition;
    private final ImmutableList<ScalarTypeExtensionDefinition> extensionDefinitions;
    private final DirectivesUtil.DirectivesHolder directives;
    private final String specifiedByUrl;

    public static final String CHILD_DIRECTIVES = "directives";
    public static final String CHILD_APPLIED_DIRECTIVES = "appliedDirectives";

    @Internal
    private GraphQLScalarType(String name,
                              String description,
                              Coercing coercing,
                              List<GraphQLDirective> directives,
                              List<GraphQLAppliedDirective> appliedDirectives,
                              ScalarTypeDefinition definition,
                              List<ScalarTypeExtensionDefinition> extensionDefinitions,
                              String specifiedByUrl) {
        assertValidName(name);
        assertNotNull(coercing, () -> "coercing can't be null");
        assertNotNull(directives, () -> "directives can't be null");

        this.name = name;
        this.description = description;
        this.coercing = coercing;
        this.definition = definition;
        this.directives = new DirectivesUtil.DirectivesHolder(directives, appliedDirectives);
        this.extensionDefinitions = ImmutableList.copyOf(extensionDefinitions);
        this.specifiedByUrl = specifiedByUrl;
    }

    @Override
    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }

    public String getSpecifiedByUrl() {
        return specifiedByUrl;
    }

    public Coercing getCoercing() {
        return coercing;
    }

    public ScalarTypeDefinition getDefinition() {
        return definition;
    }

    public List<ScalarTypeExtensionDefinition> getExtensionDefinitions() {
        return extensionDefinitions;
    }

    @Override
    public List<GraphQLDirective> getDirectives() {
        return directives.getDirectives();
    }

    @Override
    public Map<String, GraphQLDirective> getDirectivesByName() {
        return directives.getDirectivesByName();
    }

    @Override
    public Map<String, List<GraphQLDirective>> getAllDirectivesByName() {
        return directives.getAllDirectivesByName();
    }

    @Override
    public GraphQLDirective getDirective(String directiveName) {
        return directives.getDirective(directiveName);
    }

    @Override
    public List<GraphQLAppliedDirective> getAppliedDirectives() {
        return directives.getAppliedDirectives();
    }

    @Override
    public Map<String, List<GraphQLAppliedDirective>> getAllAppliedDirectivesByName() {
        return directives.getAllAppliedDirectivesByName();
    }

    @Override
    public GraphQLAppliedDirective getAppliedDirective(String directiveName) {
        return directives.getAppliedDirective(directiveName);
    }

    @Override
    public String toString() {
        return "GraphQLScalarType{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", coercing=" + coercing +
                '}';
    }

    /**
     * This helps you transform the current GraphQLObjectType into another one by starting a builder with all
     * the current values and allows you to transform it how you want.
     *
     * @param builderConsumer the consumer code that will be given a builder to transform
     *
     * @return a new object based on calling build on that builder
     */
    public GraphQLScalarType transform(Consumer<Builder> builderConsumer) {
        Builder builder = newScalar(this);
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Override
    public GraphQLSchemaElement copy() {
        return newScalar(this).build();
    }


    @Override
    public TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor) {
        return visitor.visitGraphQLScalarType(this, context);
    }

    @Override
    public List<GraphQLSchemaElement> getChildren() {
        List<GraphQLSchemaElement> children = new ArrayList<>(directives.getDirectives());
        children.addAll(directives.getAppliedDirectives());
        return children;
    }

    @Override
    public SchemaElementChildrenContainer getChildrenWithTypeReferences() {
        return newSchemaElementChildrenContainer()
                .children(CHILD_DIRECTIVES, directives.getDirectives())
                .children(CHILD_APPLIED_DIRECTIVES, directives.getAppliedDirectives())
                .build();
    }

    @Override
    public GraphQLScalarType withNewChildren(SchemaElementChildrenContainer newChildren) {
        return transform(builder ->
                builder.replaceDirectives(newChildren.getChildren(CHILD_DIRECTIVES))
                        .replaceAppliedDirectives(newChildren.getChildren(CHILD_APPLIED_DIRECTIVES))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }


    public static Builder newScalar() {
        return new Builder();
    }

    public static Builder newScalar(GraphQLScalarType existing) {
        return new Builder(existing);
    }


    @PublicApi
    public static class Builder extends GraphqlDirectivesContainerTypeBuilder {
        private Coercing coercing;
        private ScalarTypeDefinition definition;
        private List<ScalarTypeExtensionDefinition> extensionDefinitions = emptyList();
        private String specifiedByUrl;

        public Builder() {
        }

        public Builder(GraphQLScalarType existing) {
            name = existing.getName();
            description = existing.getDescription();
            coercing = existing.getCoercing();
            definition = existing.getDefinition();
            extensionDefinitions = existing.getExtensionDefinitions();
            specifiedByUrl = existing.getSpecifiedByUrl();
            copyExistingDirectives(existing);
        }

        @Override
        public Builder name(String name) {
            super.name(name);
            return this;
        }

        @Override
        public Builder description(String description) {
            super.description(description);
            return this;
        }

        public Builder specifiedByUrl(String specifiedByUrl) {
            this.specifiedByUrl = specifiedByUrl;
            return this;
        }

        @Override
        public Builder comparatorRegistry(GraphqlTypeComparatorRegistry comparatorRegistry) {
            super.comparatorRegistry(comparatorRegistry);
            return this;
        }

        public Builder definition(ScalarTypeDefinition definition) {
            this.definition = definition;
            return this;
        }

        public Builder extensionDefinitions(List<ScalarTypeExtensionDefinition> extensionDefinitions) {
            this.extensionDefinitions = extensionDefinitions;
            return this;
        }

        public Builder coercing(Coercing coercing) {
            this.coercing = coercing;
            return this;
        }


        public GraphQLScalarType build() {
            return new GraphQLScalarType(name,
                    description,
                    coercing,
                    sort(directives, GraphQLScalarType.class, GraphQLDirective.class),
                    sort(appliedDirectives, GraphQLScalarType.class, GraphQLAppliedDirective.class),
                    definition,
                    extensionDefinitions,
                    specifiedByUrl);
        }
    }
}
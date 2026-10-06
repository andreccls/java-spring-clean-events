package com.andrecoura.events.web;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.andrecoura.events.application.UseCase;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the Clean Architecture dependency rule. Maven modules already make most violations
 * impossible to compile; these tests also catch framework leaks that the module graph cannot see
 * (e.g. Spring or JPA annotations sneaking into the domain through a transitive dependency).
 */
@AnalyzeClasses(packages = "com.andrecoura.events", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule layersOnlyDependInward = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("com.andrecoura.events.domain..")
            .layer("Application").definedBy("com.andrecoura.events.application..")
            .layer("Infrastructure").definedBy("com.andrecoura.events.infrastructure..")
            .layer("Web").definedBy("com.andrecoura.events.web..")
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Web")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Web")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Web");

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("com.andrecoura.events.domain..")
            .should().dependOnClassesThat(resideInAnyPackage(
                    "org.springframework..", "jakarta..", "javax..", "org.hibernate..", "com.fasterxml..", "io.swagger.."))
            .as("domain must not depend on any framework");

    @ArchTest
    static final ArchRule applicationIsFrameworkFree = noClasses()
            .that().resideInAPackage("com.andrecoura.events.application..")
            .should().dependOnClassesThat(resideInAnyPackage(
                    "org.springframework..", "jakarta..", "javax..", "org.hibernate..", "com.fasterxml..", "io.swagger.."))
            .as("application must not depend on any framework");

    @ArchTest
    static final ArchRule controllersDoNotTouchPersistence = noClasses()
            .that().resideInAPackage("com.andrecoura.events.web..")
            .and().doNotHaveSimpleName("EventsApplication")
            .should().dependOnClassesThat().resideInAPackage("com.andrecoura.events.infrastructure..")
            .as("controllers talk to use cases, never to adapters (only the composition root imports infrastructure)");

    @ArchTest
    static final ArchRule useCasesLiveInApplication = classes()
            .that().areAnnotatedWith(UseCase.class)
            .should().resideInAPackage("com.andrecoura.events.application..")
            .andShould().bePublic();
}

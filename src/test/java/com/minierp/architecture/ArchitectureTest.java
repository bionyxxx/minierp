package com.minierp.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Guards the hexagonal layer boundaries on every {@code ./mvnw verify} run.
 * Only production classes are analyzed; tests may legitimately span layers.
 */
@AnalyzeClasses(packages = "com.minierp", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /** {@code domain/} is pure Java: no Spring, no JPA/Jakarta, no framework. */
    @ArchTest
    static final ArchRule domain_is_framework_free = noClasses()
            .that().resideInAPackage("com.minierp.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..", "javax..", "org.hibernate..");

    /** {@code application/} orchestrates use cases through ports, never through infrastructure. */
    @ArchTest
    static final ArchRule application_does_not_depend_on_infrastructure = noClasses()
            .that().resideInAPackage("com.minierp.application..")
            .should().dependOnClassesThat().resideInAPackage("com.minierp.infrastructure..");

    /** The web adapter drives use cases; it must not reach into persistence internals. */
    @ArchTest
    static final ArchRule web_does_not_depend_on_persistence = noClasses()
            .that().resideInAPackage("com.minierp.infrastructure.web..")
            .should().dependOnClassesThat().resideInAPackage("com.minierp.infrastructure.persistence..");
}

// Copyright © 2010-2016, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package org.specsy.junit5;

import org.junit.Test;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestId;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.listeners.LoggingListener;
import org.junit.gen5.launcher.listeners.SummaryGeneratingListener;
import org.junit.gen5.launcher.listeners.TestExecutionSummary;
import org.junit.gen5.launcher.main.LauncherFactory;
import org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder;
import org.specsy.java.JavaSpecsy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.ClasspathSelector.forPath;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.EngineIdFilter.byEngineId;

public class SpecsyTestEngineTest {

    @Test
    public void runs_Specsy_with_JUnit5() {
        Launcher launcher = LauncherFactory.create();

        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(summaryListener);
        launcher.registerTestExecutionListeners(new LoggingListener((throwable, messageSupplier) -> {
            System.out.println("[INFO] " + messageSupplier.get());
        }));

        launcher.execute(TestDiscoveryRequestBuilder.request()
                .select(forClass(DummySpec.class))
                .build());

        TestExecutionSummary summary = summaryListener.getSummary();
        summary.printOn(new PrintWriter(System.out));
        summary.printFailuresOn(new PrintWriter(System.out));
//        ConsoleRunner.main(DummySpec.class.getName());

        String summaryString = toString(summary);
        assertThat(summaryString, containsString("4 tests found"));
        assertThat(summaryString, containsString("5 tests started"));
        assertThat(summaryString, containsString("4 tests successful"));
        assertThat(summaryString, containsString("1 tests failed"));
    }

    @Test
    public void reported_unique_ids() {
        List<TestIdentifier> tests = runTests(forClass(DummySpec.class));

        assertThat("tests", tests, hasSize(6));
        assertUniqueId(tests, 0, "specsy");
        assertUniqueId(tests, 1, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec");
        assertUniqueId(tests, 2, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec", "0");
        assertUniqueId(tests, 3, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec", "0", "0");
        assertUniqueId(tests, 4, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec");
        assertUniqueId(tests, 5, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec", "1");
    }

    @Test
    public void reported_display_names() {
        List<TestIdentifier> tests = runTests(forClass(DummySpec.class));

        assertThat(tests.get(0).getDisplayName(), is("Specsy"));
        assertThat(tests.get(1).getDisplayName(), is("DummySpec"));
        assertThat(tests.get(2).getDisplayName(), is("passing"));
        assertThat(tests.get(3).getDisplayName(), is("nested"));
        assertThat(tests.get(5).getDisplayName(), is("failing"));
    }

    @Test
    public void select_tests_by_UniqueId() {
        List<TestIdentifier> tests = runTests(forUniqueId("specsy:org.specsy.junit5.SpecsyTestEngineTest$DummySpec:0"));

        assertThat("tests", tests, hasSize(4));
        assertUniqueId(tests, 0, "specsy");
        assertUniqueId(tests, 1, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec");
        assertUniqueId(tests, 2, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec", "0");
        assertUniqueId(tests, 3, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec", "0", "0");
    }

    @Test
    public void select_tests_by_classpath() {
        List<TestIdentifier> tests = runTests(forPath(myClasspathRoot()));

        assertThat("tests", tests, hasSize(6));
        assertUniqueId(tests, 0, "specsy");
        assertUniqueId(tests, 1, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec");
    }

    @Test
    public void select_tests_by_package() {
        List<TestIdentifier> tests = runTests(forPackageName(getClass().getPackage().getName()));

        assertThat("tests", tests, hasSize(6));
        assertUniqueId(tests, 0, "specsy");
        assertUniqueId(tests, 1, "specsy", "org.specsy.junit5.SpecsyTestEngineTest$DummySpec");
    }


    // guinea pigs

    public static class DummySpec extends JavaSpecsy {
        @Override
        public void run() throws Throwable {
            spec("passing", () -> {
                spec("nested", () -> {
                });
            });
            spec("failing", () -> {
                throw new AssertionError("dummy failure");
            });
        }
    }


    // helpers

    private static List<TestIdentifier> runTests(DiscoverySelector selector) {
        return runTests(Collections.singletonList(selector));
    }

    private static List<TestIdentifier> runTests(List<DiscoverySelector> selectors) {
        Launcher launcher = LauncherFactory.create();
        List<TestIdentifier> tests = new ArrayList<>();
        launcher.registerTestExecutionListeners(new TestExecutionListener() {
            @Override
            public void executionStarted(TestIdentifier testIdentifier) {
                tests.add(testIdentifier);
            }
        });
        launcher.execute(TestDiscoveryRequestBuilder.request()
                .select(selectors)
                .filter(byEngineId("specsy")) // XXX: JUnit5TestableFactory.fromUniqueId() fails when running any other engines with forUniqueId
                .build());
        return tests;
    }

    private static void assertUniqueId(List<TestIdentifier> tests, int index,
                                       String... expectedUniqueIdParts) {
        assertThat("tests[" + index + "].uniqueId", tests.get(index).getUniqueId(), is(toTestId(expectedUniqueIdParts)));
        assertThat("tests[" + index + "].parentId", tests.get(index).getParentId(), is(toParentId(expectedUniqueIdParts)));
    }

    private static TestId toTestId(String[] uniqueIdParts) {
        String uniqueId = Stream.of(uniqueIdParts)
                .reduce(new StringJoiner(":"), StringJoiner::add, StringJoiner::merge)
                .toString();
        return new TestId(uniqueId);
    }

    private static Optional<TestId> toParentId(String[] uniqueIdParts) {
        String parentId = Stream.of(uniqueIdParts)
                .limit(uniqueIdParts.length - 1)
                .reduce(new StringJoiner(":"), StringJoiner::add, StringJoiner::merge)
                .toString();
        if (parentId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new TestId(parentId));
    }

    private static String toString(TestExecutionSummary summary) {
        StringWriter sw = new StringWriter();
        summary.printOn(new PrintWriter(sw));
        return sw.toString();
    }

    private String myClasspathRoot() {
        String classFile = getClass().getName().replace(".", "/") + ".class";
        URL resource = getClass().getResource("/" + classFile);
        return resource.getPath().replace(classFile, "");
    }
}
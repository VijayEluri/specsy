// Copyright © 2010-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package org.specsy.core;

import fi.jumi.api.drivers.*;

import java.util.*;

import static org.specsy.core.Context.Status.*;

public class Context {

    enum Status {
        NOT_STARTED,
        RUNNING,
        FINISHED
    }

    private final Path targetPath;
    private final SuiteNotifier notifier;

    private Status status = NOT_STARTED;
    private SpecDeclaration current = null;
    private final List<Path> postponed = new ArrayList<>();

    public Context(Path targetPath, SuiteNotifier notifier) {
        this.targetPath = targetPath;
        this.notifier = notifier;
    }

    // control flow

    public void bootstrap(String className, Closure rootSpec) {
        changeStatus(NOT_STARTED, RUNNING);

        enterRootSpec(className);
        processSpec(rootSpec);
        exitSpec();

        changeStatus(RUNNING, FINISHED);
    }

    private void enterRootSpec(String name) {
        current = new SpecDeclaration(name, null, Path.ROOT, targetPath);
    }

    public void specify(String name, Closure body) {
        assertStatusIs(RUNNING);

        enterSpec(name);
        processSpec(body);
        exitSpec();
    }

    private void enterSpec(String name) {
        current = current.nextChild(name);
    }

    private void processSpec(Closure body) {
        if (current.shouldExecute()) {
            execute(body);
        }
        if (current.shouldPostpone()) {
            postponed.add(current.path());
        }
    }

    private void exitSpec() {
        current = current.parent();
    }

    // executing

    private void execute(Closure body) {
        notifier.fireTestFound(current.path().toTestId(), current.name());
        TestNotifier tn = notifier.fireTestStarted(current.path().toTestId());

        executeSafely(body, tn);
        for (Closure deferBlock : current.deferred()) {
            executeSafely(deferBlock, tn);
        }

        tn.fireTestFinished();
    }

    private void executeSafely(Closure body, TestNotifier tn) {
        try {
            body.run();
        } catch (Throwable t) {
            tn.fireFailure(t);
        }
    }

    // side-effects

    public void shareSideEffects() {
        current.shareSideEffects();
    }

    // deferring

    public void defer(Closure body) {
        current.addDefer(body);
    }

    public Iterable<Path> postponedPaths() {
        assertStatusIs(FINISHED);
        return Collections.unmodifiableCollection(postponed);
    }

    // status

    private void changeStatus(Status from, Status to) {
        assertStatusIs(from);
        status = to;
    }

    private void assertStatusIs(Status expected) {
        if (status != expected) {
            throw new IllegalStateException("expected status " + expected + ", but was " + status);
        }
    }
}

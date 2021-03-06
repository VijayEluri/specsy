// Copyright © 2010-2016, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package org.specsy.java;

import fi.jumi.api.RunVia;
import org.specsy.Specsy;
import org.specsy.bootstrap.ContextDealer;
import org.specsy.core.Closure;
import org.specsy.core.Context;

@RunVia(Specsy.class)
public abstract class JavaSpecsy implements Closure {

    private final Context context = ContextDealer.take();

    /**
     * The root spec containing all child specs.
     */
    @Override
    public abstract void run() throws Throwable;

    /**
     * Declares a child spec.
     */
    public void spec(String name, Closure body) {
        context.spec(name, body);
    }

    /**
     * Defers the execution of a piece of code until the end of the current spec.
     * All deferred closures will be executed in LIFO order when the current spec exits.
     */
    public void defer(Closure block) {
        context.defer(block);
    }

    /**
     * Makes all child specs of the current spec able to see each other's side-effects.
     */
    public void shareSideEffects() {
        context.shareSideEffects();
    }
}

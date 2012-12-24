// Copyright © 2010-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package org.specsy.util

import fi.jumi.core.runs.{RunIdSequence, DefaultSuiteNotifier}
import fi.jumi.core.runners.TestClassListener
import fi.jumi.core.output.OutputCapturer
import fi.jumi.actors.ActorRef

class FakeSuiteNotifier(listener: TestClassListener)
        extends DefaultSuiteNotifier(ActorRef.wrap(listener), new RunIdSequence(), new OutputCapturer())


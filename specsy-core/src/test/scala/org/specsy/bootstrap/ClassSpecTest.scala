// Copyright © 2010-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package org.specsy.bootstrap

import org.junit.{Before, Test}
import fi.jumi.core.runs.{RunIdSequence, ThreadBoundSuiteNotifier, RunListener}
import fi.jumi.api.drivers.TestId
import org.specsy.core.{Path, Context, Closure}
import org.specsy.GlobalSpy
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => is, _}
import fi.jumi.core.api.RunId
import fi.jumi.actors.ActorRef
import fi.jumi.core.stdout.OutputCapturer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

class ClassSpecTest {

  private val listener = mock(classOf[RunListener])
  private val notifier = new ThreadBoundSuiteNotifier(ActorRef.wrap(listener), new RunIdSequence(), new OutputCapturer())
  private val context = new Context(Path.ROOT, notifier)

  @Before
  def resetSpy() {
    GlobalSpy.reset()
  }

  @Test
  def passes_the_context_to_the_spec_through_ContextDealer() {
    runSpec(classOf[ContextAcquiringSpec])

    GlobalSpy.assertContains("context: " + context)
  }

  @Test
  def exceptions_thrown_by_the_constructor_are_not_wrapped_in_InvocationTargetException() {
    runSpec(classOf[ExceptionFromConstructorSpec])

    verify(listener).onFailure(is(new RunId(1)), is(TestId.ROOT), isA(classOf[DummyException]))
  }

  @Test
  def executes_the_run_method_of_specs_implementing_Closure() {
    runSpec(classOf[ClosureImplementingSpec])

    GlobalSpy.assertContains("run executed")
  }

  @Test
  def has_custom_toString_for_better_logging_in_Jumi() {
    assertThat(new ClassSpec(classOf[ContextAcquiringSpec]).toString,
      equalTo("org.specsy.bootstrap.ClassSpec(org.specsy.bootstrap.ContextAcquiringSpec)"))
  }

  private def runSpec(testClass: Class[_]) {
    val spec = new ClassSpec(testClass)
    spec.run(context)
  }
}

class ContextAcquiringSpec {
  val context = ContextDealer.take()
  GlobalSpy.add("context: " + context)
}

class ExceptionFromConstructorSpec {
  throw new DummyException()
}

class DummyException extends RuntimeException

class ClosureImplementingSpec extends Closure {
  def run() {
    GlobalSpy.add("run executed")
  }
}

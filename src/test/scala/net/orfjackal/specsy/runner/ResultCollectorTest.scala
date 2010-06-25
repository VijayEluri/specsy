package net.orfjackal.specsy.runner

import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit._
import net.orfjackal.specsy.internal._
import net.orfjackal.specsy.Specsy
import net.orfjackal.specsy.runner.notification.TestSuiteNotifier

class ResultCollectorTest {
  private val collector = new ResultCollector()
  private val suite = collector: TestSuiteNotifier

  @Test
  def an_empty_suite() {
    suite.fireSuiteStarted()
    suite.fireSuiteFinished()

    assertThat(collector.visualizedTree, is(List[String]()))
  }

  @Test
  def an_empty_test_class() {
    suite.fireSuiteStarted()

    val testClass = suite.newTestClassNotifier(classOf[DummySpec])
    testClass.fireTestClassStarted()
    testClass.fireTestClassFinished()

    suite.fireSuiteFinished()

    assertThat(collector.visualizedTree, is(List(
      classOf[DummySpec].getName
      )))
  }

  @Test
  def test_class_with_only_root_spec() {
    suite.fireSuiteStarted()
    val clazz = suite.newTestClassNotifier(classOf[DummySpec])
    clazz.fireTestClassStarted()

    val run = clazz.newTestRunNotifier()
    run.fireTestStarted(Path(), "root")
    run.fireTestFinished(Path())

    clazz.fireTestClassFinished()
    suite.fireSuiteFinished()

    assertThat(collector.visualizedTree, is(List(
      classOf[DummySpec].getName,
      "- root"
      )))
  }

  @Test
  def test_class_with_one_child() {
    suite.fireSuiteStarted()
    val clazz = suite.newTestClassNotifier(classOf[DummySpec])
    clazz.fireTestClassStarted()

    val run = clazz.newTestRunNotifier()
    run.fireTestStarted(Path(), "root")
    run.fireTestStarted(Path(0), "child A")
    run.fireTestFinished(Path(0))
    run.fireTestFinished(Path())

    clazz.fireTestClassFinished()
    suite.fireSuiteFinished()

    assertThat(collector.visualizedTree, is(List(
      classOf[DummySpec].getName,
      "- root",
      "  - child A"
      )))
  }

  @Test
  def test_class_with_many_children() {
    suite.fireSuiteStarted()
    val clazz = suite.newTestClassNotifier(classOf[DummySpec])
    clazz.fireTestClassStarted()

    val run1 = clazz.newTestRunNotifier()
    run1.fireTestStarted(Path(), "root")
    run1.fireTestStarted(Path(0), "child A")
    run1.fireTestFinished(Path(0))
    run1.fireTestFinished(Path())

    val run2 = clazz.newTestRunNotifier()
    run2.fireTestStarted(Path(), "root")
    run2.fireTestStarted(Path(1), "child B")
    run2.fireTestFinished(Path(1))
    run2.fireTestFinished(Path())

    clazz.fireTestClassFinished()
    suite.fireSuiteFinished()

    assertThat(collector.visualizedTree, is(List(
      classOf[DummySpec].getName,
      "- root",
      "  - child A",
      "  - child B"
      )))
  }

  // TODO: multiple test classes
  // TODO: nested specs
  // TODO: concurrent execution order
  // TODO: prevent calling the methods in the wrong order

  private class DummySpec extends Specsy {
  }
}

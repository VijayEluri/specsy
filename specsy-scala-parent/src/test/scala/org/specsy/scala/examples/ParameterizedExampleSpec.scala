// Copyright © 2010-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package org.specsy.scala.examples

import fi.jumi.api.RunVia
import org.specsy.Specsy
import org.specsy.scala.ScalaSpecsy
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers._

@RunVia(classOf[Specsy])
class ParameterizedExampleSpec extends ScalaSpecsy {
  val parameters = List(
    (0, 0),
    (1, 1),
    (2, 4),
    (3, 9),
    (4, 16),
    (5, 25),
    (6, 36),
    (7, 49),
    (8, 64),
    (9, 81))

  for ((n, expectedSquare) <- parameters) {
    "Square of " + n + " is " + expectedSquare >> {
      assertThat(n * n, is(expectedSquare))
    }
  }
}

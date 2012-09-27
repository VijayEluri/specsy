// Copyright © 2010-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package org.specsy.scala.examples

import fi.jumi.api.RunVia
import org.specsy.Specsy
import org.specsy.scala.ScalaSpecsy
import java.io.File
import java.util.UUID

@RunVia(classOf[Specsy])
class DeferBlocksExample2Spec extends ScalaSpecsy {
  val dir = createWithCleanup(new File("temp-directory-" + UUID.randomUUID()), _.mkdir(), _.delete())
  val file1 = createWithCleanup(new File(dir, "file 1.txt"), _.createNewFile(), _.delete())

  "..." >> {
  }

  "..." >> {
    val file2 = createWithCleanup(new File(dir, "file 2.txt"), _.createNewFile(), _.delete())
  }

  def createWithCleanup(file: File, create: File => Boolean, delete: File => Boolean): File = {
    println("Creating " + file)
    assert(create(file), "failed to create: " + file)
    defer {
      println("Deleting " + file)
      assert(delete(file), "failed to delete: " + file)
    }
    file
  }
}

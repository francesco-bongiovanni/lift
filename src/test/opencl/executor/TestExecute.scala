package opencl.executor

import ir._
import ir.ast._
import lift.arithmetic._
import opencl.ir._
import opencl.ir.pattern._
import org.junit.Assert._
import org.junit._

object TestExecute {
  @BeforeClass def before() =
    Executor.loadAndInit()

  @AfterClass def after() =
    Executor.shutdown()
}

class TestExecute {

  private val N = SizeVar("N")
  private val M = SizeVar("M")

  @Test
  def testInferSeq(): Unit = {

    val size = 1024
    val input = Array.fill(size)(util.Random.nextFloat() * 10)

    val f = \(ArrayType(Float, N),
      MapSeq(plusOne) $ _
    )

    val execute = Execute()
    val (output: Array[Float], _) =
      execute(f, input)

    val (local, global) = execute.getAndValidateSizesForExecution(f,
      Execute.createValueMap(f, input))

    assertEquals(Cst(1), local(0))
    assertEquals(Cst(1), local(1))
    assertEquals(Cst(1), local(2))
    assertEquals(Cst(1), global(0))
    assertEquals(Cst(1), global(1))
    assertEquals(Cst(1), global(2))
    assertArrayEquals(input.map(_+1), output, 0.001f)
  }

  @Test
  def testInferNoLocalOneDim(): Unit = {

    val size = 1024
    val input = Array.fill(size)(util.Random.nextFloat() * 10)

    val f = \(ArrayType(Float, N),
      MapGlb(plusOne) $ _
    )

    val execute = Execute()
    val (output: Array[Float], _) =
      execute(f, input)

    val (local, global) = execute.getAndValidateSizesForExecution(f,
      Execute.createValueMap(f, input))

    assertEquals(Cst(128), local(0))
    assertEquals(Cst(1), local(1))
    assertEquals(Cst(1), local(2))
    assertEquals(Cst(size), global(0))
    assertEquals(Cst(1), global(1))
    assertEquals(Cst(1), global(2))
    assertArrayEquals(input.map(_+1), output, 0.001f)
  }

  @Test
  def testInferNoLocalTwoDim(): Unit = {

    val size1 = 1024
    val size2 = 512
    val input = Array.fill(size1, size2)(util.Random.nextFloat() * 10)

    val f = \(ArrayType(ArrayType(Float, M), N),
      MapGlb(1)(MapGlb(0)(plusOne)) $ _
    )

    val execute = Execute()
    val (output: Array[Float], _) =
      execute(f, input)

    val (local, global) = execute.getAndValidateSizesForExecution(f,
      Execute.createValueMap(f, input))

    assertEquals(Cst(16), local(0))
    assertEquals(Cst(16), local(1))
    assertEquals(Cst(1), local(2))
    assertEquals(Cst(size2), global(0))
    assertEquals(Cst(size1), global(1))
    assertEquals(Cst(1), global(2))
    assertArrayEquals(input.flatten.map(_+1), output, 0.001f)
  }

  @Test
  def testInferOneDim(): Unit = {
    val size = 1024
    val split = 32
    val input = Array.fill(size)(util.Random.nextFloat() * 10)

    val f = \(ArrayType(Float, N),
      MapWrg(MapLcl(plusOne)) o Split(split) $ _
    )

    val execute = Execute()
    val (output: Array[Float], _) =
      execute(f, input)

    val (local, global) = execute.getAndValidateSizesForExecution(f,
      Execute.createValueMap(f, input))

    assertEquals(Cst(split), local(0))
    assertEquals(Cst(1), local(1))
    assertEquals(Cst(1), local(2))
    assertEquals(Cst(size), global(0))
    assertEquals(Cst(1), global(1))
    assertEquals(Cst(1), global(2))
    assertArrayEquals(input.map(_+1), output, 0.001f)
  }

  @Test
  def testInferTwoDim(): Unit = {

    val size1 = 1024
    val size2 = 512
    val split1 = 32
    val split2 = 8
    val input = Array.fill(size1, size2)(util.Random.nextFloat() * 10)

    val f = \(ArrayType(ArrayType(Float, M), N),
      Untile() o
        MapWrg(1)(MapWrg(0)(MapLcl(1)(MapLcl(0)(plusOne)))) o
        Tile(split1, split2) $ _
    )

    val execute = Execute()
    val (output: Array[Float], _) =
      execute(f, input)

    val (local, global) = execute.getAndValidateSizesForExecution(f,
      Execute.createValueMap(f, input))

    assertEquals(Cst(split2), local(0))
    assertEquals(Cst(split1), local(1))
    assertEquals(Cst(1), local(2))
    assertEquals(Cst(size2), global(0))
    assertEquals(Cst(size1), global(1))
    assertEquals(Cst(1), global(2))
    assertArrayEquals(input.flatten.map(_+1), output, 0.001f)
  }

}

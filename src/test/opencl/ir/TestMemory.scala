package opencl.ir

import lift.arithmetic.SizeVar
import ir._
import ir.ast._
import opencl.generator.IllegalKernel
import opencl.ir.pattern._
import org.junit.Assert._
import org.junit.Test

class TestMemory {

  @Test
  def zipInsideToLocalAllocation(): Unit = {
    val N = SizeVar("N")

    val arrayType = ArrayType(Float, N)
    val f = fun(
      ArrayType(ArrayType(Float, N), N),
      ArrayType(ArrayType(Float, N), N),
      (A, B) =>
        MapWrg(fun( tuple =>
          toLocal(MapLcl(fun( tuple =>
            Tuple(MapSeq(id) $ Get(tuple, 0), MapSeq(id) $ Get(tuple, 1))
          ))) $ Zip(Split(1) $ Get(tuple, 0), Split(1) $ Get(tuple, 1))
        )) $ Zip(A, B)
    )

    TypeChecker(f)

    try {
      InferOpenCLAddressSpace(f)
    } catch {
      // Don't care that final is in local
      case _: IllegalKernel =>
    }

    OpenCLMemoryAllocator(f)

    assertEquals(classOf[OpenCLMemoryCollection], f.body.mem.getClass)
    val subMemories = f.body.mem.asInstanceOf[OpenCLMemoryCollection].subMemories
    assertEquals(2, subMemories.length)
    assertEquals(LocalMemory, subMemories(0).addressSpace)
    assertEquals(LocalMemory, subMemories(1).addressSpace)
    assertEquals(OpenCLMemory.getSizeInBytes(arrayType), subMemories(0).size)
    assertEquals(OpenCLMemory.getSizeInBytes(arrayType), subMemories(1).size)
  }

  @Test
  def zipInsideToGlobalAllocation(): Unit = {
    val N = SizeVar("N")

    val arrayType = ArrayType(ArrayType(Float, N), N)
    val f = fun(
      arrayType,
      arrayType,
      (A, B) =>
        toGlobal(MapGlb(fun( tuple =>
          MapSeq(fun( tuple =>
            Tuple(MapSeq(id) $ Get(tuple, 0), MapSeq(id) $ Get(tuple, 1))
          )) $ Zip(Split(1) $ Get(tuple, 0), Split(1) $ Get(tuple, 1))
        ))) $ Zip(A, B)
    )

    TypeChecker(f)

    try {
      InferOpenCLAddressSpace(f)
    } catch {
      // Don't care the output is a tuple
      case _: IllegalKernel =>
    }

    OpenCLMemoryAllocator(f)

    assertEquals(classOf[OpenCLMemoryCollection], f.body.mem.getClass)
    val subMemories = f.body.mem.asInstanceOf[OpenCLMemoryCollection].subMemories
    assertEquals(2, subMemories.length)
    assertEquals(GlobalMemory, subMemories(0).addressSpace)
    assertEquals(GlobalMemory, subMemories(1).addressSpace)
    assertEquals(OpenCLMemory.getSizeInBytes(arrayType), subMemories(0).size)
    assertEquals(OpenCLMemory.getSizeInBytes(arrayType), subMemories(1).size)
  }

  @Test
  def zipAllocation(): Unit = {
    val N = SizeVar("N")

    val arrayType = ArrayType(ArrayType(Float, N), N)
    val f = fun(
      arrayType,
      arrayType,
      (A, B) =>
        toGlobal(MapGlb(fun( tuple =>
          MapSeq(fun( tuple =>
            Tuple(MapSeq(id) $ Get(tuple, 0), MapSeq(id) $ Get(tuple, 1))
          )) $ Zip(Split(1) $ Get(tuple, 0), Split(1) $ Get(tuple, 1))
        ))) $ Zip(A, B)
    )

    TypeChecker(f)

    try {
      InferOpenCLAddressSpace(f)
    } catch {
      // Don't care the output is a tuple
      case _: IllegalKernel =>
    }

    OpenCLMemoryAllocator(f)

    assertEquals(classOf[OpenCLMemoryCollection], f.body.mem.getClass)
    val subMemories = f.body.mem.asInstanceOf[OpenCLMemoryCollection].subMemories
    assertEquals(2, subMemories.length)
    assertEquals(GlobalMemory, subMemories(0).addressSpace)
    assertEquals(GlobalMemory, subMemories(1).addressSpace)
    assertEquals(OpenCLMemory.getSizeInBytes(arrayType), subMemories(0).size)
    assertEquals(OpenCLMemory.getSizeInBytes(arrayType), subMemories(1).size)
  }
}

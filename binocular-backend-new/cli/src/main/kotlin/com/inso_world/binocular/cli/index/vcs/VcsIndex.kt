package com.inso_world.binocular.cli.index.vcs

import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.invoke.MethodHandle
import java.lang.invoke.VarHandle
import java.nio.file.Path


class VcsIndex(
  path: String
) {
  private val lookup = SymbolLookup.libraryLookup(Path.of(path), Arena.global())
  private val linker = Linker.nativeLinker()

  fun addNumbers(a: Int, b: Int): Int {
    val handle = getF(
      "add_numbers",
      ValueLayout.JAVA_INT,              // Rust's return type: i32
      ValueLayout.JAVA_INT,              // Rust's first parameter: i32
      ValueLayout.JAVA_INT               // Rust's second parameter: i32
    );

    return handle.invokeExact(a, b) as Int
  }

  fun helloWorld() {
    var handle = getF(
      "hello_world",
      ValueLayout.ADDRESS,
    );

    val result = handle.invoke() as MemorySegment
    println(result)
    println(result.fetchString())
  }

  fun findRepo(path: String) {
    val structLayout = MemoryLayout.structLayout(
      ValueLayout.JAVA_INT.withName("x"),  // Maps to Rust's i32 `x`
      ValueLayout.JAVA_INT.withName("y") // Maps to Rust's i32 `y`
    )

    var handle = getF(
      "find_repo",
      structLayout,
      ValueLayout.ADDRESS,
    );

    val arena = Arena.ofConfined()
    var pointSegment = arena.allocate(structLayout) as SegmentAllocator;

    var returnValue = handle.invoke(pointSegment, path.toCString()) as MemorySegment;

    println("returnValue: $returnValue")

    returnValue = returnValue.reinterpret(structLayout.byteSize());

    val xHandle = structLayout.varHandle(PathElement.groupElement("x")) as VarHandle
    val yHandle = structLayout.varHandle(PathElement.groupElement("y")) as VarHandle


    println(
      xHandle.get(returnValue, 0) as Int
    )
    println(
      yHandle.get(returnValue, 0) as Int
    )
  }

  private fun getF(name: String, resLayout: MemoryLayout, vararg argsLayout: MemoryLayout): MethodHandle =
    linker.downcallHandle(lookup.find(name).orElseThrow(), FunctionDescriptor.of(resLayout, *argsLayout))

  private fun getVoidF(name: String, vararg argsLayout: MemoryLayout): MethodHandle =
    linker.downcallHandle(lookup.find(name).get(), FunctionDescriptor.ofVoid(*argsLayout))
}

// SOURCE: https://github.com/PrimogemStudio/openminecraft/blob/0f8cdca44cf4c9d4dd6c65246e0b3da5d16be35b/nativeloader/src/main/kotlin/com/primogemstudio/engine/foreign/NativeUtils.kt
fun String.toCString(): MemorySegment {
  val barr = toByteArray(Charsets.UTF_8)
  val seg = Arena.ofAuto().allocate(barr.size + 1L)
  seg.copyFrom(MemorySegment.ofArray(barr))
  seg.set(JAVA_BYTE, barr.size.toLong(), 0)
  return seg
}

fun MemorySegment.fetchString(): String {
  val buf = reinterpret(Long.MAX_VALUE)
  var chr: Byte

  var idx = 0L
  while (true) {
    chr = buf.get(JAVA_BYTE, idx)
    if (chr == 0x00.toByte()) break
    idx++
  }
  val barr = ByteArray(idx.toInt())
  MemorySegment.copy(buf, JAVA_BYTE, 0, barr, 0, idx.toInt())

  return String(barr, Charsets.UTF_8)
}

//fun Array<String>.toCStrArray(): MemorySegment {
//  return Arena.ofAuto().allocate(size * sizetLength() * 1L).apply {
//    var i = 0
//    forEach {
//      set(ADDRESS, i * sizetLength() * 1L, it.toCString())
//      i++
//    }
//  }
//}

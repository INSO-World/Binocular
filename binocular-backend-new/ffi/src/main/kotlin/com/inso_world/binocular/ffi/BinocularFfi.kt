package com.inso_world.binocular.ffi

import com.inso_world.binocular.ffi.exception.FfiException
import com.inso_world.binocular.ffi.pojos.BinocularBranchPojo
import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo
import com.inso_world.binocular.ffi.pojos.toPojo
import com.inso_world.binocular.internal.AnyhowException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BinocularFfi {
    private var logger: Logger = LoggerFactory.getLogger(BinocularFfi::class.java)

    init {
        logger.info("Loading native library...")
        val rp = loadPlatformLibrary("binocular_ffi")
        logger.debug("Loaded library: $rp")
        logger.info("Library loaded successfully.")
    }

    fun hello() {
        com.inso_world.binocular.internal
            .hello()
    }

    @Throws(FfiException::class)
    fun findRepo(path: String): BinocularRepositoryPojo {
        logger.trace("Searching repository... at '$path'")
        return try {
            com.inso_world.binocular.internal
                .findRepo(path)
                .toPojo()
        } catch (e: AnyhowException) {
            throw FfiException(e)
        }
    }

    fun traverseBranch(
        repo: BinocularRepositoryPojo,
        branchName: String,
    ): List<BinocularCommitPojo> =
        com.inso_world.binocular.internal
            .traverseBranch(repo.toFfi(), branchName)
            .map { it.toPojo() }

    fun findAllBranches(repo: BinocularRepositoryPojo): List<BinocularBranchPojo> =
        com.inso_world.binocular.internal
            .findAllBranches(repo.toFfi())
            .map { it.toPojo() }

    fun findCommit(
        repo: BinocularRepositoryPojo,
        hash: String,
    ): String =
        com.inso_world.binocular.internal
            .findCommit(repo.toFfi(), hash)

    fun traverse(
        repo: BinocularRepositoryPojo,
        sourceCmt: String,
        trgtCmt: String? = null,
    ): List<BinocularCommitPojo> =
        com.inso_world.binocular.internal
            .traverse(repo.toFfi(), sourceCmt, trgtCmt)
            .map { it.toPojo() }
}

@Throws(UnsupportedOperationException::class)
private fun loadPlatformLibrary(libBaseName: String): String {
    // 1) Detect platform
    val platform = detectPlatform()

    // 2) Map the name to e.g. "libfoo.so" / "foo.dll" / "libfoo.dylib"
    val mappedName = System.mapLibraryName(libBaseName)

    // 3) Build resource path under /{platform}/{mappedName}
    val resourcePath = "/$platform/$mappedName"

    System.setProperty("uniffi.component.$libBaseName.libraryOverride", resourcePath)

    if (BinocularFfi::class.java.getResource(resourcePath) == null) {
        throw IllegalStateException("$resourcePath does not exist on the classpath")
    }

    return resourcePath
}

@Throws(UnsupportedOperationException::class)
private fun detectPlatform(): String {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()

    return when {
        // macOS
        os.contains("mac") && (arch == "x86_64" || arch == "amd64") -> "x86_64-apple-darwin"
        os.contains("mac") && (arch == "aarch64" || arch == "arm64") -> "aarch64-apple-darwin"

        // Linux
        (os.contains("nux") || os.contains("nix")) && (arch == "x86_64" || arch == "amd64") -> "x86_64-unknown-linux-gnu"
        (os.contains("nux") || os.contains("nix")) && arch == "aarch64" -> "aarch64-unknown-linux-gnu"

        // Windows
        os.contains("win") && (arch == "x86_64" || arch == "amd64") -> "x86_64-pc-windows-msvc"
        os.contains("win") && arch == "aarch64" -> "aarch64-pc-windows-msvc"

        else -> throw UnsupportedOperationException("Unsupported OS/Arch combination: $os/$arch")
    }
}

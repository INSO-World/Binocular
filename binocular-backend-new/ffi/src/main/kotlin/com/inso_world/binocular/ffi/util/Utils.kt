package com.inso_world.binocular.ffi.util

import com.inso_world.binocular.ffi.GixIndexer

internal class Utils {
    companion object {
        @Throws(UnsupportedOperationException::class)
        internal fun loadPlatformLibrary(libBaseName: String): String {
            // 1) Detect platform
            val platform = detectPlatform()

            // 2) Map the name to e.g. "libfoo.so" / "foo.dll" / "libfoo.dylib"
            val mappedName = System.mapLibraryName(libBaseName)

            // 3) Build resource path under /{platform}/{mappedName}
            val resourcePath = "/$platform/$mappedName"

            System.setProperty("uniffi.component.$libBaseName.libraryOverride", resourcePath)

            if (GixIndexer::class.java.getResource(resourcePath) == null) {
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
    }
}

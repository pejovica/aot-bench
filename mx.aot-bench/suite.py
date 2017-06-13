suite = {
  "name" : "aot-bench",
  "mxversion" : "5.111.0",
  "imports" : {
    "suites": [
    ]
  },
  "libraries" : {
  },

  "jdklibraries" : {
    "JDK_TOOLS" : {
      "path" : "lib/tools.jar",
      "sourcePath" : "lib/tools.src.zip",
      "optional" : False,
      "jdkStandardizedSince" : "9",
      "module" : "com.sun.tools",
    },
  },

  "projects" : {
    "com.oracle.svm.bench.shootouts" : {
      "subDir" : "java",
      "sourceDirs" : ["src"],
      "javaCompliance" : "1.8",
      "dependencies" : [
         "JDK_TOOLS",
      ]
    },
  },
}

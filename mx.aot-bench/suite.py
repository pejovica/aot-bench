suite = {
  "name" : "aot-bench",
  "mxversion" : "5.111.0",
  "imports" : {
    "suites": [
    ]
  },
  "libraries" : {
  },
  "projects" : {
    "com.oracle.svm.bench.shootouts" : {
      "subDir" : "java",
      "sourceDirs" : ["src"],
      "javaCompliance" : "1.8",
    },
  },
}

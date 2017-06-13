suite = {
  "name" : "aot-bench",
  "mxversion" : "5.111.0",
  "imports" : {
    "suites": [
    ]
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

  "libraries" : {
    "SCALARIFORM" : {
      "maven" : {
        "groupId" : "org.scalariform",
        "artifactId" : "scalariform_2.11",
        "version" : "0.1.8",
      },
      "sha1" : "6b1564f69e3896f475b9465c1c6e3d06cf981fab",
      "dependencies" : [
        "SCALA_LIBRARY",
        "SCALA_XML",
        "SCALA_PARSER_COMBINATORS",
      ],
    },
    "SCALA_LIBRARY" : {
      "maven" : {
        "groupId" : "org.scala-lang",
        "artifactId" : "scala-library",
        "version" : "2.11.7",
      },
      "sha1" : "f75e7acabd57b213d6f61483240286c07213ec0e",
    },
    "SCALA_XML" : {
      "maven" : {
        "groupId" : "org.scala-lang.modules",
        "artifactId" : "scala-xml_2.11",
        "version" : "1.0.5",
      },
      "sha1" : "77ac9be4033768cf03cc04fbd1fc5e5711de2459",
    },
    "SCALA_PARSER_COMBINATORS" : {
      "maven" : {
        "groupId" : "org.scala-lang.modules",
        "artifactId" : "scala-parser-combinators_2.11",
        "version" : "1.0.4",
      },
      "sha1" : "7369d653bcfa95d321994660477a4d7e81d7f490",
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
    "com.oracle.svm.bench.scalariform" : {
      "subDir" : "java",
      "sourceDirs" : ["src"],
      "javaCompliance" : "1.8",
      "dependencies" : [
        "com.oracle.svm.bench.shootouts",
        "SCALARIFORM",
      ],
    },
  },
}

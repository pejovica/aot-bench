package com.oracle.svm.bench.shootouts;

import com.oracle.svm.bench.common.AOTBench;
import com.oracle.svm.bench.common.BenchRunner;

import java.net.URI;
import java.util.Collections;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;

@AOTBench("javac")
public class javac {

    private static final String SOURCE = "import java.util.Arrays;\n" +
                    "\n" +
                    "public class TestStream {\n" +
                    "\n" +
                    " static int[] array = new int[] {\n" +
                    "  1,\n" +
                    "  2,\n" +
                    "  3\n" +
                    " };\n" +
                    "\n" +
                    " static final int N = 20;\n" +
                    " static final double MALE_RATIO = 0.5;\n" +
                    " static final int MAX_AGE = 100;\n" +
                    " static final int MAX_HEIGHT = 200;\n" +
                    " /**\n" +
                    "  * @param args the command line arguments\n" +
                    "  */\n" +
                    " public static void main(String[] args) {\n" +
                    "\n" +
                    "  // Create data set.\n" +
                    "  Person[] persons = new Person[N];\n" +
                    "  for (int k = 0; k < N; ++k) {\n" +
                    "   persons[k] = new Person(Math.random() > MALE_RATIO ? Sex.MALE : Sex.FEMALE, (int)(Math.random() * MAX_HEIGHT), (int)(Math.random() * MAX_AGE));\n" +
                    "  }\n" +
                    "  long sum = 0;\n" +
                    "  for (int j = 1; j < 21; j++) {\n" +
                    "   long time = System.currentTimeMillis();\n" +
                    "   for (int i = 0; i < 1000000; ++i) {\n" +
                    "    getValue(persons);\n" +
                    "   }\n" +
                    "   long currentTime = (System.currentTimeMillis() - time);\n" +
                    "   sum += currentTime;\n" +
                    "   System.out.println(\"Iteration \" + j + \" finished in \" + currentTime + \" milliseconds.\");\n" +
                    "  }\n" +
                    "  System.out.println(\"TOTAL time: \" + sum);\n" +
                    " }\n" +
                    "\n" +
                    " public static double getValue2() {\n" +
                    "  int[] a = array;\n" +
                    "  int i = 0;\n" +
                    "  do {\n" +
                    "   a[i] = 0;\n" +
                    "  } while (i++ < a.length);\n" +
                    "  return 1.0;\n" +
                    " }\n" +
                    "\n" +
                    " public enum Sex {\n" +
                    "  MALE,\n" +
                    "  FEMALE\n" +
                    " }\n" +
                    "\n" +
                    " public static class Person {\n" +
                    "\n" +
                    "  public Person(Sex gender, int height, int age) {\n" +
                    "   this.gender = gender;\n" +
                    "   this.height = height;\n" +
                    "   this.age = age;\n" +
                    "  }\n" +
                    "\n" +
                    "  private Sex gender;\n" +
                    "  private int age;\n" +
                    "  private int height;\n" +
                    "\n" +
                    "  public int getHeight() {\n" +
                    "   return height;\n" +
                    "  }\n" +
                    "\n" +
                    "  public int getAge() {\n" +
                    "   return age;\n" +
                    "  }\n" +
                    "\n" +
                    "  public Sex getGender() {\n" +
                    "   return gender;\n" +
                    "  }\n" +
                    " }\n" +
                    "\n" +
                    "\n" +
                    " public static double getValue(Person[] persons) {\n" +
                    "  return Arrays.stream(persons)\n" +
                    "   .filter(p -> p.getGender() == Sex.MALE)\n" +
                    "   .filter(p -> p.getHeight() > 100)\n" +
                    "   .mapToInt(Person::getAge)\n" +
                    "   .filter(age -> age > 10)\n" +
                    "   .average()\n" +
                    "   .getAsDouble();\n" +
                    " }\n" +
                    "}\n";

    private static final JavaSourceFromString TEST_STREAM = new JavaSourceFromString("TestStream", SOURCE);
    private static final JavaCompiler COMPILER = com.sun.tools.javac.api.JavacTool.create();
    private static final StandardJavaFileManager FILE_MANAGER = COMPILER.getStandardFileManager(null, null, null);


    public static void main(String[] args) {
        BenchRunner.run(javac::bench, javac.class.getAnnotation(AOTBench.class).value(), args);
    }

    private static void bench(String[] args) {
        COMPILER.getTask(null, FILE_MANAGER, null, Collections.singletonList("-proc:none"), null, Collections.singletonList(TEST_STREAM)).call();
    }

    private static class JavaSourceFromString extends SimpleJavaFileObject {
        /**
         * The source code of this "file".
         */
        final String code;

        /**
         * Constructs a new JavaSourceFromString.
         *
         * @param name the name of the compilation unit represented by this file object
         * @param code the source code for the compilation unit represented by this file object
         */
        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
                            Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

}

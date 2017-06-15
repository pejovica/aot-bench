package com.oracle.svm.bench.scalariform;

import com.oracle.svm.bench.common.AOTBench;
import com.oracle.svm.bench.common.BenchRunner;

import scalariform.ScalaVersions;
import scalariform.formatter.ScalaFormatter$;
import scalariform.formatter.preferences.FormattingPreferences$;

@AOTBench("scalariform")
public class scalariform {

    public static void main(String[] args) {
        String benchName = scalariform.class.getAnnotation(AOTBench.class).value();
        BenchRunner.run(scalariform::bench, benchName, args);
    }

    private static void bench(String[] args) {
        System.out.println(ScalaFormatter$.MODULE$.format(SCALA_SOURCE, FormattingPreferences$.MODULE$, null, 0, ScalaVersions.DEFAULT_VERSION()));
    }

    private static final String SCALA_SOURCE = "" +
            "/*                     __                                               *\\\n" +
            "**     ________ ___   / /  ___     Scala API                            **\n" +
            "**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **\n" +
            "**  __\\ \\/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **\n" +
            "** /____/\\___/_/ |_/____/_/ | |                                         **\n" +
            "**                          |/                                          **\n" +
            "\\*                                                                      */\n" +
            "\n" +
            "package scala.io\n" +
            "\n" +
            "import java.util.Arrays\n" +
            "import java.io.{ InputStream, BufferedReader, InputStreamReader, PushbackReader }\n" +
            "import Source.DefaultBufSize\n" +
            "import scala.collection.{ Iterator, AbstractIterator }\n" +
            "import scala.collection.mutable.ArrayBuffer\n" +
            "\n" +
            "/** This object provides convenience methods to create an iterable\n" +
            " *  representation of a source file.\n" +
            " *\n" +
            " *  @author  Burak Emir, Paul Phillips\n" +
            " */\n" +
            "class BufferedSource(inputStream: InputStream, bufferSize: Int)(implicit val codec: Codec) extends Source {\n" +
            "  def this(inputStream: InputStream)(implicit codec: Codec) = this(inputStream, DefaultBufSize)(codec)\n" +
            "  def reader() = new InputStreamReader(inputStream, codec.decoder)\n" +
            "  def bufferedReader() = new BufferedReader(reader(), bufferSize)\n" +
            "\n" +
            "  // The same reader has to be shared between the iterators produced\n" +
            "  // by iter and getLines. This is because calling hasNext can cause a\n" +
            "  // block of data to be read from the stream, which will then be lost\n" +
            "  // to getLines if it creates a new reader, even though next() was\n" +
            "  // never called on the original.\n" +
            "  private var charReaderCreated = false\n" +
            "  private lazy val charReader = {\n" +
            "    charReaderCreated = true\n" +
            "    bufferedReader()\n" +
            "  }\n" +
            "\n" +
            "  override lazy val iter = (\n" +
            "    Iterator\n" +
            "    continually (codec wrap charReader.read())\n" +
            "    takeWhile (_ != -1)\n" +
            "    map (_.toChar)\n" +
            "  )\n" +
            "\n" +
            "  private def decachedReader: BufferedReader = {\n" +
            "    // Don't want to lose a buffered char sitting in iter either. Yes,\n" +
            "    // this is ridiculous, but if I can't get rid of Source, and all the\n" +
            "    // Iterator bits are designed into Source, and people create Sources\n" +
            "    // in the repl, and the repl calls toString for the result line, and\n" +
            "    // that calls hasNext to find out if they're empty, and that leads\n" +
            "    // to chars being buffered, and no, I don't work here, they left a\n" +
            "    // door unlocked.\n" +
            "    // To avoid inflicting this silliness indiscriminately, we can\n" +
            "    // skip it if the char reader was never created: and almost always\n" +
            "    // it will not have been created, since getLines will be called\n" +
            "    // immediately on the source.\n" +
            "    if (charReaderCreated && iter.hasNext) {\n" +
            "      val pb = new PushbackReader(charReader)\n" +
            "      pb unread iter.next().toInt\n" +
            "      new BufferedReader(pb, bufferSize)\n" +
            "    }\n" +
            "    else charReader\n" +
            "  }\n" +
            "\n" +
            "\n" +
            "  class BufferedLineIterator extends AbstractIterator[String] with Iterator[String] {\n" +
            "    private val lineReader = decachedReader\n" +
            "    var nextLine: String = null\n" +
            "\n" +
            "    override def hasNext = {\n" +
            "      if (nextLine == null)\n" +
            "        nextLine = lineReader.readLine\n" +
            "\n" +
            "      nextLine != null\n" +
            "    }\n" +
            "    override def next(): String = {\n" +
            "      val result = {\n" +
            "        if (nextLine == null) lineReader.readLine\n" +
            "        else try nextLine finally nextLine = null\n" +
            "      }\n" +
            "      if (result == null) Iterator.empty.next()\n" +
            "      else result\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  override def getLines(): Iterator[String] = new BufferedLineIterator\n" +
            "\n" +
            "  /** Efficiently converts the entire remaining input into a string. */\n" +
            "  override def mkString = {\n" +
            "    // Speed up slurping of whole data set in the simplest cases.\n" +
            "    val allReader = decachedReader\n" +
            "    val sb = new StringBuilder\n" +
            "    val buf = new Array[Char](bufferSize)\n" +
            "    var n = 0\n" +
            "    while (n != -1) {\n" +
            "      n = allReader.read(buf)\n" +
            "      if (n>0) sb.appendAll(buf, 0, n)\n" +
            "    }\n" +
            "    sb.result\n" +
            "  }\n" +
            "}";
}

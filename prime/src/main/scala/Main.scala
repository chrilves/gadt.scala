package chrilves.gadt.prime;

object Main {
  def renderMain(n: Int): Unit = {
    import java.io._
    val pw = new PrintWriter(new File(s"Prime$n.scala"))
    pw.write(Prime.file(n).run)
    pw.close
  }

  def main(args: Array[String]): Unit = {
    import scala.util._

    Try(args(0).toInt).toOption match {
      case Some(p) =>
        renderMain(p)
      case None =>
        println("Usage: Gadts <a positive integer>")
        System.exit(1)
    }
  }
}

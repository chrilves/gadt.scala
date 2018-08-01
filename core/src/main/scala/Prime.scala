package chrilves.gadt.prime

object Prime {

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit final class EqOps[A](val self: A) {
    @inline def ===(o: A): Boolean =
      self == o
  }

  import Pretty._

  val types: Pretty =
    log(
      """
      |/** N1 < N2 */
      |sealed abstract class LT[N1, N2]
      |object LT {
      |  /** N < (N+1) */
      |  final case class Z[N]() extends LT[N, S[N]]
      |
      |  /** N1 < N2 => N1 < (N2+1) */
      |  final case class L[N1, N2](hr: LT[N1, N2]) extends LT[N1, S[N2]]
      |}
      |
      |/** N1 + N2 = N3 */
      |sealed abstract class Add[N1, N2, N3]
      |object Add {
      |  /** N + 0 = N */
      |  final case class Z[N]() extends Add[N, _0, N]
      |
      |  /** N1 + N2 = N3 => N1 + (N2+1) = (N3+1) */
      |  final case class L[N1, N2, N3](hr: Add[N1, N2, N3])
      |      extends Add[N1, S[N2], S[N3]]
      |}
      |
      |/** `N1` does not divide `N2`: N1 ∤ N2 */
      |sealed abstract class NotDiv[N1, N2]
      |object NotDiv {
      |  /** (N2+1) < N1 => N1 ∤ (N2+1) */
      |  final case class TooBig[N1, N2](lt: LT[S[N2], N1]) extends NotDiv[N1, S[N2]]
      |
      |  /* N1 ∤ N2 => N1 ∤ (N1+N2) */
      |  final case class Sub[N1, N2, N3](hr: NotDiv[N1, N2], add: Add[N1, N2, N3])
      |      extends NotDiv[N1, N3]
      |}
      |
      |/** ∀N, N1 ≤ N < N2 ⇒ N ∤ N2 */
      |sealed abstract class ForAll[N1, N2]
      |object ForAll {
      |  /** N2 < (N1+1) ⇒ ForAll[N1, N2] */
      |  final case class Empty[N1, N2](lt: LT[N2, S[N1]]) extends ForAll[N1, N2]
      |
      |  /** N1 ∤ N2 && ForAll[(N1+1), N2] => ForAll[N1, N2] */
      |  final case class Cons[N1, N2](head: NotDiv[N1, N2], tail: ForAll[S[N1], N2])
      |      extends ForAll[N1, N2]
      |}
      |
      |/** N is prime if and only if ForAll[2, N] */
      |type Prime[N] = ForAll[_2, N]
      |""".stripMargin)

  def tNats(e: Int): Pretty = {

    def list(n: Int): String =
      (1 to n).foldLeft("String") {
        case (s, _) => s"List[$s]"
      }

    val r: Pretty =
      Pretty.log(
        """
        |/*
        |   Implementation of natural numbers (i.e. non negative integers)
        |   in the type sytem. Each natural number n is represented by a
        |   type _n :
        |
        |   type _n = List[...List[String]] with n occurences of 'List'
        |
        |   The successor operation is the type function List[_].
        | */
        |type S[N] = List[N]
        |
        |// We take the type String as Zero (it could be anything apart from List)
        |type _0 = String
        |""".stripMargin)

    (1 to (e + 1)).foldLeft(r) {
      case (s, n) => s + Pretty.log(s"type _$n = S[_${n - 1}] // ${list(n)}\n")
    }
  }

  import Term._
  import Type._

  def tNat(i: Int): Type =
    tleaf(s"_$i")

  def lt(i: Int, j: Int): Option[Term] = {
    def tpe(i: Int, j: Int): Type =
      tnode("LT", tNat(i), tNat(j))

    def z(n: Int): Option[Term] =
      Some(leaf(tpe(n, n + 1), "LT.Z", tNat(n)))

    def l(n1: Int, n2: Int): Option[Term] =
      for {
        hr <- lt(n1, n2)
      } yield
        nodet(
          tpe(n1, n2 + 1),
          tnode(
            "LT.L",
            tNat(n1),
            tNat(n2)
          ),
          hr
        )

    if (i >= j)
      None
    else if (j === i + 1)
      z(i)
    else
      l(i, j - 1)
  }

  def add(i: Int, j: Int): Option[Term] = {
    def tpe(i: Int, j: Int): Type =
      tnode("Add", tNat(i), tNat(j), tNat(i + j))

    def z(i: Int): Option[Term] =
      Some(leaf(tpe(i, 0), "Add.Z", tNat(i)))

    def l(n1: Int, n2: Int): Option[Term] =
      for {
        hr <- add(n1, n2)
      } yield
        nodet(tpe(n1, n2 + 1),
              tnode("Add.L", tNat(n1), tNat(n2), tNat(n1 + n2)),
              hr)

    if (j < 0 || i < 0)
      None;
    else if (j === 0)
      z(i)
    else
      l(i, j - 1)
  }

  def notDiv(n1: Int, n2: Int): Option[Term] = {
    def tpe(i: Int, j: Int): Type =
      tnode("NotDiv", tNat(i), tNat(j))

    def tooBig(n1: Int, n2: Int): Option[Term] =
      for {
        hr <- lt(n2 + 1, n1)
      } yield
        nodet(tpe(n1, n2 + 1), tnode("NotDiv.TooBig", tNat(n1), tNat(n2)), hr)

    def sub(n1: Int, n2: Int): Option[Term] =
      for {
        hr <- notDiv(n1, n2)
        tl <- add(n1, n2)
      } yield
        nodet(tpe(n1, n1 + n2),
              tnode("NotDiv.Sub", tNat(n1), tNat(n2), tNat(n1 + n2)),
              hr,
              tl)

    if (n1 < 0 || n2 < 0)
      None
    else if (0 < n2 && n2 < n1)
      tooBig(n1, n2 - 1)
    else
      sub(n1, n2 - n1)
  }

  def forAll(n1: Int, n2: Int): Option[Term] = {
    def tpe(i: Int, j: Int): Type =
      tnode("ForAll", tNat(i), tNat(j))

    def empty(n1: Int, n2: Int): Option[Term] =
      for {
        hr <- lt(n2, n1 + 1)
      } yield nodet(tpe(n1, n2), tnode("ForAll.Empty", tNat(n1), tNat(n2)), hr)

    def cons(n1: Int, n2: Int): Option[Term] =
      for {
        hr <- notDiv(n1, n2)
        tl <- forAll(n1 + 1, n2)
      } yield
        nodet(tpe(n1, n2), tnode("ForAll.Cons", tNat(n1), tNat(n2)), hr, tl)

    if (n1 >= n2)
      empty(n1, n2)
    else
      cons(n1, n2)
  }

  def prime(n: Int): Option[Term] =
    forAll(2, n)

  def file(n: Int): Pretty =
    log(s"object Prime$n {\n  ") +
      Pretty.block(
        tNats(n) +
          log("\n\n") +
          types +
          log("\n\n") +
          Pretty.log(s"val prime$n: Prime[_$n] =\n  ") +
          Pretty.block(
            prime(n) match {
              case Some(p) =>
                p.print
              case None =>
                Pretty.log("null")
            }
          ) +
          Pretty.log(
            s"""\n\ndef main(args: Array[String]): Unit =\n  println(s"$$prime$n")""")
      ) +
      Pretty.log("\n}")
}

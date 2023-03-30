package chrilves.gadt.prime

object Prime {

  extension [A](self: A)
    @SuppressWarnings(
      Array("org.wartremover.warts.Equals")
    )
    inline def ===(o: A): Boolean =
      self == o

  import Pretty._
  import cats._
  import cats.syntax.functor._
  import cats.syntax.flatMap._

  val types: Pretty =
    log(
      """
        |/** The type LessThan[N1, N2] encodes the property that N1 < N2
        |  *
        |  * N1 < N2 means:
        |  * - there exists some strictly positive k such that N2 = N1 + k
        |  * - so that N2 = N1 + 1 + ... + 1 with k times "+ 1"
        |  * - thus in our encoding: N2 = S[...S[N1]] with k times S[..]
        |  *
        |  * There exists a value of type LessThan[N1, N2] if and only if N1 < N2.
        |  *
        |  * For example, there exists a value of type LessThan[_0,_1]:
        |  *   val v1: LessThan[_0, S[_0]] = LessThan.LTTrivial[_0]()
        |  * but there is no value of type LessThan[_1,_0].
        |  */
        |enum LessThan[N1 <: Nat, N2 <: Nat]:
        |  /** LTBase[N] encodes the property that N < (N+1)
        |    * which is in our encoding LessThan[N, S[N]]
        |    */
        |  case LTBase[N <: Nat]() extends LessThan[N, S[N]]
        |
        |  /** Remember we want, for any types N1 and N2, that:
        |    * there exists a value of type LessThan[N1, N2] if and only if N1 < N2.
        |    *
        |    * If N1 < N2, then N1 < (N2+1). So if there exists a value of type
        |    * LessThan[N1, N2], then we need to make sure there exists also a
        |    * value of type LessThan[N1, S[N2]]. Next ensures this.
        |    */
        |  case LTRec[N1 <: Nat, N2 <: Nat](hypothesis: LessThan[N1, N2]) extends LessThan[N1, S[N2]]
        |
        |/** The type Add[N1, N2, N3] encodes the property that N1 + N2 = N3
        |  *
        |  * Again, there exists a value of type Add[N1, N2, N3] if and only if N1 + N2 = N3.
        |  */
        |enum Add[N1 <: Nat, N2 <: Nat, N3 <: Nat]:
        |  /** For any N, we have N + 0 = N
        |    * So for any N there must exist a value of type Add[N, _0, N]
        |    *
        |    * Remember that our encoding of the number 0 is the type _0
        |    */
        |  case AddZero[N <: Nat]() extends Add[N, _0, N]
        |
        |  /** For any natural number N1, N2 and N3,
        |    *   if N1 + N2 = N3 then N1 + (N2+1) = (N3+1)
        |    *
        |    * Then in our encoding it means that for any type N1, N2 and N3,
        |    *   if there exists a value of type Add[N1, N2, N3]
        |    *   then there exists a value of type Add[N1, S[N2], S[N3]]
        |    *
        |    * AddPlus1 ensures exactly that.
        |    */
        |  case AddPlus1[N1 <: Nat, N2 <: Nat, N3 <: Nat](hypothesis: Add[N1, N2, N3])
        |      extends Add[N1, S[N2], S[N3]]
        |
        |/** The type NotZero[N] encodes the property that N ≠ 0
        |  *
        |  * Again, there exists a value of type NotZero[N] if and only if N ≠ 0
        |  */
        |enum NotZero[N <: Nat]:
        |  /* The only possible way to have a value of type NotZero[N]
        |    * is through SIsPositive[X]() for some type X which is
        |    * of type NotZero[S[X]] so S[X] = N.
        |    *
        |    * Thus any S[X] will do but not _0, which is
        |    * precisely the desired property.
        |    */
        |  case SIsPositive[N <: Nat]() extends NotZero[S[N]]
        |  
        |/** The type NotDiv[N1, N2] encodes the property that N1 does not divide N2,
        |  * which is written: N1 ∤ N2.
        |  *
        |  * Again, there exists a value of type NotDiv[N1, N2] if and only if N1 ∤ N2.
        |  */
        |enum NotDiv[N1 <: Nat, N2 <: Nat]:
        |  /** If 0 < N2 < N1 then N1 can not divide N2 */
        |  case NotDivBase[N1 <: Nat, N2 <: Nat](notZero: NotZero[N2], lessThan: LessThan[N2, N1])
        |    extends NotDiv[N1, N2]
        |
        |  /* If N1 do not divide N2, then N1 does not divide N2 + N1.
        |      Proof.
        |      If N1 do not divide N2, then there exists q and r such that
        |      N2 = q*N1 + r  and 0 < r < N1
        |
        |      Thus N1 + N2 = N1 + (q*N1 + r) = (q+1)*N1 + r
        |      So N1 does not divide N1 + N2 either.
        |  */
        |  case NotDivRec[N1 <: Nat, N2 <: Nat, N3 <: Nat](hypothesis: NotDiv[N1, N2], add: Add[N1, N2, N3])
        |    extends NotDiv[N1, N3]
        |
        |  
        |/** The type ForAll[N1, N2] means:
        |  *   - N1 ≤ N2
        |  *   - and for all N, if N1 ≤ N < N2, then N do not divide N2
        |  *
        |  * Again there exists a value of type ForAll[N1, N2] if and only if:
        |  *   - N1 ≤ N2
        |  *   - and for all N, if N1 ≤ N < N2, then N do not divide N2
        |  */
        |enum ForAll[N1 <: Nat, N2 <: Nat]:
        |  /** We trivialy have
        |    *   - N ≤ N
        |    *   - and for all M, if N ≤ M < N, then M do not divide N
        |    * because there is no M such that N ≤ M and M < N.
        |    */
        |  case ForAllBase[N <: Nat]() extends ForAll[N, N]
        |
        |  /** If N1 do not divide N2
        |    * and N1 + 1 ≤ N2
        |    * and for all N, if N1+1 ≤ N < N2, then N do not divide N2
        |    * then
        |    *   - N1 ≤ N2
        |    *   - and for all N, if N1 ≤ N < N2, then N do not divide N2
        |    *
        |    * Which corresponds to:
        |    * If we have a value of type NotDiv[N1, N2] ensuring N1 do not divide N2
        |    * and a value of type ForAll[List[N1], N2] ensuring both that N1 + 1 ≤ N2
        |    * and for all N, if N1+1 ≤ N < N2, then N do not divide N2
        |    * then we have a value of type ForAll[N1, N2].
        |    *
        |    * ForAllRec ensures this.
        |    */
        |  case ForAllRec[N1 <: Nat, N2 <: Nat](head: NotDiv[N1, N2], tail: ForAll[S[N1], N2])
        |    extends ForAll[N1, N2]
        |  
        |/** The type Prime[N] encodes the property that N is prime.
        |  *
        |  * Again there exists a value of type Prime[N] if and only if N is prime.
        |  */
        |type Prime[N <: Nat] = ForAll[_2, N]
        |
        |import LessThan.*
        |import Add.*
        |import NotZero.*
        |import NotDiv.*
        |import ForAll.*
        |""".stripMargin
    )

  def tNats(e: Int): Pretty = {

    def typeN(n: Int): String =
      (1 to n).foldLeft("_0") { case (s, _) =>
        s"S[$s]"
      }

    val r: Pretty =
      Pretty.log(
        ("""
        |/*
        |  /!\ READ THIS FIRST /!\
        |
        |  This presentation is about how we can encode popositions over
        |  types and proofs of these propositions in Scala. From a logical
        |  standpoint, Scala has many unsafe feature so for this presentation
        |  to have some value, we need to restrict ourself to a safe subset
        |  of Scala (which is very close the Scalazzi subset).
        |
        |  The goal of this exercise is to exibit a value of type Prime[_""" + s"$e" + s"""]
        |  (see value `val prime$e: Prime[_$e]` below), SO:
        |  - DO NOT ALTER, IN ANY WAY, THE DEFINITION OF ANY TYPE OR VALUE BELOW
        |  - DO NOT ADD SUB CLASSES/OBJETS TO TYPES BELOW
        |  - DO NOT USE NULL IN ANY WAY
        |  - ONLY USE THE GIVEN CASE OBJECTS AND CASE CLASSES BELOW
        |  - THE GOAL IS TO PRODUCE A `val prime$e: Prime[_$e]`,
        |      NOT A `def prime$e: Prime[_$e]`,
        |      NOT A `lazy val prime$e: Prime[_$e]`!
        |  - YOUR CODE SHOULD TYPE-CHECK AND RUN PRINTING THE VALUE `prime$e`
        |
        |  You can define type aliases for convenience but you do not need to
        |  create new types.
        |*/
        |
        |/*
        |  The property we want to prove is that $e is a prime number.
        |  Firstly, we need to find some way to encode natural number
        |  (non-negative integers) as types.
        |
        |  Note that, for every natural number n:
        |
        |  n == 0 + 1 + ... + 1 with "+ 1" repeated n times.
        |
        |  1 == 0 + 1, 2 == 0 + 1 + 1, 3 = 0 + 1 + 1 + 1, etc
        |        |  
        |  We can use this to assiciate to every natural number n a type _n:
        |    
        |  0     will be assiciated with a type _0 (any type would fit).
        |  n + 1 will be associated with a type S[_n] meaning "successor of n"
        |
        |  It gives for _n:
        |
        |  type _n = S[...S[_0]] with "S[" repeated n times.
        |
        |  Note that _n is just an alias, the real concrete type is S[...S[_0]].
        |
        |  A natural number is thus:
        |   - either 0
        |   - or the successor of another natural nubmer (i.e. n - 1)
        |*/
        |
        |type Nat = _0 | S[?]
        |final abstract class _0
        |final abstract class S[N <: Nat]
        |
        |
        |""").stripMargin
      )

    (1 to (e + 1)).foldLeft(r) { case (s, n) =>
      s + Pretty.log(s"type _$n = S[_${n - 1}] // ${typeN(n)}\n")
    }
  }

  import Term._
  import Type._

  def tNat(i: Int): Type =
    tleaf(s"_$i")

  type MonadProof[F[_]] = MonadError[F, Unit]

  def lessThan[F[_]](i: Int, j: Int)(using F: MonadProof[F]): F[Term] = {
    def tpe(i: Int, j: Int): Type =
      tnode("LessThan", tNat(i), tNat(j))

    def ltBase(n: Int): F[Term] =
      F.pure(leaf(tpe(n, n + 1), "LTBase", tNat(n)))

    def ltRec(n1: Int, n2: Int): F[Term] =
      for {
        hr <- lessThan(n1, n2)
      } yield nodet(
        tpe(n1, n2 + 1),
        tnode(
          "LTRec",
          tNat(n1),
          tNat(n2)
        ),
        hr
      )

    if (i >= j)
      F.raiseError(())
    else if (j === i + 1)
      ltBase(i)
    else
      ltRec(i, j - 1)
  }

  def add[F[_]](i: Int, j: Int)(using F: MonadProof[F]): F[Term] = {
    def tpe(i: Int, j: Int): Type =
      tnode("Add", tNat(i), tNat(j), tNat(i + j))

    def addZero(i: Int): F[Term] =
      F.pure(leaf(tpe(i, 0), "AddZero", tNat(i)))

    def addPlus1(n1: Int, n2: Int): F[Term] =
      for {
        hr <- add(n1, n2)
      } yield nodet(
        tpe(n1, n2 + 1),
        tnode("AddPlus1", tNat(n1), tNat(n2), tNat(n1 + n2)),
        hr
      )

    if (j < 0 || i < 0)
      F.raiseError(())
    else if (j === 0)
      addZero(i)
    else
      addPlus1(i, j - 1)
  }

  def notZero[F[_]](i: Int)(using F: MonadProof[F]): F[Term] = {
    def tpe(i: Int): Type =
      tnode("NotZero", tNat(i))

    def sIsPositive(i: Int): F[Term] =
      F.pure(leaf(tpe(i + 1), "SIsPositive", tNat(i)))

    if (i <= 0)
      F.raiseError(())
    else
      sIsPositive(i - 1)
  }

  def notDiv[F[_]](n1: Int, n2: Int)(using F: MonadProof[F]): F[Term] = {
    def tpe(i: Int, j: Int): Type =
      tnode("NotDiv", tNat(i), tNat(j))

    def notDivBasic(n1: Int, n2: Int): F[Term] =
      for {
        nz <- notZero(n2)
        lt <- lessThan(n2, n1)
      } yield nodet(
        tpe(n1, n2),
        tnode("NotDivBase", tNat(n1), tNat(n2)),
        nz,
        lt
      )

    def notDivRec(n1: Int, n2: Int): F[Term] =
      for {
        hr <- notDiv(n1, n2)
        tl <- add(n1, n2)
      } yield nodet(
        tpe(n1, n1 + n2),
        tnode("NotDivRec", tNat(n1), tNat(n2), tNat(n1 + n2)),
        hr,
        tl
      )

    if (n1 < 0 || n2 < 0)
      F.raiseError(())
    else if (0 < n2 && n2 < n1)
      notDivBasic(n1, n2)
    else
      notDivRec(n1, n2 - n1)
  }

  def forAll[F[_]](n1: Int, n2: Int)(using F: MonadProof[F]): F[Term] = {
    def tpe(i: Int, j: Int): Type =
      tnode("ForAll", tNat(i), tNat(j))

    def forAllNil(n: Int): F[Term] =
      F.pure(nodet(tpe(n, n), tnode("ForAllBase", tNat(n))))

    def forAllCons(n1: Int, n2: Int): F[Term] =
      for {
        hr <- notDiv(n1, n2)
        tl <- forAll(n1 + 1, n2)
      } yield nodet(
        tpe(n1, n2),
        tnode("ForAllRec", tNat(n1), tNat(n2)),
        hr,
        tl
      )

    if (n1 > n2)
      F.raiseError(())
    else if (n1 === n2)
      forAllNil(n1)
    else
      forAllCons(n1, n2)
  }

  def prime[F[_]: MonadProof](n: Int): F[Term] =
    forAll(2, n)

  import cats.instances.option._

  def file(n: Int): Pretty =
    tNats(n) +
      log("\n\n") +
      types +
      log("\n\n") +
      Pretty.log(s"val prime$n: Prime[_$n] =\n  ") +
      Pretty.block(
        prime[Option](n) match {
          case Some(p) =>
            p.print
          case None =>
            Pretty.log(
              "??? // Try to build a value that pass the type checker using only the constructors."
            )
        }
      ) +
      Pretty.log(
        s"""\n\n@main\ndef primalityProofOf$n: Unit =\n  println(s"$$prime$n")"""
      )
}

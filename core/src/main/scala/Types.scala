package chrilves.gadt.prime

object Types {
  //type _n = List[...List[String]] N occurences de 'List'
  type S[N] = List[N]

  type _0 = String
  type _1 = S[_0]
  type _2 = S[_1]

  /** N1 < N2 */
  sealed abstract class LT[N1, N2]
  object LT {

    /** N < (N+1) */
    final case class Z[N]() extends LT[N, S[N]]

    /** N1 < N2 => N1 < (N2+1) */
    final case class L[N1, N2](hr: LT[N1, N2]) extends LT[N1, S[N2]]
  }

  /** N1 + N2 = N3 */
  sealed abstract class Add[N1, N2, N3]
  object Add {

    /** N + 0 = N */
    final case class Z[N]() extends Add[N, _0, N]

    /** N1 + N2 = N3 => N1 + (N2+1) = (N3+1) */
    final case class L[N1, N2, N3](hr: Add[N1, N2, N3])
        extends Add[N1, S[N2], S[N3]]
  }

  /** `N1` does not divide `N2`: N1 ∤ N2 */
  sealed abstract class NotDiv[N1, N2]
  object NotDiv {

    /** (N2+1) < N1 => N1 ∤ (N2+1) */
    final case class TooBig[N1, N2](lt: LT[S[N2], N1]) extends NotDiv[N1, S[N2]]

    /* N1 ∤ N2 => N1 ∤ (N1+N2) */
    final case class Sub[N1, N2, N3](hr: NotDiv[N1, N2], add: Add[N1, N2, N3])
        extends NotDiv[N1, N3]
  }

  /** ∀N, N1 ≤ N < N2 ⇒ N ∤ N2 */
  sealed abstract class ForAll[N1, N2]
  object ForAll {

    /** N2 < (N1+1) ⇒ ForAll<N1, N2> */
    final case class Empty[N1, N2](lt: LT[N2, S[N1]]) extends ForAll[N1, N2]

    /** N1 ∤ N2 && ForAll<(N1+1), N2> => ForAll<N1, N2> */
    final case class Cons[N1, N2](head: NotDiv[N1, N2], tail: ForAll[S[N1], N2])
        extends ForAll[N1, N2]
  }

  /** N is prime if and only if ForAll<2, N> */
  type Prime[N] = ForAll[_2, N]
}

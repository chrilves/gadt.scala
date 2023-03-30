# [Generalized Algebraic Data Types](https://en.wikipedia.org/wiki/Generalized_algebraic_data_type) example in [Scala](https://www.scala-lang.org/)

- [On Pijul Nest](https://nest.pijul.com/chrilves/gadt.scala)
- [On GitLab](https://gitlab.com/chrilves/gadt.scala)
- [On GitGub](https://github.com/chrilves/gadt.scala)

[Algebraic Data Types](https://en.wikipedia.org/wiki/Algebraic_data_type) (called **ADTs**) are a marvelous tool to model data stuctures. They are intensively used in typed functional programming like in [Haskell](https://haskell-lang.org/), [OCaml](https://ocaml.org), [Scala](https://www.scala-lang.org/), [F#](https://fsharp.org/) but also in modern imperative programming languages like [Rust](https://www.rust-lang.org), [Haxe](https://haxe.org/) or [TypeScript](https://www.typescriptlang.org/docs/handbook/advanced-types.html).


**ADTs** are defined like this: a **type** `T`, eventually with **type variables** (a.k.a. **generics**) and a finite set of functions called **constructor** or **variant**, whose return type is `T`:

```java
f1: C1 -> T
...
fn: Cn -> T
```

For example, the type of lists whose elements are of type `a`, called `List[a]`, is defined like this:

```scala
sealed abstract class List[A]
final case class Nil[A]() extends List[A]
final case class Cons[A](head: A, tail: List[A]) extends List[A]

def nil[A] :      ()      => List[A] = Nil.apply[A] _
def cons[A]: (A, List[A]) => List[A] = Cons.apply[A] _
```

Note that, *for any type `A`*, all *constructors* are required to produce values of type `List[A]`. [Generalized Algebraic Data Types](https://en.wikipedia.org/wiki/Generalized_algebraic_data_type) (called **GADTs**) remove this restriction, allowing *constructors* to produce values for specific types `A`. For example:

```scala
sealed abstract class T[A]
case object IsInt extends T[Int]
```

The *constructor* `IsInt` only produces a value of type `T[Int]` thus only in the case `A = Int`. We can then encode properties over types. For example having a value of type `T[A]` is only possible if `A` is `Int` but nothing else.

```scala
def f[A](a: A, evidence: T[A]): Int =
  evidence match {
    case IsInt => a
  }
```

This project encodes the property that the type `N` represents a prime number. The natural number number `N` is encoded as the type `List[List[...List[String]]]` with exactly `N` occurrences of `List`. The property is:
> The natural number `N` is prime *if and only if* there exists a value of type `Prime[_N]`.

For any prime number `N` the program computes a **valid value** of type `Prime[_N]`

# Usage

Run the program, where `N` is a prime natural number, via:

```sh
sbt prime/run 11
```

It produces a file named `PrimeN.scala` containing the type declarations and the value of type `Prime[_N]`. This is a valid *Scala* file that can be compiled and run to print the value via:

```sh
scala -nc Prime11.scala
```

Open the file `PrimeN.scala` to see the value `primeN` of type `Prime[_N]`.

# Encoding

## Natural numbers

```scala
type Nat = _0 | S[?]
final abstract class _0
final abstract class S[N <: Nat]


type _1 = S[_0] // S[_0]
type _2 = S[_1] // S[S[_0]]
type _3 = S[_2] // S[S[S[_0]]]
```

## `N1 < N2`

```scala
enum LessThan[N1 <: Nat, N2 <: Nat]:
  /** N < (N+1) */
  case LTBase[N <: Nat]() extends LessThan[N, S[N]]

  /** N1 < N2 => N1 < (N2+1) */
  case LTRec[N1 <: Nat, N2 <: Nat](hypothesis: LessThan[N1, N2]) extends LessThan[N1, S[N2]]
```

## `N1 + N2 = N3`

```scala
enum Add[N1 <: Nat, N2 <: Nat, N3 <: Nat]:
  /** N + 0 = N */
  case AddZero[N <: Nat]() extends Add[N, _0, N]

  /** N1 + N2 = N3 => N1 + (N2+1) = (N3+1) */
  case AddPlus1[N1 <: Nat, N2 <: Nat, N3 <: Nat](hypothesis: Add[N1, N2, N3])
      extends Add[N1, S[N2], S[N3]]
```

## `N ≠ 0`

`N1` is not `0`

```scala
enum NotZero[N <: Nat]:
  /** N ≠ 0 */
  case SIsPositive[N <: Nat]() extends NotZero[S[N]]
```

## `N1 ∤ N2`

`N1` does not divide `N2`

```scala
enum NotDiv[N1 <: Nat, N2 <: Nat]:
  /** (N2+1) < N1 => N1 ∤ (N2+1) */
  case NotDivBase[N1 <: Nat, N2 <: Nat](notZero: NotZero[N2], lessThan: LessThan[N2, N1])
    extends NotDiv[N1, N2]

  /* N1 ∤ N2 => N1 ∤ (N1+N2) */
  case NotDivRec[N1 <: Nat, N2 <: Nat, N3 <: Nat](hypothesis: NotDiv[N1, N2], add: Add[N1, N2, N3])
    extends NotDiv[N1, N3]
```

## `∀N, N1 ≤ N < N2 ⇒ N ∤ N2`

```scala
sealed abstract class ForAll[N1, N2]
object ForAll {
  final case class Empty[N1, N2](lt: LT[N2, S[N1]]) extends ForAll[N1, N2]

  final case class Cons[N1, N2](head: NotDiv[N1, N2], tail: ForAll[S[N1], N2])
      extends ForAll[N1, N2]
}

enum ForAll[N1 <: Nat, N2 <: Nat]:
  /** N2 < (N1+1) ⇒ ForAll<N1, N2> */
  case ForAllBase[N <: Nat]() extends ForAll[N, N]

  /** N1 ∤ N2 && ForAll<(N1+1), N2> => ForAll<N1, N2> */
  case ForAllRec[N1 <: Nat, N2 <: Nat](head: NotDiv[N1, N2], tail: ForAll[S[N1], N2])
    extends ForAll[N1, N2]
```

## `N` is prime

```scala
/* N is prime if and only if ForAll<2, N> */
type Prime[N] = ForAll[_2, N]
```
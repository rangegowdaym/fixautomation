# TypeScript Deep Guide (PDF-Ready)
**Comprehensive Notes + Examples + Practice**

**Author:** Copilot  
**Date:** 2026-03-29

---

## Table of Contents
1. [Core Types & Inference](#1-core-types--inference)  
2. [Unions, Intersections, Composition](#2-unions-intersections-composition)  
3. [Arrays & Tuples](#3-arrays--tuples)  
4. [Functions & Call Signatures](#4-functions--call-signatures)  
5. [Objects, Interfaces & Type Aliases](#5-objects-interfaces--type-aliases)  
6. [Narrowing & Control-Flow Analysis](#6-narrowing--control-flow-analysis)  
7. [Generics 101 → Practical](#7-generics-101--practical)  
8. [Built-in Utility Types (Essentials)](#8-built-in-utility-types-essentials)  
9. [Classes & OOP in TS (Conceptual)](#9-classes--oop-in-ts-conceptual)  
10. [Practice Exercises](#10-practice-exercises)  
11. [Practice Solutions](#11-practice-solutions)  
12. [Interview Q&A (Quick Prep)](#12-interview-qa-quick-prep)  
13. [One-Page Cheat Sheet](#13-one-page-cheat-sheet)  

---

## 1) Core Types & Inference

### 1.1 Annotate vs infer
TypeScript infers many types automatically:

```ts
let count = 10;        // inferred: number
const app = "CRM";     // inferred: "CRM" (literal)
```

**When inference is best**
- Local variables with clear assigned values.
- Small functions where intent is obvious.

**When explicit annotation is better**
- Public functions/APIs.
- Values initialized as empty object/array.
- Complex return types where you want stable contracts.

```ts
const ids: number[] = []; // clearer than relying on inference
```

---

### 1.2 Primitive types
- `string`, `number`, `boolean`, `bigint`, `symbol`

```ts
let username: string = "alice";
let age: number = 30;
let active: boolean = true;
let huge: bigint = 9007199254740993n;
let id: symbol = Symbol("id");
```

---

### 1.3 Special types
- `null`: intentional empty value
- `undefined`: value not assigned/present
- `void`: function returns no meaningful value
- `never`: function never successfully returns
- `unknown`: type-safe unknown value
- `any`: disables type safety (use sparingly)

```ts
function log(msg: string): void {
  console.log(msg);
}

function fail(msg: string): never {
  throw new Error(msg);
}

function handle(v: unknown) {
  if (typeof v === "string") {
    console.log(v.toUpperCase()); // safe after narrowing
  }
}
```

**Why `any` is risky**
```ts
let x: any = "hello";
x.notReal().boom; // no compile error, runtime crash likely
```

Prefer `unknown` + narrowing over `any`.

---

### 1.4 Object types: required, optional, readonly
```ts
type User = {
  id: number;                 // required
  email?: string;             // optional
  readonly role: "admin" | "user"; // cannot be reassigned
};
```

---

### 1.5 Literal types and widening
```ts
let mode = "dark";   // string (widened)
const fixed = "dark"; // "dark" literal
```

Literal types help model finite states:
```ts
type Theme = "light" | "dark";
```

---

### 1.6 Type assertions (`as`) and non-null (`!`)
Use only when necessary.

```ts
const input = document.getElementById("email") as HTMLInputElement;
input.value = "x@y.com";
```

```ts
const root = document.getElementById("root")!;
root.innerHTML = "Ready";
```

Better:
```ts
const root = document.getElementById("root");
if (!root) throw new Error("Missing #root");
```

---

### 1.7 `as const` for exact values
```ts
const config = {
  env: "prod",
  retries: 3,
} as const;
```

Now:
- `env` is `"prod"` (not `string`)
- `retries` is `3` (not `number`)
- properties become readonly

Great for constants, action objects, tuple-like arrays.

---

## 2) Unions, Intersections, Composition

### 2.1 Unions (`A | B`)
Model alternatives:

```ts
type Id = string | number;
```

Discriminated unions (recommended pattern):
```ts
type ApiResult<T> =
  | { kind: "ok"; data: T }
  | { kind: "error"; message: string };

function render(r: ApiResult<number>) {
  if (r.kind === "ok") return `Data: ${r.data}`;
  return `Error: ${r.message}`;
}
```

---

### 2.2 Intersections (`A & B`)
Combine requirements:

```ts
type HasId = { id: string };
type Audited = { createdAt: Date; updatedAt: Date };
type Entity = HasId & Audited;
```

---

### 2.3 Composition for DRY types
```ts
type Pagination = { page: number; pageSize: number };
type UserFilter = { role?: "admin" | "user" };
type UserQuery = Pagination & UserFilter;
```

Compose small types instead of repeating fields across multiple definitions.

---

## 3) Arrays & Tuples

### 3.1 `T[]` vs `Array<T>`
Equivalent:

```ts
let tags: string[] = ["ts", "js"];
let tags2: Array<string> = ["ts", "js"];
```

---

### 3.2 Readonly arrays
```ts
const nums: readonly number[] = [1, 2, 3];
// nums.push(4); // error
```

Useful to enforce non-mutating function parameters:

```ts
function total(values: readonly number[]) {
  return values.reduce((a, b) => a + b, 0);
}
```

---

### 3.3 Tuples for fixed positions
```ts
let point: [number, number] = [10, 20];
```

Optional elements:
```ts
type ResponseTuple = [status: number, body: string, error?: string];
```

Labeled tuples improve readability in editors and hints.

---

## 4) Functions & Call Signatures

### 4.1 Parameter and return annotations
```ts
function add(a: number, b: number): number {
  return a + b;
}
```

---

### 4.2 Optional/default parameters
```ts
function greet(name: string, title?: string) {
  return title ? `${title} ${name}` : name;
}

function increment(value: number, by = 1) {
  return value + by;
}
```

Notes:
- Optional parameter behaves like `T | undefined`.
- Defaulted parameter is treated as present inside function body.

---

### 4.3 Rest parameters with tuples
```ts
function route(...args: [method: "GET" | "POST", path: string]) {
  const [method, path] = args;
  return `${method} ${path}`;
}
```

---

### 4.4 Return inference vs explicit return
**Inference good for local code**
```ts
const square = (n: number) => n * n;
```

**Explicit return preferred for exported APIs**
```ts
export function parseIntSafe(input: string): { ok: true; value: number } | { ok: false } {
  const n = Number(input);
  if (Number.isNaN(n)) return { ok: false };
  return { ok: true, value: n };
}
```

---

## 5) Objects, Interfaces & Type Aliases

### 5.1 Interface basics
```ts
interface Product {
  id: string;
  price: number;
}
```

Extends:
```ts
interface DigitalProduct extends Product {
  downloadUrl: string;
}
```

Multiple extends:
```ts
interface A { a: number }
interface B { b: string }
interface C extends A, B { c: boolean }
```

---

### 5.2 Type aliases
```ts
type ProductId = string | number;
type Product = { id: ProductId; name: string };
```

Type aliases are necessary for unions/intersections/tuples/advanced mapped types.

---

### 5.3 Interface vs type
- Use **interface** for object contracts and extension-heavy designs.
- Use **type** for union/intersection-heavy modeling.
- Both can model object shapes.

**Declaration merging (interface only)**
```ts
interface Settings { theme: string }
interface Settings { lang: string }
// merged: { theme: string; lang: string }
```

---

### 5.4 Index signatures and safer patterns
```ts
type Scores = {
  [key: string]: number;
};
```

Limit: every property must be compatible with `number`.

Safer with known keys:
```ts
type Role = "admin" | "user" | "guest";
type RoleCount = Record<Role, number>;
```

---

## 6) Narrowing & Control-Flow Analysis

TypeScript refines types based on runtime checks.

### 6.1 `typeof`
```ts
function format(v: string | number) {
  if (typeof v === "string") return v.trim();
  return v.toFixed(2);
}
```

### 6.2 `instanceof`
```ts
class Bird { fly() {} }
class Fish { swim() {} }

function move(x: Bird | Fish) {
  if (x instanceof Bird) x.fly();
  else x.swim();
}
```

### 6.3 `in` operator
```ts
type Cat = { meow: () => void };
type Dog = { bark: () => void };

function speak(pet: Cat | Dog) {
  if ("meow" in pet) pet.meow();
  else pet.bark();
}
```

### 6.4 Optional chaining and nullish coalescing
```ts
type User = { profile?: { city?: string | null } };

function cityName(user: User) {
  return user.profile?.city ?? "Unknown";
}
```

- `?.` stops on `null/undefined`.
- `??` only uses fallback for `null/undefined` (not empty string/0/false).

---

## 7) Generics 101 → Practical

### 7.1 Reusable generic function
```ts
function identity<T>(arg: T): T {
  return arg;
}
```

Usually inferred automatically:
```ts
const n = identity(123); // T = number
```

---

### 7.2 Constraints (`extends`)
```ts
function lengthOf<T extends { length: number }>(x: T): number {
  return x.length;
}
```

---

### 7.3 Defaults
```ts
type Box<T = string> = { value: T };
const a: Box = { value: "hi" };       // T defaults to string
const b: Box<number> = { value: 42 };
```

---

### 7.4 Key constraints (`K extends keyof T`)
```ts
function getProp<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}

const u = { id: 1, name: "Alice" };
const name = getProp(u, "name"); // string
// getProp(u, "email"); // compile error
```

This pattern powers many safe helper utilities in real projects.

---

## 8) Built-in Utility Types (Essentials)

Given:
```ts
type User = {
  id: number;
  name: string;
  email?: string;
};
```

### 8.1 Object utilities
```ts
type UserPatch = Partial<User>;
type UserFull = Required<User>;
type UserRead = Readonly<User>;
type UserBasic = Pick<User, "id" | "name">;
type UserNoEmail = Omit<User, "email">;
type RoleMap = Record<"admin" | "user", number>;
```

### 8.2 Union helpers
```ts
type A = "a" | "b" | "c";
type B = Exclude<A, "a">; // "b" | "c"
type C = Extract<A, "b" | "x">; // "b"
type D = NonNullable<string | null | undefined>; // string
```

### 8.3 Promise helper
```ts
type X = Awaited<Promise<Promise<number>>>; // number
```

### Pitfalls
- `Partial` is shallow (nested objects not deeply partial).
- `Readonly` is shallow (nested objects can still be mutable unless deeply wrapped).

---

## 9) Classes & OOP in TS (Conceptual)

### 9.1 Fields, readonly, parameter properties
```ts
class Service {
  constructor(
    public readonly name: string,
    private endpoint: string
  ) {}
}
```

---

### 9.2 Access modifiers
- `public`: accessible everywhere
- `private`: class only
- `protected`: class + subclasses

---

### 9.3 Abstract classes + interfaces
```ts
interface Logger {
  log(msg: string): void;
}

abstract class Task {
  abstract run(): void;
}

class EmailTask extends Task implements Logger {
  run() {
    this.log("Running email task");
  }

  log(msg: string) {
    console.log(msg);
  }
}
```

---

### 9.4 Getters and setters for encapsulation
```ts
class Counter {
  private _value = 0;

  get value() {
    return this._value;
  }

  set value(v: number) {
    if (v < 0) throw new Error("Value must be >= 0");
    this._value = v;
  }
}
```

---

## 10) Practice Exercises

1. Create a `Status` union with `"idle" | "loading" | "done" | "error"`.  
2. Define `User` with required `id`, optional `email`, readonly `createdAt`.  
3. Write `sum(numbers: readonly number[]): number`.  
4. Create tuple type `[x: number, y: number, label?: string]`.  
5. Write function `toArray<T>(value: T): T[]`.  
6. Build `getProp<T, K extends keyof T>(obj: T, key: K): T[K]`.  
7. Create `ApiResult<T>` discriminated union with `ok`/`error` cases.  
8. Convert `User` to `UserPatch` using utility types.  
9. Use `Record` to map roles `"admin" | "user"` to permission levels.  
10. Create class `BankAccount` with private balance and getter/setter validation.  
11. Demonstrate `unknown` narrowing from `string | number` parsing input.  
12. Show why `any` can hide bugs with one short example.  
13. Build intersection type combining `HasId` and `Timestamped`.  
14. Create function with rest tuple `(...args: [method: "GET" | "POST", path: string])`.  
15. Use `as const` on a config object and explain resulting type.

---

## 11) Practice Solutions

### 1
```ts
type Status = "idle" | "loading" | "done" | "error";
```

### 2
```ts
type User = {
  id: string;
  email?: string;
  readonly createdAt: Date;
};
```

### 3
```ts
function sum(numbers: readonly number[]): number {
  return numbers.reduce((a, b) => a + b, 0);
}
```

### 4
```ts
type Point = [x: number, y: number, label?: string];
```

### 5
```ts
function toArray<T>(value: T): T[] {
  return [value];
}
```

### 6
```ts
function getProp<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}
```

### 7
```ts
type ApiResult<T> =
  | { kind: "ok"; data: T }
  | { kind: "error"; message: string };
```

### 8
```ts
type UserPatch = Partial<User>;
```

### 9
```ts
type Role = "admin" | "user";
type PermissionLevel = Record<Role, number>;
```

### 10
```ts
class BankAccount {
  private _balance = 0;

  get balance() {
    return this._balance;
  }

  set balance(value: number) {
    if (value < 0) throw new Error("Balance cannot be negative");
    this._balance = value;
  }
}
```

### 11
```ts
function parse(input: unknown): number {
  if (typeof input === "number") return input;
  if (typeof input === "string") return Number(input);
  return NaN;
}
```

### 12
```ts
let value: any = "hello";
value.toFixed(); // runtime crash likely, TS doesn't stop you
```

### 13
```ts
type HasId = { id: string };
type Timestamped = { createdAt: Date; updatedAt: Date };
type Entity = HasId & Timestamped;
```

### 14
```ts
function request(...args: [method: "GET" | "POST", path: string]) {
  const [method, path] = args;
  return `${method} ${path}`;
}
```

### 15
```ts
const cfg = {
  mode: "dark",
  retries: 2,
} as const;

// type:
// {
//   readonly mode: "dark";
//   readonly retries: 2;
// }
```

---

## 12) Interview Q&A (Quick Prep)

**Q1: Difference between `unknown` and `any`?**  
`unknown` is safer: you must narrow before use. `any` disables type checks.

**Q2: When to use union vs intersection?**  
Union for alternatives (`A | B`), intersection for combining requirements (`A & B`).

**Q3: Why explicit return types on exported functions?**  
Stabilizes API contracts and prevents accidental inference changes.

**Q4: Interface vs type?**  
Both can model objects; interfaces support declaration merging and are great for OO contracts. Types shine for unions/intersections/advanced composition.

**Q5: Why use `readonly`?**  
Prevents accidental mutation and communicates immutability intent.

**Q6: What is a discriminated union?**  
A union with a common literal tag (`kind`) enabling safe narrowing.

**Q7: Why `K extends keyof T` is useful?**  
Restricts keys to valid object keys and returns precise property type (`T[K]`).

---

## 13) One-Page Cheat Sheet

- Prefer inference for local obvious variables.
- Annotate function params and exported/public return types.
- Prefer `unknown` over `any`.
- Use unions for alternatives; discriminated unions for robust state modeling.
- Use intersections to compose reusable type building blocks.
- Use `readonly` arrays for non-mutating APIs.
- Tuples for fixed structure; labeled tuples for readability.
- Use `keyof` + generics for safe property helpers.
- Core utilities: `Partial`, `Required`, `Readonly`, `Pick`, `Omit`, `Record`.
- Union utilities: `Exclude`, `Extract`, `NonNullable`.
- Async utility: `Awaited<T>`.
- Use assertions (`as`, `!`) sparingly; prefer real runtime checks.
- Enable `"strict": true` in `tsconfig.json`.

---

## Export to PDF

1. Save this file as `typescript-deep-guide.pdf-ready.md`.
2. Open in VS Code / Typora / Obsidian / any Markdown editor.
3. Export or print to PDF.

**Tip:** In VS Code, install “Markdown PDF” extension and run export.

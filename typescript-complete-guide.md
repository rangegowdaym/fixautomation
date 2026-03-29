---
title: "TypeScript Complete Deep Guide"
subtitle: "Core Types → OOP — With Detailed Explanations, Examples, Exercises & Interview Q&A"
author: "Copilot"
date: "2026-03-29"
---

# TypeScript Complete Deep Guide

> **How to export this as PDF:**
> - **VS Code:** Install the "Markdown PDF" extension → Right-click this file → "Markdown PDF: Export (pdf)"
> - **Pandoc:** `pandoc typescript-complete-guide.md -o typescript-guide.pdf --highlight-style=tango`
> - **Online:** Paste at [markdowntopdf.com](https://markdowntopdf.com)
> - **Typora / Obsidian:** File → Export → PDF

---

## Table of Contents

1. [Core Types & Inference](#1-core-types--inference)
2. [Unions, Intersections & Composition](#2-unions-intersections--composition)
3. [Arrays & Tuples](#3-arrays--tuples)
4. [Functions & Call Signatures](#4-functions--call-signatures)
5. [Objects, Interfaces & Type Aliases](#5-objects-interfaces--type-aliases)
6. [Narrowing & Control-Flow Analysis](#6-narrowing--control-flow-analysis)
7. [Generics 101 → Practical](#7-generics-101--practical)
8. [Built-in Utility Types (Essentials)](#8-built-in-utility-types-essentials)
9. [Classes & OOP in TypeScript](#9-classes--oop-in-typescript)
10. [Practice Exercises + Solutions](#10-practice-exercises--solutions)
11. [Interview Q&A (Quick Prep)](#11-interview-qa-quick-prep)
12. [One-Page Cheat Sheet](#12-one-page-cheat-sheet)

---

## 1) Core Types & Inference

### What is type inference?

TypeScript's compiler automatically deduces the type of a variable or expression
from context — you don't always need to write `: string` or `: number` manually.
This is called **type inference** and it is one of TypeScript's most useful
features: you get safety without extra noise.

**Rule of thumb:**
- Let TypeScript infer types when the value is obvious from context.
- Annotate explicitly when the type is part of a contract, boundary, or API.

```ts
// Inference — TS knows this is a number
let score = 100;

// Inference — TS knows this is a string
let city = "London";

// Explicit — empty array; TS cannot infer element type
const ids: number[] = [];

// Explicit — function boundary / public API
function getTotal(price: number, tax: number): number {
  return price + tax;
}
```

> **Why this matters:** Relying on inference for local variables reduces noise
> and makes refactoring easier. Explicit annotations on public functions act as
> documentation and catch mistakes early.

---

### 1.1 Primitive types

TypeScript mirrors JavaScript's primitives but adds static types on top.

| Type      | Description                               | Example            |
|-----------|-------------------------------------------|--------------------|
| `string`  | Text data                                 | `"hello"`          |
| `number`  | All numbers (int + float)                 | `42`, `3.14`       |
| `boolean` | true / false                              | `true`             |
| `bigint`  | Integers larger than Number.MAX_SAFE_INTEGER | `9007199254740992n` |
| `symbol`  | Unique, immutable identifier              | `Symbol("id")`     |

```ts
let username: string  = "alice";
let age: number       = 30;
let active: boolean   = true;
let bigNum: bigint    = 1234567890123456789n;
let uid: symbol       = Symbol("uid");
```

> **Note on `bigint`:** Requires `target: ES2020` or higher in `tsconfig.json`.
> Regular `number` can only safely represent integers up to `2^53 - 1`.

---

### 1.2 Special types in depth

#### `null` and `undefined`

In strict mode (`"strictNullChecks": true`), `null` and `undefined` are their
own distinct types and cannot be assigned to `string`, `number`, etc. without
explicit union.

```ts
let a: null      = null;
let b: undefined = undefined;

// With strict mode — this is an error:
// let name: string = null; ❌

// Correct:
let name: string | null = null; // ✅
```

---

#### `void`

`void` is the return type for functions that do not return a meaningful value.
It's different from `undefined` in subtle ways — a `void`-typed variable can
accept `undefined` but signals "this return value should be ignored."

```ts
function logMessage(msg: string): void {
  console.log(msg);
  // no return statement needed
}
```

---

#### `never`

`never` means a function can NEVER successfully return. This applies to:
- Functions that always throw.
- Functions with infinite loops.
- Exhaustive checks in switch statements.

```ts
function crash(msg: string): never {
  throw new Error(msg);
}

function infiniteLoop(): never {
  while (true) {}
}

// Exhaustive check with never:
type Direction = "north" | "south" | "east" | "west";

function move(d: Direction) {
  switch (d) {
    case "north": return "Going north";
    case "south": return "Going south";
    case "east":  return "Going east";
    case "west":  return "Going west";
    default:
      const _exhaustive: never = d; // error if Direction grows but switch didn't
      return _exhaustive;
  }
}
```

> **Pro tip:** Using `never` in exhaustive checks means TypeScript will
> alert you immediately when you add a new union member but forget to handle it.

---

#### `unknown` — the safe top type

`unknown` accepts any value (like `any`) but forces you to narrow before use.
It represents "I don't know the type yet, but I will check before using it."

```ts
function handleInput(value: unknown) {
  // value.toUpperCase(); // ❌ cannot use without checking

  if (typeof value === "string") {
    console.log(value.toUpperCase()); // ✅ safe
  } else if (typeof value === "number") {
    console.log(value.toFixed(2));    // ✅ safe
  }
}
```

---

#### `any` — why it is risky

`any` completely opts out of TypeScript's type system. This means:
- No autocomplete.
- No error detection.
- Silent runtime failures.

```ts
let data: any = "hello";
data.nonExistentMethod().x.y.z; // TS is silent — runtime crash guaranteed

// Compare with unknown:
let safe: unknown = "hello";
// safe.nonExistentMethod(); // ❌ TS prevents this immediately
```

> **When is `any` acceptable?** Gradually migrating a JS codebase to TS, or
> as a last resort with third-party libraries lacking type definitions.
> Always prefer `unknown` over `any` for safety.

---

### 1.3 Object types: required, optional, readonly

```ts
type Config = {
  host: string;            // required — must be present
  port?: number;           // optional — may be absent (port | undefined)
  readonly apiKey: string; // readonly — can be set once, never reassigned
};

const cfg: Config = {
  host: "localhost",
  apiKey: "secret-key",
  // port omitted — OK because it's optional
};

cfg.host = "prod-server"; // ✅ allowed
// cfg.apiKey = "new-key"; // ❌ error: cannot assign to readonly property
```

> **Important distinction:** `readonly` is a compile-time check only. At
> runtime (JavaScript), the property can still technically be reassigned.
> For deep immutability, use `Object.freeze()`.

---

### 1.4 Literal types and widening explained

**Widening** happens when TypeScript broadens a type to its base type
(e.g., `"dark"` widens to `string`).

```ts
// let — widened to base type
let theme = "dark";      // type: string

// const — literal preserved (can't change anyway)
const mode = "dark";     // type: "dark"
```

**Why literal types matter:**

```ts
type Theme = "light" | "dark" | "system";

function applyTheme(t: Theme) { /* ... */ }

let t = "dark";        // type: string
// applyTheme(t);      // ❌ string is not assignable to Theme

const t2 = "dark";     // type: "dark"
applyTheme(t2);        // ✅ "dark" satisfies Theme
```

**Forcing a literal type on a `let`:**
```ts
let direction = "north" as const; // type: "north"
let speed = 60 as const;          // type: 60
```

---

### 1.5 Type assertions (`as`) and non-null assertion (`!`)

#### `as` — type assertion

Use when you have more information about a type than TypeScript can infer.
The compiler trusts you without a runtime check.

```ts
// DOM API returns HTMLElement | null
const canvas = document.getElementById("canvas") as HTMLCanvasElement;
canvas.getContext("2d"); // ✅ now TS knows it's a canvas
```

**Double assertion (escape hatch — use with extreme caution):**
```ts
const x = (value as unknown) as SpecificType;
```

#### `!` — non-null assertion

Tells TS "this value is definitely not null or undefined."

```ts
const form = document.getElementById("loginForm")!; // cannot be null
form.addEventListener("submit", handler);
```

**Safe alternative — prefer this in production code:**
```ts
const form = document.getElementById("loginForm");
if (!form) throw new Error("loginForm element not found in DOM");
form.addEventListener("submit", handler);
```

> **Rule:** Use `as` and `!` only at true integration boundaries (DOM, external
> APIs). Never use them to silence TS errors inside business logic.

---

### 1.6 `as const` for exact immutable values

`as const` does two things at once:
1. Prevents widening (keeps exact literal types).
2. Makes all properties/elements `readonly`.

```ts
const direction = ["north", "south", "east", "west"] as const;
// type: readonly ["north", "south", "east", "west"]
// direction[0] is "north" — not string

const settings = {
  theme: "dark",
  fontSize: 16,
  lang: "en",
} as const;
// type: { readonly theme: "dark"; readonly fontSize: 16; readonly lang: "en" }
```

**Real-world use — action type constants:**
```ts
const Actions = {
  LOGIN:  "auth/login",
  LOGOUT: "auth/logout",
} as const;

type ActionType = typeof Actions[keyof typeof Actions];
// "auth/login" | "auth/logout"
```

---

## 2) Unions, Intersections & Composition

### 2.1 Unions (`A | B`) — "one OR the other"

A union type says "this value can be any ONE of these types."

```ts
type StringOrNumber = string | number;

function printId(id: StringOrNumber) {
  console.log(`ID: ${id}`);
}

printId(101);      // ✅
printId("ABC-7");  // ✅
```

#### Unions with object types

```ts
type Circle    = { kind: "circle";    radius: number };
type Rectangle = { kind: "rectangle"; width: number; height: number };
type Triangle  = { kind: "triangle";  base: number; height: number };

type Shape = Circle | Rectangle | Triangle;
```

#### Discriminated union pattern (most important union pattern)

A shared literal property (`kind`, `type`, `tag`) is the "discriminant."
TypeScript uses it to narrow down the exact member.

```ts
function area(shape: Shape): number {
  switch (shape.kind) {
    case "circle":
      return Math.PI * shape.radius ** 2;
    case "rectangle":
      return shape.width * shape.height;
    case "triangle":
      return 0.5 * shape.base * shape.height;
  }
}
```

> **Why discriminated unions are powerful:** They model real-world state
> machines (loading/success/error), Redux actions, API responses, and more.
> Each branch is fully type-safe without any casting.

---

### 2.2 Intersections (`A & B`) — "BOTH at once"

An intersection type says "this value must satisfy ALL of these types."

```ts
type HasId = { id: string };
type HasName = { name: string };
type HasAudit = { createdAt: Date; updatedAt: Date };

type User = HasId & HasName & HasAudit;
// Must have: id, name, createdAt, updatedAt
```

**Intersection of interfaces:**
```ts
interface Serializable {
  serialize(): string;
}
interface Loggable {
  log(): void;
}

type LoggableEntity = Serializable & Loggable;
// must implement both serialize() and log()
```

---

### 2.3 Composition keeps types DRY

Instead of repeating properties, build small atomic types and compose them.

```ts
// Atomic building blocks
type Paginatable = {
  page: number;
  pageSize: number;
  total: number;
};

type Sortable = {
  sortBy?: string;
  sortDir?: "asc" | "desc";
};

type Filterable<T> = {
  filters?: Partial<T>;
};

// Compose for a specific feature
type UserListQuery = Paginatable & Sortable & Filterable<{ role: string; active: boolean }>;
```

> **Benefit:** When you change `Paginatable`, every query type that
> composes it is automatically updated — no copy-paste drift.

---

## 3) Arrays & Tuples

### 3.1 `T[]` vs `Array<T>` — both are identical

These are two syntax styles for the same type:

```ts
let numbers: number[]       = [1, 2, 3];
let strings: Array<string>  = ["a", "b", "c"];
```

Pick one style and stay consistent across your codebase.
Most style guides prefer `T[]` for simplicity, but `Array<T>` reads
better when `T` is itself complex:

```ts
// Easier to read than (string | number)[]
let mixed: Array<string | number> = [1, "two", 3];
```

---

### 3.2 Readonly arrays — preventing accidental mutation

```ts
const primes: readonly number[] = [2, 3, 5, 7, 11];

// primes.push(13);         // ❌ error
// primes[0] = 1;           // ❌ error
// primes.reverse();        // ❌ error
```

**Why use readonly arrays in function parameters?**

It communicates to the caller: "I will not mutate your array."
It prevents accidental bugs inside the function.

```ts
function sum(values: readonly number[]): number {
  return values.reduce((acc, val) => acc + val, 0);
}

function top3(scores: readonly number[]): number[] {
  return [...scores].sort((a, b) => b - a).slice(0, 3);
  // spread first, then sort — original untouched
}
```

**`ReadonlyArray<T>` is equivalent:**
```ts
function process(items: ReadonlyArray<string>) { /* ... */ }
```

---

### 3.3 Tuples — arrays with fixed structure

A tuple is an array where:
- The length is fixed.
- Each position has a specific type.

```ts
// Position 0 must be number, position 1 must be string
let entry: [number, string] = [1, "Alice"];

// Destructuring
const [id, name] = entry;
// id: number, name: string
```

**Optional tuple elements:**
```ts
type Coordinate = [lat: number, lng: number, elevation?: number];

const a: Coordinate = [51.5, -0.12];          // ✅ elevation optional
const b: Coordinate = [51.5, -0.12, 34.5];   // ✅ with elevation
```

**Labeled tuples (TypeScript 4.0+):**
Labels don't change the type but massively improve editor hints.

```ts
type HttpResponse = [
  statusCode: number,
  body:       string,
  headers:    Record<string, string>,
  error?:     string
];
```

**Tuples in function return (returning multiple values safely):**
```ts
function useState<T>(initial: T): [T, (value: T) => void] {
  let state = initial;
  return [state, (v) => { state = v; }];
}
```

---

## 4) Functions & Call Signatures

### 4.1 Parameter and return type annotations

```ts
// Named function
function multiply(a: number, b: number): number {
  return a * b;
}

// Arrow function
const divide = (a: number, b: number): number => a / b;

// Function type alias
type MathOperation = (a: number, b: number) => number;

const add: MathOperation = (a, b) => a + b; // params inferred from type
```

---

### 4.2 Optional parameters

An optional parameter is `T | undefined` inside the function.

```ts
function greet(name: string, title?: string): string {
  // title is string | undefined here
  if (title) {
    return `${title} ${name}`;
  }
  return name;
}

greet("Alice");            // "Alice"
greet("Alice", "Dr.");     // "Dr. Alice"
```

> **Important:** Optional parameters must come AFTER required ones.

---

### 4.3 Default parameters

Default values set a fallback. The parameter type is inferred from the default.

```ts
function paginate(items: string[], page = 1, size = 10) {
  const start = (page - 1) * size;
  return items.slice(start, start + size);
}

paginate(items);          // page=1, size=10
paginate(items, 2);       // page=2, size=10
paginate(items, 2, 20);   // page=2, size=20
```

**Default can be an expression:**
```ts
function log(msg: string, timestamp = new Date().toISOString()) {
  console.log(`[${timestamp}] ${msg}`);
}
```

---

### 4.4 Rest parameters with tuples

Standard rest parameters collect all extra arguments:
```ts
function sum(...nums: number[]): number {
  return nums.reduce((a, b) => a + b, 0);
}
```

**Typed rest with tuple — enforces order and types:**
```ts
type FetchArgs = [url: string, method?: "GET" | "POST", body?: unknown];

function fetchData(...args: FetchArgs) {
  const [url, method = "GET", body] = args;
  console.log(url, method, body);
}

fetchData("https://api.example.com/users");
fetchData("https://api.example.com/users", "POST", { name: "Alice" });
```

---

### 4.5 Return type inference vs explicit annotation

**Inference is fine for local/private utilities:**
```ts
const double = (n: number) => n * 2;
// inferred as (n: number) => number
```

**Explicit return type is better for exported/public functions:**
```ts
export function createUser(name: string, role: "admin" | "user"): User {
  return { id: crypto.randomUUID(), name, role, createdAt: new Date() };
}
```

Why explicit is valuable:
1. Documents intent clearly.
2. Catches bugs when return paths accidentally return wrong type.
3. Prevents "return type drift" on refactors.

---

### 4.6 Function overloads

Define multiple callable signatures for the same function:

```ts
function format(value: string): string;
function format(value: number, decimals: number): string;
function format(value: string | number, decimals?: number): string {
  if (typeof value === "string") return value.trim();
  return value.toFixed(decimals ?? 2);
}

format("  hello  ");   // string overload
format(3.14159, 2);    // number overload
```

---

## 5) Objects, Interfaces & Type Aliases

### 5.1 Interfaces — the contract for object shapes

Interfaces define the shape an object must have.

```ts
interface Address {
  street: string;
  city: string;
  country: string;
  postcode?: string;
}

function sendMail(address: Address) {
  console.log(`Sending to ${address.city}, ${address.country}`);
}
```

---

### 5.2 Interface extending — single and multiple

```ts
interface Animal {
  name: string;
  age: number;
}

interface Pet extends Animal {
  owner: string;
}

interface ServiceAnimal extends Pet {
  trained: boolean;
  handler: string;
}

const guide: ServiceAnimal = {
  name: "Rex",
  age: 3,
  owner: "Alice",
  trained: true,
  handler: "Bob",
};
```

**Multiple extends:**
```ts
interface Flyable  { fly(): void }
interface Swimmable { swim(): void }

interface Duck extends Flyable, Swimmable {
  quack(): void;
}
```

---

### 5.3 Type aliases — beyond simple names

Type aliases can express things interfaces cannot:

```ts
// Union (impossible with interface alone)
type StringOrNumber = string | number;

// Intersection
type AdminUser = User & { adminSince: Date };

// Tuple
type Point3D = [x: number, y: number, z: number];

// Mapped type
type Optional<T> = { [K in keyof T]?: T[K] };

// Conditional type
type IsString<T> = T extends string ? true : false;
```

---

### 5.4 Interface vs type — detailed comparison

| Feature                   | `interface`  | `type`       |
|---------------------------|:------------|:-------------|
| Object shape              | ✅           | ✅            |
| Union types               | ❌           | ✅            |
| Intersection              | via `extends`| ✅ (`&`)      |
| Tuples                    | ❌           | ✅            |
| Declaration merging       | ✅           | ❌            |
| Extends / implements      | ✅           | ✅ (partial)  |
| Mapped/conditional types  | ❌           | ✅            |

**Declaration merging (interface-only feature):**
When you declare the same interface name twice, TypeScript merges them.

```ts
// lib.d.ts adds to Window interface
interface Window {
  myPlugin: () => void;
}

// Your code adds to it again
interface Window {
  analyticsId: string;
}

// Result: Window has both myPlugin and analyticsId
```

---

### 5.5 Index signatures — dynamic keys

Use when you don't know the keys in advance:

```ts
type TranslationMap = {
  [key: string]: string;
};

const en: TranslationMap = {
  hello: "Hello",
  goodbye: "Goodbye",
  unknown_key: "any value", // all fine
};
```

**Limitation — all string keys must match value type:**
```ts
type Invalid = {
  [key: string]: string;
  count: number; // ❌ error: 'number' is not compatible with 'string'
};
```

**Safer pattern with `Record` for known keys:**
```ts
type Locale = "en" | "fr" | "de" | "ja";
type Translations = Record<Locale, Record<string, string>>;
```

---

## 6) Narrowing & Control-Flow Analysis

### What is narrowing?

TypeScript's control-flow analysis tracks the type of a variable
at each point in code. After a check, the type "narrows" to a
more specific one in that branch.

---

### 6.1 `typeof` narrowing

```ts
function formatValue(v: string | number | boolean) {
  if (typeof v === "string") {
    // v is string here
    return v.toUpperCase();
  }
  if (typeof v === "number") {
    // v is number here
    return v.toFixed(2);
  }
  // v is boolean here
  return v ? "Yes" : "No";
}
```

---

### 6.2 `instanceof` narrowing

```ts
class NetworkError extends Error {
  constructor(public statusCode: number, message: string) {
    super(message);
  }
}

class ValidationError extends Error {
  constructor(public fields: string[], message: string) {
    super(message);
  }
}

function handleError(err: NetworkError | ValidationError) {
  if (err instanceof NetworkError) {
    console.error(`HTTP ${err.statusCode}: ${err.message}`);
  } else {
    console.error(`Invalid fields: ${err.fields.join(", ")}`);
  }
}
```

---

### 6.3 `in` operator narrowing

```ts
interface AdminUser {
  role: "admin";
  deleteUser(id: string): void;
}

interface RegularUser {
  role: "user";
  profile: { bio: string };
}

function renderDashboard(user: AdminUser | RegularUser) {
  if ("deleteUser" in user) {
    // user is AdminUser
    console.log("Admin panel");
    user.deleteUser("123");
  } else {
    // user is RegularUser
    console.log(user.profile.bio);
  }
}
```

---

### 6.4 Discriminated union narrowing

```ts
type LoadingState = { status: "loading" };
type SuccessState<T> = { status: "success"; data: T };
type ErrorState = { status: "error"; code: number; message: string };

type AsyncState<T> = LoadingState | SuccessState<T> | ErrorState;

function renderState<T>(state: AsyncState<T>) {
  switch (state.status) {
    case "loading":
      return "Loading...";
    case "success":
      return `Data loaded: ${JSON.stringify(state.data)}`;
    case "error":
      return `Error ${state.code}: ${state.message}`;
  }
}
```

---

### 6.5 Optional chaining (`?.`) and nullish coalescing (`??`)

#### Optional chaining — short-circuit on null/undefined

```ts
type Company = {
  name: string;
  address?: {
    city?: string | null;
    postcode?: string;
  };
};

function getCity(company: Company): string {
  // Without optional chaining (verbose):
  // const city = company.address && company.address.city
  //   ? company.address.city : "Unknown";

  // With optional chaining (clean):
  return company.address?.city ?? "Unknown";
}
```

#### Nullish coalescing vs OR

```ts
const userInput = "";

// || falls back on any falsy value (0, "", false, null, undefined)
const a = userInput || "default";  // "default" — even for empty string!

// ?? falls back ONLY on null/undefined
const b = userInput ?? "default";  // "" — respects intentional empty string
```

> **Rule:** Prefer `??` over `||` for default values unless you explicitly
> want to treat `0`, `""`, and `false` as "no value."

---

### 6.6 Type predicates (custom type guards)

Write your own narrowing functions:

```ts
interface Dog { bark(): void }
interface Cat { meow(): void }

function isDog(pet: Dog | Cat): pet is Dog {
  return (pet as Dog).bark !== undefined;
}

function interact(pet: Dog | Cat) {
  if (isDog(pet)) {
    pet.bark(); // TS knows: Dog here
  } else {
    pet.meow(); // TS knows: Cat here
  }
}
```

---

## 7) Generics 101 → Practical

### Why generics?

Without generics, you either:
- Repeat yourself (one function per type), or
- Use `any` and lose all safety.

Generics let you write **one function/type that works for multiple types**
while keeping full type information.

---

### 7.1 Basic generic function

```ts
function identity<T>(value: T): T {
  return value;
}

const a = identity("hello"); // T = string
const b = identity(42);      // T = number
const c = identity({ x: 1 }); // T = { x: number }
```

---

### 7.2 Generic with constraints

Use `extends` to restrict what types `T` can be:

```ts
function getLength<T extends { length: number }>(value: T): number {
  return value.length;
}

getLength("hello");    // ✅ string has .length
getLength([1, 2, 3]);  // ✅ array has .length
// getLength(42);      // ❌ number has no .length
```

---

### 7.3 Multiple type parameters

```ts
function pair<A, B>(first: A, second: B): [A, B] {
  return [first, second];
}

const p = pair("age", 30); // [string, number]
```

---

### 7.4 Generic defaults

```ts
type ApiResponse<T = unknown> = {
  data: T;
  error: string | null;
  status: number;
};

// With default
const r1: ApiResponse = { data: null, error: null, status: 200 };

// Overriding default
const r2: ApiResponse<{ name: string }> = {
  data: { name: "Alice" },
  error: null,
  status: 200,
};
```

---

### 7.5 `keyof` and indexed access types

`keyof T` gives the union of all keys of type `T`:

```ts
type User = { id: number; name: string; email: string };
type UserKeys = keyof User; // "id" | "name" | "email"
```

**Indexed access `T[K]`** gets the type of a property:
```ts
type IdType = User["id"];    // number
type NameType = User["name"]; // string
```

**`getProp` — safe property reader:**
```ts
function getProp<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}

const user: User = { id: 1, name: "Alice", email: "a@b.com" };

const name  = getProp(user, "name");   // string
const id    = getProp(user, "id");     // number
// getProp(user, "role");              // ❌ compile error — "role" not in User
```

---

### 7.6 Generic classes

```ts
class Stack<T> {
  private items: T[] = [];

  push(item: T): void {
    this.items.push(item);
  }

  pop(): T | undefined {
    return this.items.pop();
  }

  peek(): T | undefined {
    return this.items[this.items.length - 1];
  }

  get size(): number {
    return this.items.length;
  }
}

const numStack = new Stack<number>();
numStack.push(1);
numStack.push(2);
console.log(numStack.pop()); // 2

const strStack = new Stack<string>();
strStack.push("hello");
```

---

## 8) Built-in Utility Types (Essentials)

Given this base type:

```ts
type User = {
  id: number;
  name: string;
  email: string;
  role: "admin" | "user";
  bio?: string;
};
```

---

### 8.1 `Partial<T>` — all properties optional

Use when you want to update only some properties (PATCH endpoint).

```ts
type UserUpdate = Partial<User>;
// All fields become optional

function updateUser(id: number, changes: Partial<User>) {
  // changes can have any subset of User's properties
}

updateUser(1, { name: "Bob" });       // ✅
updateUser(1, { email: "b@c.com" });  // ✅
```

---

### 8.2 `Required<T>` — all properties required

```ts
type FullUser = Required<User>;
// bio? becomes bio — required
```

---

### 8.3 `Readonly<T>` — all properties immutable

```ts
type FrozenUser = Readonly<User>;
const u: FrozenUser = { id: 1, name: "Alice", email: "a@b.com", role: "user" };
// u.name = "Bob"; // ❌ error
```

---

### 8.4 `Pick<T, K>` — select specific properties

```ts
type UserCard = Pick<User, "id" | "name">;
// { id: number; name: string }
```

Useful for exposing only safe/needed data.

---

### 8.5 `Omit<T, K>` — remove specific properties

```ts
type PublicUser = Omit<User, "email" | "role">;
// { id: number; name: string; bio?: string }
```

---

### 8.6 `Record<K, V>` — map keys to value type

```ts
type RolePermissions = Record<"admin" | "user" | "guest", string[]>;

const perms: RolePermissions = {
  admin: ["create", "read", "update", "delete"],
  user:  ["read", "update"],
  guest: ["read"],
};
```

---

### 8.7 `Exclude<T, U>` — remove from union

```ts
type Status  = "active" | "inactive" | "banned" | "pending";
type Active  = Exclude<Status, "inactive" | "banned">;
// "active" | "pending"
```

---

### 8.8 `Extract<T, U>` — keep only matching union members

```ts
type Primitives  = string | number | boolean | object | null;
type Truthy      = Extract<Primitives, string | number | boolean>;
// string | number | boolean
```

---

### 8.9 `NonNullable<T>` — remove null and undefined

```ts
type MaybeString = string | null | undefined;
type DefiniteString = NonNullable<MaybeString>; // string
```

---

### 8.10 `Awaited<T>` — unwrap Promise chains

```ts
type A = Awaited<Promise<string>>;             // string
type B = Awaited<Promise<Promise<number>>>;    // number
type C = Awaited<string>;                      // string (unchanged)
```

Useful for typing async utility functions:
```ts
async function fetchUser(): Promise<User> { /* ... */ }
type FetchedUser = Awaited<ReturnType<typeof fetchUser>>; // User
```

---

### 8.11 `ReturnType<T>` and `Parameters<T>`

```ts
function createOrder(userId: string, total: number, items: string[]) {
  return { id: "ord_1", userId, total, items };
}

type OrderResult = ReturnType<typeof createOrder>;
// { id: string; userId: string; total: number; items: string[] }

type CreateOrderParams = Parameters<typeof createOrder>;
// [userId: string, total: number, items: string[]]
```

---

### Common pitfalls

| Pitfall                        | Issue                                         | Fix                                       |
|-------------------------------|-----------------------------------------------|-------------------------------------------|
| `Partial` is shallow           | Nested objects not made optional              | Use `DeepPartial<T>` custom recursive type|
| `Readonly` is shallow          | Nested objects can still be mutated           | Use `DeepReadonly<T>` or `Object.freeze()`|
| `Omit` with unions             | May behave unexpectedly with union types      | Use `DistributiveOmit<T, K>` helper       |

---

## 9) Classes & OOP in TypeScript

### 9.1 Class fields and field initialization

```ts
class Animal {
  name: string;
  sound: string;

  constructor(name: string, sound: string) {
    this.name = name;
    this.sound = sound;
  }

  speak() {
    return `${this.name} says ${this.sound}`;
  }
}
```

---

### 9.2 Parameter properties (shorthand)

Declare + initialize fields directly in constructor:

```ts
class Animal {
  constructor(
    public name: string,
    private sound: string,
    protected age: number,
    public readonly species: string
  ) {}

  speak() {
    return `${this.name} says ${this.sound}`;
  }
}

const dog = new Animal("Rex", "Woof", 3, "Canis lupus");
dog.name;        // ✅ public
// dog.sound;    // ❌ private
dog.species;     // �� readonly
// dog.species = "other"; // ❌ readonly
```

---

### 9.3 Access modifiers in depth

| Modifier    | Class body | Subclass | Outside |
|-------------|:----------:|:--------:|:-------:|
| `public`    | ✅          | ✅        | ✅       |
| `protected` | ✅          | ✅        | ❌       |
| `private`   | ✅          | ❌        | ❌       |
| `#` (native)| ✅          | ❌        | ❌ (runtime enforced) |

```ts
class Person {
  public name: string;
  protected age: number;
  private _ssn: string;

  constructor(name: string, age: number, ssn: string) {
    this.name = name;
    this.age = age;
    this._ssn = ssn;
  }
}

class Employee extends Person {
  constructor(name: string, age: number, ssn: string, public company: string) {
    super(name, age, ssn);
  }

  describe() {
    return `${this.name}, age ${this.age} at ${this.company}`;
    // this._ssn; // ❌ private — not accessible in subclass
  }
}
```

---

### 9.4 `readonly` class fields

```ts
class Config {
  readonly version = "1.0.0";
  readonly createdAt: Date;

  constructor() {
    this.createdAt = new Date(); // OK to assign in constructor
  }

  update() {
    // this.version = "2.0.0"; // ❌ error
  }
}
```

---

### 9.5 Abstract classes

Abstract classes define structure but leave implementation to subclasses.
You cannot instantiate them directly.

```ts
abstract class Shape {
  abstract area(): number;
  abstract perimeter(): number;

  describe(): string {
    return `Area: ${this.area().toFixed(2)}, Perimeter: ${this.perimeter().toFixed(2)}`;
  }
}

class Circle extends Shape {
  constructor(public radius: number) { super(); }

  area()      { return Math.PI * this.radius ** 2; }
  perimeter() { return 2 * Math.PI * this.radius; }
}

class Rectangle extends Shape {
  constructor(public width: number, public height: number) { super(); }

  area()      { return this.width * this.height; }
  perimeter() { return 2 * (this.width + this.height); }
}

// const s = new Shape(); // ❌ error: cannot instantiate abstract class

const c = new Circle(5);
console.log(c.describe()); // Area: 78.54, Perimeter: 31.42
```

---

### 9.6 Implementing interfaces

A class can implement one or more interfaces.

```ts
interface Serializable {
  serialize(): string;
}

interface Validatable {
  validate(): boolean;
}

class UserModel implements Serializable, Validatable {
  constructor(
    public id: string,
    public name: string,
    public email: string
  ) {}

  serialize(): string {
    return JSON.stringify({ id: this.id, name: this.name, email: this.email });
  }

  validate(): boolean {
    return this.name.length > 0 && this.email.includes("@");
  }
}
```

---

### 9.7 Getters and setters for encapsulation

```ts
class BankAccount {
  private _balance: number = 0;
  private _transactions: { type: "credit" | "debit"; amount: number }[] = [];

  get balance(): number {
    return this._balance;
  }

  set balance(amount: number) {
    if (amount < 0) throw new Error("Balance cannot be negative");
    this._balance = amount;
  }

  deposit(amount: number) {
    if (amount <= 0) throw new Error("Deposit must be positive");
    this._balance += amount;
    this._transactions.push({ type: "credit", amount });
  }

  withdraw(amount: number) {
    if (amount > this._balance) throw new Error("Insufficient funds");
    this._balance -= amount;
    this._transactions.push({ type: "debit", amount });
  }

  get transactionHistory() {
    return [...this._transactions]; // return copy
  }
}

const acct = new BankAccount();
acct.deposit(1000);
acct.withdraw(200);
console.log(acct.balance); // 800
```

---

### 9.8 Full OOP example — putting it together

```ts
interface Logger {
  log(level: "info" | "warn" | "error", message: string): void;
}

abstract class BaseRepository<T extends { id: string }> {
  protected items: T[] = [];

  abstract findById(id: string): T | undefined;

  findAll(): readonly T[] {
    return this.items;
  }

  count(): number {
    return this.items.length;
  }
}

class UserRepository
  extends BaseRepository<UserModel>
  implements Logger {

  findById(id: string): UserModel | undefined {
    return this.items.find(u => u.id === id);
  }

  add(user: UserModel): void {
    if (!user.validate()) throw new Error("Invalid user");
    this.items.push(user);
    this.log("info", `User ${user.name} added`);
  }

  log(level: "info" | "warn" | "error", message: string): void {
    console[level](`[UserRepository] ${message}`);
  }
}
```

---

## 10) Practice Exercises + Solutions

### Exercises

**Section 1 — Core Types**
1. Declare all five primitive types with valid values.
2. Write a function `parse(input: unknown): number` using `typeof` narrowing.
3. Create a `Config` type with required `host`, optional `port`, readonly `apiKey`.
4. Use `as const` on a permissions array and derive its element type.

**Section 2 — Unions & Intersections**
5. Create a discriminated union `PaymentMethod` with `"card"`, `"paypal"`, `"crypto"` variants.
6. Write `processPayment(method: PaymentMethod): string` using a switch.
7. Build an `AuditedEntity` intersection from `HasId`, `HasName`, `HasTimestamps`.

**Section 3 — Arrays & Tuples**
8. Write `first<T>(arr: readonly T[]): T | undefined`.
9. Create a labeled tuple type `RGB = [r: number, g: number, b: number]`.

**Section 4 — Functions**
10. Write a function accepting optional `timeout` defaulting to `5000`.
11. Build an overloaded `stringify` for `string` and `number`.

**Section 5 — Generics**
12. Build `getProp<T, K extends keyof T>(obj: T, key: K): T[K]`.
13. Write `filterBy<T>(arr: T[], predicate: (item: T) => boolean): T[]`.

**Section 6 — Utilities**
14. From `User`, create: `UserPreview`, `NewUser`, `UserPatch` using utility types.
15. Create a `Record<"get" | "post" | "put" | "delete", (url: string) => void>`.

---

### Solutions

```ts
// 1 — Primitive types
let str: string = "typescript";
let num: number = 3.14;
let bool: boolean = false;
let big: bigint = 9999999999999n;
let sym: symbol = Symbol("sym");

// 2 — unknown narrowing
function parse(input: unknown): number {
  if (typeof input === "number") return input;
  if (typeof input === "string") {
    const n = Number(input);
    if (!Number.isNaN(n)) return n;
  }
  return NaN;
}

// 3 — Config type
type Config = {
  host: string;
  port?: number;
  readonly apiKey: string;
};

// 4 — as const + element type
const PERMISSIONS = ["read", "write", "delete"] as const;
type Permission = typeof PERMISSIONS[number]; // "read" | "write" | "delete"

// 5 — Discriminated union
type CardPayment   = { method: "card";   cardNumber: string; expiry: string };
type PaypalPayment = { method: "paypal"; email: string };
type CryptoPayment = { method: "crypto"; wallet: string; coin: string };
type PaymentMethod = CardPayment | PaypalPayment | CryptoPayment;

// 6 — processPayment
function processPayment(p: PaymentMethod): string {
  switch (p.method) {
    case "card":    return `Charging card ****${p.cardNumber.slice(-4)}`;
    case "paypal":  return `Paying via PayPal: ${p.email}`;
    case "crypto":  return `Sending ${p.coin} to ${p.wallet}`;
  }
}

// 7 — Intersection
type HasId = { id: string };
type HasName = { name: string };
type HasTimestamps = { createdAt: Date; updatedAt: Date };
type AuditedEntity = HasId & HasName & HasTimestamps;

// 8 — first
function first<T>(arr: readonly T[]): T | undefined {
  return arr[0];
}

// 9 — RGB tuple
type RGB = [r: number, g: number, b: number];

// 10 — default parameter
function fetchWithTimeout(url: string, timeout = 5000) {
  return fetch(url, { signal: AbortSignal.timeout(timeout) });
}

// 11 — Overloads
function stringify(value: string): string;
function stringify(value: number): string;
function stringify(value: string | number): string {
  return String(value);
}

// 12 — getProp
function getProp<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}

// 13 — filterBy
function filterBy<T>(arr: T[], predicate: (item: T) => boolean): T[] {
  return arr.filter(predicate);
}

// 14 — Utility types
type User = { id: number; name: string; email: string; password: string; bio?: string };
type UserPreview = Pick<User, "id" | "name">;
type NewUser     = Omit<User, "id">;
type UserPatch   = Partial<Omit<User, "id">>;

// 15 — Record for HTTP methods
type HttpHandler = Record<"get" | "post" | "put" | "delete", (url: string) => void>;
```

---

## 11) Interview Q&A (Quick Prep)

**Q: What is the difference between `unknown` and `any`?**
> `any` disables all type checking. `unknown` accepts any value but requires
> you to narrow the type before using it. Always prefer `unknown` over `any`
> because it maintains type safety.

**Q: What is a discriminated union and when would you use it?**
> A discriminated union has a shared literal property (the discriminant, e.g.,
> `kind`) that TypeScript uses to narrow which union member is present.
> Use it for state machines, action types (Redux), API responses, and anywhere
> you have mutually exclusive states.

**Q: What is the difference between `interface` and `type`?**
> Both can describe object shapes. `interface` supports declaration merging
> and is preferred for object-oriented contracts. `type` supports unions,
> intersections, tuples, and advanced mapped/conditional types. In practice,
> both work for most scenarios — pick a team standard and stay consistent.

**Q: What does `as const` do?**
> It prevents type widening (keeps exact literal types) and makes all
> properties/elements `readonly`. It's great for constants, config objects,
> and deriving union types from arrays.

**Q: What is the difference between `??` and `||`?**
> `||` falls back on ANY falsy value: `0`, `""`, `false`, `null`, `undefined`.
> `??` falls back ONLY on `null` or `undefined`. Prefer `??` when `0` and
> empty string are valid values.

**Q: Explain `K extends keyof T` and why it matters.**
> `K extends keyof T` restricts a type parameter to be a valid key of `T`.
> The return type `T[K]` gives the exact type for that key. This creates
> fully type-safe property accessors without any casting.

**Q: When should you use `never`?**
> 1. Functions that always throw or loop forever.
> 2. Exhaustive switch/if-else checks to ensure all union cases are handled.
> TS will raise an error if a new union member is added but not handled.

**Q: What are the pitfalls of `Partial<T>`?**
> `Partial<T>` is shallow — it only makes the top-level properties optional.
> Nested object types remain fully required. For deep partial, you need a
> custom recursive utility type.

**Q: What is declaration merging?**
> Only interfaces support this: when you declare the same interface name
> multiple times, TypeScript merges them into one definition. Useful for
> extending library types (e.g., `Window`, `Express.Request`).

**Q: What does `Awaited<T>` do?**
> It recursively unwraps `Promise<T>` chains, giving you the resolved type.
> `Awaited<Promise<Promise<number>>>` gives `number`.

---

## 12) One-Page Cheat Sheet

```
CORE TYPES
──────────────────────────────────────────────────────────────
string | number | boolean | bigint | symbol   — primitives
null | undefined                               — absence
void          — function returns nothing
never         — function never returns
unknown       — safe any (narrow before use)
any           — unsafe escape hatch (avoid)

LITERALS & IMMUTABILITY
──────────────────────────────────────────────────────────────
const x = "open"           — literal "open" (not widened)
let  y = "open"            — string (widened)
{ ... } as const           — exact types + all readonly

OBJECT TYPES
──────────────────────────────────────────────────────────────
prop: T                    — required
prop?: T                   — optional (T | undefined)
readonly prop: T           — immutable
[key: string]: T           — index signature (all keys → T)

UNIONS & INTERSECTIONS
──────────────────────────────────────────────────────────────
A | B      — one OR the other
A & B      — must satisfy BOTH
Discriminated union: shared literal tag for safe narrowing

ARRAYS & TUPLES
──────────────────────────────────────────────────────────────
T[]  =  Array<T>                — mutable array
readonly T[]  =  ReadonlyArray<T>  — immutable array
[T, U, V?]                       — tuple (fixed positions)
[label: T, label2: U]            — labeled tuple

FUNCTIONS
──────────────────────────────────────────────────────────────
(a: T, b: U): R            — annotated
(a: T, b?: U): R           — optional param
(a: T, b = default): R     — default param (R inferred)
(...args: [T, U]): R       — rest with tuple

GENERICS
──────────────────────────────────────────────────────────────
<T>(x: T): T                  — basic
<T extends X>(x: T): T        — with constraint
<T = Default>(x: T): T        — with default
<T, K extends keyof T>        — key constraint
T[K]                          — indexed access

NARROWING GUARDS
──────────────────────────────────────────────────────────────
typeof x === "string"     — primitive check
x instanceof ClassName    — class instance check
"key" in object           — property existence
x.status === "done"       — discriminant check
fn(x): x is T             — type predicate

NULLISH OPERATORS
──────────────────────────────────────────────────────────────
a?.b?.c       — stop if null/undefined
a ?? b        — fallback only for null/undefined
a!            — assert non-null (use sparingly)

UTILITY TYPES (object)
──────────────────────────────────────────────────────────────
Partial<T>      — all optional
Required<T>     — all required
Readonly<T>     — all readonly
Pick<T, K>      — keep keys K
Omit<T, K>      — remove keys K
Record<K, V>    — map K → V

UTILITY TYPES (union)
──────────────────────────────────────────────────────────────
Exclude<T, U>        — remove U from T
Extract<T, U>        — keep only U in T
NonNullable<T>       — remove null | undefined
Awaited<T>           — unwrap Promise<T> recursively
ReturnType<F>        — return type of function F
Parameters<F>        — parameter tuple of function F

OOP QUICK REFERENCE
──────────────────────────────────────────────────────────────
public    — anywhere
protected — class + subclasses
private   — class only
readonly  — once assigned, immutable
abstract  — must be implemented by subclass
implements — class must satisfy interface contract
get / set  — safe encapsulation of private fields
```

---

*End of TypeScript Deep Guide — Happy coding!*
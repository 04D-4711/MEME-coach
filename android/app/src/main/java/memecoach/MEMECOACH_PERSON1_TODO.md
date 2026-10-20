# Meme Coach – work left for person 1

Person 2 (Jiaqian) has finished `MemeCoachManager`, `FeedbackPostFactory` and
`app/test/MemeCoachTests.java`. To keep the project compiling, the classes below
were added as **skeletons only**: every method throws
`UnsupportedOperationException("TODO(person 1) ...")`.
Please replace the bodies and keep the signatures, since the Manager and the
Android UI already call them.

Search the code for `TODO(person 1)` to find every spot.

## 1. Skeletons to implement (already in `app/src/memecoach/`)

| Class | What it must do |
|---|---|
| `FeedbackPersonaFactory.makePersona(String id)` | Return the persona for `"supportive"`, `"penguin"` or `"teacher"`. Throw `IllegalArgumentException` for any other id (including `null`). The Manager turns that exception into `onError("Unknown persona: ...")`; any other exception type will crash the caller. |
| `TemplateFeedbackService(Random)` / `generateFeedback(request, persona)` | Build a `QuizFeedback` from the request and persona: headline from `persona.makeHeadline(request, random)`, explanation from `request.correctExplanation()` (use a generic fallback sentence when it is `""`), and copy `personaId`, `personaName`, `imageKey`, `question`, `selectedAnswer`, `correctAnswer`. Must not return `null`. |
| `DemoExplanationProvider(Map<String, String>)` / `getExplanation(question)` | Copy the map in the constructor. Exact match on question text; return `""` when not found (or when `question` is `null`). |

## 2. New classes still to create

| Class | Notes |
|---|---|
| `SupportiveDisappointedPersona` | implements `FeedbackPersona`; id `supportive`, image key `meme_supportive` |
| `PenguinPersona` | id `penguin`, image key `meme_penguin` |
| `ChillTeacherPersona` | id `teacher`, image key `meme_teacher` |
| `LLMClient` (interface) | `String requestExplanation(String systemPrompt, String userPrompt) throws Exception;` extension point only, no real HTTP client |
| `LLMFeedbackService(LLMClient, FeedbackService fallback)` | Uses the LLM text as the explanation; on exception or blank result, delegates to `fallback` |

Each persona's `makeHeadline` should pick a quote using the `Random` passed in,
so tests can fix the seed.

## 3. How the Manager uses your code

```java
MemeCoachManager manager = new MemeCoachManager(
        new TemplateFeedbackService(new Random()),
        new DemoExplanationProvider(explanations),
        Executors.newSingleThreadExecutor(),        // work thread
        ContextCompat.getMainExecutor(context));    // Android main thread
// This constructor resolves personas with FeedbackPersonaFactory::makePersona.
```

The Manager validates the input and persona **before** recording the answer,
calls `generateFeedback` on the work executor, and catches any
`RuntimeException` from your service and reports it through `onError`.

## 4. Please also

- Write comments in English (Code Quality is marked by English-speaking tutors).
- Add tests for the personas, the factory (including an unknown id) and the
  services (fixed-seed `Random`, LLM failure → fallback).
- `MemeCoachTests` uses its own stub persona and service, so it passes
  without your code; once your classes are in, the app wiring above works as is.

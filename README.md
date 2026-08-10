# SmartMail — AI Reply Console

Spring Boot (WebFlux, reactive) backend + Svelte (Vite) frontend + Gemini API.
The two are fully decoupled: Svelte never talks to Google directly — every
Gemini call goes through the Spring backend, so your API key never reaches
the browser.

```
smartmail-ai/
├── backend/     Spring Boot 3 (Java 17, WebFlux)
└── frontend/    Svelte 4 + Vite
```

---

## 1. What makes this different from a bog-standard CRUD demo

Most "Spring Boot + AI" tutorials do one boring `POST -> full JSON response`.
This project does three things most tutorials skip, which is exactly the part
people will ask about:

| Feature | Where | Why it's the "wow" |
|---|---|---|
| **Live token-by-token streaming reply** | `StreamReplyController` → `/api/reply/stream` (SSE) + `StreamingReply.svelte` | The reply visibly *types itself* in the console UI, like ChatGPT — not a spinner-then-blob-of-text. Uses WebFlux `Flux<String>` piped straight from Gemini's own `streamGenerateContent?alt=sse` endpoint through to a raw `ReadableStream` reader in the browser. |
| **Three tones generated in parallel** | `MultiToneController` → `/api/reply/multi-tone` | Fires 3 concurrent Gemini calls (`Flux.merge`) — Professional / Friendly / Firm — and returns all three at once so you can pick, not regenerate-and-hope. |
| **Priority radar (urgency + sentiment + reasoning)** | `PriorityController` → `/api/analyze/priority` + `PriorityGauge.svelte` | Gemini returns structured JSON (score, sentiment, category, one-line reasoning) and the UI renders it as an animated radial gauge instead of a wall of text. |

None of this needs a database — it's intentionally stateless so you can focus
on the AI plumbing. (Adding persistence, e.g. saving reply history to
Postgres, is a natural next iteration — see §5.)

---

## 2. Get a Gemini API key (free tier is enough)

1. Go to **https://aistudio.google.com/apikey**
2. Sign in, click **Create API key**
3. Copy it — you'll set it as an environment variable, never hardcode it

## 3. Run it

**Backend**
```bash
cd backend
export GEMINI_API_KEY=your_key_here      # macOS/Linux
# setx GEMINI_API_KEY "your_key_here"    # Windows (new shell needed after)
./mvnw spring-boot:run
```
Backend starts on `http://localhost:8080`. Hit `http://localhost:8080/api/health`
to confirm `geminiKeyConfigured: true`.

**Frontend** (separate terminal)
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`. Vite's dev proxy forwards `/api/*` to the
backend, so there's nothing else to configure locally.

**Production build**: `npm run build` in `frontend/` outputs static files to
`frontend/dist/` — point Spring's static resource handler at that folder, or
serve it separately behind Nginx/Netlify/Vercel and set `app.cors.allowed-origins`
in `application.yml` to that domain.

---

## 4. Configuring extra features

### Swap the model
`backend/src/main/resources/application.yml`:
```yaml
gemini:
  model: gemini-2.0-flash   # try gemini-1.5-pro for higher quality, slower/costlier
```

### Tune creativity / length
In `GeminiService.buildRequestBody`, the `generationConfig` block:
```java
"generationConfig", Map.of(
    "temperature", 0.7,       // 0 = deterministic, 1+ = more creative
    "maxOutputTokens", 1024
)
```

### Add a 4th tone (or change the three)
Edit the `Tone` enum in `dto/Tone.java` — the multi-tone endpoint and the
frontend tone cards both read from it automatically, no other changes needed.

### CORS for a deployed frontend
```yaml
app:
  cors:
    allowed-origins: http://localhost:5173,https://your-deployed-frontend.com
```

### Rate limiting / abuse protection (recommended before going public)
Add `spring-boot-starter-actuator` + a bucket4j filter in front of
`/api/reply/*`, since each button click is a real (metered) Gemini call.

---

## 5. Built using the RAD (Rapid Application Development) model

RAD trades heavy upfront specification for fast, visible, iterative loops —
appropriate here because "smart reply" is a UX-driven feature best judged by
actually using it, not by a spec document. This project followed RAD's four
classic phases:

**Phase 1 — Requirements Planning (fast, ~1 pass)**
Defined the single core user story — *"paste an email, get a usable reply
in under 5 seconds, without leaving the page"* — plus three feature
candidates (streaming, multi-tone, priority) scoped small enough to each be
buildable as an independent vertical slice.

**Phase 2 — User Design (prototype-and-review loop)**
Each feature was built backend-endpoint-first, then wired to a minimal
Svelte component immediately, rather than designing the whole schema/UI
upfront. This is RAD's defining trait: the "prototype" *is* the spec —
`StreamReplyController` → `StreamingReply.svelte` was working end-to-end
before `MultiToneController` was even started, so the pattern could be
reused instead of re-derived.

**Phase 3 — Construction**
Vertical slices assembled in parallel-friendly, independently testable
units: `GeminiService` is the one shared reactive core; controllers and
Svelte components are thin and disposable, so any one feature can be ripped
out or replaced without touching the others.

**Phase 4 — Cutover**
Stateless-by-design (no DB, no auth) means this can go from `npm run dev` to
a deployed static frontend + containerized backend with no migration step —
intentional, since RAD favors shipping the working increment over polishing
a big-bang release.

**Why RAD fit this specific project:** the risky part wasn't the CRUD
skeleton, it was "does the streaming UX actually feel good, and is
multi-tone actually useful or just a gimmick?" — questions only answerable
by running the thing, which is exactly what RAD optimizes for over
Waterfall's spec-first approach.

---

## 6. Natural next iterations
- Persist reply history (add `spring-boot-starter-data-jpa` + Postgres/H2)
- Real inbox integration (Gmail API / IMAP) instead of paste-in
- Auth (Spring Security + OAuth2) if this becomes multi-user
- Swap Gemini for a provider-agnostic layer (Spring AI) if you want to A/B models

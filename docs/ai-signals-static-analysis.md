# Signals of AI-Generated Content for Static Analysis

A reference catalogue of **static** signals usable to detect AI-generated **text, photos, and video**, with suggested weights, severity, and concrete implementation notes for turning each signal into a Java rule.

> **Scope.** "Static" here means *no inference through the project's own detection AI model is required*. A signal is still "static" even if it needs deterministic computation (FFT, n-gram perplexity, MP4 atom parsing). What separates static from dynamic is: static = parsing, pattern matching, statistics, and signal processing on the artifact itself; dynamic = running the content through a learned classifier.

---

## 0. How to read this document

### 0.1 Terminology (matches the system design)

- **Rule** — a Java class implementing detection logic for exactly one signal.
- **RuleResult** — output of a rule for one artifact; carries 0..N evidence items.
- **Evidence** — a single concrete hit. Has:
  - **confidence** `[0.0–1.0]` — how sure we are this hit is *real* (i.e. did we correctly detect the thing), *not* how sure we are it's AI. A literal byte marker like `\u200B` is confidence `1.0` (it's definitely there); a statistical estimate like "low burstiness" is `0.4–0.7`.
  - **severity** — `LOW | MEDIUM | HIGH | CRITICAL` — how incriminating this hit is for an AI verdict.
- **weight** — per-rule importance toward the final score.

### 0.2 The scoring contract (suggestion)

Static and dynamic analysis feed a shared score. A clean separation that survives weight re-tuning:

```
ruleContribution = weight * f(evidence)
where f(evidence) aggregates evidence with diminishing returns, scaled by confidence and severity.
```

Suggested per-rule aggregation (avoids "1000 em-dashes = infinite points"):

```
rawHits      = Σ over evidence of (confidence_i * severityMultiplier(severity_i))
saturated    = rawHits / (rawHits + k)          // k tunes how fast the rule saturates, 0..1 output
ruleScore    = weight * saturated
```

`severityMultiplier`: LOW=1, MEDIUM=3, HIGH=7, CRITICAL=20 (tune freely).

Final static score = `Σ ruleScore` (optionally with the combination bonus below), then map to a probability via logistic squashing so it composes with the dynamic model's probability.

### 0.3 Two weight philosophies — pick deliberately per rule

1. **Definitive / near-proof signals** (CRITICAL): a single high-confidence hit should nearly settle the verdict on its own. Examples: a valid C2PA manifest naming an AI generator, a Stable Diffusion `parameters` PNG chunk, raw prompt-leakage text. Give these large weights *and* design them so one hit dominates.
2. **Probabilistic / circumstantial signals** (LOW–HIGH): individually weak, meaningful only in aggregate. Examples: a single em-dash, missing EXIF, uniform sentence length. Keep individual weights small and rely on the **combination bonus**.

### 0.4 Combination bonus (strongly recommended)

The research is right that *co-occurrence* matters more than any single weak signal. A human might overuse em-dashes; a human is unlikely to overuse em-dashes **and** "delve" **and** perfect smart-quotes **and** uniform sentence length all at once. Suggested:

```
distinctWeakSignals = count of rules with at least one MEDIUM+ hit
if distinctWeakSignals >= 3:  staticScore *= (1 + 0.15 * (distinctWeakSignals - 2))   // cap the multiplier, e.g. at 2.0
```

### 0.5 False-positive discipline (calibration notes)

Every probabilistic signal has a legitimate non-AI source. The weights below assume you respect these caveats:

- Professional/edited content is *stripped* of metadata routinely → "missing EXIF" is weak alone.
- Good human writers use em-dashes, smart quotes, and bullet lists.
- Screenshots, exports, and re-encodes destroy original metadata and re-stamp `Lavf`/PIL tables onto *human* content.
- Translation tools and CMS editors inject smart quotes and zero-width characters into human text.
- Treat any single circumstantial signal as a nudge, never a verdict.

---

# PART 1 — TEXT

Text is the cheapest and richest modality for static analysis. Signals fall into: **leakage** (definitive), **hidden characters** (definitive when watermark-grade), **typography**, **lexicon/phrasing**, **structure/formatting**, **statistical/stylometric**, and **watermark** families.

## 1.A Summary table — Text

| # | Signal | Family | Suggested weight | Typical severity | Detection confidence basis |
|---|--------|--------|:----------------:|:----------------:|----------------------------|
| T1 | Direct prompt/assistant leakage ("As an AI language model", refusals) | Leakage | **+45 to +70** | CRITICAL | Exact/near-exact string → ~1.0 |
| T2 | Conversational scaffold leakage ("Certainly! Here's…", "I hope this helps!") | Leakage | +15 to +35 | HIGH | High |
| T3 | Knowledge-cutoff / no-real-time-data disclaimers | Leakage | +20 to +40 | HIGH | High |
| T4 | Placeholder leakage (`[insert X]`, `example.com`, `[Your Name]`) | Leakage | +15 to +30 | HIGH | High |
| T5 | Repeated zero-width / invisible characters | Hidden chars | +20 to +40 | HIGH→CRITICAL | Byte-exact → ~1.0 |
| T6 | Variation-selector / tag-character steganography | Hidden chars | +30 to +50 | CRITICAL | Byte-exact → ~1.0 |
| T7 | Unicode confusables / mixed-script anomalies | Hidden chars | +3 to +10 | LOW–MEDIUM | Medium |
| T8 | Em-dash overuse (density) | Typography | +1 to +12 (density-scaled) | LOW→MEDIUM | High (count is exact) |
| T9 | Consistent "smart" typography (curly quotes, …, proper apostrophe) | Typography | +3 to +10 | LOW–MEDIUM | High |
| T10 | AI lexical markers ("delve", "tapestry", "crucial"…) | Lexicon | +5 to +20 | MEDIUM | High (lexical match) |
| T11 | AI transition/cliché phrases ("It's important to note", "In today's fast-paced world") | Phrasing | +5 to +18 | MEDIUM | High |
| T12 | Rhetorical-template constructions ("It's not just X, it's Y", rule-of-three) | Phrasing | +4 to +12 | LOW–MEDIUM | Medium |
| T13 | Excessive Markdown/list/bold structure | Structure | +5 to +12 | MEDIUM | Medium–High |
| T14 | Emoji-decorated headers / bullets | Structure | +3 to +8 | LOW–MEDIUM | High |
| T15 | Low sentence-length variance (burstiness) | Stylometry | +5 to +15 | MEDIUM | Medium |
| T16 | Low perplexity (predictability) | Stylometry | +5 to +15 | MEDIUM | Medium |
| T17 | Repetitive sentence openers / paragraph templating | Stylometry | +4 to +12 | LOW–MEDIUM | Medium |
| T18 | Hedging / false-balance density | Stylometry | +3 to +10 | LOW–MEDIUM | Medium |
| T19 | Fabricated-citation / fake-reference patterns | Semantic | +5 to +15 | MEDIUM | Medium |
| T20 | SynthID-Text / cryptographic watermark detection | Watermark | **+50 to +90** | CRITICAL | Detector-dependent |

---

## 1.B Detailed signals — Text

### T1 — Direct prompt / assistant leakage  *(CRITICAL)*
The single strongest text signal. Raw, unedited output containing the model's own meta-commentary or refusal templates.

**Phrase library (English):**
- `as an AI language model`, `as a large language model`, `as an AI assistant`, `I am an AI`, `I'm an AI developed by`
- `I cannot fulfill (that|this) request`, `I can't assist with that`, `I'm sorry, but I can(no|')t`, `I'm unable to provide`
- `I cannot provide (medical|legal|financial) advice`
- `As of my last (knowledge )?update`, `my knowledge cutoff`, `my training data`
- self-naming: `I'm ChatGPT`, `as Claude`, `I am Gemini`, `as Copilot`
- prompt echo: `Rewrite the following`, `Here is the rewritten`, `Sure, here is`

**Phrase library (Russian — from research):**
- `как языковая модель ИИ`, `как искусственный интеллект`, `я не могу выполнить этот запрос`, `я — языковая модель`, `на момент моего последнего обновления`

**Detection:** case-insensitive regex set, Unicode-normalized (NFKC) first so that smart quotes/zero-width tricks don't defeat the match. One hit → CRITICAL, confidence ≈ 1.0.

```regex
(?i)\bas an? (?:AI|artificial intelligence)(?: language)?(?: model)?\b
(?i)\bI(?:'m| am)?\s+(?:sorry,?\s+)?(?:but\s+)?(?:I\s+)?can(?:no|')?t\s+(?:fulfil|assist|provide|help)
(?i)\b(?:as of )?my (?:last )?(?:knowledge|training)(?: data| cutoff| update)?\b
```

**Moving parts:** normalization → multi-pattern matcher (Aho-Corasick for the literal set, regex for the templated set). Java: a precompiled `Pattern[]` plus an Aho-Corasick lib (e.g. `org.ahocorasick:ahocorasick`) for speed on long pages.

**FP notes:** an article *about* AI may quote these phrases. Lower severity to HIGH if the phrase appears inside a quotation block / code block.

---

### T2 — Conversational scaffold leakage  *(HIGH)*
Politeness/framing that survives lazy copy-paste:
`Certainly!`, `Sure!`, `Of course!`, `Great question!`, `Absolutely!` as a *sentence-initial standalone*; openers `Here (is|are) the …you requested`; closers `I hope this helps`, `Let me know if you (need|have)`, `Feel free to`, `Is there anything else`.

**Detection:** anchor to line/paragraph start or end (these scaffolds live at boundaries). Weight scales with how many distinct scaffolds co-occur.

---

### T3 — Knowledge-cutoff / capability disclaimers  *(HIGH)*
`I don't have access to real-time (data|information)`, `I cannot browse the internet`, `I'm not able to access current`, `as of (my training|2023|2024)`. Strong because humans almost never write these.

---

### T4 — Placeholder leakage  *(HIGH)*
Unfilled templates the user forgot to edit:
`[insert ...]`, `[Your Name]`, `[Company Name]`, `[date]`, `[X]`, `example.com`, `john.doe@example.com`, `lorem ipsum` (weak), `(add details here)`, `<your text here>`.

```regex
\[(?:insert|your|company|name|date|topic|x)\b[^\]]*\]
\bexample\.(?:com|org|net)\b
```

---

### T5 — Zero-width / invisible characters  *(HIGH → CRITICAL)*
Some platforms and API wrappers embed watermarks as runs of invisible code points; CMS/translation tools also inject stray ones, so **density and patterning** decide severity.

**Code points to scan:**
| Code point | Name |
|---|---|
| `U+200B` | Zero-width space |
| `U+200C` | Zero-width non-joiner |
| `U+200D` | Zero-width joiner |
| `U+2060` | Word joiner |
| `U+FEFF` | Zero-width no-break space / BOM |
| `U+00AD` | Soft hyphen |
| `U+180E` | Mongolian vowel separator |
| `U+061C`, `U+200E`, `U+200F` | Bidi marks (context-dependent) |

**Severity ladder:**
- 1–2 stray (likely editor artifact) → LOW, low confidence it's AI.
- Repeating run / regular spacing / count proportional to text length → **HIGH**.
- Decodable bit pattern (see T6) → **CRITICAL**.

**Detection:** scan the codepoint stream; record positions; test for periodicity (FFT or autocorrelation on the position deltas) — a regular period screams "encoded payload."

---

### T6 — Variation-selector / tag-character steganography  *(CRITICAL)*
A newer hidden-watermark vector. Two ranges can carry an invisible payload after a visible character:
- **Variation selectors** `U+FE00–U+FE0F` and `U+E0100–U+E01EF`
- **Tag characters** `U+E0000–U+E007F` (an entire invisible ASCII-mirror alphabet)

Long runs of these encode bytes (account ID, "AI-generated" flag). Presence in body text (outside legitimate emoji VS-16 usage `U+FE0F`) is almost never benign.

**Detection:** strip legitimate emoji `…\uFE0F` usage, then flag remaining runs. If you can map tag chars back to ASCII you may literally recover a payload → log it as evidence text.

---

### T7 — Unicode confusables / mixed-script  *(LOW–MEDIUM)*
Cyrillic `а/е/о/с/р` mixed into Latin words, fullwidth chars, NBSP (`U+00A0`) where a normal space is expected. Weak on its own (also a phishing/spam signal), useful as a co-signal. Use ICU4J `SpoofChecker` / confusable skeletons.

---

### T8 — Em-dash overuse  *(density-scaled, LOW→MEDIUM)*
Currently one of the most reliable *stylistic* tells of modern chat models. **The signal is density, not presence.** One em-dash is noise; an em-dash every 1–2 sentences with no spaces around it (`word—word`) is a strong stylistic fingerprint.

**Detection:**
```
emDashes = count of U+2014 (—)   // also consider U+2013 – used as em-dash
density  = emDashes / sentenceCount
```
Severity ladder: density < 0.05 → LOW (often 0 points); 0.05–0.15 → MEDIUM; > 0.15 → MEDIUM/HIGH. Also flag the *unspaced* `\w—\w` form, which models prefer over the human `word — word`.

**FP notes:** editors, essayists, and certain house styles love em-dashes. Keep individual weight small; let the combination bonus do the work.

---

### T9 — Consistent "smart" typography  *(LOW–MEDIUM)*
Models emit typographically perfect text where casual humans type ASCII:
- curly quotes `“ ” ‘ ’` (`U+201C/D`, `U+2018/19`) instead of `" '`
- ellipsis char `…` (`U+2026`) instead of `...`
- proper apostrophe `’` instead of `'`
- non-breaking/thin spaces around units

**Signal = *consistency*.** Humans mix straight and curly; an artifact that is 100% curly with zero straight quotes is suspicious. Compute the ratio; flag near-perfect consistency, not mere presence.

---

### T10 — AI lexical markers  *(MEDIUM)*
Specific high-frequency vocabulary. Score = weighted hit count over text length (per-1000-words), with diminishing returns.

**English marker lexicon (non-exhaustive — make it a config file):**
`delve, tapestry, testament, crucial, underscore, realm, beacon, landscape (metaphorical), multifaceted, nuanced, intricate, intricacies, pivotal, comprehensive, robust, leverage (verb), facilitate, showcase, foster, navigate (metaphorical), navigating, elevate, embark, harness, unlock, boast, seamless, seamlessly, vibrant, bustling, ever-evolving, rapidly evolving, game-changer, treasure trove, plethora, myriad, paramount, cornerstone, holistic, synergy, streamline, cutting-edge, state-of-the-art, transformative, empower, unparalleled, meticulous, meticulously`

**Russian marker lexicon:**
`многогранный, безусловно, ключевой, подчеркивает, важно отметить, играет важную роль, в современном мире, неотъемлемой частью, стоит отметить, в конечном счёте`

**Notes:** `delve` is the canonical meme-marker (used far more by chat models than people). Maintain per-language lexicons with individual sub-weights — `delve`/`tapestry` outweigh `crucial`/`comprehensive`, which appear in normal prose. Match on lemmatized/word-boundary basis to avoid substring false hits.

---

### T11 — AI transition / cliché phrases  *(MEDIUM)*
Multi-word collocations, stronger than single words:

`it's important to note`, `it's worth noting`, `it is worth mentioning`, `that being said`, `in conclusion`, `in summary`, `to sum up`, `furthermore`, `moreover`, `additionally`, `in today's (fast-paced|digital|modern) world`, `in the realm of`, `when it comes to`, `at the end of the day`, `navigating the complexities of`, `a testament to`, `plays a (crucial|vital|key|pivotal) role`, `stark reminder`, `rich tapestry`, `the world of`, `dive into`, `let's dive in`, `let's explore`, `unlock the (potential|power|secrets)`, `in this (article|guide|post), we will`

**Russian:** `важно отметить`, `в заключение`, `стоит отметить`, `этот аспект подчеркивает`, `подводя итог`, `с одной стороны … с другой стороны`

Use Aho-Corasick over the phrase set. Score scales with distinct-phrase variety more than raw count.

---

### T12 — Rhetorical-template constructions  *(LOW–MEDIUM)*
Syntactic templates models overuse:
- **Negative parallelism / antithesis:** `It's not (just )?X, it's Y` / `This isn't about X. It's about Y.`
- **Whether-you-are:** `Whether you're a … or a …,`
- **From-X-to-Y enumeration:** `From … to …,` opening a sentence
- **Rule of three:** runs of exactly three comma-separated adjectives/nouns (e.g. "fast, reliable, and scalable") — count their density.
- `In a world where …`

Regex-detectable but with higher FP risk; medium confidence per hit.

---

### T13 — Excessive Markdown / list / bold structure  *(MEDIUM)*
Models over-format. Compute structural density per 1000 chars:
- `**bold**` runs (`\*\*[^*]+\*\*`)
- bullet/numbered list items at line start (`^\s*([-*+]|\d+\.)\s`)
- ratio of list lines to prose lines
- "bold lead-in" list items: `^\s*[-*]\s+\*\*[^*]+\*\*:` (e.g. `- **Scalability:** …`) — a very strong AI list template
- repeated `Header → 1 short paragraph → bullets` blocks

**Detection:** parse Markdown (or detect raw Markdown when content arrived as HTML — `<strong>`/`<ul>` density works equivalently). Flag when bold-per-paragraph or list-density exceeds human norms.

---

### T14 — Emoji-decorated headers / bullets  *(LOW–MEDIUM)*
Section headers prefixed with topical emoji (`🚀 Getting Started`, `✨ Features`, `📈 Growth`) and emoji used as bullet glyphs. Strong stylistic tell of marketing-flavored AI output. Detect emoji adjacent to heading text or at list-item start.

---

### T15 — Low sentence-length variance (burstiness)  *(MEDIUM)*
Human writing is "bursty": short sentences next to long ones. AI tends toward uniform length.

**Detection:**
```
lengths   = [word count per sentence]   // sentence-split with ICU BreakIterator
burstiness = stddev(lengths) / mean(lengths)   // coefficient of variation
```
Low CV (e.g. < 0.4) and a tight, near-Gaussian distribution → MEDIUM evidence. Confidence is moderate (text length matters; short samples are unreliable — require ≥ ~8 sentences).

---

### T16 — Low perplexity / high predictability  *(MEDIUM)*
AI text is "average," so each token is highly predictable. Without the original model you approximate with a small reference language model.

**Detection options (no heavy ML model needed):**
- Ship a small **n-gram LM** (KenLM-style, or your own trie) trained on a human corpus; compute mean negative log-likelihood. Low perplexity → suspicious.
- Cheaper proxy: ratio of high-frequency function words, repetition of n-grams, low type-token ratio.

**Moving parts:** a packaged n-gram model file + a scorer. Flag this signal's runtime cost; it's the most expensive "static" text rule. Confidence moderate; calibrate threshold against a human/AI dev set.

---

### T17 — Repetitive sentence openers / paragraph templating  *(LOW–MEDIUM)*
Models reuse openers ("Additionally,", "However,", "It's important…") and clone paragraph shape (topic sentence → 2 supports → wrap). Detect: distribution of first 1–2 tokens per sentence/paragraph; flag low entropy. Detect near-duplicate paragraph skeletons via shingled hashing.

---

### T18 — Hedging / false-balance density  *(LOW–MEDIUM)*
Over-hedging (`generally`, `typically`, `often`, `in many cases`, `it depends`, `there are pros and cons`) and reflexive both-sides framing. Count hedge terms and `on (the )?one hand … on the other hand` patterns per 1000 words.

---

### T19 — Fabricated-citation patterns  *(MEDIUM)*
AI invents authoritative-looking references. Static heuristics:
- citation-shaped strings with no resolvable target: `(Author, 2023)` clusters, fake DOIs (`10.xxxx/...` format check), URLs to nonexistent-looking paths
- "References" sections with suspiciously uniform formatting and round-number years
- arXiv IDs / ISBNs that fail checksum/format validation

You can't confirm fabrication statically, but format anomalies + non-resolvable structure are a usable medium signal.

---

### T20 — Cryptographic / distributional text watermark  *(CRITICAL when detectable)*
- **SynthID-Text (Google DeepMind, open-sourced 2024):** a distributional watermark over token choices. If you integrate the released detector and possess the relevant config, a positive detection is near-definitive.
- **Aaronson scheme (OpenAI):** PRNG-keyed green/red token bias. Without the key you cannot confirm, but you *can* measure green-list skew if you ever obtain a key. Treat as opportunistic.

**Implementation:** wrap the official detector as a service; on positive, emit one CRITICAL evidence with very high weight. Until integrated, leave a stub rule returning no evidence (so the rule slot exists for later).

---

# PART 2 — PHOTO / IMAGE

Image signals fall into: **provenance metadata** (definitive), **embedded generator metadata** (definitive), **filename patterns**, **format/dimension tells**, and **signal-processing artifacts** (need computation).

## 2.A Summary table — Image

| # | Signal | Family | Suggested weight | Typical severity | Detection confidence basis |
|---|--------|--------|:----------------:|:----------------:|----------------------------|
| I1 | Valid C2PA manifest naming an AI generator | Provenance | **+60 to +95** | CRITICAL | Signed manifest → ~1.0 |
| I2 | IPTC `digitalSourceType = trainedAlgorithmicMedia` | Provenance | **+50 to +85** | CRITICAL | Field-exact → ~1.0 |
| I3 | Stable Diffusion / ComfyUI / NovelAI param chunks | Embedded meta | **+55 to +90** | CRITICAL | Chunk-exact → ~1.0 |
| I4 | EXIF `Software`/`Artist` = known generator | Embedded meta | +30 to +60 | HIGH | High |
| I5 | XMP AI-generation tags | Embedded meta | +30 to +60 | HIGH | High |
| I6 | Generator filename patterns (DALL·E, MJ, SD, ComfyUI) | Filename | +20 to +45 | HIGH | High (but renamable) |
| I7 | Missing camera EXIF on "photographic" image | Metadata absence | +3 to +12 | LOW–MEDIUM | Medium |
| I8 | "Too-clean" sRGB ICC / default profile | Metadata | +2 to +8 | LOW | Low–Medium |
| I9 | Generator-default dimensions / aspect ratios | Format | +3 to +12 | LOW–MEDIUM | Medium |
| I10 | PNG/WebP delivered as a "photo" | Format | +2 to +8 | LOW | Low–Medium |
| I11 | Default/library quantization tables (PIL/ffmpeg) | Format | +5 to +15 | MEDIUM | Medium |
| I12 | FFT/DCT spectral grid artifacts | Signal proc | +10 to +25 | MEDIUM–HIGH | Medium |
| I13 | Absent sensor-noise fingerprint (PRNU) | Signal proc | +8 to +20 | MEDIUM | Medium |
| I14 | SynthID image watermark | Watermark | **+50 to +90** | CRITICAL | Detector-dependent |
| I15 | Perceptual-hash match to known AI-output corpus | Hash DB | +15 to +40 | MEDIUM–HIGH | High if match |

---

## 2.B Detailed signals — Image

### I1 — C2PA / Content Credentials manifest  *(CRITICAL)*
The industry provenance standard (Coalition for Content Provenance and Authenticity), adopted by OpenAI (DALL·E 3), Microsoft, Adobe Firefly, and others.

**What to look for:**
- **JUMBF** container (`JPEG Universal Metadata Box Format`). In JPEG it rides in an `APP11` marker; in PNG in a `caBX`-type chunk; in BMFF/MP4-family in a dedicated box.
- Inside: a **C2PA manifest** with a `claim_generator` field (e.g. `Adobe_Firefly`, `OpenAI`, `Microsoft …`), assertion `c2pa.actions` containing `c2pa.created` with `digitalSourceType` of `trainedAlgorithmicMedia`, and a cryptographic signature.

**Detection:**
- Quick hex scan for ASCII markers: `c2pa`, `jumb`, `jumd`, `urn:uuid:`, `claim_generator`, `Adobe`, `OpenAI`. Cheap pre-filter.
- Proper parse + signature validation via a real library: **`c2pa` (Rust)** through JNI, or the `c2patool` CLI, or `contentauth` Java bindings. **Validate the signature** — an unsigned/forged manifest is weaker evidence than a cryptographically valid one (which is near-proof).

**Severity:** valid signature + AI generator → CRITICAL, confidence ~1.0. Note: C2PA can equally certify a *camera* origin — so always read the assertions; presence of C2PA ≠ AI.

---

### I2 — IPTC `digitalSourceType = trainedAlgorithmicMedia`  *(CRITICAL)*
The IPTC Photo Metadata standard defines an explicit field for synthetic media. Values of interest (stored in XMP/IPTC):
- `…/digitalsourcetype/trainedAlgorithmicMedia` — fully AI-generated
- `…/digitalsourcetype/compositeSynthetic` — AI-composited
- `…/digitalsourcetype/algorithmicMedia` — algorithmically produced

A populated `trainedAlgorithmicMedia` is a self-declaration of AI generation. **Detection:** parse XMP (`Iptc4xmpExt:DigitalSourceType`). Java: `metadata-extractor` (Drew Noakes) or Apache Tika exposes XMP. CRITICAL.

---

### I3 — Embedded generator parameter blocks  *(CRITICAL — the goldmine)*
Local-generation tools embed their full recipe in the file:

- **Automatic1111 / Stable Diffusion WebUI** → PNG `tEXt`/`iTXt` chunk keyed **`parameters`** containing literally: the prompt, `Negative prompt:`, `Steps:`, `Sampler:`, `CFG scale:`, `Seed:`, `Size:`, `Model hash:`, `Model:`.
- **ComfyUI** → PNG chunks keyed **`prompt`** and **`workflow`** holding the node-graph JSON.
- **NovelAI** → `Software`=`NovelAI`, plus `Comment`/`Source` chunks; sometimes a steganographic alpha-channel signature.
- **InvokeAI / Fooocus / DreamStudio** → their own metadata keys.

**Detection:** read PNG ancillary chunks (`tEXt`, `zTXt`, `iTXt`) and JPEG comment/EXIF; match keys (`parameters`, `prompt`, `workflow`, `Software`) and the value sub-fields (`Steps:`, `Sampler:`, `CFG scale:`, `Seed:`). Any hit → CRITICAL, confidence ~1.0. Bonus: you can extract and log the actual prompt/model as rich evidence.

```regex
(?s)Steps:\s*\d+.*?Sampler:.*?CFG scale:.*?Seed:\s*\d+   // A1111 signature
"class_type"\s*:\s*"[A-Za-z]+"                            // ComfyUI workflow JSON
```

**Moving parts:** a raw PNG chunk reader (don't rely only on EXIF libraries — some skip `tEXt`). Java: `metadata-extractor` reads PNG textual chunks; for completeness also do a direct chunk walk.

---

### I4 — EXIF `Software` / `Artist` = known generator  *(HIGH)*
EXIF/`Software` field set to `DALL·E`, `Midjourney`, `Adobe Firefly`, `Stable Diffusion`, `DreamStudio`, `Picsart`, `Canva` (AI features), etc. **Detection:** EXIF parse + generator name list. HIGH (editors can spoof, so not quite CRITICAL alone).

---

### I5 — XMP AI-generation tags  *(HIGH)*
Beyond IPTC, vendors write XMP namespaces (Adobe `xmpMM`, custom `GenAI` flags, `Firefly` provenance). Parse the XMP packet (`<x:xmpmeta>…</x:xmpmeta>`) and match vendor namespaces/keys.

---

### I6 — Generator filename patterns  *(HIGH, but renamable)*
Users upload files as-downloaded. Patterns:

| Generator | Pattern / example |
|---|---|
| DALL·E | `DALL·E 2023-11-05 14.23.11 - <prompt>.png` (note `·` = `U+00B7`) |
| Midjourney | `username_prompt_text_<8hex>.png`, or a bare UUID `.png` |
| SD WebUI (A1111) | `00012-123456789.png` (5-digit index + seed) |
| ComfyUI | `ComfyUI_00001_.png` |
| Bing Image Creator | `OIG.<hash>.jpeg`, `_<hash>.jpeg` |
| NovelAI | UUID-style |
| Generic AI | no camera prefix; `image (1).png`, `download.png`, `generated.png` |

**Contrast with camera filenames:** `IMG_1234.JPG`, `DSC_0123.JPG`, `PXL_20240101_120000.jpg`, `DSCF`, `P1010101.JPG`. Absence of any camera prefix + presence of a generator pattern → HIGH; weight down because trivially renamed.

```regex
^DALL(?:·|\u00B7|\.)E \d{4}-\d{2}-\d{2}
^\d{5}-\d{6,12}\.(png|jpg)$          // A1111
^ComfyUI_\d{5}_?\.png$
^OIG[._]
```

---

### I7 — Missing camera EXIF on a photographic image  *(LOW–MEDIUM)*
Real photos almost always carry `Make`, `Model`, `ExposureTime`, `FNumber`, `ISO`, `FocalLength`, `DateTimeOriginal`. AI images either strip everything (Midjourney → "clean" PNG) or leave a partial, camera-less set.

**Detection:** classify image as "photographic" (continuous tone, not a chart/logo — quick variance/color-histogram heuristic), then check for the camera-tag cluster. If photographic but missing the whole cluster → evidence. **Weak alone** (social platforms strip EXIF on upload), strong as co-signal. Suspicious sub-case: `ModifyDate` present but `DateTimeOriginal` absent.

---

### I8 — "Too-clean" ICC profile  *(LOW)*
Generators emit pristine standardized profiles (`sRGB IEC61966-2.1`) or no profile; cameras embed manufacturer profiles (e.g. `Display P3`, device-specific). A perfectly generic sRGB profile is a faint co-signal only.

---

### I9 — Generator-default dimensions / aspect ratios  *(LOW–MEDIUM)*
Models output characteristic sizes:
- DALL·E 3: `1024×1024`, `1024×1792`, `1792×1024`
- SD: multiples of 64; native `512×512`, `768×768`, `1024×1024`
- Midjourney: fixed upscale sizes; exact `1:1`, `16:9`, `2:3` ratios
- Power-of-two / multiple-of-8/64 dimensions with no odd cropping

Cameras produce sensor-specific dimensions (e.g. `4032×3024`, `6000×4000`). **Detection:** check `(w,h)` against a default-size set and test divisibility by 64. Medium at best (legitimate exports also hit round sizes).

---

### I10 — PNG/WebP delivered as a "photo"  *(LOW)*
Real-world photographs are JPEG/HEIC. A photographic-looking PNG (lossless, large) is mildly suspicious because many generators default to PNG. Co-signal only.

---

### I11 — Default/library quantization tables  *(MEDIUM)*
JPEGs carry quantization tables in the `DQT` marker. Camera firmware uses bespoke tables; **PIL/Pillow, libjpeg, and ffmpeg defaults are well-known fixed tables**. An AI pipeline that renders raw then saves via PIL/ffmpeg stamps these defaults.

**Detection:** parse `DQT`, hash the table, compare against a library of known software-default tables and against camera-table databases. A known PIL/ffmpeg-default table on a "photo" → MEDIUM. (Also fires on re-saved human images — calibrate.)

---

### I12 — FFT / DCT spectral artifacts  *(MEDIUM–HIGH)*
GAN/diffusion upsampling (transposed convolutions) leaves periodic, grid-like peaks in the frequency domain that natural images lack. The research report's "grid artifacts" point.

**Detection pipeline:**
1. Grayscale → 2D FFT (Java: **JTransforms**) → magnitude spectrum (log).
2. Look for regular peak lattices / spikes at characteristic frequencies (checkerboard from stride-2 deconv).
3. Optionally azimuthal average of the power spectrum; diffusion images show a distinctive high-frequency falloff/spectral signature vs natural `1/f`.

**Notes:** purely deterministic signal processing (qualifies as static), but compute-heavy and sensitive to JPEG re-compression (which also adds 8×8 DCT structure — don't confuse it). Medium–high when peaks are clear; moderate confidence.

---

### I13 — Absent sensor-noise fingerprint (PRNU)  *(MEDIUM)*
Real sensors imprint **Photo-Response Non-Uniformity** — a faint, fixed per-pixel noise pattern. AI images have no PRNU, or a spatially inconsistent one.

**Detection:** denoise (wavelet/Gaussian) → take the noise residual → analyze its statistics/spatial consistency. Absence of camera-like residual structure → evidence. Heavier to implement; weight accordingly.

---

### I14 — SynthID image watermark  *(CRITICAL when detectable)*
Google Imagen (and others) embed an invisible, robust watermark in the wavelet/frequency domain that survives crops and compression. **You cannot detect it without Google's detector.** If you integrate the official SynthID detector API, a positive is near-definitive. Until then, leave a rule stub.

---

### I15 — Perceptual-hash match to known AI corpus  *(MEDIUM–HIGH)*
Maintain a database of perceptual hashes (pHash/dHash/aHash) of known AI outputs (e.g. scraped from generator galleries). A near match (low Hamming distance) is strong. **Detection:** compute pHash → nearest-neighbor lookup (BK-tree / VP-tree on Hamming distance). High confidence on a tight match; coverage-limited.

---

# PART 3 — VIDEO

Video = images over time + a container + codecs + (usually) audio. Signals: **container/provenance metadata** (definitive), **encoder fingerprints**, **audio-track absence**, **codec/GOP artifacts**, and **duration/resolution defaults**.

## 3.A Summary table — Video

| # | Signal | Family | Suggested weight | Typical severity | Detection confidence basis |
|---|--------|--------|:----------------:|:----------------:|----------------------------|
| V1 | C2PA manifest in MP4/BMFF naming AI generator | Provenance | **+60 to +95** | CRITICAL | Signed → ~1.0 |
| V2 | Container metadata names generator (Sora/Runway/Pika/Kling/Luma) | Container meta | +30 to +60 | HIGH | High |
| V3 | No audio track at all | Audio absence | +15 to +35 | MEDIUM–HIGH | High (track-exact) |
| V4 | Silent/empty/synthetic audio track | Audio | +5 to +15 | LOW–MEDIUM | Medium |
| V5 | `Lavf`/ffmpeg encoder tags + no camera tags | Encoder fingerprint | +10 to +25 | MEDIUM | High (string-exact) |
| V6 | Default codec params (libx264 crf=23, yuv420p, default GOP) | Codec | +8 to +20 | MEDIUM | Medium |
| V7 | Default/ffmpeg quantization matrices | Codec | +5 to +15 | MEDIUM | Medium |
| V8 | Too-regular GOP / I-P-B structure | Codec | +5 to +15 | LOW–MEDIUM | Medium |
| V9 | Duration clustered at model max (4s/5s/10s/20s) | Format | +5 to +15 | LOW–MEDIUM | Medium |
| V10 | Generator-default resolution/fps | Format | +3 to +12 | LOW–MEDIUM | Medium |
| V11 | Generator filename pattern | Filename | +15 to +35 | HIGH | High (renamable) |
| V12 | Per-frame image artifacts (FFT) sampled across frames | Signal proc | +10 to +25 | MEDIUM–HIGH | Medium |
| V13 | Missing GPS/gyro/camera-motion metadata track | Metadata absence | +3 to +10 | LOW | Low–Medium |

---

## 3.B Detailed signals — Video

### V1 — C2PA in the video container  *(CRITICAL)*
Same standard as images, embedded in the BMFF/MP4 box tree (commonly a top-level `uuid` box or within `meta`, often inside the `moov` atom). **Detection:** parse the atom/box tree (Java: **`mp4parser`/isoparser**, or `Box`-walking), locate the C2PA box, validate via the C2PA library, read `claim_generator`/`digitalSourceType`. Hex pre-filter on `c2pa`/`jumb`. CRITICAL on a valid AI manifest.

---

### V2 — Container metadata names the generator  *(HIGH)*
MP4/MOV metadata atoms (`udta`, `meta`, `ilst`, the `©too`/`©enc` "encoder" tags) or WebM/Matroska tags carrying `Sora`, `Runway`, `Gen-2`, `Pika`, `Kling`, `Luma`, `HeyGen`, `Synthesia`. **Detection:** parse metadata atoms → match a generator-name list. HIGH.

---

### V3 — No audio track at all  *(MEDIUM–HIGH)*
Most pure text/image-to-video generators (Sora-era, Runway Gen-2, Pika early) output **video only**. Real phone/camera footage almost always carries at least one audio track (even if near-silent room tone). **Detection:** enumerate tracks in the container; zero audio tracks on a multi-second clip → evidence. Strong, since it's a structural fact (confidence ~1.0 on detection); severity MEDIUM–HIGH because legitimately muxed/edited human clips can also lack audio.

---

### V4 — Silent / synthetic audio track  *(LOW–MEDIUM)*
An audio track present but **digital-silence** (all-zero PCM), or a clearly synthetic/TTS track with no environmental noise floor. **Detection:** sample audio; measure RMS/noise floor; flag perfect silence or unnaturally clean spectra. Co-signal.

---

### V5 — `Lavf`/ffmpeg encoder fingerprint  *(MEDIUM)*
AI pipelines render frames then mux with ffmpeg, stamping `Lavf<version>` (libavformat) in the encoder/`©too` metadata and characteristic muxer defaults — while carrying **no camera make/model/firmware tags**. **Detection:** read encoder/handler strings; flag `Lavf`, `libavformat`, `Lavc`, `x264 - core …` defaults *combined with* absence of device tags. (Lots of legitimate edited video is also ffmpeg-muxed → MEDIUM, lean on co-signals.)

---

### V6 — Default codec parameters  *(MEDIUM)*
libx264/libx265 defaults betray a generic encode: `crf=23`, `preset=medium`, pixel format `yuv420p`, no scene-cut adaptive GOP, BT.709 default color tags. The `x264 - core` settings string (often in metadata) lists these verbatim. Camera hardware encoders use distinct, device-specific parameter sets. **Detection:** parse the SPS/PPS and/or the `x264`/`x265` options string; compare against known defaults.

---

### V7 — Default quantization matrices  *(MEDIUM)*
As with JPEG (I11), but for video: H.264/H.265 can carry custom scaling lists; AI/ffmpeg encodes typically use flat/default matrices. Camera encoders often tune them. **Detection:** parse scaling-list/quant-matrix from SPS/PPS; compare to default tables. MEDIUM.

---

### V8 — Too-regular GOP structure  *(LOW–MEDIUM)*
Hardware camera encoders produce content-adaptive GOPs (keyframes at scene cuts, variable I/P/B). A frame-by-frame AI render re-encoded by ffmpeg often yields a **rigidly periodic** GOP (e.g. keyframe every N frames, fixed B-frame pyramid). **Detection:** demux and read frame types (without decoding pixels you can read NAL unit types / packet flags) → measure GOP-length variance and I/P/B ratios; very low variance → evidence. Medium implementation effort.

---

### V9 — Duration clustered at model limits  *(LOW–MEDIUM)*
Generators cap clip length, producing telltale exact durations:
- Runway Gen-2/Gen-3: ~4s / extendable
- Pika: ~3s
- Kling: ~5s / 10s
- Sora-class: up to ~20s, often exactly 5s/10s
- Exact integer seconds with no fractional remainder

**Detection:** read `duration` from `mvhd`/track headers; flag exact small-integer durations (3/4/5/10/20s) within ±1 frame. Co-signal (humans also trim to round durations).

---

### V10 — Generator-default resolution / fps  *(LOW–MEDIUM)*
Exact `1280×720`, `1920×1080`, `1024×576`, model-specific squares, and exactly `24`/`30` fps with zero drift. Phone cameras use device-specific resolutions and slightly non-integer fps (e.g. `29.97`, variable frame rate). **Detection:** read dimensions + average frame rate; flag exact model defaults and perfectly constant frame timing. (Note: constant vs variable frame rate is itself a tell — phones often record VFR.)

---

### V11 — Generator filename pattern  *(HIGH, renamable)*
| Generator | Example |
|---|---|
| Runway | `Gen-2 <hash>.mp4`, `Gen-3 …` |
| Sora | platform-specific export names |
| Pika / Kling / Luma | app-prefixed or UUID names |
| Generic | `output.mp4`, `video.mp4`, `download.mp4`, no camera prefix |

**Contrast with camera:** `VID_20240101_…`, `MOV_1234.MP4`, `PXL_…`, `C0001.MP4` (Sony), `GX010001.MP4` (GoPro), `IMG_1234.MOV`. Generator pattern + no camera prefix → HIGH; low-ish weight (renamable).

---

### V12 — Per-frame image artifacts sampled across frames  *(MEDIUM–HIGH)*
Apply the image FFT/spectral test (I12) and metadata logic to **sampled keyframes**. Decode every Nth keyframe → run the image-domain detectors → aggregate. Consistent diffusion spectral signatures across frames raise confidence vs a single-image test. Reuse the image pipeline; flag the decode cost.

> **Note on temporal artifacts (flicker, morphing, warping hands, inconsistent backgrounds):** these are powerful but require *understanding* the frames — route them to **dynamic analysis**, not static. Listed here only to mark the boundary.

---

### V13 — Missing motion/GPS metadata  *(LOW)*
Phone video frequently carries GPS, gyroscope, and camera-motion metadata tracks (`gpmd` on GoPro, Apple/Google motion atoms). Total absence on otherwise-handheld-looking footage is a faint co-signal. Low weight.

---

# PART 4 — Cross-cutting & implementation notes

## 4.1 Signals that span all modalities
- **Provenance (C2PA):** one parser family serves JPEG/PNG/MP4. Build it once; share the manifest validator.
- **Generator-name dictionary:** a single maintained list of generator brand strings (DALL·E, Midjourney, Stable Diffusion, Firefly, Sora, Runway, Pika, Kling, Luma, NovelAI, …) feeds the EXIF/XMP/container rules across modalities.
- **Perceptual-hash DB:** extend the image pHash DB to video keyframes.
- **Filename heuristics:** one regex set per modality; cheap pre-filter for everything else.

## 4.2 Suggested rule-engine wiring (maps to your Java design)
- One `Rule` class per signal above (e.g. `EmDashDensityRule`, `C2paManifestRule`, `SdParametersChunkRule`).
- Group rules by **family** so you can disable/tune a family at once.
- Each rule declares: `weight`, default `severity`, and the `confidence` it assigns to its evidence.
- **Decode/parse once, analyze many:** a per-artifact context object (decoded bytes, parsed EXIF/XMP, parsed MP4 atom tree, sentence-split text, FFT spectrum cache) shared by all rules avoids re-parsing. Heavy artifacts (FFT, PRNU, perplexity LM, frame decode) should be **lazy** and cached, computed only if a cheaper rule already raised suspicion (a two-stage pipeline: cheap rules gate expensive rules).
- **Order:** run definitive/cheap rules first (leakage, metadata, filename). If a CRITICAL fires with high confidence, you may short-circuit the expensive signal-processing rules (configurable) to save compute.

## 4.3 Suggested Java libraries / tooling
| Need | Library |
|---|---|
| EXIF / XMP / IPTC / PNG text chunks | `metadata-extractor` (Drew Noakes), Apache Tika |
| C2PA parse + signature validation | `c2pa` (Rust) via JNI, or `c2patool` CLI, or contentauth bindings |
| MP4/BMFF atom tree | `mp4parser` (sannies/isoparser) |
| Codec/SPS/PPS, frame types, GOP | bitstream parse, or ffprobe (JSON) via process call |
| FFT | JTransforms |
| Unicode normalization / confusables | ICU4J (`Normalizer2`, `SpoofChecker`, `BreakIterator`) |
| Multi-pattern string match | `org.ahocorasick:ahocorasick` |
| Perplexity / n-gram LM | KenLM (JNI) or a custom trie scorer |
| Perceptual hashing | a pHash impl + BK-tree index |

## 4.4 Severity → weight mapping cheat-sheet
| Severity | Meaning | Typical single-hit weight band |
|---|---|---|
| CRITICAL | Near-proof; one hit should dominate the static verdict | +45 to +95 |
| HIGH | Strong; a couple of these settle it | +15 to +45 |
| MEDIUM | Real but circumstantial | +5 to +18 |
| LOW | Faint; meaningful only via the combination bonus | +1 to +8 |

## 4.5 Composing static + dynamic into the final verdict
1. Compute `staticScore` (Section 0.2), apply the combination bonus (0.4), squash to a probability `p_static`.
2. Take `p_dynamic` from the AI model.
3. Combine — options:
   - **Weighted average:** `p = α·p_static + (1-α)·p_dynamic`.
   - **Override:** if any CRITICAL static evidence with confidence ≥ 0.95 fired (valid C2PA AI manifest, SD `parameters` chunk, prompt leakage), force `p ≈ 1.0` regardless of the model.
   - **Noisy-OR / log-odds sum:** sum log-odds of independent-ish signals — clean when many weak signals stack.
4. Always return **explainable evidence** (which rules fired, their severity/confidence) so a user sees *why*, not just a percentage.

## 4.6 Anti-evasion reminders
- **Normalize before matching** (NFKC) — defeats smart-quote/zero-width obfuscation of leakage phrases.
- Treat **metadata stripping** as expected from real platforms; never let "missing metadata" alone exceed LOW.
- Filenames and `Software` tags are trivially editable — keep those weights below the cryptographic/provenance signals, which are hard to forge.
- Re-encoding/re-saving destroys many artifacts and *adds* misleading ones (PIL tables, `Lavf`) to human content — lean on **combinations**, not lone circumstantial hits.

---

*End of reference. Treat every weight as a starting point to calibrate against a labelled dev set; the relative ordering (CRITICAL ≫ HIGH ≫ MEDIUM ≫ LOW) matters more than the absolute numbers.*

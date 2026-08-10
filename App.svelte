<script>
  import { streamReply, generateMultiTone, analyzePriority, checkHealth } from './lib/api.js';
  import PriorityGauge from './lib/components/PriorityGauge.svelte';
  import ToneCards from './lib/components/ToneCards.svelte';
  import StreamingReply from './lib/components/StreamingReply.svelte';
  import { onMount } from 'svelte';

  let originalEmail = '';
  let instructions = '';

  let streamText = '';
  let isStreaming = false;

  let toneReplies = [];
  let isToneLoading = false;

  let priorityAnalysis = null;
  let isAnalyzing = false;

  let backendUp = null;

  onMount(async () => {
    try {
      const h = await checkHealth();
      backendUp = h.geminiKeyConfigured ? 'ready' : 'no-key';
    } catch {
      backendUp = 'down';
    }
  });

  async function runStream() {
    if (!originalEmail.trim()) return;
    streamText = '';
    isStreaming = true;
    await streamReply(
      originalEmail,
      instructions,
      (chunk) => { streamText += chunk; },
      () => { isStreaming = false; },
      (err) => { isStreaming = false; streamText = `[error] ${err.message}`; }
    );
  }

  async function runMultiTone() {
    if (!originalEmail.trim()) return;
    isToneLoading = true;
    try {
      toneReplies = await generateMultiTone(originalEmail, instructions);
    } catch (e) {
      toneReplies = [];
    } finally {
      isToneLoading = false;
    }
  }

  async function runAnalysis() {
    if (!originalEmail.trim()) return;
    isAnalyzing = true;
    try {
      priorityAnalysis = await analyzePriority(originalEmail);
    } finally {
      isAnalyzing = false;
    }
  }

  async function runAll() {
    runStream();
    runMultiTone();
    runAnalysis();
  }
</script>

<main>
  <header>
    <div class="brand">
      <div class="brand-mark">SM</div>
      <div>
        <h1>SmartMail<span class="accent">.</span></h1>
        <p class="tagline">AI reply console — Spring Boot + Svelte + Gemini</p>
      </div>
    </div>
    <div class="status">
      {#if backendUp === 'ready'}
        <span class="dot ok"></span> backend live
      {:else if backendUp === 'no-key'}
        <span class="dot warn"></span> GEMINI_API_KEY missing
      {:else if backendUp === 'down'}
        <span class="dot bad"></span> backend unreachable
      {:else}
        <span class="dot"></span> checking…
      {/if}
    </div>
  </header>

  <section class="input-panel">
    <label for="email">Paste the email you need to reply to</label>
    <textarea id="email" rows="6" placeholder="Paste the incoming email here…" bind:value={originalEmail}></textarea>

    <label for="instructions">Optional steer (tone, points to include, what to decline, etc.)</label>
    <input id="instructions" type="text" placeholder="e.g. accept but ask to push the deadline by a week" bind:value={instructions} />

    <div class="actions">
      <button class="primary" on:click={runAll} disabled={!originalEmail.trim()}>Generate everything</button>
      <button on:click={runStream} disabled={!originalEmail.trim()}>Live reply</button>
      <button on:click={runMultiTone} disabled={!originalEmail.trim()}>3 tones</button>
      <button on:click={runAnalysis} disabled={!originalEmail.trim()}>Priority scan</button>
    </div>
  </section>

  <section class="grid">
    <div class="col span-2">
      <h2>Live reply</h2>
      <StreamingReply text={streamText} streaming={isStreaming} />
    </div>

    <div class="col">
      <h2>Priority radar</h2>
      <PriorityGauge analysis={priorityAnalysis} loading={isAnalyzing} />
    </div>
  </section>

  <section>
    <h2>Tone variants</h2>
    <ToneCards replies={toneReplies} loading={isToneLoading} />
  </section>
</main>

<style>
  :global(:root) {
    --ink-black: #0b0e14;
    --panel: #12161f;
    --hairline: #232a38;
    --ink-bright: #eef1f6;
    --ink-dim: #7c8494;
    --signal-warm: #f5a623;
    --signal-hot: #ef5b5b;
    --signal-cool: #4ea1ff;
    --signal-cool-bright: #7ec8ff;
  }
  :global(body) {
    margin: 0;
    background: var(--ink-black);
    color: var(--ink-bright);
    font-family: 'Space Grotesk', sans-serif;
  }
  main { max-width: 980px; margin: 0 auto; padding: 2.5rem 1.5rem 4rem; }

  header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; }
  .brand { display: flex; gap: 0.9rem; align-items: center; }
  .brand-mark {
    width: 42px; height: 42px; border-radius: 10px;
    background: linear-gradient(135deg, var(--signal-warm), var(--signal-hot));
    display: flex; align-items: center; justify-content: center;
    font-family: 'JetBrains Mono', monospace; font-weight: 600; color: #14100a;
  }
  h1 { font-size: 1.5rem; margin: 0; font-weight: 700; }
  .accent { color: var(--signal-warm); }
  .tagline { margin: 0.1rem 0 0; color: var(--ink-dim); font-size: 0.85rem; }

  .status { font-family: 'JetBrains Mono', monospace; font-size: 0.75rem; color: var(--ink-dim); display: flex; align-items: center; gap: 0.4rem; }
  .dot { width: 8px; height: 8px; border-radius: 50%; background: var(--ink-dim); display: inline-block; }
  .dot.ok { background: #4cd97b; box-shadow: 0 0 8px #4cd97b; }
  .dot.warn { background: var(--signal-warm); box-shadow: 0 0 8px var(--signal-warm); }
  .dot.bad { background: var(--signal-hot); box-shadow: 0 0 8px var(--signal-hot); }

  .input-panel {
    background: var(--panel); border: 1px solid var(--hairline); border-radius: 10px;
    padding: 1.25rem; display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 2rem;
  }
  label { font-size: 0.75rem; color: var(--ink-dim); letter-spacing: 0.03em; margin-top: 0.4rem; }
  textarea, input {
    background: var(--ink-black); border: 1px solid var(--hairline); border-radius: 6px;
    color: var(--ink-bright); font-family: 'Space Grotesk', sans-serif; font-size: 0.9rem;
    padding: 0.65rem 0.8rem; resize: vertical;
  }
  textarea:focus, input:focus { outline: 2px solid var(--signal-cool); outline-offset: 1px; }

  .actions { display: flex; gap: 0.6rem; margin-top: 0.9rem; flex-wrap: wrap; }
  button {
    background: transparent; border: 1px solid var(--hairline); color: var(--ink-bright);
    font-family: 'Space Grotesk', sans-serif; font-size: 0.85rem; padding: 0.55rem 1rem;
    border-radius: 6px; cursor: pointer; transition: all 0.15s ease;
  }
  button:hover:not(:disabled) { border-color: var(--signal-cool); }
  button:disabled { opacity: 0.4; cursor: not-allowed; }
  button.primary { background: var(--signal-warm); border-color: var(--signal-warm); color: #14100a; font-weight: 600; }
  button.primary:hover:not(:disabled) { filter: brightness(1.08); }

  .grid { display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; margin-bottom: 2rem; }
  .col h2, section > h2 { font-size: 0.95rem; color: var(--ink-dim); font-weight: 600; margin: 0 0 0.6rem; }
  @media (max-width: 720px) { .grid { grid-template-columns: 1fr; } }
</style>

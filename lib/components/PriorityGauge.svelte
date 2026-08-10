<script>
  export let analysis = null; // { urgencyScore, sentiment, category, reasoning }
  export let loading = false;

  $: score = analysis?.urgencyScore ?? 0;
  $: circumference = 2 * Math.PI * 52;
  $: offset = circumference - (score / 100) * circumference;
  $: color = score >= 70 ? 'var(--signal-hot)' : score >= 40 ? 'var(--signal-warm)' : 'var(--signal-cool)';
</script>

<div class="gauge-card">
  <div class="gauge-wrap">
    <svg viewBox="0 0 120 120" class="gauge">
      <circle cx="60" cy="60" r="52" class="track" />
      <circle
        cx="60" cy="60" r="52"
        class="progress"
        style="stroke: {color}; stroke-dasharray: {circumference}; stroke-dashoffset: {loading ? circumference : offset};"
      />
    </svg>
    <div class="gauge-center">
      <span class="gauge-score">{loading ? '···' : score}</span>
      <span class="gauge-label">urgency</span>
    </div>
  </div>

  <div class="gauge-meta">
    {#if loading}
      <p class="scanning">scanning signal…</p>
    {:else if analysis}
      <div class="tag" style="border-color: {color}; color: {color};">{analysis.sentiment}</div>
      <p class="category">{analysis.category}</p>
      <p class="reasoning">{analysis.reasoning}</p>
    {:else}
      <p class="placeholder">Run analysis to see the priority radar for this email.</p>
    {/if}
  </div>
</div>

<style>
  .gauge-card {
    display: flex;
    align-items: center;
    gap: 1.25rem;
    background: var(--panel);
    border: 1px solid var(--hairline);
    border-radius: 10px;
    padding: 1.25rem;
  }
  .gauge-wrap { position: relative; width: 96px; height: 96px; flex-shrink: 0; }
  .gauge { width: 100%; height: 100%; transform: rotate(-90deg); }
  .track { fill: none; stroke: var(--hairline); stroke-width: 8; }
  .progress {
    fill: none;
    stroke-width: 8;
    stroke-linecap: round;
    transition: stroke-dashoffset 0.9s cubic-bezier(0.22, 1, 0.36, 1), stroke 0.4s ease;
  }
  .gauge-center {
    position: absolute; inset: 0;
    display: flex; flex-direction: column; align-items: center; justify-content: center;
  }
  .gauge-score { font-family: 'JetBrains Mono', monospace; font-size: 1.5rem; color: var(--ink-bright); }
  .gauge-label { font-family: 'JetBrains Mono', monospace; font-size: 0.6rem; letter-spacing: 0.1em; color: var(--ink-dim); text-transform: uppercase; }
  .gauge-meta { flex: 1; min-width: 0; }
  .tag {
    display: inline-block; font-family: 'JetBrains Mono', monospace; font-size: 0.7rem;
    letter-spacing: 0.05em; text-transform: uppercase; border: 1px solid; border-radius: 999px;
    padding: 0.15rem 0.6rem; margin-bottom: 0.4rem;
  }
  .category { font-weight: 600; color: var(--ink-bright); margin: 0 0 0.2rem; }
  .reasoning { color: var(--ink-dim); font-size: 0.85rem; margin: 0; line-height: 1.4; }
  .placeholder, .scanning { color: var(--ink-dim); font-size: 0.85rem; margin: 0; }
  .scanning { font-family: 'JetBrains Mono', monospace; }
</style>

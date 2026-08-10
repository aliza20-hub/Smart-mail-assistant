<script>
  export let replies = [];
  export let loading = false;

  async function copy(text) {
    try { await navigator.clipboard.writeText(text); } catch (e) { /* ignore */ }
  }

  const toneAccent = {
    PROFESSIONAL: 'var(--signal-cool)',
    FRIENDLY: 'var(--signal-warm)',
    FIRM: 'var(--signal-hot)'
  };
</script>

<div class="tone-grid">
  {#if loading}
    {#each Array(3) as _}
      <div class="tone-card skeleton">
        <div class="skeleton-line" style="width: 40%"></div>
        <div class="skeleton-line" style="width: 90%"></div>
        <div class="skeleton-line" style="width: 75%"></div>
        <div class="skeleton-line" style="width: 60%"></div>
      </div>
    {/each}
  {:else if replies.length}
    {#each replies as r}
      <div class="tone-card" style="border-top-color: {toneAccent[r.tone] || 'var(--hairline)'}">
        <div class="tone-header">
          <span class="tone-label">{r.label}</span>
          <button class="copy-btn" on:click={() => copy(r.reply)}>copy</button>
        </div>
        <p class="tone-body">{r.reply}</p>
      </div>
    {/each}
  {:else}
    <p class="placeholder">Generate to see three tone variants side by side.</p>
  {/if}
</div>

<style>
  .tone-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 0.9rem;
  }
  .tone-card {
    background: var(--panel);
    border: 1px solid var(--hairline);
    border-top: 3px solid var(--hairline);
    border-radius: 8px;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    gap: 0.6rem;
  }
  .tone-header { display: flex; justify-content: space-between; align-items: center; }
  .tone-label {
    font-family: 'JetBrains Mono', monospace; font-size: 0.7rem;
    letter-spacing: 0.08em; text-transform: uppercase; color: var(--ink-dim);
  }
  .copy-btn {
    background: none; border: 1px solid var(--hairline); color: var(--ink-dim);
    font-family: 'JetBrains Mono', monospace; font-size: 0.65rem; letter-spacing: 0.05em;
    border-radius: 4px; padding: 0.2rem 0.5rem; cursor: pointer; transition: all 0.15s ease;
  }
  .copy-btn:hover { color: var(--ink-bright); border-color: var(--ink-dim); }
  .tone-body { margin: 0; font-size: 0.9rem; line-height: 1.5; color: var(--ink-bright); white-space: pre-wrap; }
  .placeholder { color: var(--ink-dim); font-size: 0.85rem; grid-column: 1 / -1; }

  .skeleton-line { height: 0.7rem; border-radius: 4px; background: var(--hairline); margin-bottom: 0.5rem; animation: pulse 1.4s ease-in-out infinite; }
  @keyframes pulse { 0%, 100% { opacity: 0.4; } 50% { opacity: 0.9; } }
</style>

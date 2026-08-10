<script>
  export let text = '';
  export let streaming = false;
</script>

<div class="console">
  <div class="console-header">
    <div class="dots"><span></span><span></span><span></span></div>
    <span class="console-title">live reply {streaming ? '· generating' : ''}</span>
  </div>
  <div class="console-body">
    {#if !text && !streaming}
      <span class="placeholder">// the reply will stream in here, token by token</span>
    {:else}
      <span class="console-text">{text}</span>
      {#if streaming}<span class="cursor"></span>{/if}
    {/if}
  </div>
</div>

<style>
  .console {
    background: var(--ink-black);
    border: 1px solid var(--hairline);
    border-radius: 10px;
    overflow: hidden;
  }
  .console-header {
    display: flex; align-items: center; gap: 0.6rem;
    padding: 0.55rem 0.9rem;
    background: rgba(255,255,255,0.03);
    border-bottom: 1px solid var(--hairline);
  }
  .dots { display: flex; gap: 0.3rem; }
  .dots span { width: 8px; height: 8px; border-radius: 50%; background: var(--hairline); }
  .console-title {
    font-family: 'JetBrains Mono', monospace; font-size: 0.7rem;
    color: var(--ink-dim); letter-spacing: 0.05em;
  }
  .console-body {
    padding: 1.1rem;
    min-height: 140px;
    font-family: 'JetBrains Mono', monospace;
    font-size: 0.88rem;
    line-height: 1.6;
    color: var(--signal-cool-bright);
    white-space: pre-wrap;
  }
  .placeholder { color: var(--ink-dim); }
  .cursor {
    display: inline-block; width: 7px; height: 1rem;
    background: var(--signal-warm); margin-left: 2px;
    animation: blink 0.9s steps(1) infinite; vertical-align: -2px;
  }
  @keyframes blink { 50% { opacity: 0; } }
</style>

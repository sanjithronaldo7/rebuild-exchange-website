(() => {
  const panel = document.querySelector('#chatPanel');
  if (!panel) return;
  const launch = document.querySelector('.chat-launch');
  const messages = document.querySelector('#chatMessages');
  const input = document.querySelector('#chatInput');
  const API_BASE = window.REBUILD_API_BASE || 'http://localhost:8080/api';
  let chatSessionId = localStorage.getItem('rebuild-chat-session') || '';

  const prompts = {
    search: 'Use the search box on Discover to enter a material or keyword. Browse by category, sort by price, or select Free to collect.',
    list: 'Choose “List materials” and add a title, category, price, and pickup location. Listings sync with MySQL when signed in.',
    impact: 'The impact totals are calculated based on listed quantities and standard material weights to reflect diverted landfill waste.',
    sign: 'Sign in using your email and password, or create an account with OTP email verification.',
    pickup: 'Buyer and seller coordinate pickup and payment directly. Confirm the material condition and pickup details before travelling.',
    category: 'Current categories include Tiles, Wood, Bricks, Paint, Cement, Electrical, Plumbing, Pipes, and Other.',
    fallback: 'I can help with finding materials, listing surplus, pickup, sign-in, and how impact estimates work. Ask any question!'
  };

  function answerFor(question) {
    const q = question.toLowerCase();
    if (/find|search|browse|looking|material/.test(q)) return prompts.search;
    if (/list|sell|post|surplus/.test(q)) return prompts.list;
    if (/impact|estimate|waste|sustain|carbon/.test(q)) return prompts.impact;
    if (/sign|login|password|account/.test(q)) return prompts.sign;
    if (/pickup|payment|pay|collect|contact/.test(q)) return prompts.pickup;
    if (/categor|type/.test(q)) return prompts.category;
    return prompts.fallback;
  }

  function addMessage(text, kind) {
    const node = document.createElement('div');
    node.className = `chat-message ${kind}-message`;
    node.textContent = text;
    messages.appendChild(node);
    messages.scrollTop = messages.scrollHeight;
  }

  async function send(text) {
    const question = text.trim();
    if (!question) return;
    addMessage(question, 'user');
    input.value = '';

    // Show temporary typing indicator
    const typing = document.createElement('div');
    typing.className = 'chat-message bot-message';
    typing.textContent = '...';
    messages.appendChild(typing);
    messages.scrollTop = messages.scrollHeight;

    try {
      const res = await fetch(`${API_BASE}/chatbot/message`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: question, sessionId: chatSessionId })
      });
      typing.remove();

      if (res.ok) {
        const json = await res.json();
        if (json.data && json.data.reply) {
          addMessage(json.data.reply, 'bot');
          if (json.data.sessionId) {
            chatSessionId = json.data.sessionId;
            localStorage.setItem('rebuild-chat-session', chatSessionId);
          }
          return;
        }
      }
      addMessage(answerFor(question), 'bot');
    } catch (e) {
      typing.remove();
      addMessage(answerFor(question), 'bot');
    }
  }

  function setOpen(open) {
    panel.hidden = !open;
    launch.setAttribute('aria-expanded', String(open));
    launch.classList.toggle('is-open', open);
    if (open) input.focus();
  }

  launch.addEventListener('click', () => setOpen(panel.hidden));
  document.querySelector('.chat-close')?.addEventListener('click', () => setOpen(false));
  document.querySelector('#chatForm')?.addEventListener('submit', e => {
    e.preventDefault();
    send(input.value);
  });
  document.querySelectorAll('[data-prompt]').forEach(button =>
    button.addEventListener('click', () => send(button.dataset.prompt))
  );
  document.addEventListener('click', e => {
    if (e.target.closest('.faq-chat-open')) setOpen(true);
  });
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && !panel.hidden) {
      setOpen(false);
      launch.focus();
    }
  });
})();

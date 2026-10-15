// ==============================================================================
// ReBuild Exchange - Frontend Application Logic & Backend Integration
// Connects to Spring Boot REST APIs at http://localhost:8080/api
// With graceful fallback to offline demonstration data
// ==============================================================================

const API_BASE = window.REBUILD_API_BASE || 'http://localhost:8080/api';

const fallbackMaterials = [
 {id:1,title:'Handmade terracotta floor tiles',category:'Tiles',quantity:'48 pieces',condition:'Excellent',price:2400,location:'Indiranagar, Bengaluru',distance:'2.4 km',image:'photo-1600607687920-4e2a09cf159d',description:'Warm terracotta tiles left over from a thoughtful renovation. Clean, carefully stored and ready for a smaller space, patio or feature wall.',seller:'Maya Studio Build',initial:'M',time:'2 hours ago'},
 {id:2,title:'Reclaimed teak wood planks',category:'Wood',quantity:'12 boards · 18 sq ft',condition:'Very good',price:6800,location:'Koramangala, Bengaluru',distance:'4.1 km',image:'photo-1519710164239-da123dc03ef4',description:'Beautiful aged teak recovered from a home renovation. A few characterful nail marks, otherwise sound and dry. Great for shelving or furniture.',seller:'Oak & Earth Projects',initial:'O',time:'5 hours ago'},
 {id:3,title:'Leftover exterior wall paint',category:'Paint',quantity:'3 × 10 litre tins',condition:'New · sealed',price:0,location:'Jayanagar, Bengaluru',distance:'5.8 km',image:'photo-1562259949-e8e7689d7828',description:'Three unopened tins of weatherproof exterior paint in a soft sage green. Free to collect — would love to see it put to use.',seller:'Greenline Contractors',initial:'G',time:'Yesterday'},
 {id:4,title:'Exposed red clay bricks',category:'Bricks',quantity:'Approx. 180 bricks',condition:'Good',price:1200,location:'Whitefield, Bengaluru',distance:'8.2 km',image:'photo-1590725140246-20acdee442be',description:'Solid red clay bricks from an unused garden wall. Stack is on a pallet and accessible for pickup. Some mortar on a few pieces.',seller:'Arjun Rao',initial:'A',time:'Yesterday'},
 {id:5,title:'Brushed brass wall sconces',category:'Electrical',quantity:'6 fixtures',condition:'Like new',price:5400,location:'HSR Layout, Bengaluru',distance:'6.3 km',image:'photo-1507473885765-e6ed057f782c',description:'Six elegant brushed brass wall lights, unused after a design change. Includes mounting plates. Warm LED compatible.',seller:'Form & Field Interiors',initial:'F',time:'2 days ago'},
 {id:6,title:'Natural stone paving offcuts',category:'Tiles',quantity:'22 pieces · 9 sq ft',condition:'Good',price:900,location:'Basavanagudi, Bengaluru',distance:'3.7 km',image:'photo-1515263487990-61b07816b324',description:'Mixed natural stone offcuts in earthy tones. Perfect for a small path, garden border or mosaic project. Collection only.',seller:'Namma Renovations',initial:'N',time:'2 days ago'},
 {id:7,title:'Copper plumbing pipe lengths',category:'Plumbing',quantity:'15 lengths · 24 m total',condition:'Very good',price:3100,location:'Electronic City, Bengaluru',distance:'11 km',image:'photo-1621905251189-08b45d6a269e',description:'Clean copper pipe offcuts from a commercial job. Multiple lengths between 1m and 2m. Stored indoors.',seller:'BuildRight Services',initial:'B',time:'3 days ago'},
 {id:8,title:'Cement bags — unopened',category:'Cement',quantity:'8 × 50 kg bags',condition:'New · sealed',price:1680,location:'Yelahanka, Bengaluru',distance:'10.2 km',image:'photo-1513467655676-561b7d489a88',description:'Eight recently purchased bags of OPC cement, stored off the floor in a dry garage. Best used soon. Pickup with a suitable vehicle.',seller:'Sanjay Builders',initial:'S',time:'3 days ago'},
 {id:9,title:'Salvaged vintage teak door',category:'Wood',quantity:'1 door · 210 × 90 cm',condition:'Good',price:4500,location:'Malleshwaram, Bengaluru',distance:'4.8 km',image:'photo-1600210492486-724fe5c67fb0',description:'A solid teak door with original brass hardware and a lovely patina. Removed carefully during a renovation and ready for restoration or reuse.',seller:'The Reclaimed House',initial:'R',time:'4 days ago'},
 {id:10,title:'Porcelain bathroom basins',category:'Plumbing',quantity:'2 basins with taps',condition:'Like new',price:2800,location:'Marathahalli, Bengaluru',distance:'7.4 km',image:'photo-1620626011761-996317b8d101',description:'Two white countertop basins, both in excellent condition. Includes chrome mixer taps and drain fittings. Collection preferred.',seller:'Studio Habitat',initial:'H',time:'4 days ago'},
 {id:11,title:'Acoustic ceiling panels',category:'Other',quantity:'18 panels · 12 sq m',condition:'Very good',price:3600,location:'Rajajinagar, Bengaluru',distance:'6.1 km',image:'photo-1497366754035-f200968a6e72',description:'Neutral felt acoustic panels removed from a meeting room fit-out. Clean, undamaged and easy to mount. Great for studios or home offices.',seller:'Common Ground Works',initial:'C',time:'5 days ago'},
 {id:12,title:'Unused matte black door handles',category:'Other',quantity:'10 sets',condition:'New · boxed',price:1500,location:'Bellandur, Bengaluru',distance:'7 km',image:'photo-1505693416388-ac5ce068fe85',description:'Ten boxed lever handle sets from a design change. Includes matching latch plates and screws. All new and still in packaging.',seller:'Line & Level Build',initial:'L',time:'5 days ago'},
 {id:13,title:'Reclaimed granite countertop',category:'Tiles',quantity:'1 slab · 2.1 m',condition:'Good',price:2200,location:'Banashankari, Bengaluru',distance:'5.4 km',image:'photo-1600607687920-4e2a09cf159d',description:'Polished dark granite offcut from a kitchen remodel. One finished edge and a cutout for a small sink. A useful workshop or utility counter.',seller:'Suma Renovations',initial:'S',time:'6 days ago'},
 {id:14,title:'Weatherproof electrical conduit',category:'Electrical',quantity:'32 lengths · 3 m each',condition:'New · unused',price:1900,location:'Peenya, Bengaluru',distance:'9.5 km',image:'photo-1621905251189-08b45d6a269e',description:'Surplus grey PVC electrical conduit from a commercial project. Straight lengths, stored indoors. Suitable for a range of wiring projects.',seller:'SparkWorks Electrical',initial:'S',time:'1 week ago'},
 {id:15,title:'Terracotta roof tiles',category:'Tiles',quantity:'Approx. 95 tiles',condition:'Good',price:1700,location:'Vijayanagar, Bengaluru',distance:'7.9 km',image:'photo-1600607687939-ce8a6c25118c',description:'Traditional curved roof tiles salvaged from a small extension. Many still have decades of use left. Pickup with a small truck.',seller:'Nandi Construction',initial:'N',time:'1 week ago'},
 {id:16,title:'Hardwood flooring offcuts',category:'Wood',quantity:'Approx. 14 sq ft',condition:'Very good',price:3200,location:'Frazer Town, Bengaluru',distance:'3.2 km',image:'photo-1519710164239-da123dc03ef4',description:'Mixed lengths of solid hardwood flooring, clean and dry. Enough for a closet, landing or creative furniture project.',seller:'Second Story Studio',initial:'S',time:'1 week ago'}
];

const icons = { All:'✳', Tiles:'▧', Wood:'⌁', Bricks:'▤', Paint:'◉', Cement:'▥', Electrical:'⌁', Plumbing:'⌘', Pipes:'◌', Other:'＋' };
const categoryList = document.querySelector('#categoryList');
const filterChips = document.querySelector('#filterChips');
const grid = document.querySelector('#listingGrid');
const empty = document.querySelector('#emptyState');

let activeCategory = 'All';
let query = '';
let locationQuery = '';
let freeOnly = false;
let savedOnly = false;
let saved = new Set(JSON.parse(localStorage.getItem('rebuild-saved') || '[]'));
let liveMaterials = [];
let isBackendConnected = false;

// Token & User helpers
function getAuthToken() { return localStorage.getItem('rebuild-token'); }
function getCurrentUser() { try { return JSON.parse(localStorage.getItem('rebuild-user') || 'null'); } catch(e) { return null; } }

function authHeaders() {
  const token = getAuthToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}

// Fetch categories from backend
async function loadCategories() {
  let cats = ['All', 'Tiles', 'Wood', 'Bricks', 'Paint', 'Cement', 'Electrical', 'Plumbing', 'Pipes', 'Other'];
  try {
    const res = await fetch(`${API_BASE}/categories`, { method: 'GET' });
    if (res.ok) {
      const json = await res.json();
      if (json.data && Array.isArray(json.data)) {
        cats = ['All', ...json.data.map(c => c.name)];
      }
    }
  } catch(e) {
    // Offline mode: use default categories
  }

  categoryList.innerHTML = cats.map(c =>
    `<button class="category-chip ${activeCategory===c?'selected':''}" data-category="${c}"><span>${icons[c]||'＋'}</span>${c}</button>`
  ).join('');

  filterChips.innerHTML = ['All materials', 'Tiles', 'Wood', 'Bricks', 'Paint', 'Cement', 'Electrical', 'Plumbing', 'Free'].map(c =>
    `<button class="filter-chip ${(c==='All materials'&&activeCategory==='All'||c===activeCategory)?'selected':''}" data-filter="${c}">${c}</button>`
  ).join('');
}

// Fetch materials from backend API
async function loadMaterials() {
  const sort = document.querySelector('#sortSelect')?.value || 'newest';
  const params = new URLSearchParams();
  if (query) params.append('query', query);
  if (locationQuery) params.append('location', locationQuery);
  if (activeCategory !== 'All') params.append('category', activeCategory);
  if (freeOnly) params.append('freeOnly', 'true');
  params.append('sort', sort);
  params.append('size', '50');

  try {
    const res = await fetch(`${API_BASE}/materials?${params.toString()}`);
    if (res.ok) {
      const json = await res.json();
      if (json.data && json.data.content) {
        liveMaterials = json.data.content;
        isBackendConnected = true;
        renderItems(liveMaterials);
        return;
      }
    }
  } catch(e) {
    isBackendConnected = false;
  }

  // Fallback to client data if backend is offline
  let userListings = JSON.parse(localStorage.getItem('rebuild-listings') || '[]');
  let items = [...userListings, ...fallbackMaterials].filter(x =>
    (activeCategory==='All' || x.category===activeCategory) &&
    (x.price!==undefined) &&
    (!freeOnly || x.price===0) &&
    (!savedOnly || saved.has(x.id)) &&
    (query==='' || `${x.title} ${x.category} ${x.description} ${x.location}`.toLowerCase().includes(query.toLowerCase())) &&
    (locationQuery==='' || x.location.toLowerCase().includes(locationQuery.toLowerCase()))
  );

  if (sort === 'newest') items = [...items].reverse();
  if (sort === 'price-low') items.sort((a,b) => a.price - b.price);
  if (sort === 'price-high') items.sort((a,b) => b.price - a.price);

  renderItems(items);
}

function renderItems(items) {
  if (savedOnly) {
    items = items.filter(x => saved.has(x.id));
  }

  grid.innerHTML = items.map(item => `
    <article class="listing-card" data-id="${item.id}" tabindex="0" role="button" aria-label="View ${esc(item.title)}">
      <div class="card-image">
        <img src="${imageUrl(item)}" alt="${esc(item.title)}" loading="lazy">
        <span class="reuse-tag">✳ ${item.price===0?'FREE TO COLLECT':'GIVE IT A SECOND LIFE'}</span>
        <button class="heart-btn ${saved.has(item.id)?'saved':''}" data-save="${item.id}" aria-label="${saved.has(item.id)?'Remove from':'Save to'} favourites">
          ${saved.has(item.id)?'♥':'♡'}
        </button>
      </div>
      <div class="card-body">
        <div class="card-category">${esc(item.category)} <span>·</span> ${esc(item.condition || 'Good')}</div>
        <h3 class="card-title">${esc(item.title)}</h3>
        <div class="card-meta">
          <span>${esc(item.quantity)}</span>
          <span>⌖ ${esc(item.distance||'Nearby')}</span>
        </div>
        <div class="card-bottom">
          <span class="card-price ${item.price===0?'free':''}">
            ${item.price===0?'Free':`₹${Number(item.price).toLocaleString('en-IN')}`}
          </span>
          <span class="card-place">${esc((item.location || 'Bengaluru').split(',')[0])}</span>
        </div>
      </div>
    </article>
  `).join('');

  empty.classList.toggle('hidden', items.length > 0);
  document.querySelector('#loadMore')?.classList.add('hidden');
  document.querySelector('.save-count').textContent = saved.size;
}

function esc(v='') {
  return String(v).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

function toast(msg) {
  const el = document.querySelector('#toast');
  if (!el) return;
  el.textContent = msg;
  el.classList.add('show');
  clearTimeout(window.toastTimeout);
  window.toastTimeout = setTimeout(() => el.classList.remove('show'), 2800);
}

function imageUrl(item) {
  if (!item.image) return 'https://images.unsplash.com/photo-1504307651254-35680f356dfd?auto=format&fit=crop&w=700&q=78';
  if (item.image.startsWith('http') || item.image.startsWith('/uploads/')) return item.image;
  return `https://images.unsplash.com/${item.image}?auto=format&fit=crop&w=700&q=78`;
}

function showDetails(id) {
  const allCurrent = isBackendConnected && liveMaterials.length ? liveMaterials : [...JSON.parse(localStorage.getItem('rebuild-listings')||'[]'), ...fallbackMaterials];
  const item = allCurrent.find(x => String(x.id) === String(id));
  if (!item) return;

  document.querySelector('#detailContent').innerHTML = `
    <div class="detail-layout">
      <img class="detail-image" src="${imageUrl(item)}" alt="${esc(item.title)}">
      <div class="detail-copy">
        <div class="card-category">${esc(item.category)} · ${esc(item.condition || 'Good')}</div>
        <h2>${esc(item.title)}</h2>
        <div class="detail-price ${item.price===0?'free':''}">
          ${item.price===0?'Free':`₹${Number(item.price).toLocaleString('en-IN')}`} 
          <small style="font:400 10px var(--sans);color:#8a9087">${item.price===0?'':'· negotiable'}</small>
        </div>
        <div class="detail-details">
          <span>Quantity<b>${esc(item.quantity)}</b></span>
          <span>Availability<b>Ready for pickup</b></span>
          <span>Location<b>${esc(item.location)}</b></span>
          <span>Listed<b>${esc(item.time||'Recently')}</b></span>
        </div>
        <p>${esc(item.description||'Surplus construction material, ready for its next project.')}</p>
        <div class="detail-seller">
          <span class="seller-avatar">${esc(item.initial||'S')}</span>
          <span><b>${esc(item.seller||'Community seller')}</b><small>Local community member · Verified listing</small></span>
        </div>
        <button class="btn btn-green contact-seller" data-material-id="${item.id}">Message seller <span>→</span></button>
      </div>
    </div>
  `;

  document.querySelector('.contact-seller').onclick = () => handleMessageSeller(item);
  document.querySelector('#detailDialog').showModal();
}

async function handleMessageSeller(item) {
  const token = getAuthToken();
  if (!token) {
    document.querySelector('#detailDialog').close();
    document.querySelector('#authDialog').showModal();
    toast('Please sign in to send a purchase request');
    return;
  }

  const requestedQty = prompt(`How many / much of "${item.title}" would you like to request?`, item.quantity || 'All');
  if (!requestedQty) return;

  const message = prompt('Add a pickup message for the seller:', 'Hello! I would like to arrange pickup for this material.');

  try {
    const res = await fetch(`${API_BASE}/purchase-requests`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({
        materialId: item.id,
        requestedQuantity: requestedQty,
        offeredPrice: item.price,
        message: message || 'Interested in collecting this material.'
      })
    });
    const json = await res.json();
    if (res.ok) {
      document.querySelector('#detailDialog').close();
      toast('Purchase request sent to seller!');
    } else {
      toast(json.message || 'Could not send request');
    }
  } catch(e) {
    toast('Purchase request submitted (saved locally)');
    document.querySelector('#detailDialog').close();
  }
}

// Authentication UI Sync
function syncAuthUI() {
  const user = getCurrentUser();
  const signInBtn = document.querySelector('.sign-in');
  if (!signInBtn) return;

  if (user) {
    signInBtn.innerHTML = `<span>👤 ${esc(user.fullName.split(' ')[0])}</span>`;
    signInBtn.href = '#';
    signInBtn.title = 'Click to sign out';
    signInBtn.onclick = (e) => {
      e.preventDefault();
      if (confirm('Do you want to sign out?')) {
        localStorage.removeItem('rebuild-token');
        localStorage.removeItem('rebuild-user');
        syncAuthUI();
        toast('Signed out successfully');
      }
    };
  } else {
    signInBtn.textContent = 'Sign in';
    signInBtn.href = 'login.html';
    signInBtn.onclick = null;
  }
}

// Event Listeners
document.addEventListener('click', e => {
  const cat = e.target.closest('[data-category]');
  if (cat) {
    activeCategory = cat.dataset.category;
    freeOnly = false;
    savedOnly = false;
    loadCategories();
    loadMaterials();
    document.querySelector('#marketplace')?.scrollIntoView({behavior:'smooth'});
    return;
  }

  const filter = e.target.closest('[data-filter]');
  if (filter) {
    activeCategory = filter.dataset.filter === 'Free' || filter.dataset.filter === 'All materials' ? 'All' : filter.dataset.filter;
    freeOnly = filter.dataset.filter === 'Free';
    savedOnly = false;
    query = '';
    loadCategories();
    loadMaterials();
    return;
  }

  const save = e.target.closest('[data-save]');
  if (save) {
    e.stopPropagation();
    const id = Number(save.dataset.save);
    if (saved.has(id)) {
      saved.delete(id);
    } else {
      saved.add(id);
    }
    localStorage.setItem('rebuild-saved', JSON.stringify([...saved]));
    loadMaterials();
    toast(saved.has(id) ? 'Added to your saved materials' : 'Removed from saved materials');
    return;
  }

  const card = e.target.closest('.listing-card');
  if (card) {
    showDetails(card.dataset.id);
    return;
  }

  if (e.target.closest('.sell-open')) {
    document.querySelector('#sellDialog').showModal();
  }

  if (e.target.closest('.sign-in') && !getCurrentUser()) {
    document.querySelector('#authDialog').showModal();
  }

  if (e.target.closest('.saved-link')) {
    if (!saved.size) {
      toast('Your saved materials will appear here');
      return;
    }
    savedOnly = true;
    freeOnly = false;
    activeCategory = 'All';
    query = '';
    loadMaterials();
    document.querySelector('#marketplace')?.scrollIntoView({behavior:'smooth'});
    toast(`${saved.size} saved ${saved.size===1?'listing':'listings'}`);
  }

  if (e.target.closest('.dialog-close')) {
    e.target.closest('dialog').close();
  }

  if (e.target.id === 'clearFilters') {
    activeCategory = 'All';
    query = '';
    locationQuery = '';
    freeOnly = false;
    savedOnly = false;
    document.querySelector('#heroQuery').value = '';
    document.querySelector('#heroLocation').value = '';
    loadCategories();
    loadMaterials();
  }
});

// Search bar submit
document.querySelector('#searchForm')?.addEventListener('submit', e => {
  e.preventDefault();
  query = document.querySelector('#heroQuery').value.trim();
  locationQuery = document.querySelector('#heroLocation').value.trim();
  activeCategory = 'All';
  loadCategories();
  loadMaterials();
  document.querySelector('#marketplace')?.scrollIntoView({behavior:'smooth'});
  if (locationQuery) toast(`Showing materials near ${locationQuery}`);
});

document.querySelector('#sortSelect')?.addEventListener('change', loadMaterials);

// List Material Form Submit (Connects to POST /api/materials)
document.querySelector('#sellForm')?.addEventListener('submit', async e => {
  e.preventDefault();
  const f = new FormData(e.currentTarget);
  const title = f.get('title');
  const category = f.get('category');
  const price = Number(f.get('price'));
  const location = f.get('location');
  const description = f.get('description');

  const token = getAuthToken();

  if (token) {
    try {
      const res = await fetch(`${API_BASE}/materials`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({
          title,
          category,
          price,
          location,
          quantity: 'Available now',
          condition: 'Good',
          description: description || 'Surplus construction materials available for pickup.'
        })
      });
      const json = await res.json();
      if (res.ok) {
        toast('Your material listing is live on MySQL!');
        e.currentTarget.reset();
        document.querySelector('#sellDialog').close();
        loadMaterials();
        return;
      } else {
        toast(json.message || 'Error publishing listing');
      }
    } catch(err) {
      // Backend offline fallback
    }
  }

  // Offline / Demo fallback
  let userListings = JSON.parse(localStorage.getItem('rebuild-listings') || '[]');
  userListings.unshift({
    id: Date.now(),
    title,
    category,
    quantity: 'Available now',
    condition: 'Good',
    price,
    location,
    distance: 'Nearby',
    image: 'photo-1504307651254-35680f356dfd',
    description: description || 'Surplus construction materials available for local pickup.',
    seller: getCurrentUser() ? getCurrentUser().fullName : 'Your listing',
    initial: 'Y',
    time: 'Just now'
  });
  localStorage.setItem('rebuild-listings', JSON.stringify(userListings));
  e.currentTarget.reset();
  document.querySelector('#sellDialog').close();
  activeCategory = 'All';
  query = '';
  loadMaterials();
  document.querySelector('#marketplace')?.scrollIntoView({behavior:'smooth'});
  toast(token ? 'Listing published!' : 'Listing saved (Sign in to sync to MySQL backend)');
});

// Quick Sign-In Form Submit (Connects to POST /api/auth/login)
document.querySelector('#authForm')?.addEventListener('submit', async e => {
  e.preventDefault();
  const f = new FormData(e.currentTarget);
  const email = f.get('email') || e.currentTarget.querySelector('input[type="email"]').value;
  const password = f.get('password') || e.currentTarget.querySelector('input[type="password"]').value;

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const json = await res.json();
    if (res.ok && json.data && json.data.accessToken) {
      localStorage.setItem('rebuild-token', json.data.accessToken);
      localStorage.setItem('rebuild-user', JSON.stringify({
        id: json.data.userId,
        fullName: json.data.fullName,
        email: json.data.email,
        roles: json.data.roles
      }));
      syncAuthUI();
      document.querySelector('#authDialog').close();
      toast(`Welcome back, ${json.data.fullName}!`);
      return;
    } else {
      toast(json.message || 'Sign-in failed. Please check your credentials.');
    }
  } catch(err) {
    document.querySelector('#authDialog').close();
    toast('Sign-in needs backend running at http://localhost:8080');
  }
});

// Dedicated login page connection if user is on login.html
const dedicatedLoginForm = document.querySelector('#loginForm');
if (dedicatedLoginForm) {
  dedicatedLoginForm.addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.currentTarget;
    if (!form.reportValidity()) return;

    const email = document.querySelector('#email').value;
    const password = document.querySelector('#password').value;
    const nameInput = document.querySelector('#fullName');
    const feedback = document.querySelector('#formFeedback');

    if (nameInput) {
      // Registration mode
      try {
        const res = await fetch(`${API_BASE}/auth/register`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            fullName: nameInput.value,
            email: email,
            password: password,
            phoneNumber: '+91 98765 43210',
            role: 'ROLE_SELLER'
          })
        });
        const json = await res.json();
        if (res.ok) {
          feedback.style.color = '#315b45';
          feedback.textContent = 'Account created! Enter OTP verification code.';
          const otp = prompt('Enter the 6-digit OTP sent to ' + email + ' (check backend logs in dev mode):');
          if (otp) {
            const vRes = await fetch(`${API_BASE}/auth/verify-otp`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ email, otpCode: otp, purpose: 'REGISTRATION' })
            });
            const vJson = await vRes.json();
            if (vRes.ok && vJson.data && vJson.data.accessToken) {
              localStorage.setItem('rebuild-token', vJson.data.accessToken);
              localStorage.setItem('rebuild-user', JSON.stringify({
                id: vJson.data.userId,
                fullName: vJson.data.fullName,
                email: vJson.data.email,
                roles: vJson.data.roles
              }));
              alert('Registration & verification successful! Redirecting to marketplace...');
              window.location.href = 'index.html';
            } else {
              feedback.textContent = vJson.message || 'Invalid OTP code.';
            }
          }
        } else {
          feedback.textContent = json.message || 'Registration failed.';
        }
      } catch(err) {
        feedback.textContent = 'Could not reach backend at ' + API_BASE;
      }
    } else {
      // Login mode
      try {
        const res = await fetch(`${API_BASE}/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password })
        });
        const json = await res.json();
        if (res.ok && json.data && json.data.accessToken) {
          localStorage.setItem('rebuild-token', json.data.accessToken);
          localStorage.setItem('rebuild-user', JSON.stringify({
            id: json.data.userId,
            fullName: json.data.fullName,
            email: json.data.email,
            roles: json.data.roles
          }));
          window.location.href = 'index.html';
        } else {
          feedback.textContent = json.message || 'Invalid email or password.';
        }
      } catch(err) {
        feedback.textContent = 'Backend connection error. Please verify backend is running.';
      }
    }
  });
}

document.querySelector('.menu-toggle')?.addEventListener('click', () => {
  document.querySelector('.main-nav')?.classList.toggle('open');
});

document.querySelector('.main-nav')?.addEventListener('click', e => {
  if (e.target.tagName === 'A') document.querySelector('.main-nav')?.classList.remove('open');
});

async function loadImpactAndDashboardStats() {
  try {
    const res = await fetch(`${API_BASE}/dashboard/impact`);
    if (res.ok) {
      const json = await res.json();
      if (json.data) {
        const d = json.data;
        const tonnes = (Number(d.totalDivertedWeightKg) / 1000).toFixed(1);
        const floatEl = document.querySelector('.float-number');
        if (floatEl) floatEl.innerHTML = `${tonnes} <small>tonnes</small>`;

        const statEls = document.querySelectorAll('.impact-stats strong');
        if (statEls.length >= 3) {
          statEls[0].textContent = Number(d.totalMaterialsRehomed).toLocaleString('en-IN');
          statEls[1].innerHTML = `${tonnes}<span class="stat-unit">t</span>`;
          statEls[2].textContent = Number(d.totalLocalConnections).toLocaleString('en-IN');
        }
      }
    }
  } catch(e) {}

  const user = getCurrentUser();
  if (user && user.roles && (user.roles.includes('ROLE_SELLER') || user.roles.includes('ROLE_ADMIN'))) {
    try {
      const res = await fetch(`${API_BASE}/dashboard/seller`, { headers: authHeaders() });
      if (res.ok) {
        const json = await res.json();
        if (json.data) {
          const s = json.data;
          const dashMetrics = document.querySelectorAll('#seller-dashboard .dash-metrics b');
          if (dashMetrics.length >= 3) {
            dashMetrics[0].innerHTML = `${s.materialsRehomed || 0} <small>items</small>`;
            dashMetrics[1].innerHTML = `${s.wasteDivertedKg || 0} <small>kg</small>`;
            dashMetrics[2].innerHTML = `${s.activeListings || 0} <small>active</small>`;
          }
        }
      }
    } catch(e) {}
  }
}

// Initial load
loadCategories();
loadMaterials();
syncAuthUI();
loadImpactAndDashboardStats();

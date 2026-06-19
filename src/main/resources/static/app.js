const api = (path) => `/api${path}`;

const state = {
  view: 'dashboard',
  data: {},
  search: '',
  sidebarCollapsed: false,
  charts: {}
};

const views = {
  dashboard: { title: 'Dashboard', kicker: 'Estoque' },
  marmitas: { title: 'Marmitas fisicas', kicker: 'Unidades' },
  receitas: { title: 'Receitas', kicker: 'Cardapio' },
  lotes: { title: 'Lotes de producao', kicker: 'Validade' },
  categorias: { title: 'Categorias', kicker: 'Organizacao' },
  ingredientes: { title: 'Ingredientes', kicker: 'Insumos' },
  locais: { title: 'Locais de armazenamento', kicker: 'Congelador' },
  consumidores: { title: 'Consumidores', kicker: 'Historico' }
};

const configs = {
  categorias: {
    endpoint: '/categorias',
    columns: [['nome', 'Nome'], ['descricao', 'Descricao']],
    fields: [
      { name: 'nome', label: 'Nome', required: true },
      { name: 'descricao', label: 'Descricao' }
    ]
  },
  ingredientes: {
    endpoint: '/ingredientes',
    columns: [['nome', 'Nome'], ['unidade_medida', 'Unidade']],
    fields: [
      { name: 'nome', label: 'Nome', required: true },
      { name: 'unidadeMedida', label: 'Unidade de medida', required: true, placeholder: 'g, ml, un' }
    ]
  },
  consumidores: {
    endpoint: '/consumidores',
    columns: [['nome', 'Nome']],
    fields: [{ name: 'nome', label: 'Nome', required: true }]
  },
  locais: {
    endpoint: '/locais',
    columns: [['nome', 'Nome'], ['descricao', 'Descricao']],
    fields: [
      { name: 'nome', label: 'Nome', required: true },
      { name: 'descricao', label: 'Descricao' }
    ]
  }
};

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.nav-item').forEach((button) => {
    button.addEventListener('click', () => navigate(button.dataset.view));
  });
  document.getElementById('refresh').addEventListener('click', load);
  document.getElementById('sidebar-toggle').addEventListener('click', toggleSidebar);
  document.getElementById('search').addEventListener('input', (event) => {
    state.search = event.target.value.toLowerCase();
    render();
  });
  decorate();
  load();
});

async function request(path, options = {}) {
  const response = await fetch(api(path), {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message || 'Falha na requisicao.');
  }
  if (response.status === 204) return null;
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

async function load() {
  try {
    const common = [
      request('/categorias'),
      request('/ingredientes'),
      request('/receitas'),
      request('/lotes'),
      request('/locais'),
      request('/consumidores'),
      request('/marmitas'),
      request('/dashboard')
    ];
    const [categorias, ingredientes, receitas, lotes, locais, consumidores, marmitas, dashboard] = await Promise.all(common);
    state.data = { categorias, ingredientes, receitas, lotes, locais, consumidores, marmitas, dashboard };
    render();
    decorate();
  } catch (error) {
    showToast(error.message);
  }
}

function navigate(view) {
  state.view = view;
  document.getElementById('search').value = '';
  state.search = '';
  render();
}

function render() {
  const meta = views[state.view];
  document.getElementById('view-title').textContent = meta.title;
  document.getElementById('view-kicker').textContent = meta.kicker;
  document.querySelectorAll('.nav-item').forEach((item) => item.classList.toggle('active', item.dataset.view === state.view));

  const app = document.getElementById('app');
  if (state.view === 'dashboard') app.innerHTML = dashboardView();
  else if (state.view === 'receitas') app.innerHTML = receitasView();
  else if (state.view === 'lotes') app.innerHTML = lotesView();
  else if (state.view === 'marmitas') app.innerHTML = marmitasView();
  else app.innerHTML = simpleCrudView(state.view);

  bindForms();
  if (state.view === 'dashboard') {
    renderDashboardCharts();
  } else {
    destroyCharts();
  }
  decorate();
}

function toggleSidebar() {
  state.sidebarCollapsed = !state.sidebarCollapsed;
  document.body.classList.toggle('sidebar-collapsed', state.sidebarCollapsed);
  const toggle = document.getElementById('sidebar-toggle');
  const isExpanded = !state.sidebarCollapsed;
  toggle.title = isExpanded ? 'Recolher menu' : 'Expandir menu';
  toggle.setAttribute('aria-label', toggle.title);
  toggle.setAttribute('aria-expanded', String(isExpanded));
  toggle.innerHTML = `<i data-lucide="${isExpanded ? 'panel-left-close' : 'panel-left-open'}"></i>`;
  decorate();
  window.setTimeout(() => {
    Object.values(state.charts).forEach((chart) => chart.resize());
  }, 260);
}

function dashboardView() {
  const d = state.data.dashboard || {};
  const cards = [
    ['Disponiveis', d.disponiveis || 0, 'package-check', 'text-emerald-300'],
    ['Proximas do vencimento', d.proximas || 0, 'alarm-clock', 'text-amber-300'],
    ['Vencidas', d.vencidas || 0, 'triangle-alert', 'text-red-300'],
    ['Consumidas', d.consumidas || 0, 'utensils', 'text-cyan-300'],
    ['Descartadas', d.descartadas || 0, 'trash-2', 'text-zinc-300'],
    ['Total registrado', d.total || 0, 'database', 'text-violet-300']
  ];
  const categoryRows = (d.porCategoria || []).map((item) => `
    <div class="flex items-center justify-between border-b border-zinc-800 py-3 last:border-b-0">
      <span class="text-sm text-zinc-300">${escapeHtml(item.nome)}</span>
      <span class="rounded-md bg-cyan-400/10 px-2 py-1 text-xs text-cyan-200">${item.total}</span>
    </div>`).join('');
  const recentRows = (d.recentes || []).map((item) => `
    <tr class="border-b border-zinc-800 last:border-0">
      <td class="py-3 text-zinc-300">#${item.id_marmita}</td>
      <td class="py-3">${escapeHtml(item.receita)}</td>
      <td class="py-3"><span class="status">${escapeHtml(item.tipo)}</span></td>
      <td class="py-3 text-zinc-400">${formatDate(item.data_movimentacao)}</td>
    </tr>`).join('');

  return `
    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      ${cards.map(([label, value, icon, color]) => `
        <article class="rounded-lg border border-zinc-800 bg-zinc-900/70 p-5 shadow-xl shadow-black/20">
          <div class="flex items-center justify-between">
            <p class="text-sm text-zinc-400">${label}</p>
            <i data-lucide="${icon}" class="h-5 w-5 ${color}"></i>
          </div>
          <p class="mt-4 text-4xl font-semibold text-white">${value}</p>
        </article>`).join('')}
    </div>
    <div class="mt-5 grid gap-5 xl:grid-cols-[0.8fr_1.2fr]">
      <section class="rounded-lg border border-zinc-800 bg-zinc-900/60 p-5">
        <div class="mb-4 flex items-center justify-between">
          <h2 class="text-base font-semibold">Status do estoque</h2>
          <span class="text-xs uppercase text-zinc-500">pizza</span>
        </div>
        <div class="chart-box">
          <canvas id="status-chart"></canvas>
        </div>
      </section>
      <section class="rounded-lg border border-zinc-800 bg-zinc-900/60 p-5">
        <div class="mb-4 flex items-center justify-between">
          <h2 class="text-base font-semibold">Marmitas por categoria</h2>
          <span class="text-xs uppercase text-zinc-500">barras</span>
        </div>
        <div class="chart-box wide">
          <canvas id="category-chart"></canvas>
        </div>
      </section>
    </div>
    <div class="mt-5 grid gap-5 xl:grid-cols-[0.85fr_1.15fr]">
      <section class="rounded-lg border border-zinc-800 bg-zinc-900/60 p-5">
        <h2 class="text-base font-semibold">Disponiveis por categoria</h2>
        <div class="mt-3">${categoryRows || emptyState('Sem categorias cadastradas.')}</div>
      </section>
      <section class="rounded-lg border border-zinc-800 bg-zinc-900/60 p-5">
        <h2 class="text-base font-semibold">Movimentacoes recentes</h2>
        <div class="mt-3 overflow-x-auto">
          <table class="w-full min-w-[520px] text-left text-sm">
            <thead class="text-xs uppercase text-zinc-500"><tr><th class="py-2">Marmita</th><th>Receita</th><th>Tipo</th><th>Data</th></tr></thead>
            <tbody>${recentRows || `<tr><td colspan="4">${emptyState('Nenhuma movimentacao registrada.')}</td></tr>`}</tbody>
          </table>
        </div>
      </section>
    </div>`;
}

function renderDashboardCharts() {
  if (typeof Chart === 'undefined') {
    return;
  }
  destroyCharts();
  const d = state.data.dashboard || {};
  const statusCanvas = document.getElementById('status-chart');
  const categoryCanvas = document.getElementById('category-chart');
  if (!statusCanvas || !categoryCanvas) {
    return;
  }

  const textColor = '#d4d4d8';
  const gridColor = 'rgba(63, 63, 70, 0.55)';
  Chart.defaults.color = textColor;
  Chart.defaults.font.family = 'Inter, ui-sans-serif, system-ui, sans-serif';

  state.charts.status = new Chart(statusCanvas, {
    type: 'doughnut',
    data: {
      labels: ['Disponiveis', 'Consumidas', 'Descartadas'],
      datasets: [{
        data: [d.disponiveis || 0, d.consumidas || 0, d.descartadas || 0],
        backgroundColor: ['#34d399', '#38bdf8', '#f87171'],
        borderColor: '#18181b',
        borderWidth: 3,
        hoverOffset: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '62%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: { boxWidth: 10, boxHeight: 10, padding: 16 }
        }
      }
    }
  });

  const categories = (d.porCategoria || []).slice(0, 10);
  state.charts.category = new Chart(categoryCanvas, {
    type: 'bar',
    data: {
      labels: categories.map((item) => item.nome),
      datasets: [{
        label: 'Disponiveis',
        data: categories.map((item) => item.total),
        backgroundColor: '#22d3ee',
        borderColor: '#67e8f9',
        borderWidth: 1,
        borderRadius: 6,
        maxBarThickness: 44
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          grid: { display: false },
          ticks: { maxRotation: 35, minRotation: 0 }
        },
        y: {
          beginAtZero: true,
          grid: { color: gridColor },
          ticks: { precision: 0 }
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (context) => `${context.parsed.y} marmitas disponiveis`
          }
        }
      }
    }
  });
}

function destroyCharts() {
  Object.values(state.charts).forEach((chart) => chart?.destroy());
  state.charts = {};
}

function simpleCrudView(name) {
  const config = configs[name];
  const rows = filtered(state.data[name] || []);
  return `
    <div class="grid gap-5 xl:grid-cols-[380px_1fr]">
      ${formPanel(name, config.fields)}
      ${tablePanel(name, config.columns, rows)}
    </div>`;
}

function receitasView() {
  const rows = filtered(state.data.receitas || []);
  return `
    <div class="grid gap-5 xl:grid-cols-[420px_1fr]">
      ${formPanel('receitas', [
        { name: 'nome', label: 'Nome', required: true },
        { name: 'idCategoria', label: 'Categoria', type: 'select', options: state.data.categorias || [], required: true },
        { name: 'idIngrediente', label: 'Ingrediente principal', type: 'select', options: state.data.ingredientes || [] },
        { name: 'quantidade', label: 'Quantidade do ingrediente', type: 'number', step: '0.01', placeholder: 'Ex: 150' },
        { name: 'descricao', label: 'Descricao' },
        { name: 'modoPreparo', label: 'Modo de preparo', type: 'textarea' }
      ])}
      ${tablePanel('receitas', [['nome', 'Nome'], ['categoria', 'Categoria'], ['descricao', 'Descricao']], rows)}
    </div>`;
}

function lotesView() {
  const rows = filtered(state.data.lotes || []);
  return `
    <div class="grid gap-5 xl:grid-cols-[380px_1fr]">
      ${formPanel('lotes', [
        { name: 'idReceita', label: 'Receita', type: 'select', options: state.data.receitas || [], required: true },
        { name: 'dataPreparo', label: 'Data de preparo', type: 'date', required: true },
        { name: 'dataValidade', label: 'Data de validade', type: 'date', required: true }
      ])}
      ${tablePanel('lotes', [['receita', 'Receita'], ['data_preparo', 'Preparo'], ['data_validade', 'Validade'], ['marmitas', 'Unidades']], rows)}
    </div>`;
}

function marmitasView() {
  const rows = filtered(state.data.marmitas || []);
  const tableRows = rows.map((row) => `
    <tr class="row">
      <td>#${row.id}</td>
      <td>${escapeHtml(row.receita)}</td>
      <td>${escapeHtml(row.local)}</td>
      <td>${formatDate(row.data_validade)}</td>
      <td><span class="status ${statusClass(row.situacao)}">${escapeHtml(row.situacao)}</span></td>
      <td class="text-right">
        <button class="table-btn" data-consume="${row.id}" ${row.status !== 'DISPONIVEL' ? 'disabled' : ''}>Consumir</button>
        <button class="table-btn danger" data-discard="${row.id}" ${row.status !== 'DISPONIVEL' ? 'disabled' : ''}>Descartar</button>
        <button class="table-btn danger" data-delete="/marmitas/${row.id}">Excluir</button>
      </td>
    </tr>`).join('');

  return `
    <div class="grid gap-5 xl:grid-cols-[380px_1fr]">
      ${formPanel('marmitas', [
        { name: 'idLote', label: 'Lote', type: 'select', options: state.data.lotes || [], labelKey: 'receita', required: true },
        { name: 'idLocal', label: 'Local', type: 'select', options: state.data.locais || [], required: true },
        { name: 'status', label: 'Status', type: 'select-static', options: ['DISPONIVEL', 'CONSUMIDA', 'DESCARTADA'], required: true }
      ])}
      <section class="rounded-lg border border-zinc-800 bg-zinc-900/60 p-5">
        <div class="mb-4 flex items-center justify-between">
          <h2 class="font-semibold">Estoque fisico</h2>
          <span class="text-sm text-zinc-500">${rows.length} registros</span>
        </div>
        <div class="overflow-x-auto">
          <table class="data-table">
            <thead><tr><th>ID</th><th>Receita</th><th>Local</th><th>Validade</th><th>Status</th><th></th></tr></thead>
            <tbody>${tableRows || `<tr><td colspan="6">${emptyState('Nenhuma marmita cadastrada.')}</td></tr>`}</tbody>
          </table>
        </div>
      </section>
    </div>`;
}

function formPanel(name, fields) {
  return `
    <section class="rounded-lg border border-zinc-800 bg-zinc-900/60 p-5">
      <h2 class="font-semibold">Cadastro</h2>
      <form class="mt-4 space-y-3" data-form="${name}">
        ${fields.map(fieldHtml).join('')}
        <div class="grid grid-cols-[1fr_auto] gap-2">
          <button class="primary-btn" type="submit"><i data-lucide="save"></i><span>Salvar</span></button>
          <button class="icon-btn" type="reset" data-cancel-edit title="Limpar"><i data-lucide="x"></i></button>
        </div>
      </form>
    </section>`;
}

function fieldHtml(field) {
  const required = field.required ? 'required' : '';
  if (field.type === 'textarea') {
    return `<label class="field"><span>${field.label}</span><textarea name="${field.name}" ${required}></textarea></label>`;
  }
  if (field.type === 'select') {
    return `<label class="field"><span>${field.label}</span><select name="${field.name}" ${required}>
      <option value="">Selecione</option>
      ${(field.options || []).map((option) => `<option value="${option.id}">${escapeHtml(option[field.labelKey || 'nome'])}${field.name === 'idLote' ? ` - validade ${formatDate(option.data_validade)}` : ''}</option>`).join('')}
    </select></label>`;
  }
  if (field.type === 'select-static') {
    return `<label class="field"><span>${field.label}</span><select name="${field.name}" ${required}>
      ${field.options.map((option) => `<option value="${option}">${option}</option>`).join('')}
    </select></label>`;
  }
  return `<label class="field"><span>${field.label}</span><input name="${field.name}" type="${field.type || 'text'}" step="${field.step || ''}" placeholder="${field.placeholder || ''}" ${required}></label>`;
}

function tablePanel(name, columns, rows) {
  const tableRows = rows.map((row) => `
    <tr class="row">
      ${columns.map(([key]) => `<td>${escapeHtml(formatCell(row[key]))}</td>`).join('')}
      <td class="text-right">
        <button class="table-btn" data-edit="${name}" data-id="${row.id}">Editar</button>
        <button class="table-btn danger" data-delete="${configs[name]?.endpoint || `/${name}`}/${row.id}">Excluir</button>
      </td>
    </tr>`).join('');
  return `
    <section class="rounded-lg border border-zinc-800 bg-zinc-900/60 p-5">
      <div class="mb-4 flex items-center justify-between">
        <h2 class="font-semibold">Registros</h2>
        <span class="text-sm text-zinc-500">${rows.length} itens</span>
      </div>
      <div class="overflow-x-auto">
        <table class="data-table">
          <thead><tr>${columns.map(([, label]) => `<th>${label}</th>`).join('')}<th></th></tr></thead>
          <tbody>${tableRows || `<tr><td colspan="${columns.length + 1}">${emptyState('Nenhum registro encontrado.')}</td></tr>`}</tbody>
        </table>
      </div>
    </section>`;
}

function bindForms() {
  document.querySelectorAll('form[data-form]').forEach((form) => {
    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      const name = form.dataset.form;
      const body = Object.fromEntries(new FormData(form).entries());
      const id = form.dataset.editing;
      try {
        await request(id ? `${endpointFor(name)}/${id}` : endpointFor(name), {
          method: id ? 'PUT' : 'POST',
          body: JSON.stringify(normalizeBody(name, body))
        });
        form.reset();
        delete form.dataset.editing;
        showToast(id ? 'Registro atualizado.' : 'Registro salvo.');
        await load();
      } catch (error) {
        showToast(error.message);
      }
    });
  });

  document.querySelectorAll('[data-delete]').forEach((button) => {
    button.addEventListener('click', async () => {
      if (!confirm('Excluir este registro?')) return;
      try {
        await request(button.dataset.delete, { method: 'DELETE' });
        showToast('Registro excluido.');
        await load();
      } catch (error) {
        showToast(error.message);
      }
    });
  });

  document.querySelectorAll('[data-edit]').forEach((button) => {
    button.addEventListener('click', () => startEdit(button.dataset.edit, Number(button.dataset.id)));
  });
  document.querySelectorAll('[data-cancel-edit]').forEach((button) => {
    button.addEventListener('click', () => delete button.closest('form').dataset.editing);
  });
  document.querySelectorAll('[data-consume]').forEach((button) => {
    button.addEventListener('click', () => consume(button.dataset.consume));
  });
  document.querySelectorAll('[data-discard]').forEach((button) => {
    button.addEventListener('click', () => discard(button.dataset.discard));
  });
}

async function startEdit(name, id) {
  const form = document.querySelector(`form[data-form="${name}"]`);
  const row = (state.data[name] || []).find((item) => Number(item.id) === id);
  if (!form || !row) return;
  form.dataset.editing = id;
  const values = await toFormValues(name, row);
  Object.entries(values).forEach(([key, value]) => {
    const input = form.elements[key];
    if (input) input.value = value ?? '';
  });
  form.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

async function consume(id) {
  try {
    const consumidor = state.data.consumidores?.[0];
    if (!consumidor) return showToast('Cadastre um consumidor primeiro.');
    await request(`/marmitas/${id}/consumir`, {
      method: 'POST',
      body: JSON.stringify({ dataMovimentacao: today(), idConsumidor: consumidor.id })
    });
    showToast('Marmita consumida.');
    await load();
  } catch (error) {
    showToast(error.message);
  }
}

async function discard(id) {
  try {
    const motivo = prompt('Motivo do descarte:', 'Validade vencida');
    if (!motivo) return;
    await request(`/marmitas/${id}/descartar`, {
      method: 'POST',
      body: JSON.stringify({ dataMovimentacao: today(), motivo })
    });
    showToast('Descarte registrado.');
    await load();
  } catch (error) {
    showToast(error.message);
  }
}

function endpointFor(name) {
  return configs[name]?.endpoint || `/${name}`;
}

function normalizeBody(name, body) {
  if (name === 'lotes') return { ...body, idReceita: Number(body.idReceita) };
  if (name === 'marmitas') return { ...body, idLote: Number(body.idLote), idLocal: Number(body.idLocal) };
  if (name === 'receitas') {
    const ingredientes = body.idIngrediente && body.quantidade
      ? [{ idIngrediente: Number(body.idIngrediente), quantidade: Number(body.quantidade) }]
      : [];
    return {
      nome: body.nome,
      descricao: body.descricao,
      modoPreparo: body.modoPreparo,
      idCategoria: Number(body.idCategoria),
      ingredientes
    };
  }
  return body;
}

async function toFormValues(name, row) {
  if (name === 'ingredientes') return { nome: row.nome, unidadeMedida: row.unidade_medida };
  if (name === 'receitas') {
    const ingredientes = await request(`/receitas/${row.id}/ingredientes`).catch(() => []);
    const principal = ingredientes[0] || {};
    return {
      nome: row.nome,
      descricao: row.descricao,
      modoPreparo: row.modo_preparo,
      idCategoria: row.id_categoria,
      idIngrediente: principal.id,
      quantidade: principal.quantidade
    };
  }
  if (name === 'lotes') return {
    idReceita: row.id_receita,
    dataPreparo: row.data_preparo,
    dataValidade: row.data_validade
  };
  if (name === 'marmitas') return {
    idLote: row.id_lote,
    idLocal: row.id_local,
    status: row.status
  };
  return row;
}

function filtered(rows) {
  if (!state.search) return rows;
  return rows.filter((row) => JSON.stringify(row).toLowerCase().includes(state.search));
}

function statusClass(value) {
  return {
    OK: 'ok',
    PROXIMA: 'warn',
    VENCIDA: 'bad',
    CONSUMIDA: 'done',
    DESCARTADA: 'bad'
  }[value] || '';
}

function formatCell(value) {
  if (value == null) return '';
  if (/^\d{4}-\d{2}-\d{2}$/.test(String(value))) return formatDate(value);
  return value;
}

function formatDate(value) {
  if (!value) return '';
  return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR');
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

function emptyState(message) {
  return `<div class="rounded-lg border border-dashed border-zinc-800 px-4 py-8 text-center text-sm text-zinc-500">${message}</div>`;
}

function showToast(message) {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.classList.remove('hidden');
  clearTimeout(showToast.timeout);
  showToast.timeout = setTimeout(() => toast.classList.add('hidden'), 3200);
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[char]));
}

function decorate() {
  lucide.createIcons();
}

const style = document.createElement('style');
style.textContent = `
  .nav-item { display:flex; align-items:center; gap:.75rem; min-width:max-content; border-radius:.5rem; padding:.7rem .9rem; color:#a1a1aa; transition:.15s ease; }
  .nav-item svg { width:1.05rem; height:1.05rem; }
  .nav-item:hover, .nav-item.active { background:rgba(39,39,42,.9); color:#f4f4f5; }
  .nav-item.active { box-shadow: inset 0 0 0 1px rgba(125,211,252,.22); }
  #app-shell { display:grid; grid-template-columns:minmax(0, 1fr); }
  #sidebar { min-width:0; }
  #main-content { width:100%; min-width:0; overflow-x:hidden; }
  #app .grid > * { min-width:0; }
  #sidebar, #main-content, #app-shell { transition:grid-template-columns .25s ease, width .25s ease; }
  #sidebar-toggle { flex: 0 0 auto; }
  .icon-btn { display:grid; place-items:center; height:2.5rem; width:2.5rem; border-radius:.5rem; border:1px solid #27272a; background:#09090b; color:#d4d4d8; }
  .icon-btn svg, .primary-btn svg { width:1rem; height:1rem; }
  .field { display:block; }
  .field span { display:block; margin-bottom:.4rem; font-size:.8rem; color:#a1a1aa; }
  .field input, .field select, .field textarea { width:100%; border-radius:.5rem; border:1px solid #27272a; background:#09090b; padding:.7rem .8rem; color:#f4f4f5; outline:none; }
  .field textarea { min-height:7rem; resize:vertical; }
  .field input:focus, .field select:focus, .field textarea:focus { box-shadow:0 0 0 2px rgba(34,211,238,.35); }
  .primary-btn { display:flex; align-items:center; justify-content:center; gap:.5rem; width:100%; border-radius:.5rem; background:#06b6d4; color:#082f49; padding:.75rem 1rem; font-weight:700; }
  .data-table { width:100%; min-width:680px; text-align:left; font-size:.875rem; }
  .data-table th { padding:.65rem .75rem; color:#71717a; font-size:.72rem; text-transform:uppercase; border-bottom:1px solid #27272a; }
  .data-table td { padding:.8rem .75rem; color:#d4d4d8; border-bottom:1px solid #27272a; vertical-align:middle; }
  .row:hover td { background:rgba(39,39,42,.35); }
  .table-btn { border-radius:.4rem; border:1px solid #3f3f46; padding:.35rem .55rem; color:#e4e4e7; margin-left:.25rem; }
  .table-btn:hover { border-color:#67e8f9; color:#a5f3fc; }
  .table-btn.danger:hover { border-color:#f87171; color:#fecaca; }
  .table-btn:disabled { opacity:.35; pointer-events:none; }
  .status { display:inline-flex; border-radius:.4rem; background:rgba(113,113,122,.18); padding:.25rem .5rem; font-size:.75rem; color:#d4d4d8; }
  .status.ok { background:rgba(16,185,129,.12); color:#6ee7b7; }
  .status.warn { background:rgba(245,158,11,.13); color:#fcd34d; }
  .status.bad { background:rgba(239,68,68,.13); color:#fca5a5; }
  .status.done { background:rgba(14,165,233,.13); color:#7dd3fc; }
  .chart-box { position:relative; width:100%; min-width:0; height:20rem; overflow:hidden; }
  .chart-box canvas { display:block; max-width:100% !important; }
  .chart-box.wide { height:22rem; }
  .sidebar-collapsed #sidebar-navigation,
  .sidebar-collapsed .sidebar-note { display:none; }
  @media (min-width:1024px) {
    #app-shell { grid-template-columns:18rem minmax(0, 1fr); align-items:start; }
    .sidebar-collapsed #app-shell { grid-template-columns:4.75rem minmax(0, 1fr); }
    #sidebar { overflow:hidden; }
    #sidebar-navigation { display:block; }
    .sidebar-collapsed #sidebar-navigation { display:block; padding-left:.75rem; padding-right:.75rem; }
    .sidebar-collapsed #sidebar .brand-copy,
    .sidebar-collapsed #sidebar .sidebar-note,
    .sidebar-collapsed #sidebar .nav-item span { display:none; }
    .sidebar-collapsed #sidebar > div:first-child { justify-content:center; padding-left:.75rem; padding-right:.75rem; }
    .sidebar-collapsed #sidebar > div:first-child > div:first-child { display:none; }
    .sidebar-collapsed #sidebar-toggle { margin-left:0; }
    .sidebar-collapsed #sidebar .nav-item { justify-content:center; min-width:0; width:100%; padding:.7rem; }
  }
  @media (max-width:1023px) {
    .sidebar-collapsed #sidebar > div:first-child { height:4.5rem; }
  }
`;
  document.head.appendChild(style);

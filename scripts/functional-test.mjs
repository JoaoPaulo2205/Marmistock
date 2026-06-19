const baseUrl = process.env.BASE_URL || 'http://localhost:8080/api';
const stamp = Date.now();
const created = {};

async function request(method, path, body) {
  const response = await fetch(`${baseUrl}${path}`, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  });
  const text = await response.text();
  let payload = null;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = text;
    }
  }
  return { status: response.status, ok: response.ok, payload };
}

function assert(condition, message, details) {
  if (!condition) {
    const error = new Error(message);
    error.details = details;
    throw error;
  }
}

async function step(name, fn) {
  try {
    await fn();
    console.log(`OK ${name}`);
  } catch (error) {
    console.error(`FAIL ${name}`);
    console.error(error.message);
    if (error.details) console.error(JSON.stringify(error.details, null, 2));
    process.exitCode = 1;
  }
}

async function create(path, body) {
  const response = await request('POST', path, body);
  assert(response.status === 201, `Expected 201 creating ${path}`, response);
  assert(response.payload?.id, `Expected generated id creating ${path}`, response);
  return Number(response.payload.id);
}

async function update(path, body) {
  const response = await request('PUT', path, body);
  assert(response.ok, `Expected success updating ${path}`, response);
}

async function remove(path) {
  const response = await request('DELETE', path);
  assert(response.ok, `Expected success deleting ${path}`, response);
}

await step('GET dashboard', async () => {
  const response = await request('GET', '/dashboard');
  assert(response.ok, 'Dashboard should respond', response);
  assert(response.payload.total >= 2500, 'Dashboard should include populated stock', response);
});

await step('GET all collection endpoints', async () => {
  for (const path of ['/categorias', '/ingredientes', '/receitas', '/lotes', '/locais', '/consumidores', '/marmitas']) {
    const response = await request('GET', path);
    assert(response.ok && Array.isArray(response.payload), `${path} should return a list`, response);
  }
});

await step('CRUD categoria', async () => {
  created.categoria = await create('/categorias', { nome: `Teste Categoria ${stamp}`, descricao: 'Criada pela varredura' });
  await update(`/categorias/${created.categoria}`, { nome: `Teste Categoria ${stamp} editada`, descricao: 'Atualizada' });
  const id = await create('/categorias', { nome: `Teste Categoria Delete ${stamp}`, descricao: 'Sera removida' });
  await remove(`/categorias/${id}`);
});

await step('CRUD ingrediente', async () => {
  created.ingrediente = await create('/ingredientes', { nome: `Teste Ingrediente ${stamp}`, unidadeMedida: 'g' });
  await update(`/ingredientes/${created.ingrediente}`, { nome: `Teste Ingrediente ${stamp} editado`, unidadeMedida: 'kg' });
  const id = await create('/ingredientes', { nome: `Teste Ingrediente Delete ${stamp}`, unidadeMedida: 'un' });
  await remove(`/ingredientes/${id}`);
});

await step('CRUD local', async () => {
  created.local = await create('/locais', { nome: `Teste Local ${stamp}`, descricao: 'Gaveta de teste' });
  await update(`/locais/${created.local}`, { nome: `Teste Local ${stamp} editado`, descricao: 'Atualizado' });
  const id = await create('/locais', { nome: `Teste Local Delete ${stamp}`, descricao: 'Sera removido' });
  await remove(`/locais/${id}`);
});

await step('CRUD consumidor', async () => {
  created.consumidor = await create('/consumidores', { nome: `Teste Consumidor ${stamp}` });
  await update(`/consumidores/${created.consumidor}`, { nome: `Teste Consumidor ${stamp} editado` });
  const id = await create('/consumidores', { nome: `Teste Consumidor Delete ${stamp}` });
  await remove(`/consumidores/${id}`);
});

await step('CRUD receita with ingredient composition', async () => {
  created.receita = await create('/receitas', {
    nome: `Teste Receita ${stamp}`,
    descricao: 'Receita de teste',
    modoPreparo: 'Preparar e congelar.',
    idCategoria: created.categoria,
    ingredientes: [{ idIngrediente: created.ingrediente, quantidade: 123.45 }]
  });
  const ingredientes = await request('GET', `/receitas/${created.receita}/ingredientes`);
  assert(ingredientes.ok && ingredientes.payload.length === 1, 'Recipe ingredient should be stored', ingredientes);
  await update(`/receitas/${created.receita}`, {
    nome: `Teste Receita ${stamp} editada`,
    descricao: 'Receita atualizada',
    modoPreparo: 'Atualizar preparo.',
    idCategoria: created.categoria,
    ingredientes: [{ idIngrediente: created.ingrediente, quantidade: 77.7 }]
  });
  const deleteOnly = await create('/receitas', {
    nome: `Teste Receita Delete ${stamp}`,
    descricao: 'Sera removida',
    modoPreparo: 'Preparar.',
    idCategoria: created.categoria,
    ingredientes: []
  });
  await remove(`/receitas/${deleteOnly}`);
});

await step('CRUD lote', async () => {
  created.lote = await create('/lotes', {
    dataPreparo: '2026-06-18',
    dataValidade: '2026-07-18',
    idReceita: created.receita
  });
  await update(`/lotes/${created.lote}`, {
    dataPreparo: '2026-06-19',
    dataValidade: '2026-07-20',
    idReceita: created.receita
  });
  const deleteOnly = await create('/lotes', {
    dataPreparo: '2026-06-20',
    dataValidade: '2026-07-21',
    idReceita: created.receita
  });
  await remove(`/lotes/${deleteOnly}`);
});

await step('CRUD marmita', async () => {
  created.marmita = await create('/marmitas', { status: 'DISPONIVEL', idLote: created.lote, idLocal: created.local });
  await update(`/marmitas/${created.marmita}`, { status: 'DISPONIVEL', idLote: created.lote, idLocal: created.local });
  await remove(`/marmitas/${created.marmita}`);
});

await step('Consume marmita action', async () => {
  created.marmitaConsumo = await create('/marmitas', { status: 'DISPONIVEL', idLote: created.lote, idLocal: created.local });
  const response = await request('POST', `/marmitas/${created.marmitaConsumo}/consumir`, {
    dataMovimentacao: '2026-06-18',
    idConsumidor: created.consumidor
  });
  assert(response.ok, 'Consuming should succeed for available marmita', response);
  const duplicate = await request('POST', `/marmitas/${created.marmitaConsumo}/consumir`, {
    dataMovimentacao: '2026-06-18',
    idConsumidor: created.consumidor
  });
  assert(duplicate.status === 400, 'Consuming a non-available marmita should return 400', duplicate);
});

await step('Discard marmita action', async () => {
  created.marmitaDescarte = await create('/marmitas', { status: 'DISPONIVEL', idLote: created.lote, idLocal: created.local });
  const response = await request('POST', `/marmitas/${created.marmitaDescarte}/descartar`, {
    dataMovimentacao: '2026-06-18',
    motivo: 'Teste funcional'
  });
  assert(response.ok, 'Discarding should succeed for available marmita', response);
  const duplicate = await request('POST', `/marmitas/${created.marmitaDescarte}/descartar`, {
    dataMovimentacao: '2026-06-18',
    motivo: 'Teste funcional'
  });
  assert(duplicate.status === 400, 'Discarding a non-available marmita should return 400', duplicate);
});

await step('Validation rejects invalid status', async () => {
  const response = await request('POST', '/marmitas', { status: 'INVALIDO', idLote: created.lote, idLocal: created.local });
  assert(response.status === 400, 'Invalid status should return 400', response);
});

await step('Linked record delete returns conflict', async () => {
  const response = await request('DELETE', `/lotes/${created.lote}`);
  assert(response.status === 409, 'Deleting lote with marmitas should return 409', response);
});

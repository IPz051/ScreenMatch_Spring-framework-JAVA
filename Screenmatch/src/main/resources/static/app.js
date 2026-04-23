const $ = (id) => document.getElementById(id);

const state = {
  apiBase: "",
  data: [],
  filtered: [],
};

function normalizeText(value) {
  return (value ?? "")
    .toString()
    .normalize("NFD")
    .replace(/\p{M}+/gu, "")
    .trim()
    .toLowerCase();
}

function parseDate(value) {
  if (!value) return null;
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return null;
  return d;
}

function formatRating(value) {
  if (value == null) return "N/A";
  const n = Number(value);
  if (!Number.isFinite(n) || n <= 0) return "N/A";
  return n.toFixed(1);
}

function safe(value, fallback = "N/A") {
  if (value == null) return fallback;
  const s = value.toString().trim();
  return s.length ? s : fallback;
}

function apiDefault() {
  if (location.hostname === "localhost" && location.port === "8080") return "";
  if (location.hostname === "127.0.0.1" && location.port === "8080") return "";
  return "http://localhost:8080";
}

function loadApiBase() {
  const stored = localStorage.getItem("screenmatch.apiBase");
  const base = stored == null || stored.trim() === "" ? apiDefault() : stored.trim();
  state.apiBase = base;
  $("apiBase").value = base;
}

function saveApiBase() {
  const value = $("apiBase").value.trim();
  localStorage.setItem("screenmatch.apiBase", value);
  state.apiBase = value;
}

function setStatus(text) {
  $("status").textContent = text ?? "";
}

function buildGenreOptions(items) {
  const select = $("genero");
  const genres = new Map();
  for (const s of items) {
    const g = safe(s.genero, "");
    if (!g) continue;
    genres.set(g, g);
  }
  const options = Array.from(genres.values()).sort((a, b) => a.localeCompare(b, "pt-BR"));
  select.innerHTML = `<option value="">Todos</option>${options
    .map((g) => `<option value="${g}">${g}</option>`)
    .join("")}`;
}

function applyFilters() {
  const q = normalizeText($("q").value);
  const genero = $("genero").value;
  const minRating = $("minRating").value.trim();
  const minSeasons = $("minSeasons").value.trim();
  const startDate = $("startDate").value;
  const endDate = $("endDate").value;
  const sort = $("sort").value;

  const minRatingNum = minRating === "" ? null : Number(minRating);
  const minSeasonsNum = minSeasons === "" ? null : Number(minSeasons);
  const start = startDate ? parseDate(startDate) : null;
  const end = endDate ? parseDate(endDate) : null;

  const filtered = state.data.filter((s) => {
    if (genero && safe(s.genero, "") !== genero) return false;

    if (minRatingNum != null && Number.isFinite(minRatingNum)) {
      const r = Number(s.avaliacao);
      if (!Number.isFinite(r) || r < minRatingNum) return false;
    }

    if (minSeasonsNum != null && Number.isFinite(minSeasonsNum)) {
      const t = Number(s.temporada);
      if (!Number.isFinite(t) || t < minSeasonsNum) return false;
    }

    if (start || end) {
      const d = s.dataLancamento ? parseDate(s.dataLancamento) : null;
      if (!d) return false;
      if (start && d < start) return false;
      if (end && d > end) return false;
    }

    if (q) {
      const title = normalizeText(s.titulo);
      const actors = normalizeText(s.atores);
      if (!title.includes(q) && !actors.includes(q)) return false;
    }

    return true;
  });

  filtered.sort((a, b) => {
    if (sort === "rating_desc") return Number(b.avaliacao) - Number(a.avaliacao);
    if (sort === "rating_asc") return Number(a.avaliacao) - Number(b.avaliacao);

    if (sort === "release_desc") {
      const da = parseDate(a.dataLancamento)?.getTime() ?? -Infinity;
      const db = parseDate(b.dataLancamento)?.getTime() ?? -Infinity;
      return db - da;
    }
    if (sort === "release_asc") {
      const da = parseDate(a.dataLancamento)?.getTime() ?? Infinity;
      const db = parseDate(b.dataLancamento)?.getTime() ?? Infinity;
      return da - db;
    }

    if (sort === "title_desc") return safe(b.titulo, "").localeCompare(safe(a.titulo, ""), "pt-BR");
    return safe(a.titulo, "").localeCompare(safe(b.titulo, ""), "pt-BR");
  });

  state.filtered = filtered;
  render();
}

function openDialog(serie) {
  $("dialogTitle").textContent = safe(serie.titulo, "Série");
  const rows = [
    ["Lançamento", safe(serie.dataLancamento)],
    ["Temporadas", safe(serie.temporada)],
    ["IMDb", formatRating(serie.avaliacao)],
    ["Gênero", safe(serie.genero)],
    ["Atores", safe(serie.atores)],
    ["Poster", safe(serie.poster)],
    ["Sinopse", safe(serie.sinopse)],
  ];

  $("dialogBody").innerHTML = rows
    .map(
      ([label, value]) =>
        `<div class="row"><div class="row__label">${label}</div><div class="row__value">${value}</div></div>`
    )
    .join("");

  const dialog = $("dialog");
  if (!dialog.open) dialog.showModal();
}

function render() {
  const grid = $("grid");
  const items = state.filtered;

  setStatus(`${items.length} série(s)`);

  grid.innerHTML = items
    .map((s) => {
      const poster = safe(s.poster, "");
      const posterHtml = poster && poster !== "N/A" ? `<img class="poster" src="${poster}" alt="${safe(s.titulo)}" />` : `<div class="poster"></div>`;
      const release = safe(s.dataLancamento, "N/A");
      const rating = formatRating(s.avaliacao);
      const seasons = safe(s.temporada, "N/A");
      const genre = safe(s.genero, "N/A");
      return `
        <article class="card" data-id="${s.id}">
          ${posterHtml}
          <div class="card__body">
            <div class="card__title">${safe(s.titulo)}</div>
            <div class="meta">
              <span class="tag">IMDb: ${rating}</span>
              <span class="tag">Temp: ${seasons}</span>
              <span class="tag">${genre}</span>
              <span class="tag">${release}</span>
            </div>
          </div>
        </article>
      `;
    })
    .join("");

  for (const el of grid.querySelectorAll(".card")) {
    el.addEventListener("click", () => {
      const id = Number(el.getAttribute("data-id"));
      const serie = state.data.find((x) => Number(x.id) === id);
      if (serie) openDialog(serie);
    });
  }
}

async function loadSeries() {
  const url = `${state.apiBase}/series`;
  setStatus(`Carregando: ${url}`);
  try {
    const res = await fetch(url, { headers: { Accept: "application/json" } });
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`);
    }
    const data = await res.json();
    if (!Array.isArray(data)) {
      throw new Error("Resposta inválida");
    }
    state.data = data;
    buildGenreOptions(state.data);
    applyFilters();
  } catch (e) {
    state.data = [];
    state.filtered = [];
    $("grid").innerHTML = "";
    setStatus(`Erro ao carregar /series: ${e?.message ?? e}`);
  }
}

function clearFilters() {
  $("q").value = "";
  $("genero").value = "";
  $("minRating").value = "";
  $("minSeasons").value = "";
  $("startDate").value = "";
  $("endDate").value = "";
  $("sort").value = "rating_desc";
  applyFilters();
}

function bind() {
  $("apiSave").addEventListener("click", async () => {
    saveApiBase();
    await loadSeries();
  });

  $("reload").addEventListener("click", loadSeries);
  $("clear").addEventListener("click", clearFilters);

  $("q").addEventListener("input", applyFilters);
  $("genero").addEventListener("change", applyFilters);
  $("minRating").addEventListener("input", applyFilters);
  $("minSeasons").addEventListener("input", applyFilters);
  $("startDate").addEventListener("change", applyFilters);
  $("endDate").addEventListener("change", applyFilters);
  $("sort").addEventListener("change", applyFilters);

  $("dialogClose").addEventListener("click", () => $("dialog").close());
  $("dialog").addEventListener("click", (e) => {
    const rect = $("dialog").getBoundingClientRect();
    const inside =
      e.clientX >= rect.left && e.clientX <= rect.right && e.clientY >= rect.top && e.clientY <= rect.bottom;
    if (!inside) $("dialog").close();
  });
}

function start() {
  loadApiBase();
  bind();
  loadSeries();
}

start();

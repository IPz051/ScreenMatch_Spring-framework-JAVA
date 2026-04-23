import "./styles.css";

const $ = (id) => document.getElementById(id);

const API_BASE = (import.meta.env.VITE_API_BASE ?? "").toString().trim().replace(/\/$/, "");

const state = {
  data: [],
  filtered: [],
  endpoint: "/series",
  dialogAbortController: null
};

function apiUrl(path) {
  const p = (path ?? "").toString();
  if (!API_BASE) return p;
  if (!p.startsWith("/")) return `${API_BASE}/${p}`;
  return `${API_BASE}${p}`;
}

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

function setStatus(text) {
  $("status").textContent = text ?? "";
}

function setAddError(message) {
  const el = $("addError");
  if (!el) return;
  el.textContent = message ?? "";
  el.classList.toggle("helper--error", Boolean(message));
}

async function readErrorMessage(res) {
  const contentType = res.headers.get("content-type") ?? "";
  if (contentType.includes("application/json") || contentType.includes("application/problem+json")) {
    try {
      const body = await res.json();
      return body?.detail || body?.message || body?.error || JSON.stringify(body);
    } catch {
      return `HTTP ${res.status}`;
    }
  }
  try {
    const text = await res.text();
    return text?.trim() ? text.trim() : `HTTP ${res.status}`;
  } catch {
    return `HTTP ${res.status}`;
  }
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

function buildTags(serie) {
  const tags = [];
  tags.push({ label: `IMDb ${formatRating(serie.avaliacao)}`, tone: "a" });
  tags.push({ label: `Temp ${safe(serie.temporada)}`, tone: "b" });
  tags.push({ label: safe(serie.genero), tone: "c" });
  if (serie.dataLancamento) {
    tags.push({ label: safe(serie.dataLancamento), tone: "d" });
  }
  return tags.filter((t) => t.label && t.label !== "N/A");
}

function openDialog(serie) {
  $("dialogTitle").textContent = safe(serie.titulo, "Série");
  const dialog = $("dialog");
  dialog.classList.remove("dialog--closing");
  if (state.dialogAbortController) {
    state.dialogAbortController.abort();
    state.dialogAbortController = null;
  }

  const poster = safe(serie.poster, "");
  const posterHtml =
    poster && poster !== "N/A"
      ? `<img class="modal__posterImg" src="${poster}" alt="${safe(serie.titulo)}" />`
      : `<div class="modal__posterFallback"></div>`;

  const tags = buildTags(serie);
  const tagsHtml = tags
    .map(
      (t, i) =>
        `<span class="pill pill--${t.tone} anim" style="--i:${i}">${t.label}</span>`
    )
    .join("");

  const actors = safe(serie.atores);
  const synopsis = safe(serie.sinopse);

  $("dialogBody").innerHTML = `
    <div class="modal">
      <div class="modal__hero">
        <div class="modal__poster anim" style="--i:0">
          ${posterHtml}
          <div class="modal__posterGlow"></div>
        </div>
        <div class="modal__summary">
          <div class="modal__pills">${tagsHtml}</div>
          <div class="modal__grid">
            <div class="kpi anim" style="--i:1">
              <div class="kpi__label">Gênero</div>
              <div class="kpi__value">${safe(serie.genero)}</div>
            </div>
            <div class="kpi anim" style="--i:2">
              <div class="kpi__label">Temporadas</div>
              <div class="kpi__value">${safe(serie.temporada)}</div>
            </div>
            <div class="kpi anim" style="--i:3">
              <div class="kpi__label">Lançamento</div>
              <div class="kpi__value">${safe(serie.dataLancamento)}</div>
            </div>
            <div class="kpi anim" style="--i:4">
              <div class="kpi__label">IMDb</div>
              <div class="kpi__value">${formatRating(serie.avaliacao)}</div>
            </div>
          </div>
          <div class="section anim" style="--i:5">
            <div class="section__title">Elenco</div>
            <div class="section__text">${actors}</div>
          </div>
        </div>
      </div>
      <div class="section section--wide anim" style="--i:6">
        <div class="section__title">Sinopse</div>
        <div class="section__text">${synopsis}</div>
      </div>
      <div class="section section--wide anim" style="--i:7">
        <div class="section__title">Temporadas e episódios</div>
        <div id="episodesMount" class="episodes">
          <div class="episodes__loading">Carregando episódios…</div>
        </div>
      </div>
    </div>
  `;

  if (!dialog.open) dialog.showModal();
  dialog.classList.add("dialog--opening");
  window.setTimeout(() => dialog.classList.remove("dialog--opening"), 260);

  loadEpisodesForDialog(serie.id);
}

function groupEpisodesBySeason(episodes) {
  const map = new Map();
  for (const e of episodes) {
    const season = Number(e.temporada ?? 0);
    if (!map.has(season)) map.set(season, []);
    map.get(season).push(e);
  }
  return Array.from(map.entries()).sort((a, b) => a[0] - b[0]);
}

function buildEpisodesHtml(episodes) {
  const grouped = groupEpisodesBySeason(episodes);
  if (!grouped.length) {
    return `<div class="episodes__empty">Nenhum episódio encontrado no banco para esta série.</div>`;
  }

  return grouped
    .map(([season, items], seasonIndex) => {
      const validRatings = items.map((x) => Number(x.avaliacao)).filter((n) => Number.isFinite(n) && n > 0);
      const avg =
        validRatings.length > 0
          ? (validRatings.reduce((acc, n) => acc + n, 0) / validRatings.length).toFixed(1)
          : null;

      const list = items
        .slice()
        .sort((a, b) => Number(a.numeroEpisodio) - Number(b.numeroEpisodio))
        .map((ep, i) => {
          const num = String(ep.numeroEpisodio ?? "").padStart(2, "0");
          const title = safe(ep.titulo, "Episódio");
          const rating = formatRating(ep.avaliacao);
          const delay = 1 + seasonIndex + i * 0.15;
          return `
            <div class="episode anim" style="--i:${delay}">
              <div class="episode__left">
                <div class="episode__code">S${String(season).padStart(2, "0")}E${num}</div>
                <div class="episode__title">${title}</div>
              </div>
              <div class="episode__rating">${rating}</div>
            </div>
          `;
        })
        .join("");

      return `
        <details class="season anim" style="--i:${seasonIndex}">
          <summary class="season__summary">
            <div class="season__title">Temporada ${season}</div>
            <div class="season__meta">
              <span class="season__chip">${items.length} eps</span>
              ${avg ? `<span class="season__chip season__chip--a">média ${avg}</span>` : ""}
            </div>
          </summary>
          <div class="season__body">
            ${list}
          </div>
        </details>
      `;
    })
    .join("");
}

async function loadEpisodesForDialog(serieId) {
  const mount = $("episodesMount");
  if (!mount) return;

  const controller = new AbortController();
  state.dialogAbortController = controller;
  const timeoutId = window.setTimeout(() => controller.abort(), 15000);

  try {
    const res = await fetch(apiUrl(`/series/${serieId}/episodios`), {
      headers: { Accept: "application/json" },
      signal: controller.signal
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    if (!Array.isArray(data)) throw new Error("Resposta inválida");
    mount.innerHTML = buildEpisodesHtml(data);
  } catch (e) {
    const msg = e?.name === "AbortError" ? "Tempo esgotado ao carregar episódios" : e?.message ?? String(e);
    mount.innerHTML = `<div class="episodes__error">Não foi possível carregar os episódios (${msg}).</div>`;
  } finally {
    window.clearTimeout(timeoutId);
    if (state.dialogAbortController === controller) state.dialogAbortController = null;
  }
}

function closeDialogAnimated() {
  const dialog = $("dialog");
  if (!dialog.open) return;
  if (state.dialogAbortController) {
    state.dialogAbortController.abort();
    state.dialogAbortController = null;
  }
  dialog.classList.add("dialog--closing");
  window.setTimeout(() => {
    dialog.classList.remove("dialog--closing");
    dialog.close();
  }, 180);
}

function render() {
  const grid = $("grid");
  const items = state.filtered;

  setStatus(`${items.length} série(s)`);

  grid.innerHTML = items
    .map((s) => {
      const poster = safe(s.poster, "");
      const posterHtml =
        poster && poster !== "N/A"
          ? `<img class="poster" src="${poster}" alt="${safe(s.titulo)}" />`
          : `<div class="poster"></div>`;
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
  const url = state.endpoint;
  setStatus(`Carregando: ${url}`);
  try {
    const res = await fetch(apiUrl(url), { headers: { Accept: "application/json" } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);

    const data = await res.json();
    if (!Array.isArray(data)) throw new Error("Resposta inválida");

    state.data = data;
    buildGenreOptions(state.data);
    applyFilters();
  } catch (e) {
    state.data = [];
    state.filtered = [];
    $("grid").innerHTML = "";
    setStatus(`Erro ao carregar ${url}: ${e?.message ?? e}`);
  }
}

async function addSerie() {
  const input = $("addTitle");
  const titulo = input.value.trim();
  if (!titulo) {
    setAddError("Digite um título para adicionar");
    return;
  }

  setAddError("");
  const btn = $("addBtn");
  btn.disabled = true;
  input.classList.remove("field__input--error");

  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), 15000);

  setStatus("Adicionando série...");
  try {
    const res = await fetch(apiUrl("/series"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json"
      },
      body: JSON.stringify({ titulo }),
      signal: controller.signal
    });

    if (!res.ok) {
      const message = await readErrorMessage(res);
      if (res.status === 400) {
        throw new Error(message || "Título inválido");
      }
      if (res.status === 404) {
        throw new Error(message || "Série não encontrada");
      }
      throw new Error(message || `HTTP ${res.status}`);
    }

    input.value = "";
    state.endpoint = "/series";
    setModeButtons();
    await loadSeries();
    setStatus("Série adicionada");
  } catch (e) {
    const msg = e?.name === "AbortError" ? "Tempo esgotado ao conectar com o servidor" : e?.message ?? String(e);
    setAddError(msg);
    input.classList.add("field__input--error");
    setStatus("Falha ao adicionar");
  } finally {
    window.clearTimeout(timeoutId);
    btn.disabled = false;
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

function setModeButtons() {
  const allBtn = $("modeAll");
  const topBtn = $("modeTop5");
  const releasesBtn = $("modeReleases");
  const label = $("sourceLabel");

  allBtn.classList.remove("segmented__btn--active");
  topBtn.classList.remove("segmented__btn--active");
  if (releasesBtn) releasesBtn.classList.remove("segmented__btn--active");

  if (state.endpoint === "/series/top5") {
    topBtn.classList.add("segmented__btn--active");
    label.textContent = "/series/top5";
    return;
  }

  if (state.endpoint === "/series/lancamentos") {
    if (releasesBtn) releasesBtn.classList.add("segmented__btn--active");
    label.textContent = "/series/lancamentos";
    return;
  }

  allBtn.classList.add("segmented__btn--active");
  label.textContent = "/series";
}

function bind() {
  $("reload").addEventListener("click", loadSeries);
  $("clear").addEventListener("click", clearFilters);
  $("addBtn").addEventListener("click", addSerie);
  $("addTitle").addEventListener("keydown", (e) => {
    if (e.key === "Enter") addSerie();
  });
  $("addTitle").addEventListener("input", () => {
    $("addTitle").classList.remove("field__input--error");
    setAddError("");
  });

  $("modeAll").addEventListener("click", async () => {
    state.endpoint = "/series";
    setModeButtons();
    await loadSeries();
  });
  $("modeTop5").addEventListener("click", async () => {
    state.endpoint = "/series/top5";
    setModeButtons();
    await loadSeries();
  });
  const releasesBtn = $("modeReleases");
  if (releasesBtn) {
    releasesBtn.addEventListener("click", async () => {
      state.endpoint = "/series/lancamentos";
      $("sort").value = "release_desc";
      setModeButtons();
      await loadSeries();
    });
  }

  $("q").addEventListener("input", applyFilters);
  $("genero").addEventListener("change", applyFilters);
  $("minRating").addEventListener("input", applyFilters);
  $("minSeasons").addEventListener("input", applyFilters);
  $("startDate").addEventListener("change", applyFilters);
  $("endDate").addEventListener("change", applyFilters);
  $("sort").addEventListener("change", applyFilters);

  $("dialogClose").addEventListener("click", closeDialogAnimated);
  $("dialog").addEventListener("click", (e) => {
    const rect = $("dialog").getBoundingClientRect();
    const inside =
      e.clientX >= rect.left && e.clientX <= rect.right && e.clientY >= rect.top && e.clientY <= rect.bottom;
    if (!inside) closeDialogAnimated();
  });
  window.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeDialogAnimated();
  });
}

function start() {
  bind();
  setModeButtons();
  loadSeries();
}

start();

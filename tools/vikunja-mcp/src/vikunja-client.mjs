const TICKET_ID_PATTERN = /^([A-Z][A-Z0-9_-]*)-([1-9][0-9]*)$/i;
const DEFAULT_BASE_URL = "http://localhost:3456";
const DEFAULT_API_VERSION = "v1";
const DEFAULT_TIMEOUT_MS = 10_000;
const DEFAULT_PAGE_SIZE = 50;
const MAX_FALLBACK_PAGES = 100;
export const RELATION_KINDS = Object.freeze([
  "subtask", "parenttask", "related", "duplicateof", "duplicates",
  "blocking", "blocked", "precedes", "follows", "copiedfrom", "copiedto",
]);

export class VikunjaError extends Error {
  constructor(message, options = {}) {
    super(message, options);
    this.name = "VikunjaError";
  }
}

export class VikunjaHttpError extends VikunjaError {
  constructor(status, message, options = {}) {
    super(message, options);
    this.name = "VikunjaHttpError";
    this.status = status;
  }
}

export function parseTicketId(value) {
  if (typeof value !== "string") {
    throw new VikunjaError("Die Ticket-ID muss eine Zeichenkette sein.");
  }

  const ticketId = value.trim().toUpperCase();
  const match = TICKET_ID_PATTERN.exec(ticketId);
  if (!match) {
    throw new VikunjaError(
      `Ungültige Ticket-ID "${value}". Erwartet wird eine Form wie PV-1.`,
    );
  }

  return {
    ticketId,
    projectIdentifier: match[1],
    index: Number.parseInt(match[2], 10),
  };
}

export function loadConfig(env = process.env) {
  const baseUrl = normalizeBaseUrl(env.VIKUNJA_BASE_URL ?? DEFAULT_BASE_URL);
  const apiVersion = env.VIKUNJA_API_VERSION ?? DEFAULT_API_VERSION;
  if (!/^v[12]$/.test(apiVersion)) {
    throw new VikunjaError(
      `VIKUNJA_API_VERSION muss "v1" oder "v2" sein, nicht "${apiVersion}".`,
    );
  }

  return {
    baseUrl,
    apiVersion,
    token: env.VIKUNJA_API_TOKEN?.trim() ?? "",
    timeoutMs:
      parseOptionalPositiveInteger(
        env.VIKUNJA_TIMEOUT_MS,
        "VIKUNJA_TIMEOUT_MS",
      ) ?? DEFAULT_TIMEOUT_MS,
  };
}

export class VikunjaClient {
  constructor(config, fetchImplementation = globalThis.fetch) {
    if (typeof fetchImplementation !== "function") {
      throw new VikunjaError("Diese Node.js-Version stellt kein fetch bereit.");
    }
    this.config = config;
    this.fetch = fetchImplementation;
  }

  async getTask(ticketIdInput) {
    const ticket = parseTicketId(ticketIdInput);
    this.#assertConfigured();

    try {
      const task = await this.#getTaskByProjectIndex(ticket);
      return this.#createResult(ticket, task, "by-index");
    } catch (error) {
      if (
        !(error instanceof VikunjaHttpError) ||
        ![401, 404, 405].includes(error.status)
      ) {
        throw error;
      }
    }

    const task = await this.#findTaskInGlobalListing(ticket);
    return this.#createResult(ticket, task, "global-task-list-fallback");
  }

  async createTask({ project_id, title, description = "" }) {
    if (!Number.isSafeInteger(project_id) || project_id <= 0) {
      throw new VikunjaError("project_id muss eine positive Ganzzahl sein.");
    }
    validateChanges({ title, description });
    this.#assertConfigured();
    const { body } = await this.#request(
      `/projects/${project_id}/tasks`,
      this.config.apiVersion === "v2" ? "POST" : "PUT",
      { title, description },
    );
    return this.#writeResult(body, "created");
  }

  async updateTask(ticketId, changes) {
    validateChanges(changes);
    const resolved = await this.getTask(ticketId);
    const id = resolved.task.id;
    if (!Number.isSafeInteger(id) || id <= 0) {
      throw new VikunjaError("Vikunja lieferte keine gültige Task-ID.");
    }
    // Read HTML again: getTask may return Markdown. Preserve untouched fields.
    const { body: currentBody } = await this.#request(`/tasks/${id}`);
    const current = unwrapSingleTask(currentBody);
    if (current?.id !== id) {
      throw new VikunjaError("Vikunja lieferte einen unerwarteten Task.");
    }
    const { body } = await this.#request(
      `/tasks/${id}`,
      this.config.apiVersion === "v2" ? "PUT" : "POST",
      { ...current, ...changes },
    );
    return this.#writeResult(body, "updated");
  }

  async createTaskRelation(args) {
    return this.#changeTaskRelation(args, false);
  }

  async deleteTaskRelation(args) {
    return this.#changeTaskRelation(args, true);
  }

  async #changeTaskRelation({ ticket_id, other_ticket_id, relation_kind }, remove) {
    const source = parseTicketId(ticket_id);
    const target = parseTicketId(other_ticket_id);
    if (!RELATION_KINDS.includes(relation_kind)) {
      throw new VikunjaError("Ungültiger relation_kind.");
    }
    if (source.ticketId === target.ticketId) {
      throw new VikunjaError("Ein Task kann nicht mit sich selbst verknüpft werden.");
    }
    const resolved = await this.getTask(source.ticketId);
    const other = await this.getTask(target.ticketId);
    const task_id = resolved.task.id;
    const other_task_id = other.task.id;
    if (![task_id, other_task_id].every(id => Number.isSafeInteger(id) && id > 0) ||
        task_id === other_task_id) {
      throw new VikunjaError("Ungültige oder identische aufgelöste Task-IDs.");
    }
    const relation = { task_id, other_task_id, relation_kind };
    const exists = await this.#hasRelation(relation);
    const result = resolution => ({
      ticket_id: source.ticketId,
      other_ticket_id: target.ticketId,
      relation_kind,
      resolution,
      task_id,
      other_task_id,
    });
    if (exists === !remove) {
      return result(remove ? "already_absent" : "already_exists");
    }
    const path = remove
      ? `/tasks/${task_id}/relations/${relation_kind}/${other_task_id}`
      : `/tasks/${task_id}/relations`;
    try {
      await this.#request(path,
        remove ? "DELETE" : this.config.apiVersion === "v2" ? "POST" : "PUT",
        relation);
      if (await this.#hasRelation(relation) !== !remove) {
        throw new VikunjaError("Die erwartete Beziehung wurde beim Nachlesen nicht bestätigt.");
      }
    } catch (error) {
      throw new VikunjaError(
        `Beziehungsänderung nicht bestätigt: ${error.message} Vor erneutem Schreiben beide Tasks lesen; keine automatische Wiederholung.`,
        { cause: error },
      );
    }
    return result(remove ? "deleted" : "created");
  }

  async #hasRelation({ task_id, other_task_id, relation_kind }) {
    const { body } = await this.#request(`/tasks/${task_id}`);
    const task = unwrapSingleTask(body);
    if (task?.id !== task_id ||
        !Object.hasOwn(task, "related_tasks") ||
        (task.related_tasks !== null &&
          (typeof task.related_tasks !== "object" || Array.isArray(task.related_tasks)))) {
      throw new VikunjaError("Vikunja lieferte keinen verlässlichen Beziehungsstand.");
    }
    const relations = task.related_tasks?.[relation_kind] ?? [];
    if (!Array.isArray(relations)) {
      throw new VikunjaError("Vikunja lieferte eine ungültige Beziehungsliste.");
    }
    return relations.some(task => task.id === other_task_id);
  }

  #writeResult(body, resolution) {
    const task = unwrapSingleTask(body);
    if (!Number.isSafeInteger(task?.id) || task.id <= 0) {
      throw new VikunjaError("Schreibantwort ohne gültige Task-ID; vor erneutem Versuch in Vikunja prüfen.");
    }
    return this.#createResult({ ticketId: task.identifier ?? null }, task, resolution);
  }

  async #getTaskByProjectIndex(ticket) {
    const project = encodeURIComponent(ticket.projectIdentifier);
    const path =
      `/projects/${project}/tasks/by-index/${ticket.index}` +
      "?format=markdown";
    const { body } = await this.#request(path);
    return unwrapSingleTask(body);
  }

  async #findTaskInGlobalListing(ticket) {
    for (let page = 1; page <= MAX_FALLBACK_PAGES; page += 1) {
      const path = `/tasks?page=${page}&per_page=${DEFAULT_PAGE_SIZE}`;
      const { body, headers } = await this.#request(path);
      const tasks = unwrapTaskList(body);
      const task = tasks.find(
        (candidate) =>
          typeof candidate?.identifier === "string" &&
          candidate.identifier.toUpperCase() === ticket.ticketId,
      );
      if (task) {
        return task;
      }

      const totalPages = parsePositiveIntegerHeader(
        headers.get("x-pagination-total-pages"),
      );
      if (
        (totalPages && page >= totalPages) ||
        (!totalPages && tasks.length < DEFAULT_PAGE_SIZE)
      ) {
        break;
      }
    }

    throw new VikunjaError(
      `Der Vikunja-Task ${ticket.ticketId} wurde in den zugänglichen Tasks nicht gefunden.`,
    );
  }

  async #request(path, method = "GET", payload) {
    const url = `${this.config.baseUrl}/api/${this.config.apiVersion}${path}`;
    let response;
    try {
      response = await this.fetch(url, {
        method,
        redirect: "error",
        ...(payload === undefined ? {} : { body: JSON.stringify(payload) }),
        headers: {
          Accept: "application/json",
          ...(payload === undefined ? {} : { "Content-Type": "application/json" }),
          Authorization: `Bearer ${this.config.token}`,
        },
        signal: AbortSignal.timeout(this.config.timeoutMs),
      });
    } catch (error) {
      if (error?.name === "TimeoutError") {
        throw new VikunjaError(
          `Vikunja antwortete nicht innerhalb von ${this.config.timeoutMs} ms.`,
          { cause: error },
        );
      }
      throw new VikunjaError(
        `Vikunja ist unter ${this.config.baseUrl} nicht erreichbar: ${error.message}`,
        { cause: error },
      );
    }

    const text = await response.text();
    const body = parseResponseBody(text);
    if (!response.ok) {
      throw new VikunjaHttpError(
        response.status,
        formatHttpError(response.status, body),
      );
    }

    return { body, headers: response.headers };
  }

  #assertConfigured() {
    if (!this.config.token) {
      throw new VikunjaError(
        "VIKUNJA_API_TOKEN ist nicht gesetzt. Erzeuge in Vikunja einen " +
          "API-Token mit den benötigten Lese-/Schreibrechten und hinterlege ihn in der MCP-Konfiguration " +
          "oder als Umgebungsvariable.",
      );
    }
  }

  #createResult(ticket, task, resolution) {
    if (!task || typeof task !== "object" || Array.isArray(task)) {
      throw new VikunjaError("Vikunja lieferte kein gültiges Task-Objekt.");
    }

    const canonicalTask = {
      ...task,
      identifier: task.identifier ?? ticket.ticketId,
    };
    const numericId = Number(canonicalTask.id);

    return {
      ticket_id: ticket.ticketId,
      resolution,
      web_url:
        Number.isSafeInteger(numericId) && numericId > 0
          ? `${this.config.baseUrl}/tasks/${numericId}`
          : null,
      task: canonicalTask,
    };
  }
}

function normalizeBaseUrl(value) {
  let url;
  try {
    url = new URL(value);
  } catch (error) {
    throw new VikunjaError(
      `VIKUNJA_BASE_URL ist keine gültige URL: "${value}".`,
      { cause: error },
    );
  }
  if (!["http:", "https:"].includes(url.protocol)) {
    throw new VikunjaError(
      "VIKUNJA_BASE_URL muss eine http- oder https-URL sein.",
    );
  }
  if (url.username || url.password || url.search || url.hash) {
    throw new VikunjaError(
      "VIKUNJA_BASE_URL darf keine Zugangsdaten, Query oder Fragment enthalten.",
    );
  }
  return url.toString().replace(/\/+$/, "");
}

function parseOptionalPositiveInteger(value, name) {
  if (value === undefined || value === "") {
    return null;
  }
  if (!/^[1-9][0-9]*$/.test(value)) {
    throw new VikunjaError(`${name} muss eine positive Ganzzahl sein.`);
  }
  const parsed = Number.parseInt(value, 10);
  if (!Number.isSafeInteger(parsed)) {
    throw new VikunjaError(`${name} ist zu groß.`);
  }
  return parsed;
}

function parsePositiveIntegerHeader(value) {
  if (!value || !/^[1-9][0-9]*$/.test(value)) {
    return null;
  }
  return Number.parseInt(value, 10);
}

function parseResponseBody(text) {
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function unwrapSingleTask(body) {
  if (body?.data && typeof body.data === "object" && !Array.isArray(body.data)) {
    return body.data;
  }
  if (body?.task && typeof body.task === "object" && !Array.isArray(body.task)) {
    return body.task;
  }
  return body;
}

function unwrapTaskList(body) {
  if (Array.isArray(body)) {
    return body;
  }
  if (Array.isArray(body?.data)) {
    return body.data;
  }
  if (Array.isArray(body?.tasks)) {
    return body.tasks;
  }
  throw new VikunjaError("Vikunja lieferte keine gültige Task-Liste.");
}

function formatHttpError(status, body) {
  const message =
    (body && typeof body === "object" && (body.message || body.error)) ||
    (typeof body === "string" && body) ||
    "keine Fehlerbeschreibung";
  return `Vikunja antwortete mit HTTP ${status}: ${String(message).slice(0, 500)}`;
}

function validateChanges(changes) {
  if (!changes || typeof changes !== "object" || Array.isArray(changes) ||
      Object.keys(changes).length === 0 ||
      Object.keys(changes).some(key => !["title", "description", "done"].includes(key))) {
    throw new VikunjaError("Mindestens ein unterstütztes Feld (title, description, done) ist erforderlich.");
  }
  if ("title" in changes && (typeof changes.title !== "string" || !changes.title.trim())) {
    throw new VikunjaError("title darf nicht leer sein.");
  }
  if ("description" in changes && typeof changes.description !== "string") {
    throw new VikunjaError("description muss eine Zeichenkette sein.");
  }
  if ("done" in changes && typeof changes.done !== "boolean") {
    throw new VikunjaError("done muss ein Boolean sein.");
  }
}

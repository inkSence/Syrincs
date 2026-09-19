import assert from "node:assert/strict";
import { createServer } from "node:http";
import { afterEach, test } from "node:test";

import {
  loadConfig,
  parseTicketId,
  VikunjaClient,
  VikunjaError,
} from "../src/vikunja-client.mjs";

const openServers = new Set();

afterEach(async () => {
  await Promise.all(
    [...openServers].map(
      (server) =>
        new Promise((resolve) => server.close(() => resolve(undefined))),
    ),
  );
  openServers.clear();
});

test("parseTicketId normalisiert eine gültige Kurz-ID", () => {
  assert.deepEqual(parseTicketId(" pv-2 "), {
    ticketId: "PV-2",
    projectIdentifier: "PV",
    index: 2,
  });
});

test("parseTicketId weist mehrdeutige oder ungültige IDs zurück", () => {
  for (const value of ["PV", "PV-0", "3", "../PV-1", "PV-1/labels"]) {
    assert.throws(() => parseTicketId(value), VikunjaError);
  }
});

test("getTask nutzt den direkten by-index-Endpunkt und sendet sichere Header", async () => {
  const requests = [];
  const fixture = {
    id: 3,
    identifier: "PV-2",
    title: "MCP anbinden",
    description: "Akzeptanzkriterien",
  };
  const baseUrl = await startFixtureServer((request, response) => {
    requests.push({
      url: request.url,
      authorization: request.headers.authorization,
      accept: request.headers.accept,
    });
    sendJson(response, 200, fixture);
  });

  const client = new VikunjaClient(
    loadConfig({
      VIKUNJA_BASE_URL: baseUrl,
      VIKUNJA_API_TOKEN: "secret-token",
    }),
  );

  const result = await client.getTask("PV-2");

  assert.equal(result.ticket_id, "PV-2");
  assert.equal(result.resolution, "by-index");
  assert.equal(result.web_url, `${baseUrl}/tasks/3`);
  assert.deepEqual(result.task, fixture);
  assert.deepEqual(requests, [
    {
      url: "/api/v1/projects/PV/tasks/by-index/2?format=markdown",
      authorization: "Bearer secret-token",
      accept: "application/json",
    },
  ]);
});

test("getTask fällt bei älteren Vikunja-Versionen auf die globale Taskliste zurück", async () => {
  const requestedPaths = [];
  const baseUrl = await startFixtureServer((request, response) => {
    requestedPaths.push(request.url);
    if (request.url.includes("/tasks/by-index/")) {
      sendJson(response, 404, { message: "Not Found" });
      return;
    }
    sendJson(
      response,
      200,
      [
        { id: 2, identifier: "PV-1", title: "Gesuchtes Ticket" },
        { id: 3, identifier: "PV-2", title: "Anderes Ticket" },
      ],
      { "x-pagination-total-pages": "1" },
    );
  });

  const client = new VikunjaClient(
    loadConfig({
      VIKUNJA_BASE_URL: baseUrl,
      VIKUNJA_API_TOKEN: "secret-token",
    }),
  );

  const result = await client.getTask("PV-1");

  assert.equal(result.resolution, "global-task-list-fallback");
  assert.equal(result.task.id, 2);
  assert.deepEqual(requestedPaths, [
    "/api/v1/projects/PV/tasks/by-index/1?format=markdown",
    "/api/v1/tasks?page=1&per_page=50",
  ]);
});

test("getTask löst beliebige Projektkürzel über die globale Taskliste auf", async () => {
  const requestedPaths = [];
  const baseUrl = await startFixtureServer((request, response) => {
    requestedPaths.push(request.url);
    if (request.url.includes("/tasks/by-index/")) {
      sendJson(response, 401, {
        message:
          "missing, malformed, expired or otherwise invalid token provided",
      });
      return;
    }
    sendJson(response, 200, [
      { id: 4, identifier: "RV-2", title: "Ticket mit anderem Projektkürzel" },
    ]);
  });

  const client = new VikunjaClient(
    loadConfig({
      VIKUNJA_BASE_URL: baseUrl,
      VIKUNJA_API_TOKEN: "valid-but-route-scoped-token",
    }),
  );

  const result = await client.getTask("RV-2");

  assert.equal(result.resolution, "global-task-list-fallback");
  assert.equal(result.task.id, 4);
  assert.deepEqual(requestedPaths, [
    "/api/v1/projects/RV/tasks/by-index/2?format=markdown",
    "/api/v1/tasks?page=1&per_page=50",
  ]);
});

test("getTask verschleiert Authentifizierungsfehler nicht durch den Fallback", async () => {
  const baseUrl = await startFixtureServer((_request, response) => {
    sendJson(response, 401, { message: "Invalid token" });
  });
  const client = new VikunjaClient(
    loadConfig({
      VIKUNJA_BASE_URL: baseUrl,
      VIKUNJA_API_TOKEN: "wrong-token",
    }),
  );

  await assert.rejects(
    () => client.getTask("PV-1"),
    /HTTP 401: Invalid token/,
  );
});

test("getTask verlangt einen API-Token vor dem Request", async () => {
  const client = new VikunjaClient(
    loadConfig({ VIKUNJA_BASE_URL: "http://localhost:3456" }),
  );

  await assert.rejects(() => client.getTask("PV-1"), /VIKUNJA_API_TOKEN/);
});

async function startFixtureServer(handler) {
  const server = createServer(handler);
  openServers.add(server);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const address = server.address();
  return `http://127.0.0.1:${address.port}`;
}

function sendJson(response, status, body, headers = {}) {
  response.writeHead(status, {
    "content-type": "application/json",
    ...headers,
  });
  response.end(JSON.stringify(body));
}

for (const apiVersion of ["v1", "v2"]) {
  test(`createTask sendet Titel und HTML-Beschreibung mit ${apiVersion}`, async () => {
    const requests = [];
    const client = new VikunjaClient(loadConfig({ VIKUNJA_API_TOKEN: "secret", VIKUNJA_API_VERSION: apiVersion }), async (url, options) => {
      requests.push({ url, ...options });
      return Response.json({ id: 42, identifier: "VR-1", ...JSON.parse(options.body) }, { status: 201 });
    });
    const input = { project_id: 5, title: "Fachliche Definition", description: "<p>Äö &amp; Test</p>" };
    const result = await client.createTask(input);
    assert.equal(requests.length, 1);
    assert.equal(requests[0].url, `http://localhost:3456/api/${apiVersion}/projects/5/tasks`);
    assert.equal(requests[0].method, apiVersion === "v1" ? "PUT" : "POST");
    assert.equal(requests[0].headers["Content-Type"], "application/json");
    assert.equal(requests[0].headers.Authorization, "Bearer secret");
    assert.equal(requests[0].redirect, "error");
    assert.deepEqual(JSON.parse(requests[0].body), { title: input.title, description: input.description });
    assert.equal(result.ticket_id, "VR-1");
    assert.equal(result.web_url, "http://localhost:3456/tasks/42");
  });

  test(`updateTask erhält nicht angegebene Felder und HTML mit ${apiVersion}`, async () => {
    const current = { id: 42, identifier: "VR-1", title: "Alt", description: "<p><u>Format</u></p>", done: true, priority: 4, due_date: "2026-10-01T00:00:00Z", labels: [{ id: 7 }] };
    const requests = [];
    const client = new VikunjaClient(loadConfig({ VIKUNJA_API_TOKEN: "secret", VIKUNJA_API_VERSION: apiVersion }), async (url, options) => {
      requests.push({ url, ...options });
      if (url.includes("by-index")) return Response.json({ ...current, description: "Format" });
      if (options.method === "GET") return Response.json(current);
      return Response.json(JSON.parse(options.body));
    });
    const result = await client.updateTask("VR-1", { title: "Neu", done: false });
    assert.equal(requests.length, 3);
    assert.equal(requests[1].url, `http://localhost:3456/api/${apiVersion}/tasks/42`);
    assert.equal(requests[2].method, apiVersion === "v1" ? "POST" : "PUT");
    assert.deepEqual(JSON.parse(requests[2].body), { ...current, title: "Neu", done: false });
    assert.equal(result.task.description, current.description);
    const cleared = await client.updateTask("VR-1", { description: "" });
    assert.equal(cleared.task.description, "");
  });
}

test("Schreibtools validieren vor Netzwerkzugriff", async () => {
  const client = new VikunjaClient(loadConfig({ VIKUNJA_API_TOKEN: "secret" }), async () => assert.fail("Kein Request erwartet"));
  for (const project_id of [0, -1, 1.5, "5", Number.MAX_SAFE_INTEGER + 1]) {
    await assert.rejects(() => client.createTask({ project_id, title: "Titel" }), /project_id/);
  }
  await assert.rejects(() => client.createTask({ project_id: 5, title: " " }), /title/);
  for (const changes of [{}, { priority: 1 }, { done: "false" }, { description: null }, { title: "" }]) {
    await assert.rejects(() => client.updateTask("VR-1", changes), VikunjaError);
  }
  const missingToken = new VikunjaClient(loadConfig({}), async () => assert.fail("Kein Request erwartet"));
  await assert.rejects(() => missingToken.createTask({ project_id: 5, title: "Titel" }), /VIKUNJA_API_TOKEN/);
});

for (const failure of [401, 403, 500, "timeout", "invalid"]) {
  test(`createTask wiederholt fehlgeschlagenen Request nicht: ${failure}`, async () => {
    let calls = 0;
    const client = new VikunjaClient(loadConfig({ VIKUNJA_API_TOKEN: "secret" }), async () => {
      calls++;
      if (failure === "timeout") throw new DOMException("Timeout", "TimeoutError");
      if (failure === "invalid") return Response.json({});
      return Response.json({ message: "Fehler" }, { status: failure });
    });
    await assert.rejects(() => client.createTask({ project_id: 5, title: "Titel" }), VikunjaError);
    assert.equal(calls, 1);
  });
}

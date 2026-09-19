import assert from "node:assert/strict";
import { test } from "node:test";
import { VikunjaClient, loadConfig, RELATION_KINDS } from "../src/vikunja-client.mjs";

const args = { ticket_id: "VR-10", other_ticket_id: "VR-11", relation_kind: "follows" };

function fixture({ version = "v1", exists = false, failure, fallback = false } = {}) {
  const requests = [];
  const client = new VikunjaClient(loadConfig({
    VIKUNJA_API_TOKEN: "test-token", VIKUNJA_API_VERSION: version,
  }), async (url, options) => {
    const path = new URL(url).pathname;
    requests.push({ path, method: options.method, body: options.body });
    const source = { id: 18, identifier: "VR-10", related_tasks: {
      follows: exists ? [{ id: 19 }] : [], related: [{ id: 99 }],
    } };
    const other = { id: 19, identifier: "VR-11", related_tasks: {} };
    if (options.method === "GET") {
      if (fallback && path.includes("by-index")) return Response.json({}, { status: 404 });
      if (path === `/api/${version}/tasks`) return Response.json([source, other]);
      return Response.json(path.endsWith("/11") ? other : source);
    }
    if (failure === "timeout") throw new DOMException("timeout", "TimeoutError");
    if (typeof failure === "number") return Response.json({ message: "Denied" }, { status: failure });
    if (failure !== "unconfirmed") exists = options.method !== "DELETE";
    return Response.json({ message: "Success" });
  });
  return { client, requests };
}

for (const version of ["v1", "v2"]) {
  test(`Relationen: Richtung, Pfad, Payload und Wiederholung ${version}`, async () => {
    const { client, requests } = fixture({ version, fallback: true });
    assert.equal((await client.createTaskRelation(args)).resolution, "created");
    assert.equal((await client.createTaskRelation(args)).resolution, "already_exists");
    assert.equal((await client.deleteTaskRelation(args)).resolution, "deleted");
    assert.equal((await client.deleteTaskRelation(args)).resolution, "already_absent");
    const writes = requests.filter(r => r.method !== "GET");
    assert.deepEqual(writes, [
      { path: `/api/${version}/tasks/18/relations`,
        method: version === "v1" ? "PUT" : "POST",
        body: JSON.stringify({ task_id: 18, other_task_id: 19, relation_kind: "follows" }) },
      { path: `/api/${version}/tasks/18/relations/follows/19`,
        method: "DELETE",
        body: JSON.stringify({ task_id: 18, other_task_id: 19, relation_kind: "follows" }) },
    ]);
  });
}

test("Relationen validieren beide Kurz-IDs und Typ vor Netzwerkzugriff", async () => {
  const client = new VikunjaClient(loadConfig({ VIKUNJA_API_TOKEN: "test" }),
    async () => assert.fail("Kein Request erwartet"));
  for (const method of ["createTaskRelation", "deleteTaskRelation"]) {
    for (const invalid of [
      { ...args, ticket_id: "invalid" },
      { ...args, other_ticket_id: "VR-0" },
      { ...args, other_ticket_id: " vr-10 " },
      { ...args, relation_kind: "unknown" },
      { ...args, relation_kind: "../follows" },
    ]) await assert.rejects(() => client[method](invalid));
  }
});

for (const method of ["createTaskRelation", "deleteTaskRelation"]) {
  for (const failure of [401, 403, 500, "timeout", "unconfirmed"]) {
    test(`${method}: kein Retry bei ${failure}`, async () => {
      const { client, requests } = fixture({
        exists: method === "deleteTaskRelation", failure,
      });
      await assert.rejects(() => client[method](args), /keine automatische Wiederholung/);
      assert.equal(requests.filter(r => r.method !== "GET").length, 1);
    });
  }
}

test("Fehlende oder ungültige Tasks verhindern Schreibzugriffe", async () => {
  for (const task of [{ id: 18 }, { id: -1, related_tasks: {} }]) {
    let writes = 0;
    const client = new VikunjaClient(loadConfig({ VIKUNJA_API_TOKEN: "test" }),
      async (url, options) => {
        if (options.method !== "GET") writes++;
        return Response.json(url.includes("/by-index/11") ? { id: 19 } : task);
      });
    await assert.rejects(() => client.createTaskRelation(args));
    assert.equal(writes, 0);
  }
});

test("Alle angebotenen Relationstypen bleiben beim Senden unverändert", async () => {
  for (const kind of RELATION_KINDS) {
    let exists = false;
    const client = new VikunjaClient(loadConfig({ VIKUNJA_API_TOKEN: "test" }),
      async (url, options) => {
        if (options.method !== "GET") {
          assert.equal(JSON.parse(options.body).relation_kind, kind);
          exists = true;
          return Response.json({});
        }
        return Response.json({ id: url.includes("/by-index/11") ? 19 : 18,
          related_tasks: exists ? { [kind]: [{ id: 19 }] } : {} });
      });
    assert.equal((await client.createTaskRelation({ ...args, relation_kind: kind })).resolution, "created");
  }
});

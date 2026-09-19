import assert from "node:assert/strict";
import { createServer as createHttpServer } from "node:http";
import { test } from "node:test";
import { fileURLToPath } from "node:url";

import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

test("MCP-Server veröffentlicht Lese- und Schreibtools und validiert Eingaben", async (t) => {
  const apiServer = createHttpServer((_request, response) => {
    response.writeHead(200, { "content-type": "application/json" });
    response.end(
      JSON.stringify({
        id: 3,
        identifier: "PV-2",
        title: "MCP-Integration",
        description: "Task-Inhalt",
      }),
    );
  });
  await new Promise((resolve) => apiServer.listen(0, "127.0.0.1", resolve));
  t.after(
    () =>
      new Promise((resolve) => apiServer.close(() => resolve(undefined))),
  );

  const address = apiServer.address();
  const transport = new StdioClientTransport({
    command: process.execPath,
    args: ["src/server.mjs"],
    cwd: fileURLToPath(new URL("..", import.meta.url)),
    env: {
      PATH: process.env.PATH,
      VIKUNJA_BASE_URL: `http://127.0.0.1:${address.port}`,
      VIKUNJA_API_TOKEN: "test-token",
    },
    stderr: "pipe",
  });
  const client = new Client({ name: "vikunja-test", version: "1.0.0" });
  await client.connect(transport);
  t.after(() => client.close());

  const tools = await client.listTools();
  assert.equal(tools.tools.length, 3);
  assert.deepEqual(tools.tools.map(tool => tool.name), ["get_task", "create_task", "update_task"]);
  assert.equal(tools.tools[1].annotations.readOnlyHint, false);
  assert.equal(tools.tools[1].annotations.idempotentHint, false);
  assert.equal(tools.tools[2].annotations.destructiveHint, true);
  assert.equal(tools.tools[0].name, "get_task");
  assert.equal(tools.tools[0].annotations.readOnlyHint, true);

  const response = await client.callTool({
    name: "get_task",
    arguments: { ticket_id: "PV-2" },
  });

  const created = await client.callTool({
    name: "create_task",
    arguments: { project_id: 5, title: "Neuer Task", description: "<p>Text</p>" },
  });
  assert.equal(created.isError, undefined);
  assert.equal(created.structuredContent.resolution, "created");
  const updated = await client.callTool({
    name: "update_task",
    arguments: { ticket_id: "PV-2", done: false, description: "" },
  });
  assert.equal(updated.isError, undefined);
  assert.equal(updated.structuredContent.resolution, "updated");
  const invalid = await client.callTool({ name: "update_task", arguments: { ticket_id: "PV-2" } });
  assert.equal(invalid.isError, true);
  const invalidCreate = await client.callTool({ name: "create_task", arguments: { project_id: -1, title: "" } });
  assert.equal(invalidCreate.isError, true);
  assert.equal(response.isError, undefined);
  assert.equal(response.structuredContent.ticket_id, "PV-2");
  assert.equal(response.structuredContent.task.description, "Task-Inhalt");
});

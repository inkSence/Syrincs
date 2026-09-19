import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { fileURLToPath } from "node:url";
import { z } from "zod";

import {
  loadConfig,
  VikunjaClient,
  VikunjaError,
} from "./vikunja-client.mjs";

export function createServer(env = process.env) {
  const server = new McpServer(
    {
      name: "syrincs-vikunja",
      version: "1.0.0",
    },
    {
      instructions:
        "Dieser Server liest und schreibt Vikunja-Tickets. Verwende get_task, wenn der " +
        "Benutzer eine Kurz-ID wie PV-1 oder RV-2 nennt. get_task verändert " +
        "keine Daten. Schreibtools nur bei ausdrücklichem Benutzerauftrag verwenden.",
    },
  );

  server.registerTool(
    "get_task",
    {
      title: "Vikunja-Task lesen",
      description:
        "Lädt den vollständigen, aktuellen Inhalt eines Vikunja-Tasks anhand " +
        "seiner Kurz-ID, zum Beispiel PV-1 oder RV-2. Alle zugänglichen " +
        "Projektkürzel werden dynamisch unterstützt. Verwende dieses Tool, " +
        "bevor du Anforderungen aus einem genannten Ticket interpretierst oder umsetzt.",
      inputSchema: {
        ticket_id: z
          .string()
          .min(3)
          .max(80)
          .describe("Vikunja-Kurz-ID in der Form PROJEKT-1"),
      },
      annotations: {
        readOnlyHint: true,
        destructiveHint: false,
        idempotentHint: true,
        openWorldHint: true,
      },
    },
    async ({ ticket_id: ticketId }) => {
      try {
        const client = new VikunjaClient(loadConfig(env));
        const result = await client.getTask(ticketId);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(result, null, 2),
            },
          ],
          structuredContent: result,
        };
      } catch (error) {
        const message =
          error instanceof VikunjaError
            ? error.message
            : `Unerwarteter Fehler: ${error.message}`;
        return {
          content: [{ type: "text", text: message }],
          isError: true,
        };
      }
    },
  );

  const fields = {
    title: z.string().refine(value => value.trim().length > 0, "Titel darf nicht leer sein"),
    description: z.string().describe("Beschreibung als HTML oder Klartext, nicht Markdown"),
    done: z.boolean(),
  };
  const writeHandler = (action) => async (args) => {
    try {
      const result = await action(new VikunjaClient(loadConfig(env)), args);
      return {
        content: [{ type: "text", text: JSON.stringify(result, null, 2) }],
        structuredContent: result,
      };
    } catch (error) {
      return { content: [{ type: "text", text: error.message }], isError: true };
    }
  };
  server.registerTool("create_task", {
    title: "Vikunja-Task anlegen",
    description: "Legt bei ausdrücklichem Benutzerauftrag einen Task im angegebenen Projekt an. Bei unklarer Schreibantwort nicht blind wiederholen (Duplikatgefahr).",
    inputSchema: {
      project_id: z.number().int().positive().max(Number.MAX_SAFE_INTEGER),
      title: fields.title,
      description: fields.description.optional(),
    },
    annotations: { readOnlyHint: false, destructiveHint: false, idempotentHint: false, openWorldHint: true },
  }, writeHandler((client, args) => client.createTask(args)));
  server.registerTool("update_task", {
    title: "Vikunja-Task aktualisieren",
    description: "Ändert bei ausdrücklichem Benutzerauftrag Titel, Beschreibung oder Erledigt-Status eines Tasks. Nicht angegebene Felder werden aus dem aktuellen Task übernommen. Gleichzeitige Änderungen sind nicht gegen Überschreiben geschützt.",
    inputSchema: {
      ticket_id: z.string().min(3).max(80),
      title: fields.title.optional(),
      description: fields.description.optional(),
      done: fields.done.optional(),
    },
    annotations: { readOnlyHint: false, destructiveHint: true, idempotentHint: true, openWorldHint: true },
  }, writeHandler((client, { ticket_id, ...changes }) => client.updateTask(ticket_id, changes)));

  return server;
}

async function main() {
  const server = createServer();
  const transport = new StdioServerTransport();

  process.on("SIGINT", async () => {
    await server.close();
    process.exit(0);
  });

  await server.connect(transport);
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  main().catch((error) => {
    console.error(error);
    process.exit(1);
  });
}

import { Client } from "@stomp/stompjs";
import { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { getAccessToken } from "@/lib/auth";
import type { Execution } from "@/types/api";

const ENDPOINT = `${import.meta.env.VITE_API_BASE ?? ""}/ws/monitoring`;
const TOPIC = "/topic/monitoring";
const MAX_ROWS = 100;

/**
 * Subscribes to the backend's STOMP monitoring topic and keeps a rolling list of
 * the most recently updated executions (newest first, de-duplicated by id).
 */
export function useMonitoring() {
  const [executions, setExecutions] = useState<Execution[]>([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const token = getAccessToken();
    const connectHeaders: Record<string, string> = token ? { Authorization: `Bearer ${token}` } : {};
    const client = new Client({
      webSocketFactory: () => new SockJS(ENDPOINT),
      connectHeaders,
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);
        client.subscribe(TOPIC, (message) => {
          const execution = JSON.parse(message.body) as Execution;
          setExecutions((prev) => {
            const next = [execution, ...prev.filter((e) => e.id !== execution.id)];
            return next.slice(0, MAX_ROWS);
          });
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;
    return () => {
      void client.deactivate();
      clientRef.current = null;
    };
  }, []);

  return { executions, connected };
}

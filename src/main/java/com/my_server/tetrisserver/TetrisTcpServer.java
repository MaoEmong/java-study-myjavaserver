package com.my_server.tetrisserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TetrisTcpServer {

    private static final TetrisRankRepository rankRepository = new TetrisRankRepository();

    public static void main(String[] args) throws Exception {
        int port = 8000;
        ServerSocket server = new ServerSocket(port);
        System.out.println("[SERVER] Listening on " + port);

        while (true) {
            Socket client = server.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private static void handleClient(Socket client) {
        try (client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))) {

            String line = in.readLine(); // 클라가 보낸 JSON 한 줄
            if (line == null || line.isBlank()) {
                writeLine(out, "{\"ok\":false,\"error\":\"empty\"}");
                return;
            }

            Map<String, String> obj = parseFlatJson(line);
            String type = obj.getOrDefault("type", "");

            // ---- ping ----
            if (type.equals("ping")) {
                writeLine(out, "{\"ok\":true,\"pong\":true}");
                System.out.println("[PING]");
                return;
            }

            // ---- submit ----
            if (type.equals("submit")) {
                String nick = obj.getOrDefault("nickname", "").trim();
                int score = parseIntSafe(obj.get("score"), -1);

                if (nick.isEmpty() || score < 0) {
                    writeLine(out, "{\"ok\":false,\"error\":\"bad_request\"}");
                    return;
                }

                boolean ok = rankRepository.insert(nick, score);
                if (ok) {
                    writeLine(out, "{\"ok\":true}");
                    System.out.println("[SUBMIT][DB] " + nick + " " + score);
                } else {
                    writeLine(out, "{\"ok\":false,\"error\":\"db_fail\"}");
                }
                return;
            }

            // ---- get ----
            if (type.equals("get")) {
                int top = parseIntSafe(obj.get("top"), 10);
                if (top <= 0) top = 10;
                if (top > 100) top = 100;

                List<RankDto> list = rankRepository.findTop(top);

                StringBuilder sb = new StringBuilder();
                sb.append("{\"ok\":true,\"rankings\":[");

                for (int i = 0; i < list.size(); i++) {
                    RankDto r = list.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{\"name\":\"")
                            .append(escape(r.name()))
                            .append("\",\"score\":")
                            .append(r.score())
                            .append("}");
                }

                sb.append("]}");
                writeLine(out, sb.toString());
                System.out.println("[GET][DB] top=" + top);
                return;
            }

            writeLine(out, "{\"ok\":false,\"error\":\"unknown_type\"}");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeLine(BufferedWriter out, String s) throws IOException {
        out.write(s);
        out.write("\n");
        out.flush();
    }

    // ====== 아주 단순한 JSON 파서 (flat key/value만) ======
    // 예: {"type":"submit","nickname":"A","score":10}
    private static Map<String, String> parseFlatJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        String[] parts = json.split(",");
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) continue;

            String k = stripQuotes(kv[0].trim());
            String v = stripQuotes(kv[1].trim());
            map.put(k, v);
        }
        return map;
    }

    private static String stripQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return fallback; }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

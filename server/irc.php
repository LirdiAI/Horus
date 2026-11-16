<?php
/**
 * Lumira / Deluxe IRC Server Backend (PHP)
 * 
 * Установка:
 * 1. Загрузите этот файл на ваш PHP хостинг (например: https://example.com/irc.php).
 * 2. Убедитесь, что PHP имеет права на запись в текущую директорию (создастся irc_data.json).
 * 3. В игре напишите:
 *    .irc url https://example.com/irc.php
 *    .irc on
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Deluxe-Key, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

$dataFile = __DIR__ . '/irc_data.json';

// Загрузка данных
$data = [
    'messages' => [],
    'online' => [],
    'cursors' => [],
    'counter' => 1
];

if (file_exists($dataFile)) {
    $content = @file_get_contents($dataFile);
    if ($content) {
        $parsed = json_decode($content, true);
        if (is_array($parsed)) {
            $data = array_merge($data, $parsed);
        }
    }
}

$now = time();

// Очистка неактивных пользователей (> 30 сек)
if (!empty($data['online'])) {
    foreach ($data['online'] as $userKey => $u) {
        if ($now - ($u['last_active'] ?? 0) > 30) {
            unset($data['online'][$userKey]);
        }
    }
}

// Обработка GET (информационная страница в браузере)
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    echo json_encode([
        'ok' => true,
        'service' => 'Lumira IRC PHP Server',
        'version' => '1.0',
        'messages_count' => count($data['messages']),
        'online_count' => count($data['online']),
        'online_users' => array_values(array_map(function($u) {
            return ['name' => $u['name'], 'role' => $u['role']];
        }, $data['online']))
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    exit;
}

// Обработка POST
$rawInput = file_get_contents('php://input');
$req = json_decode($rawInput, true);

if (!is_array($req)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'error' => 'Invalid JSON']);
    exit;
}

$action = $req['action'] ?? 'poll';
$name = trim($req['name'] ?? 'User');
$role = $req['role'] ?? 'DEFAULT';
$nick = $req['nick'] ?? $name;
$userKey = mb_strtolower($name);

if ($action === 'poll') {
    $data['online'][$userKey] = [
        'name' => $name,
        'role' => $role,
        'nick' => $nick,
        'last_active' => $now
    ];

    $lastId = $data['cursors'][$userKey] ?? null;
    $newCursor = $lastId !== null ? $lastId : 0;
    $outMessages = [];

    if ($lastId === null) {
        // Первый вход: отдаём до 15 последних сообщений
        $recent = array_slice($data['messages'], -15);
        foreach ($recent as $m) {
            $outMessages[] = $m;
            $newCursor = max($newCursor, $m['id'] ?? 0);
        }
    } else {
        foreach ($data['messages'] as $m) {
            if (($m['id'] ?? 0) > $lastId) {
                $outMessages[] = $m;
                $newCursor = max($newCursor, $m['id'] ?? 0);
            }
        }
    }

    $data['cursors'][$userKey] = $newCursor;
    @file_put_contents($dataFile, json_encode($data, JSON_UNESCAPED_UNICODE));

    echo json_encode(['ok' => true, 'messages' => $outMessages], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'send') {
    $text = trim($req['text'] ?? '');
    if ($text !== '') {
        $msgId = $data['counter']++;
        $msg = [
            'id' => $msgId,
            'name' => $name,
            'role' => $role,
            'nick' => $nick,
            'text' => $text,
            'time' => $now,
            'emoji' => abs(crc32($name)) % 20
        ];

        if (!empty($req['replyTo'])) {
            $msg['replyTo'] = $req['replyTo'];
        }
        if (!empty($req['replyToNick'])) {
            $msg['replyToNick'] = $req['replyToNick'];
        }

        $data['messages'][] = $msg;
        if (count($data['messages']) > 200) {
            array_shift($data['messages']);
        }

        $data['online'][$userKey] = [
            'name' => $name,
            'role' => $role,
            'nick' => $nick,
            'last_active' => $now
        ];

        @file_put_contents($dataFile, json_encode($data, JSON_UNESCAPED_UNICODE));
    }

    echo json_encode(['ok' => true]);
    exit;
}

if ($action === 'online') {
    $data['online'][$userKey] = [
        'name' => $name,
        'role' => $role,
        'nick' => $nick,
        'last_active' => $now
    ];

    $onlineList = [];
    foreach ($data['online'] as $u) {
        $onlineList[] = [
            'name' => $u['name'],
            'role' => $u['role'],
            'nick' => $u['nick']
        ];
    }

    @file_put_contents($dataFile, json_encode($data, JSON_UNESCAPED_UNICODE));
    echo json_encode(['ok' => true, 'online' => $onlineList], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'leave') {
    unset($data['online'][$userKey]);
    unset($data['cursors'][$userKey]);
    @file_put_contents($dataFile, json_encode($data, JSON_UNESCAPED_UNICODE));
    echo json_encode(['ok' => true]);
    exit;
}

http_response_code(400);
echo json_encode(['ok' => false, 'error' => 'Unknown action']);

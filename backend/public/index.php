<?php

use Illuminate\Http\Request;

// PHP 8.5'te bazı vendor sabitleri deprecated oldu. display_errors açıkken
// bu uyarılar JSON yanıtının başına HTML olarak eklenip istemcide
// "Unexpected JSON token" hatasına yol açıyor; bu yüzden bastırıyoruz.
error_reporting(E_ALL & ~E_DEPRECATED);

define('LARAVEL_START', microtime(true));

// Determine if the application is in maintenance mode...
if (file_exists($maintenance = __DIR__.'/../storage/framework/maintenance.php')) {
    require $maintenance;
}

// Register the Composer autoloader...
require __DIR__.'/../vendor/autoload.php';

// Bootstrap Laravel and handle the request...
(require_once __DIR__.'/../bootstrap/app.php')
    ->handleRequest(Request::capture());

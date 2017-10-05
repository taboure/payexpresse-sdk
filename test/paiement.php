<?php
/**
 * Created by PhpStorm.
 * User =>  macbookpro
 * Date =>  30/09/2017
 * Time =>  14 => 35
 */


require '../php-sdk/PayExpresse.php';
require_once 'conf.php';


$id = !empty($_POST['item_id']) ? $_POST['item_id'] : null;
$items = json_decode(file_get_contents('article.json'), true)['articles'];
$key = array_search($id, array_column($items, 'id'));

if($key === false || $id === null)
{
    echo json_encode([
        'success' => -1, //or false,
        'errors' => [
            'article avec cet id non trouvé'
        ]
    ], JSON_PRETTY_PRINT|JSON_HEX_QUOT|JSON_UNESCAPED_UNICODE);
}
else{

    $item = (object)$items[$key];

    $apiKey = '1afac858d4fa5ec74e3e3734c3829793eb6bd5f4602c84ac4a5069369812915e';
    $apiSecret = '96bc36c11560f2151c4b43eee310cefabc2e9e9000f7e315c3ca3d279e3f98ac';

    $response = (new PayExpresse($apiKey, $apiSecret))->setQuery([
        'item_name' => $item->name,
        'item_price' => $item->price,
        'command_name' => "Paiement {$item->name} Gold via PayExpresse",
    ])->setCustomeField([
        'item_id' => $id,
        'time_command' => time(),
        'ip_user' => $_SERVER['REMOTE_ADDR'],
        'lang' => $_SERVER['HTTP_ACCEPT_LANGUAGE']
    ])
        ->setTestMode(true)
        ->setCurrency($item->currency)
        ->setRefCommand(uniqid())
        ->setNotificationUrl([
            'ipn_url' => BASE_URL.'/ipn.php', //only https
            'success_url' => BASE_URL.'/index.php?state=success&id='.$id,
            'cancel_url' => BASE_URL.'/index.php?state=cancel&id='.$id
        ])->send();

    echo json_encode($response);
}






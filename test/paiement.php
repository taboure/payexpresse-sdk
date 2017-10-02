<?php
/**
 * Created by PhpStorm.
 * User =>  macbookpro
 * Date =>  30/09/2017
 * Time =>  14 => 35
 */


require '../php-sdk/PayExpresse.php';



$item = Product::select(['item_name', 'item_price']);


$apiKey = '1afac858d4fa5ec74e3e3734c3829793eb6bd5f4602c84ac4a5069369812915e';
$apiSecret = '96bc36c11560f2151c4b43eee310cefabc2e9e9000f7e315c3ca3d279e3f98ac';

$response = (new PayExpresse($apiKey, $apiSecret))->setQuery([
    'item_name' => $item->item_name,
    'item_price' => $item->price,
    'command_name' => "Paiement {$item->item_name} Gold via PayExpresse",
])->setCustomeField([
    'field_1' => 'my_value_1',
    'field_2' => 'my_value_2'
])
    ->setTestMode(true)
    ->setCurrency('xof')
    ->setRefCommand(uniqid())
    ->setNotificationUrl([
        'ipn_url' => 'https://www.domaine.com/ipn',
        'success_url' => 'https://www.domaine.com/success',
        'cancel_url' => 'https://www.domaine.com/success'
    ])->send();

echo json_encode($response);



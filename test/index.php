<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <link rel="stylesheet" href="../web-sdk/payexpresse.css">
    <script src="../web-sdk/payexpresse.js"></script>
    <style>
        .buy{
            display: block;
            height: 60px;
            width: calc(100% + 10px);
            text-decoration: none;
            color: #fff;
            background-color: #26a69a;
            text-align: center;
            letter-spacing: .5px;
            -webkit-transition: .2s ease-out;
            transition: .2s ease-out;
            cursor: pointer;
            border: 0;
        }



        .wrapper{
            display: inline-block;
            width: 190px;
            margin: 22px;
        }

        .previous-success{
            border: 2px dashed green;
        }

        .previous-cancel{
            border: 2px dashed red;
        }

    </style>
</head>
<body>

    <?php
        $id = null;
        $class = '';
        if(!empty($_GET['state']) && !empty($_GET['id']))
        {
            $id = intval($_GET['id']);
            $state = $_GET['state'];
            $class = 'previous-'.$state;
        }

    ?>

    <?php
        $items = json_decode(file_get_contents('article.json'), true)['articles'];

        foreach ($items as $item){
            $item = (object)$item;
            echo '<div class="wrapper">';
            echo sprintf('<img class="%s" src="https://api.adorable.io/avatars/200/%s">', $item->id === $id ? $class : '',$item->name);
            echo sprintf('<button class="buy" onclick="buy(this)" data-item-id="%s" >%s <br>Stock: %s<br>%s %s<br>Acheter</button>', $item->id, $item->name, $item->stock,number_format($item->price, 1,',','.'), $item->currency);
            echo '</div >';
        }
    ?>


<script>


    function buy(btn) {
        var selector = jQuery(btn);

        (new PayExpresse({
            item_id          :   selector.attr('data-item-id'),
        })).withOption({
            requestTokenUrl           :   'http://localhost:8888/payexpress-sdk/test/paiement.php',
            method              :   'POST',
            prensentationMode   :   PayExpresse.OPEN_IN_POPUP,
            didPopupClosed: function (is_completed, success_url, cancel_url) {
                if(is_completed)
                {
                    window.location.href = success_url;
                }
                else{
                    window.location.href = cancel_url;
                }
            },
            willGetToken        :   function () {
                console.log("Je me prepare a obtenir un token");
                selector.prop('disabled', true);
            },
            didGetToken         : function (token, redirectUrl) {
                console.log("Mon token est : " +  token  + ' et url est ' + redirectUrl );
                selector.prop('disabled', false);
            },
            didReceiveError: function (error) {
                console.log('erreur inconnu', error);
                selector.prop('disabled', false);
            },
            didReceiveNonSuccessResponse: function (jsonResponse) {
                console.log('non success response ',jsonResponse);
                alert(jsonResponse.errors)
                selector.prop('disabled', false);
            }
        }).send();
    }
</script>
</body>
</html>